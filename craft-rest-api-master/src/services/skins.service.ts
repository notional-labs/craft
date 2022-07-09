import { collections, redisClient } from './database.service';

import { getUsersOwnedNFTs } from './nfts.service'; // getUsersNFTsIDsList, queryToken

import axios from 'axios';


/**
 * Gets a list of the UUIDs a users wallet owns (to resync properties in game)
 * 
 * http://127.0.0.1:4000/v1/realestate/owned_uuids/craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
 * 
 * @param wallet The CRAFT address of the user
 */
export const getOwnedSkinsList = async (wallet: string) => {
    let usersProperties = await getUsersOwnedNFTs(`${process.env.ADDR721_SKINS}`, wallet); // [{_id, name, type, imageLink, ...}, {...}]
    
    if(usersProperties) {
        // [ "dbcd78cb-326e-4842-982b-9252f9ca25a7", "84561fc8-9450-4a70-9fa2-7d43227ee98f", ...]
        return Promise.all(usersProperties?.map(property => property?._id));
    }
    return [];    
};