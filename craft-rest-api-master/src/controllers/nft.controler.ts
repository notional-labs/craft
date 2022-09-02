// Express
import { CosmWasmClient } from 'cosmwasm';
import { Request, Response } from 'express';
import { getCosmWasmClient } from '../services/wasmclient.service';
import { getUsersOwnedNFTs, queryToken, queryContractInfo } from '../services/nfts.service';
import { getUsersNFTsFromOtherPlatforms, getAllNFTs } from '../services/nftsync.service';

export const getPlayersOwnedNFTs = async (req: Request, res: Response) => {
    const { addr721_address, wallet } = req.params;
    // console.log(wallet)

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    const response = await getUsersOwnedNFTs(client, addr721_address, wallet);
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'No Real Estate NFTs found for this wallet' });
};

export const getDataFromTokenID = async (req: Request, res: Response) => {
    const { addr721_address, token_id } = req.params;

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    // {"_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "Mid-sized Mansion", "description": "A beautiful mansion.", ...}
    const response = await queryToken(client, addr721_address, token_id); 
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No NFTs with the token id ${token_id} found!` });
};

export const getContractInformation = async (req: Request, res: Response) => {
    const { addr721_address } = req.params;

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    // {"_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "Mid-sized Mansion", "description": "A beautiful mansion.", ...}
    const response = await queryContractInfo(client, addr721_address); 
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No contract with this address found ${addr721_address}!` });
};

export const syncOtherPlatformNFTs = async (req: Request, res: Response) => {
    const { craft_address } = req.params;

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    // {"_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "Mid-sized Mansion", "description": "A beautiful mansion.", ...}
    const response = await getUsersNFTsFromOtherPlatforms(client, craft_address); 
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No NFTS to sync! Make this a post in the future!` });
};

export const getAllUserNFTs = async (req: Request, res: Response) => {
    const { craft_address } = req.params;

    const requested_chain = req.query.chain?.toString() || "*"; // craft, omniflix, or stargaze

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }
    
    const response = await getAllNFTs(client, craft_address, requested_chain); 
    if (response) return res.status(200).json(response);    
    else return res.status(404).json({}); // return an empty set = no nfts
};

// 
export const getAllUserNFTsIncludingOfferings = async (req: Request, res: Response) => {
    const { craft_address } = req.params;
    const requested_chain = req.query.chain?.toString() || "*"; // craft, omniflix, or stargaze

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }
    
    const response = await getAllNFTs(client, craft_address, requested_chain, true); 
    if (response) return res.status(200).json(response);    
    else return res.status(404).json({}); // return an empty set = no nfts
};

export const getContractAddresses = async (req: Request, res: Response) => {

    const other_contracts: string[] = [];
    if (process.env.OTHER_DAO_721_CONTRACTS && process.env.OTHER_DAO_721_CONTRACTS.length > 0) {
        other_contracts.push(...process.env.OTHER_DAO_721_CONTRACTS.split(','));
    }

    const addresses = {
        "ADDR721_REALESTATE": process.env.ADDR721_REALESTATE,
        "ADDR721_IMAGES": process.env.ADDR721_IMAGES,
        "OTHER": other_contracts,
        "MARKETPLACE": process.env.ADDRM,   
    }
    return res.status(200).json(addresses) 
};

export default {
    getPlayersOwnedNFTs,
    getDataFromTokenID,
    getContractAddresses,
    syncOtherPlatformNFTs,
    getAllUserNFTs,
    getAllUserNFTsIncludingOfferings,
    getContractInformation,
};