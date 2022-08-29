
import { redisClient } from './database.service';
import axios from 'axios';

import { queryToken, queryTokenOwner, queryContractInfo, queryAllTokensForContract, queryGetNFTImage } from './nfts.service';
import { getAllCW721ContractAddresses } from './nftsync.service';
import { queryOfferings } from './nftmarketplace.service';

// TODO: Which of these are in the collections service? which do we remove?

// create boolean to disable caching
const allowCache = false;

export const getDetails_Offering_TokenData_Owner = async (contract_address: string, token_id: string) => {
    // const REDIS_KEY = `cache:all_details:${contract_address}`;
    // const TTL = 30;  // 10 seconds
    // const REDIS_HSET_KEY = `${token_id}`
    // let cached_information = await redisClient?.hGet(REDIS_KEY, REDIS_HSET_KEY);
    // if (cached_information) {        
    //     return JSON.parse(cached_information);
    // }

    const found = await queryOfferings(contract_address); // "" = all
    // console.log(found);
    
    const contract_info = await queryContractInfo(contract_address);
    if(!contract_info) {
        console.log(`Error. This contract is not found for this contract.`);
        return undefined;
    }

    const token_data = await queryToken(contract_address, token_id);
    if(!token_data) {
        console.log(`Error. This token id is not found for this contract.`);
        return undefined;
    }

    let dataStructure = {
        // token_id: token_id,

        // http://localhost:4000/v1/nfts/get_all_nfts/craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0/:chain?
        contract_details: {
            name: contract_info.name,
            symbol: contract_info.symbol,
            address: contract_address,
            token_id: token_id,
        },
        owner: await queryTokenOwner(contract_address, token_id),
        token_data: token_data,
        isBeingSold: false,
        offering: {}
    }

    let offering = found.find(offering => offering.contract_addr === contract_address && offering.token_id === token_id) || undefined;
    
    if(offering) {
        dataStructure.isBeingSold = true;
        dataStructure.offering = offering;
        dataStructure.owner = offering.seller;
    } else {
        dataStructure.isBeingSold = false;
        dataStructure.offering = {}
    }

    // TODO: ADD CACHE
    // await redisClient?.hSet(REDIS_KEY, REDIS_HSET_KEY, JSON.stringify(dataStructure));
    // await redisClient?.expire(REDIS_KEY, TTL);
    return dataStructure;
};


export const getCollectionTotalVolume = async (contract_address: string = "") => {
    // Not sure if we should cache or not? maybe like 30 seconds?

    // Cache'ed offerings so we don't spam contract too often. if we requests from a single user, its just at the end
    let REDIS_KEY = `cache:collection_volume:${contract_address}`;    
    let get_volume = await redisClient?.get(REDIS_KEY);
    if (allowCache && get_volume) {    
        return JSON.parse(get_volume);
    }

    // Make query to the contract, we don't use CosmJS bc of error handling issues
    // const query = Buffer.from(`{"get_offerings":{}}`).toString('base64');
    const query = Buffer.from(`{"get_collection_volume":{"address":"${contract_address}"}}`).toString('base64');
    let api = `${process.env.CRAFTD_REST}/cosmwasm/wasm/v1/contract/${process.env.ADDRM}/smart/${query}`
    console.log(`Querying coll_volume ${contract_address} from ${api}`);

    let response = await axios.get(api).catch(err => {
        // console.log("queryToken Error (does not exist)");
        return undefined;
    })

    if(!response) {
        return 0;
    }

    console.log(response.data);

    const volume = response.data?.data;
    

    // save to redis
    await redisClient?.set(REDIS_KEY, JSON.stringify(volume));
    await redisClient?.expire(REDIS_KEY, 60); // 30 second cache time
    return volume;
};


export const getAllCollections = async () => {
    const REDIS_KEY = `cache:all_collections`;
    const TTL = 10;  // TODO: Make this like 1 day+
    let cached_collection_data = await redisClient?.get(REDIS_KEY);
    if (allowCache && cached_collection_data) { 
        return JSON.parse(cached_collection_data);
    }

    // get all assets by code id, then get them here
    let collections = {} as any;
    const collection_addresses = await getAllCW721ContractAddresses();
    if(!collection_addresses) {
        console.log(`Error. No collection addresses found.`);
        return undefined;
    }

    for(const addr of collection_addresses) {
        let [contract_info, token_ids] = await Promise.all([
            await queryContractInfo(addr), 
            await queryAllTokensForContract(addr)
        ]);

        let _nft_type = "link"
        if(addr === process.env.ADDR721_REALESTATE) { _nft_type = "real_estate"; }

        collections[addr] = {
            name: contract_info.name,
            symbol: contract_info.symbol,
            _nft_type: _nft_type,
            preview: await queryGetNFTImage(addr, token_ids[0]),
            volume: await getCollectionTotalVolume(addr),
            num_tokens: token_ids.length,
            token_ids: token_ids,       
        }
    }

    await redisClient?.set(REDIS_KEY, JSON.stringify(collections));
    await redisClient?.expire(REDIS_KEY, TTL);
    
    return collections;
};



export const getTokensInCollection = async (contract_address: string) => {
    // this is cached
    const collection_data = await getAllCollections();
    // check if key contract_address is in collection_data
    if(!collection_data[contract_address]) {
        console.log(`Error. This contract is not found for this contract.`);
        return undefined;
    }

    const collection = collection_data[contract_address];
    const token_ids = collection.token_ids;
    const token_data = await Promise.all(token_ids.map(token_id => queryToken(contract_address, token_id)));
    return token_data;    
}