import { collections, redisClient } from './database.service';

import { getUsersOwnedNFTs } from './nfts.service'; // getUsersNFTsIDsList, queryToken

import axios from 'axios';

// create boolean to disable caching
const allowCache = false;

/**
 * Get data about a property (name, desc, worldName, imageLink, etc.) - from reProperties server database
 * http://127.0.0.1:4000/v1/realestate/dbcd78cb-326e-4842-982b-9252f9ca25a7
 * 
 * @param uuid The UUID of the property
 */
export const getPropertyInformation = async (uuid: string) => {
    const REDIS_KEY = `cache:property_info`;
    let cachedPropertyData = await redisClient?.hGet(REDIS_KEY, `${uuid}`);
    if(allowCache && cachedPropertyData) {
        console.log(`Property found in redis cache -> ${REDIS_KEY}. Not calling MongoDB`);
        return JSON.parse(cachedPropertyData);
    }

    let doc = await collections?.reProperties?.find({ _id: uuid }).tryNext();

    if (doc) { 
        doc.cityName = await getCityNameFromID(doc?.cityId);
        doc.buildingName = await getBuildingNameFromID(doc?.buildingId);
        // console.log(`Cityname: ${cityName}, Buildingname: ${buildingName}`);

        await redisClient.hSet(REDIS_KEY, `${uuid}`, JSON.stringify(doc));
        // do we want to expire the HSET? maybe every hour or something.
        return doc;
    } 
    
    return "";    
};


export const getCityNameFromID = async (cityID: string) => {
    const REDIS_KEY = `cache:citynames`;
    let cachedCityname = await redisClient?.hGet(REDIS_KEY, `${cityID}`);
    if(allowCache && cachedCityname) {
        console.log(`Cityname found in redis cache -> ${REDIS_KEY}. Not calling MongoDB`);
        return cachedCityname;
    }

    let doc = await collections?.reCities?.find({ _id: cityID }).tryNext();
    if (doc) { // saves to cache for 60 seconds * minutes.
        await redisClient?.hSet(REDIS_KEY, `${cityID}`, doc.name);
        return doc.name;
    } 
    
    return "";    
}
export const getBuildingNameFromID = async (buildingId: string) => {
    const REDIS_KEY = `cache:buildingnames`;
    let cachedBuildingName = await redisClient?.hGet(REDIS_KEY, `${buildingId}`);
    if(allowCache && cachedBuildingName) {
        console.log(`BuildingName found in redis cache -> ${cachedBuildingName} from ${buildingId}. Not calling MongoDB`);
        return cachedBuildingName;
    }

    let doc = await collections?.reBuildings?.find({ _id: buildingId }).tryNext();
    if (doc) { // saves to cache for 60 seconds * minutes.        
        await redisClient?.hSet(REDIS_KEY, `${buildingId}`, doc.name);
        return doc.name;
    } 
    
    return "";    
}

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