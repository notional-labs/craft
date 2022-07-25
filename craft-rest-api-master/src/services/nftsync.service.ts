// This file is used to sync image NFTs from the chain -> in game database.
// In the future we can add real estate & use redis to post a message to resync a players NFTs on purchase from marketplace.
import axios from 'axios';

import { fromBech32, toBech32 } from "@cosmjs/encoding";

import {getUsersOwnedNFTs} from './nfts.service';

// ! TODO: Query craft contracts (get contract code, get all instances of 721 contracts, query if craft owns any NFTs there)

const allowedExtensions = [".png", ".jpg", ".jpeg"];
const prefixes = ["craft", "stars", "omniflix"];

// TODO: Put behind a redis hset? or just do with MongoDB? add cooldown?

export const getUsersNFTsFromOtherPlatforms = async (craft_address: string) => {
    if (craft_address === undefined || craft_address === null) { return {}; }
    if(isValidAddress(craft_address) == false) { return {}; }

    var allMyNFTs = getAllNFTs(craft_address);

    // TODO: Cooldown in future
    await saveNFTsToMongoDB(craft_address, allMyNFTs);
    return allMyNFTs;
}

import { collections } from './database.service';
async function saveNFTsToMongoDB(craft_address: string, nfts: any) {
    // save to the NFT collection to their address.
    await collections?.nfts?.updateOne({ address: craft_address }, { $set: { nfts } }, { upsert: true });
}

export async function getAllNFTs(craft_address: string) {
    if (craft_address === undefined || craft_address === null) { return {}; }
    if(isValidAddress(craft_address) == false) { return {}; }

    // TODO: Cache this in redis

    var allMyNFTs = {};
    for (const prefix of prefixes) {
        const address = convertBech32Address(craft_address, prefix);
        // console.log(address, prefix);

        switch (prefix) {
            // case "craft": {
            //     await queryCraftCW721NFTs(address).then(data => {
            //         for (const nft of data) {
            //             allMyNFTs[nft[0]] = nft[1];
            //             console.log(nft[0], nft[1]);
            //         }
            //     });
            //     break;
            // }
            case "stars": {
                await queryStargazeNFTs(address).then(data => {
                    for (const nft of data) {
                        allMyNFTs[nft[0]] = nft[1];
                        // console.log(nft[0], nft[1]);
                    }
                });
                break;
            }
            case "omniflix": {
                await queryOmniflixNFTs(address).then(data => {
                    for (const nft of data) {
                        allMyNFTs[nft[0]] = nft[1];
                        // console.log(nft[0], nft[1]);               
                    }
                });
                break;
            }
        }
    }
    return allMyNFTs;
}

async function queryStargazeNFTs(starsWallet) {
    const API = `https://nft-api.stargaze-apis.com/api/v1beta/profile/${starsWallet}/nfts`
    // console.log(API);

    const value = await axios.get(API);
    const JSON = value.data;


    var myStargazeNFTs = new Map();

    for (const nftObject of JSON) {
        const myName = nftObject['name'];
        const myImage = nftObject['image'];
        // console.log(myName, myImage);
        for (const extension of allowedExtensions) {
            if (myImage.endsWith(extension)) {
                // console.log(myName, myImage);                   
                // myStargazeNFTs[myName] = myImage;
                myStargazeNFTs.set(myName, myImage);
            }
        }
    }
    return myStargazeNFTs;
}

// TODO:
async function queryCraftCW721NFTs(craftWallet) {
    const CONTRACT_ADDRESSES = `${process.env.CRAFTD_REST}/cosmwasm/wasm/v1/code/${process.env.CW721_CODE}/contracts?pagination.limit=100`
    // console.log(CONTRACT_ADDRESSES);
    const addresses = await axios.get(CONTRACT_ADDRESSES).catch(err => {
        console.log(err);
        return undefined;
    });

    var myCraftNFTs = new Map();

    if(addresses === undefined) { return myCraftNFTs; }

    const CONTRACTS_LIST = addresses.data.contracts;

    for(const addr of CONTRACTS_LIST) {
        // console.log(addr);
        const tokens = await getUsersOwnedNFTs(addr, craftWallet);
        // console.log(tokens);
        for(const nft of tokens) {
            myCraftNFTs.set(nft[0], nft[1]);
        }
    }

    return myCraftNFTs;
}


async function queryOmniflixNFTs(omniflixWallet) {
    const API = `https://data-api.omniflix.studio/nfts?owner=${omniflixWallet}`
    // https://data-api.omniflix.studio/nfts?owner=omniflix12wdcv2lm6uhyh5f6ytjvh2nlkukrmkdkfgfyaw
    // console.log(API);

    const value = await axios.get(API);

    // log value keys
    const JSON = value.data.result.list;
    // console.log(JSON);

    let myOmniflixNFTs = new Map();

    for (const nftObject of JSON) {
        const myName = nftObject['name'];
        const myImage = nftObject['media_uri'];
        // console.log(myName, myImage);
        for (const extension of allowedExtensions) {
            // if (myImage.endsWith(extension)) { // omniflix doesn't use on IPFS anymore
            // console.log(myName, myImage);                   
            myOmniflixNFTs.set(myName, myImage);
            // }
        }
    }
    // console.log(myOmniflixNFTs);
    return myOmniflixNFTs;
}


const convertBech32Address = (address: string, prefix: string) => {
    const decoded = fromBech32(address);
    return toBech32(prefix, decoded.data)
};


// confirm an address is valid
const isValidAddress = (address: string) => {
    try {
        const decoded = fromBech32(address);
    } catch (error) {
        return false;
    }    
    return true;
}