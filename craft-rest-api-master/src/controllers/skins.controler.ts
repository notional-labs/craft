// Express
import { Request, Response } from 'express';

// import { getPropertyInformation,getPropertiesState, getOwnedUUIDsList } from '../services/realestate.service';
import { getUsersOwnedNFTs, queryToken } from '../services/nfts.service';


// http://localhost:4000/v1/skins/owned_skin_list/craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
export const getPlayersOwnedNFTs = async (req: Request, res: Response) => {
    const { wallet } = req.params;
    // console.log(wallet)

    const response = await getUsersOwnedNFTs(`${process.env.ADDR721_SKINS}`, wallet);
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'No Skin NFTs found for this wallet' });
};

// http://localhost:4000/v1/skins/values/1
export const getSkinValuesFromNFT = async (req: Request, res: Response) => {
    const { token_id } = req.params;

    // {"_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "Mid-sized Mansion", "description": "A beautiful mansion.", ...}
    const response = await queryToken(`${process.env.ADDR721_SKINS}`, token_id); 
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No NFTs with the token id ${token_id} found!` });
};

export default {
    getPlayersOwnedNFTs,
    getSkinValuesFromNFT,
};