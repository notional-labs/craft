import { collections, redisClient } from './database.service';

import axios from 'axios';

import { queryToken } from '../services/nfts.service';
import { getCraftUSDPrice } from '../services/pricing.service';
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
export const queryOfferings = async (contract_address: string) => {
    // Not sure if we should cache or not? maybe like 30 seconds?
    if(!contract_address) { 
        // get market offerings from only contracts with this contract address. if "", get all
        contract_address = "";
    }

    // Cache'ed offerings so we don't spam contract too often
    const REDIS_KEY = `cache:marketplace_offerings:${contract_address}`;
    let get_offerings = await redisClient?.get(REDIS_KEY);
    if (get_offerings) {
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
    // 
    let offerings: string[] = []; // selective offerings we want to return based on address
    for(let i = 0; i < data.length; i++) {
        let offering = data[i];

        if(contract_address.length > 0 && offering.contract_addr !== contract_address) {
            // if we only want a specific contract address to be returned, we do this
            continue;
        }

        // query token
        let token_data = await queryToken(offering.contract_addr, offering.token_id);
        // console.log(token_data);
        if(token_data) {
            // modify the data in line so that way it is easier for the webapp team
            offering.usd_cost = Number(craftUSDPrice) * (Number(offering.list_price)/1_000_000);
            offering.token_data = token_data;             
            offerings.push(offering);       
        }
    }

    // save to redis
    await redisClient?.set(REDIS_KEY, JSON.stringify(offerings));
    await redisClient?.expire(REDIS_KEY, 15); // 15 second cache time

    return offerings;
};