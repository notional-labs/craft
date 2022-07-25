import { Request, Response } from 'express';
import { queryOfferings,  } from '../services/nftmarketplace.service';

export const getMarketplaceOfferings = async (req: Request, res: Response) => {
    // const { uuid } = req.params;

    const found = await queryOfferings(""); // "" = all

    if (found) return res.status(200).json(found) 
    else return res.status(404).json({ message: 'Transaction not found' });
};

export const getMarketplaceRealEstateOfferings = async (req: Request, res: Response) => {
    const found = await queryOfferings(`${process.env.ADDR721_REALESTATE}`); // all from our real estate collection

    if (found) return res.status(200).json(found) 
    else return res.status(404).json({ message: 'Transaction not found' });
};

export const getMarketplaceSpecificContractOffering = async (req: Request, res: Response) => {
    const { parent_contract_address } = req.params;
    const found = await queryOfferings(parent_contract_address);
    if (found) return res.status(200).json(found) 
    else return res.status(404).json({ message: 'Transaction not found' });
};

export default {
    getMarketplaceOfferings,
    getMarketplaceRealEstateOfferings,    
    getMarketplaceSpecificContractOffering
};