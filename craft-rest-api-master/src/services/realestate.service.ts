import { collections, redisClient } from './database.service';

import { getUsersOwnedNFTs } from './nfts.service'; // getUsersNFTsIDsList, queryToken

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
 * Gets a list of the UUIDs a users wallet owns (to resync properties in game)
 * 
 * http://127.0.0.1:4000/v1/realestate/owned_uuids/craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
 * 
 * @param wallet The CRAFT address of the user
 */
export const getOwnedUUIDsList = async (wallet: string) => {
    let usersProperties = await getUsersOwnedNFTs(`${process.env.ADDR721_REALESTATE}`, wallet); // [{_id, name, type, imageLink, ...}, {...}]
    
    if(usersProperties) {
        // [ "dbcd78cb-326e-4842-982b-9252f9ca25a7", "84561fc8-9450-4a70-9fa2-7d43227ee98f", ...]
        return Promise.all(usersProperties?.map(property => property?._id));
    }
    return [];    
};