import { collections, redisClient } from './database.service';

import axios from 'axios';

import { queryToken } from '../services/realestate.service';

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
export const queryOfferings = async () => {
    // Not sure if we should cache or not? maybe like 30 seconds?

    const query = Buffer.from(`{"get_offerings":{}}`).toString('base64');
    let api = `${process.env.CRAFTD_NODE}/cosmwasm/wasm/v1/contract/${process.env.ADDRM}/smart/${query}`
    // console.log(`Querying token ${tokenId} from ${api}`);

    let response = await axios.get(api).catch(err => {
        // console.log("queryToken Error (does not exist)");
        return undefined;
    })

    // List of dicts which contain {id, token_id, list_price:{address, amount}, contract_addr, seller, token_uri}
    // where token uri is from queryToken (query the contract_addr 721 -> get base64 data, and decode)
    let data = response?.data?.data?.offerings; // base64 encoded string of the values
    
    // Queries tokens for sale with their parent contract for the offering.
    // 
    let offerings = [];
    for(let i = 0; i < data.length; i++) {
        let offering = data[i];
        // query token
        let token_data = await queryToken(offering.contract_addr, offering.token_id);
        // console.log(token_data);
        if(token_data) {
            offering.token_uri = token_data;        
        }
    }

    return data;
};