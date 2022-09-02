// Express
import { Request, Response } from 'express';


// import { getPropertyInformation,getPropertiesState, getOwnedUUIDsList } from '../services/realestate.service';
// import { getDetails_Offering_TokenData_Owner, getAllCollections, getTokensInCollection, getCollectionTotalVolume } from '../services/assets.service';

import { getAllCollections, getTokensInCollection, getCollectionData, getRecentlySoldItems } from '../services/collections.service';

import { queryToken, queryTokenOwner, queryContractInfo } from '../services/nfts.service';
import { queryOfferings } from '../services/nftmarketplace.service';
import { CosmWasmClient } from 'cosmwasm';
import { getCosmWasmClient } from '../services/wasmclient.service';

export const getAllCollectionData = async (req: Request, res: Response) => {
    // const { contract_address, token_id } = req.params;

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    const all_collections = await getAllCollections(client);
    // console.log(all_collections)

    if (all_collections) return res.status(200).json(all_collections) 
    else return res.status(404).json({ message: `Error! no collections found` });
};

export const getRecentlySold = async (req: Request, res: Response) => {    
    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    const recently_sold = await getRecentlySoldItems(client);
    // console.log(all_collections)

    if (recently_sold) return res.status(200).json(recently_sold) 
    else return res.status(404).json({ message: `Error! No recently sold issue` });
};

export const getAllTokensInCollection = async (req: Request, res: Response) => {
    const { contract_address } = req.params;

    // if contract_address == volume, return this collection can not be found
    if (contract_address == "volume") return res.status(404).json({ message: `Error! this collection can not be found` });

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    const all_assets = await getTokensInCollection(client, contract_address);
    // console.log(all_assets)

    if (all_assets) return res.status(200).json(all_assets) 
    else return res.status(404).json({ message: `Error! this collection can not be found!` });
};

export const getCollectionStats = async (req: Request, res: Response) => {
    const { contract_address } = req.params;

    const client = await getCosmWasmClient();
    if(!client) { return res.status(500).json({ message: `Error: could not connect to craft node.` }); }

    const total_volume = await getCollectionData(client, contract_address);
    
    if (total_volume) return res.status(200).json(total_volume) 
    else return res.status(404).json({ message: `Error! this collection can not be found!` });
};


export default {
    getAllCollectionData,
    getAllTokensInCollection,
    getCollectionStats,
    getRecentlySold,
};