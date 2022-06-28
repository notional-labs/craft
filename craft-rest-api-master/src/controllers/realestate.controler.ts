// Express
import { Request, Response } from 'express';
import { getPropertyInformation, getUsersOwnedNFTs, getPropertiesState, queryToken } from '../services/realestate.service';

export const getInformation = async (req: Request, res: Response) => {
    const { id } = req.params;

    const doc = await getPropertyInformation(id);
    if (doc) return res.status(200).json(doc);
    else return res.status(404).json({ message: 'Not Found' });
};

export const getPropertyState = async (req: Request, res: Response) => {
    const { state } = req.params;

    const response = await getPropertiesState(state.toUpperCase());
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No properties with state ${state.toUpperCase()} found!` });
};


export const getPlayersOwnedNFTs = async (req: Request, res: Response) => {
    const { wallet } = req.params;
    // console.log(wallet)

    const response = await getUsersOwnedNFTs(wallet); // [{_id: "dbcd78cb-326e-4842-982b-9252f9ca25a7", "name": "", "type": "GOVERNMENT", ...}, {...}]
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'No Real Estate NFTs found for this wallet' });
};

export const getPropertyByTokenFromNFT = async (req: Request, res: Response) => {
    const { token_id } = req.params;

    // {"_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "Mid-sized Mansion", "description": "A beautiful mansion.", ...}
    const response = await queryToken(`${process.env.ADDR721}`, token_id); 
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No NFTs with the token id ${token_id} found!` });
};

export default {
    getInformation,
    getPlayersOwnedNFTs,
    getPropertyState,
    getPropertyByTokenFromNFT,
    // getForRent
};