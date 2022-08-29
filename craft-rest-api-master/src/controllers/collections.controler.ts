// Express
import { Request, Response } from 'express';


// import { getPropertyInformation,getPropertiesState, getOwnedUUIDsList } from '../services/realestate.service';
import { getDetails_Offering_TokenData_Owner, getAllCollections, getTokensInCollection, getCollectionTotalVolume } from '../services/assets.service';
import { queryToken, queryTokenOwner, queryContractInfo } from '../services/nfts.service';
import { queryOfferings } from '../services/nftmarketplace.service';

export const getAllCollectionData = async (req: Request, res: Response) => {
    // const { contract_address, token_id } = req.params;

    const all_collections = await getAllCollections();
    // console.log(all_collections)

    if (all_collections) return res.status(200).json(all_collections) 
    else return res.status(404).json({ message: `Error! no collections found` });
};

export const getAllTokensInCollection = async (req: Request, res: Response) => {
    const { contract_address } = req.params;

    const all_assets = await getTokensInCollection(contract_address);
    console.log(all_assets)

    if (all_assets) return res.status(200).json(all_assets) 
    else return res.status(404).json({ message: `Error! this collection can not be found!` });
};

export const getCollectionVolume = async (req: Request, res: Response) => {
    const { contract_address } = req.params;

    const total_volume = await getCollectionTotalVolume(contract_address);
    // console.log(all_assets)

    if (total_volume) return res.status(200).json(total_volume) 
    else return res.status(404).json({ message: `Error! this collection can not be found!` });
};


export default {
    getAllCollectionData,
    getAllTokensInCollection,
    getCollectionVolume
};