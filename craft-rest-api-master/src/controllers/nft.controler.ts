// Express
import { Request, Response } from 'express';
import { getUsersOwnedNFTs, queryToken } from '../services/nfts.service';
import { getUsersNFTsFromOtherPlatforms, getAllNFTs } from '../services/nftsync.service';

export const getPlayersOwnedNFTs = async (req: Request, res: Response) => {
    const { addr721_address, wallet } = req.params;
    // console.log(wallet)

    const response = await getUsersOwnedNFTs(addr721_address, wallet);
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'No Real Estate NFTs found for this wallet' });
};

export const getDataFromTokenID = async (req: Request, res: Response) => {
    const { addr721_address, token_id } = req.params;

    // {"_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "Mid-sized Mansion", "description": "A beautiful mansion.", ...}
    const response = await queryToken(addr721_address, token_id); 
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No NFTs with the token id ${token_id} found!` });
};

export const syncOtherPlatformNFTs = async (req: Request, res: Response) => {
    const { craft_address } = req.params;

    // {"_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "Mid-sized Mansion", "description": "A beautiful mansion.", ...}
    const response = await getUsersNFTsFromOtherPlatforms(craft_address); 
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No NFTS to sync! Make this a post in the future!` });
};

export const getAllUserNFTs = async (req: Request, res: Response) => {
    const { craft_address } = req.params;

    const response = await getAllNFTs(craft_address); 
    if (response) return res.status(200).json(response);    
    else return res.status(404).json({}); // return an empty set = no nfts
};

export const getContractAddresses = async (req: Request, res: Response) => {
    const addresses = {
        "ADDR721_REALESTATE": process.env.ADDR721_REALESTATE,             
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
};