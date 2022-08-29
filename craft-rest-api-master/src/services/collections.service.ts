
import { redisClient } from './database.service';
import axios from 'axios';

import { queryToken, queryTokenOwner, queryContractInfo, queryAllTokensForContract, queryGetNFTImage } from './nfts.service';
import { getAllCW721ContractAddresses } from './nftsync.service';
import { queryOfferings } from './nftmarketplace.service';

import { CosmWasmClient } from 'cosmwasm';

// create boolean to disable caching
const allowCache = false;

export const getCollectionTotalVolume = async (client: CosmWasmClient, contract_address: string = "") => {
    // Not sure if we should cache or not? maybe like 30 seconds?

    // Cache'ed offerings so we don't spam contract too often. if we requests from a single user, its just at the end
    let REDIS_KEY = `cache:collection_volume:${contract_address}`;    
    
    if (allowCache) {    
        let get_volume = await redisClient?.get(REDIS_KEY);
        if (get_volume) {
            return JSON.parse(get_volume);
        }
    }

    let response = await client.queryContractSmart(`${process.env.ADDRM}`, {get_collection_volume: {address: contract_address}})
    // .catch(err => {
    //     // console.log("queryToken Error (does not exist)");
    //     return un;
    // });
    // console.log(response);
    
    const volume = response; // undefined if the query errored (not a good address)
    if(allowCache) {
        await redisClient?.set(REDIS_KEY, JSON.stringify(volume));
        await redisClient?.expire(REDIS_KEY, 2*60); 
    }    
    return volume;
};

// call from Promise.all()
export const getDetails_Offering_TokenData_Owner = async (client: CosmWasmClient, contract_address: string, token_id: string) => {
    const REDIS_KEY = `cache:all_details:${contract_address}`;
    const TTL = 30;  // 10 seconds
    const REDIS_HSET_KEY = `${token_id}`    
    if (allowCache) {        
        let cached_information = await redisClient?.hGet(REDIS_KEY, REDIS_HSET_KEY);
        if(cached_information) {
            return JSON.parse(cached_information);
        }            
    }    

    // query asset directly, check owner. if owner != marketplace (user owns it, not for sale). So just skip that part
    // if it is the marketplace, then we know to query it for the extra data
    
    const contract_info = await queryContractInfo(client, contract_address);
    if(!contract_info) {
        console.log(`Error. This contract is not found for in this chain.`);
        return undefined;
    }

    const found = await queryOfferings(contract_address); // "" = all
    let offering = found.find(offering => offering.contract_addr === contract_address && offering.token_id === token_id) || undefined;

    // We grab the token_data from the offering if its there, otherwise it is not being sold so we query directly
    // This saves ~2.5 seconds (on a 5 second query if it is being sold) , Thus speeding up the webapp without caching.
    let token_data = undefined;
    if(offering) {
        token_data = offering.token_data
        // console.log("found token data, ", token_data);
    } else {
        // token data not found in the query (its not being sold, so we query it directly)
        // console.log("token data not found, querying directly");        
        token_data = await queryToken(client, contract_address, token_id);
        if(!token_data) {
            console.log(`Error. This token id is not found for this contract.`);
            return undefined;
        }
    }
    
    // console.log("TOKEN-DATA:" , token_data) // can we get owner from here?

    
    // console.log(found);

    let dataStructure = {
        // token_id: token_id,

        // http://localhost:4000/v1/nfts/get_all_nfts/craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0/:chain?
        contract_details: {
            name: contract_info.name,
            symbol: contract_info.symbol,
            address: contract_address,
            token_id: token_id,
        },
        owner: await queryTokenOwner(client, contract_address, token_id),
        token_data: token_data,
        isBeingSold: false,
        offering: {}
    }

    
    
    if(offering) {
        dataStructure.isBeingSold = true;
        dataStructure.offering = offering;
        dataStructure.owner = offering.seller;
    } else {
        dataStructure.isBeingSold = false;
        dataStructure.offering = {}
    }
    
    if(allowCache){
        await redisClient?.hSet(REDIS_KEY, REDIS_HSET_KEY, JSON.stringify(dataStructure));
        await redisClient?.expire(REDIS_KEY, TTL);
    }
    return dataStructure;
};


// TODO: IMPORTANT, MOVE ALL CACHES LIKE THIS
export const getAllCollections = async () => {
    const client = await CosmWasmClient.connect(`${process.env.CRAFTD_NODE}/`);
    console.log(`Getting all collections`);

    const REDIS_KEY = `cache:all_collections`;
    const TTL = 30;  // TODO: Make this like 1 day+
    if (allowCache) { 
        let cached_collection_data = await redisClient?.get(REDIS_KEY);
        if(cached_collection_data)
            return JSON.parse(cached_collection_data);
    }

    // get all assets by code id, then get them here
    let collections = {} as any;
    const collection_addresses = await getAllCW721ContractAddresses();    
    if(!collection_addresses) {
        console.log(`Error. No collection addresses found.`);
        return undefined;
    }

    let pending_promises: any = []; // Like how 'queryOfferings' does it
    for(const addr of collection_addresses) {
        pending_promises.push({
            contract_info: queryContractInfo(client, addr),
            all_tokens: queryAllTokensForContract(client, addr),
            addr: addr,
        })        
    }

    let result = await Promise.all(pending_promises.map((p: any) => Promise.all(Object.values(p))));
    for(const [contract_info, token_ids, addr] of result) {
        let _nft_type = "link"
        if(addr === process.env.ADDR721_REALESTATE) { _nft_type = "real_estate"; }

        collections[addr] = {
            name: contract_info.name,
            symbol: contract_info.symbol,
            _nft_type: _nft_type,
            preview: await queryGetNFTImage(client, addr, token_ids[0]),
            num_tokens: token_ids.length,
            token_ids: token_ids,            
        }
    }

    if(allowCache) {
        await redisClient?.set(REDIS_KEY, JSON.stringify(collections));
        await redisClient?.expire(REDIS_KEY, TTL);
    }
    return collections;
};



export const getTokensInCollection = async (client: CosmWasmClient, contract_address: string) => {
    // this is cached
    const collection_data = await getAllCollections();
    // check if key contract_address is in collection_data

    console.log(`Getting all tokens in collection ${contract_address}`);
    // print collection data
    // console.log("collection_data", collection_data);

    // TODO:
    if(!collection_data[contract_address]) {
        console.log(`Error. This contract ${contract_address} is not found for all collections.`);
        return 0;
    }

    const collection = collection_data[contract_address];
    const token_ids = collection.token_ids;
    const token_data = await Promise.all(token_ids.map(token_id => queryToken(client, contract_address, token_id)));
    return token_data;    
}