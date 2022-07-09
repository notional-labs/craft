import { collections, redisClient } from './database.service';

import axios from 'axios';

/**
 * Get a users CRAFT owned NFTs id list
 * { tokens: [ '1', '101', '102', '2', '8', '9' ] }
 * 
 * http://127.0.0.1:4000/v1/realestate/owned/craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
 * 
 * @param wallet The CRAFT address of the user
 */
export const getUsersOwnedNFTs = async (addr721_address: string, wallet: string) => {
    let usersNFTIDs = await getUsersNFTsIDsList(addr721_address, wallet); // { tokens: [ '1', '101', '102', '2', '8', '9' ] }
    console.log("getUsersOwnedNFTs", usersNFTIDs) 

    if(usersNFTIDs) {
        // cache this maybe for a few seconds? even worth it?
        return Promise.all(usersNFTIDs?.tokens.map(token => queryToken(addr721_address, token)))  
    }

    return []; 
};

/**
 * 
 * Queries a smart contract defined in .env (ADDR721_REALESTATE) by token name. 
 * If found, decompiles the base64 data (if any) & saves to redis cache.
 * If it is a link or JSON, it will save that property as well.
 * 
 * http://localhost:4000/v1/nfts/data/craft182nff4ttmvshn6yjlqj5czapfcav9434l2qzz8aahf5pxnyd33ts98amul/2
 * 
 * @param tokenId 
 * @returns JSON information about the property from the token_uri
 */
// IMPORTANT: We allow different addr721's so we can query from the marketplace as well as we expand to other contracts
export const queryToken = async (addr721Address: string, tokenId: string) => {
    // hget cache:query_token 10

    // Get cached
    const REDIS_KEY = `cache:query_token`; 
    const REDIS_HSET_KEY = `${addr721Address}:${tokenId}` // for marketplace expansion
    let cachedToken = await redisClient?.hGet(REDIS_KEY , REDIS_HSET_KEY);
    if(cachedToken) {
        // console.log(`Token ${tokenId} found in redis cache -> ${REDIS_KEY}`);
        return JSON.parse(cachedToken);
    }

    const query = Buffer.from(`{"nft_info":{"token_id":"${tokenId}"}}`).toString('base64');
    let api = `${process.env.CRAFTD_REST}/cosmwasm/wasm/v1/contract/${addr721Address}/smart/${query}`
    let response = await axios.get(api).catch(err => {
        // console.log("queryToken Error (does not exist)");
        return undefined;
    })

    // Can be a link (http, ipfs), base64 encoded, or a JSON string
    let token_uri = response?.data?.data?.token_uri;
    // console.log(`${token_uri}`)
    if(!token_uri) {
        // console.log(`Error querying token ${tokenId}. Token likely does not exist`);
        return undefined;
    }

    let returnJsonValue;

    // If its a link, we just want to return that link directly. Could also add check for http / ipfs
    if(token_uri.includes("://")) {
        // console.log(`Token ${tokenId} is a link`); //(but we convert to JSON for our marketplace API viewing)
        returnJsonValue = {_nft_type: "link", token_uri: token_uri};

    } else if(token_uri.match(/^[A-Za-z0-9+/=]*$/)) { // base64 contains A–Z , a–z , 0–9 , + , / and =    
        // console.log(`Token ${tokenId} is base64`);
        try {
            let base64Decoded = Buffer.from(token_uri, 'base64').toString('ascii')
            returnJsonValue = JSON.parse(base64Decoded);            
        } catch (error) {
            // Is just normal JSON, so parse it & save
            console.log(`Token ${tokenId} catch error ${error}`);
            returnJsonValue = JSON.parse(token_uri);
        }
    } else {
        // console.log(`Token ${tokenId} is JSON`);
        returnJsonValue = JSON.parse(token_uri);
    }
    // append tokenId to the end of the json (useful for CRAFT Skins & real estate)
    returnJsonValue.tokenId = tokenId;
     
    // save to redis hSet cache
    await redisClient?.hSet(REDIS_KEY, REDIS_HSET_KEY, JSON.stringify(returnJsonValue));
    // sadly we can not expire a child, this can be done in KeyDB (redis fork) but not standalone.
    // so we expire the top level key (cache:query_token) every 24 hours
    await redisClient?.expire(REDIS_KEY, 86400);
    return returnJsonValue;
};


// this function gets all owned NFTs for a user, then queries all token_uris as well & returns that as a map.

export const getUsersNFTsIDsList = async (addr721_address: string, wallet: string) => {            
    let query = Buffer.from(`{"tokens":{"owner":"${wallet}","start_after":"0","limit":500}}`).toString('base64');
    
    let api = `${process.env.CRAFTD_REST}/cosmwasm/wasm/v1/contract/${addr721_address}/smart/${query}`

    let response = await axios.get(api).catch(err => {
        // console.log("getUsersNFTsIDsList Error (wallet does not exist)");
        // return { "tokens": [] };
        return undefined;
    })

    let tokens = response?.data?.data;
    // console.log(`getUsersNFTsIDsList`, tokens, api) // { tokens: [ '1', '101', '102', '2', '8', '9' ] }

    return tokens;
};