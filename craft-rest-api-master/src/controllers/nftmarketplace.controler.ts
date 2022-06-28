import { Request, Response } from 'express';
import { queryOfferings,  } from '../services/nftmarketplace.service';

export const getMarketplaceOfferings = async (req: Request, res: Response) => {
    // const { uuid } = req.params;

    const found = await queryOfferings();

    if (found) return res.status(200).json(found) 
    else return res.status(404).json({ message: 'Transaction not found' });
};

export const getContractAddresses = async (req: Request, res: Response) => {
    const addresses = {
        "CW721": process.env.ADDR721,
        "CW20": process.env.ADDR20,
        "CW-Marketplace": process.env.ADDRM,   
    }
    return res.status(200).json(addresses) 
};

export default {
    getMarketplaceOfferings,
    getContractAddresses,
};