// This file is used to sync image NFTs from the chain -> in game database.
// In the future we can add real estate & use redis to post a message to resync a players NFTs on purchase from marketplace.
import axios from 'axios';
import { redisClient } from './database.service';

import { fromBech32, toBech32 } from "@cosmjs/encoding";

import {getUsersOwnedNFTs, queryContractInfo} from './nfts.service';
import {queryOfferings} from './nftmarketplace.service';
import {getDetails_Offering_TokenData_Owner} from './assets.service';

// ! TODO: Query craft contracts (get contract code, get all instances of 721 contracts, query if craft owns any NFTs there)

const allowedExtensions = [".png", ".jpg", ".jpeg"];
const prefixes = ["craft", "stars", "omniflix"];

// TODO: Put behind a redis hset? or just do with MongoDB? add cooldown?

export const getUsersNFTsFromOtherPlatforms = async (craft_address: string) => {
    if (craft_address === undefined || craft_address === null) { return {}; }
    if(isValidAddress(craft_address) == false) { return {}; }

    var allMyNFTs = await getAllNFTs(craft_address);

    // TODO: Cooldown in future
    await saveNFTsToMongoDB(craft_address, allMyNFTs);
    return allMyNFTs;
}

import { collections } from './database.service';
async function saveNFTsToMongoDB(craft_address: string, nfts: any) {
    // save to the NFT collection to their address.
    await collections?.nfts?.updateOne({ address: craft_address }, { $set: { nfts } }, { upsert: true });
}

export async function getAllNFTs(craft_address: string, chain: string = "*", includeOfferings: boolean = false) {
    if (craft_address === undefined || craft_address === null) { return {}; }
    if(isValidAddress(craft_address) == false) { return {}; }

    // console.log(chain);
    chain = chain.toLowerCase().trim();

    // TODO: Cache this in redis

    let allMyNFTs = {
        craft: [],
        stargaze: [],
        omniflix: [],
    };
    for (const prefix of prefixes) {
        const address = convertBech32Address(craft_address, prefix);
        // console.log(address, prefix);

        switch (prefix) {
            case "craft": {
                // check chain is * or craft
                if(chain === "*" || chain.includes("craft")) {
                    await queryCraftCW721NFTs(address, includeOfferings).then(data => {
                        allMyNFTs.craft = data;
                    });
                }   
                break;             
            }
            case "stars": {
                if(chain === "*" || chain === "stars" || chain.includes("stargaze")) {
                    await queryStargazeNFTs(address).then(data => {
                        allMyNFTs.stargaze = data;
                    });
                }
                break;
            }
            case "omniflix": {
                if(chain === "*" || chain.includes("omniflix") || chain.includes("omni")) {
                    await queryOmniflixNFTs(address).then(data => {
                        allMyNFTs.omniflix = data; 
                    });
                }
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


    var myStargazeNFTs: any = [];

    for (const nftObject of JSON) {
        // const myName = nftObject['name'];
        const myImage = nftObject['image'];
        // console.log(myName, myImage);
        for (const extension of allowedExtensions) {
            if (nftObject.image.endsWith(extension)) {
                let data = {
                    contract_details: {
                        name: nftObject.collection.name,
                        symbol: nftObject.collection.symbol,
                        address: nftObject.collection.contractAddress,
                    },
                    token_data: {
                        _nft_type: "link",
                        token_uri: nftObject.image,
                        tokenId: nftObject.tokenId,
                    },
                }
                myStargazeNFTs.push(data);
            }
        }
    }
    return myStargazeNFTs;
}

// TODO: Move to NFTs.service
export async function getAllCW721ContractAddresses() {
    const CONTRACT_ADDRESSES = `${process.env.CRAFTD_REST}/cosmwasm/wasm/v1/code/${process.env.CW721_CODE}/contracts?pagination.limit=100`
    // console.log(CONTRACT_ADDRESSES);
    const addresses = await axios.get(CONTRACT_ADDRESSES).catch(err => {
        console.log(err);
        return undefined;
    });

    if(!addresses) { return []; }
    return addresses.data.contracts;
}

async function queryCraftCW721NFTs(craftWallet, includeOfferings: boolean = false) {
    const REDIS_KEY = `cache:craft_cw_721s:${craftWallet}`;
    const TTL = 30;  // 10 seconds
    let cacched_cw721_craft = await redisClient?.get(REDIS_KEY);
    if (cacched_cw721_craft) {        
        return JSON.parse(cacched_cw721_craft);
    }

    const addresses = await getAllCW721ContractAddresses();

    var myCraftNFTs: any = [];
    if(addresses === undefined) { return myCraftNFTs; }
    // console.log("addresses", addresses);

    for(const addr of addresses) {
        const contract_data = await queryContractInfo(addr);
        let data = {
            contract_details: {
                name: contract_data.name,
                symbol: contract_data.symbol,
                address: addr,
            },
            token_data: {},
        }

        // gets user owned direct tokens
        const tokens = await getUsersOwnedNFTs(addr, craftWallet);
        for(const nft of tokens) {
            console.log(nft);
            const newData = await getDetails_Offering_TokenData_Owner(addr, nft.tokenId);
            if(newData) {
                console.log(newData);
                myCraftNFTs.push(newData);
            }           
        }

        // get NFTs which are being sold in the marketplace, so technically the user still owns BUT they are being sold there
        const tokens_offerings = await queryOfferings("", craftWallet);
        for(const nft of tokens_offerings) {
            console.log(nft);
            const newData = await getDetails_Offering_TokenData_Owner(addr, nft.token_id);
            if(newData) {
                console.log(newData);
                myCraftNFTs.push(newData);
            }
        }

    }

    await redisClient?.set(REDIS_KEY, JSON.stringify(myCraftNFTs));
    await redisClient?.expire(REDIS_KEY, TTL);

    return myCraftNFTs;
}


async function queryOmniflixNFTs(omniflixWallet) {
    // use similar data structure as stargaze data
    const API = `https://data-api.omniflix.studio/nfts?owner=${omniflixWallet}`
    // https://data-api.omniflix.studio/nfts?owner=omniflix12wdcv2lm6uhyh5f6ytjvh2nlkukrmkdkfgfyaw
    // console.log(API);

    const value = await axios.get(API);

    // log value keys
    const JSON = value.data.result.list;
    // console.log(JSON);

    let myOmniflixNFTs: any = [];

    for (const nftObject of JSON) {
        // const myName = nftObject['name'];
        // const myImage = nftObject['media_uri'];
        // console.log(myName, myImage);
        // ensure nftObject.media_type has jpeg or png in it

        if(nftObject.media_type.includes("jpeg") || nftObject.media_type.includes("png")) {
            let data = {
                contract_details: {
                    name: nftObject.name,
                    symbol: nftObject.denom_id.symbol,
                    address: nftObject.denom_id.id,
                },
                token_data: {
                    _nft_type: "link",
                    token_uri: nftObject.media_uri,
                    tokenId: nftObject.denom_id._id,
                },
            }
            myOmniflixNFTs.push(data);
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