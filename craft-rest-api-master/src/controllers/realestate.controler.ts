// Express
import { Request, Response } from 'express';

import { getPropertyInformation,getPropertiesState, getOwnedUUIDsList } from '../services/realestate.service';
import { getUsersOwnedNFTs, queryToken } from '../services/nfts.service';
import { CosmWasmClient } from 'cosmwasm';
import { getCosmWasmClient } from '../services/wasmclient.service';

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
    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    const response = await getUsersOwnedNFTs(client, `${process.env.ADDR721_REALESTATE}`, wallet); // [{_id: "dbcd78cb-326e-4842-982b-9252f9ca25a7", "name": "", "type": "GOVERNMENT", ...}, {...}]
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'No Real Estate NFTs found for this wallet' });
};

export const getPlayersOwnedUUIDsList = async (req: Request, res: Response) => {
    const { wallet } = req.params;
    // console.log(wallet)

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    const response = await getOwnedUUIDsList(client, wallet); // [{_id: "dbcd78cb-326e-4842-982b-9252f9ca25a7", "name": "", "type": "GOVERNMENT", ...}, {...}]
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'No Real Estate NFTs found for this wallet' });
};

export const getPropertyByTokenFromNFT = async (req: Request, res: Response) => {
    const { token_id } = req.params;

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    // {"_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "Mid-sized Mansion", "description": "A beautiful mansion.", ...}
    const response = await queryToken(client, `${process.env.ADDR721_REALESTATE}`, token_id); 
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: `No NFTs with the token id ${token_id} found!` });
};

export default {
    getInformation,
    getPlayersOwnedNFTs,
    getPropertyState,
    getPropertyByTokenFromNFT,
    getPlayersOwnedUUIDsList,
    // getForRent
};