import { collections, redisClient } from './database.service';

import axios from 'axios';

/**
 * Get data about a property (name, desc, worldName, imageLink, etc.) - from reProperties server database
 * http://127.0.0.1:4000/v1/realestate/dbcd78cb-326e-4842-982b-9252f9ca25a7
 * 
 * @param uuid The UUID of the property
 */
export const getPropertyInformation = async (uuid: string) => {
    const REDIS_KEY = `cache:property_info:${uuid}`;
    let cachedPropertyData = await redisClient?.get(REDIS_KEY);
    if(cachedPropertyData) {
        console.log(`Property found in redis cache -> ${REDIS_KEY}. Not calling MongoDB`);
        return JSON.parse(cachedPropertyData);
    }

    let doc = await collections?.reProperties?.find({ _id: uuid }).tryNext();
    if (doc) { // saves to cache for 60 seconds * minutes.
        await redisClient?.setEx(REDIS_KEY, 60*5, JSON.stringify(doc));
        return doc;
    } 
    
    return undefined;    
};

/**
 * Gets a properties status/state (FOR_RENT, FOR_SALE, RENTED, OWNED)
 * http://127.0.0.1:4000/v1/realestate/state/FOR_RENT
 * 
 * @param state The state to query of the property (FOR_RENT, FOR_SALE, RENTED, OWNED)
 * @returns The properties with the given state as an array of Strings
 */
export const getPropertiesState = async (state: string) => {
    // FOR_RENT, FOR_SALE, RENTED, OWNED
    let properties: string[] = [];

    await collections?.reProperties?.find({ state: state }).forEach(doc => {
        const reID = doc?._id.toString();
        properties.push(reID);
    });
    
    return properties;    
};

/**
 * Get a users CRAFT owned NFTs (for now this is the omniflix for onft testing)
 * (Acts more so like middle ware)
 * 
 * http://127.0.0.1:4000/v1/realestate/owned/craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
 * 
 * @param wallet The CRAFT address of the user
 */
export const getUsersOwnedNFTs = async (wallet: string) => {
    let usersNFTIDs = await getUsersNFTsIDsList(wallet); // { tokens: [ '1', '101', '102', '2', '8', '9' ] }
    // console.log(usersNFTIDs) 

    if(usersNFTIDs) {
        return Promise.all(usersNFTIDs?.tokens.map(token => queryToken(`${process.env.ADDR721}`, token)))
    }

    return []; 
};


/**
 * 
 * Queries a smart contract defined in .env (ADDR721) by token name. 
 * If found, decompiles the base64 data & saves to redis cache.
 * 
 * http://127.0.0.1:4000/v1/realestate/get_token/1
 * 
 * @param tokenId 
 * @returns JSON information about the property from the token_uri
 */
// IMPORTANT: We allow different addr721's so we can query from the marketplace as well as we expand to other contracts
export const queryToken = async (addr721Address: string, tokenId: string) => {
    // hget cache:query_token 10

    // Get cached
    const REDIS_KEY = `cache:query_token`;
    const REDIS_HSET_KEY = `${addr721Address}-${tokenId}` // for marketplace expansion
    let cachedToken = await redisClient?.hGet(REDIS_KEY , REDIS_HSET_KEY);
    if(cachedToken) {
        // console.log(`Token ${tokenId} found in redis cache -> ${REDIS_KEY}`);
        return JSON.parse(cachedToken);
    }

    const query = Buffer.from(`{"nft_info":{"token_id":"${tokenId}"}}`).toString('base64');
    let api = `${process.env.CRAFTD_NODE}/cosmwasm/wasm/v1/contract/${addr721Address}/smart/${query}`
    // console.log(`Querying token ${tokenId} from ${api}`);

    let response = await axios.get(api).catch(err => {
        // console.log("queryToken Error (does not exist)");
        return undefined;
    })

    // base64 encoded
    let token_uri_base64 = response?.data?.data?.token_uri; // base64 encoded string of the values
    if(!token_uri_base64) {
        // console.log(`Error querying token ${tokenId}. Token likely does not exist`);
        return undefined;
    }

    let values = Buffer.from(token_uri_base64, 'base64').toString('ascii');
    let valuesJSON = JSON.parse(values);
    
    // save to redis hSet cache
    await redisClient?.hSet(REDIS_KEY, REDIS_HSET_KEY, JSON.stringify(valuesJSON));

    return valuesJSON;
};


// this function gets all owned NFTs for a user, then queries all token_uris as well & returns that as a map.

export const getUsersNFTsIDsList = async (wallet: string) => {            
    let query = Buffer.from(`{"tokens":{"owner":"${wallet}","start_after":"0","limit":500}}`).toString('base64');
    
    let api = `${process.env.CRAFTD_NODE}/cosmwasm/wasm/v1/contract/${process.env.ADDR721}/smart/${query}`
    // console.log(api)

    let response = await axios.get(api).catch(err => {
        // console.log("getUsersNFTsIDsList Error (wallet does not exist)");
        // return { "tokens": [] };
        return undefined;
    })

    let tokens = response?.data?.data;
    // console.log(tokens) // { tokens: [ '1', '101', '102', '2', '8', '9' ] }

    return tokens;
};