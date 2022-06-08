// Express
import { Request, Response } from 'express';
import { getPropertyInformation, getNFTs } from '../services/realestate.service';

export const getInformation = async (req: Request, res: Response) => {
    const { id } = req.params;

    const doc = await getPropertyInformation(id);
    if (doc) return res.status(200).json(doc);
    else return res.status(404).json({ message: 'User not found' });
};

export const getPlayersOwnedNFTs = async (req: Request, res: Response) => {
    const { wallet } = req.params;
    // console.log(wallet)

    const response = await getNFTs(wallet);
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'No Real Estate NFTs found for this wallet' });
};

export default {
    getInformation,
    getPlayersOwnedNFTs
};