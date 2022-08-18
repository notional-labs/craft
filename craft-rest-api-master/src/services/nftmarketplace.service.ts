import { collections, redisClient } from './database.service';

import axios from 'axios';

import { queryToken, queryContractInfo } from '../services/nfts.service';
import { getCraftUSDPrice } from '../services/pricing.service';

// create boolean to disable caching
const allowCache = false;

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
export const queryOfferings = async (contract_address: string = "", from_craft_address: string = "") => {
    // Not sure if we should cache or not? maybe like 30 seconds?

    // Cache'ed offerings so we don't spam contract too often. if we requests from a single user, its just at the end
    let REDIS_KEY = `cache:marketplace_offerings:${contract_address}`;
    if(from_craft_address.length > 0) { REDIS_KEY += `:${from_craft_address}`; }
    let get_offerings = await redisClient?.get(REDIS_KEY);
    if (allowCache && get_offerings) {
        // console.log(`Asset: ${denom} holdings ${get_wallet_value} found in redis cache -> ${REDIS_KEY}`);
        return JSON.parse(get_offerings);
    }

    // Make query to the contract, we don't use CosmJS bc of error handling issues
    const query = Buffer.from(`{"get_offerings":{}}`).toString('base64');
    let api = `${process.env.CRAFTD_REST}/cosmwasm/wasm/v1/contract/${process.env.ADDRM}/smart/${query}`
    // console.log(`Querying token ${tokenId} from ${api}`);

    let response = await axios.get(api).catch(err => {
        // console.log("queryToken Error (does not exist)");
        return undefined;
    })

    const craftUSDPrice = await getCraftUSDPrice();

    // List of dicts which contain {id, token_id, list_price:{address, amount}, contract_addr, seller, token_uri}
    // where token uri is from queryToken (query the contract_addr 721 -> get base64 data, and decode)
    let data = response?.data?.data?.offerings; // base64 encoded string of the values. May be other data too
    
    // Queries tokens for sale with their parent contract for the offering.
    let offerings: string[] = []; // selective offerings we want to return based on address
    if(!data) { return offerings; }
    
    for(let i = 0; i < data.length; i++) {
        let offering = data[i];

        if(contract_address.length > 0 && offering.contract_addr !== contract_address) {
            // we only want contracts being sold from a specific 721 contract
            continue;
        }
        if(from_craft_address.length > 0 && offering.seller !== from_craft_address) {
            // we only want contracts which are from a specific craft address
            console.log(`Offering ${offering.token_id} is not from ${from_craft_address}`);
            continue;
        }

        // query token
        let token_data = await queryToken(offering.contract_addr, offering.token_id);
        let contract_info = await queryContractInfo(offering.contract_addr);

        // console.log(token_data);
        if(token_data) {
            if(contract_info) {
                offering.collection_name = contract_info.name; // 721
                offering.symbol = contract_info.symbol; // 721
            }
            
            // modify the data in line so that way it is easier for the webapp team
            offering.usd_cost = Number(craftUSDPrice) * (Number(offering.list_price)/1_000_000);
            offering.token_data = token_data;

            offerings.push(offering);       
        }
    }

    // save to redis
    await redisClient?.set(REDIS_KEY, JSON.stringify(offerings));
    await redisClient?.expire(REDIS_KEY, 30); // 30 second cache time

    return offerings;
};


export const queryPaintingOfferings = async () => {
    // TODO: Redis Cache
    const REDIS_KEY = `cache:marketplace_offerings:paintings`;
    let painting_offerings = await redisClient?.get(REDIS_KEY);
    if (allowCache && painting_offerings) {
        // console.log(`Asset: ${denom} holdings ${get_wallet_value} found in redis cache -> ${REDIS_KEY}`);
        return JSON.parse(painting_offerings);
    }

    const addr_to_ignore = `${process.env.ADDR721_REALESTATE}`;
    let offerings = await queryOfferings("");

    let paintings: string[] = [];
    for(let i = 0; i < offerings.length; i++) {
        let offering = offerings[i];
        // and/or check token_data as well?
        if(offering.contract_addr !== addr_to_ignore) {
            paintings.push(offering);
        }
    }

    // save to redis
    await redisClient?.set(REDIS_KEY, JSON.stringify(paintings));
    await redisClient?.expire(REDIS_KEY, 30); // 30 second cache time

    return paintings;
}


export const queryFeatured = async (amount: number) => {
    let REDIS_KEY = `cache:marketplace_offerings_featured:${amount}`;
    let get_featured = await redisClient?.get(REDIS_KEY);
    if (allowCache && get_featured) {        
        return JSON.parse(get_featured);
    }


    let re = await queryOfferings(`${process.env.ADDR721_REALESTATE}`);
    let paintings = await queryPaintingOfferings();

    // loop through paintings & real estate, get the top by price USD, and just return those
    let feat_paintings = await getTopOfferingsSorted(paintings, amount).then(sorted => {
        return sorted;
    });     
     let feat_re = await getTopOfferingsSorted(re, amount).then(sorted => {
        return sorted;
    });

    // create a map with the keys "real_estate" & "paintings"
    let featured: any = {};
    featured.real_estate = feat_re;
    featured.paintings = feat_paintings;
    await redisClient.setEx(REDIS_KEY, 2*60, JSON.stringify(featured)); // 2 minute cache
    return featured;
}



const getTopOfferingsSorted = async (offerings: any[], amount: number) => {
    let sorted = offerings.sort((a, b) => {
        // console.log(`a: ${a.usd_cost} b: ${b.usd_cost}`);
        return b.usd_cost - a.usd_cost;
    }).slice(0, amount);
    // console.log("sorted", sorted);
    return sorted;
}