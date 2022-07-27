import { Request, Response } from 'express';
import { queryOfferings, queryPaintingOfferings, queryFeatured } from '../services/nftmarketplace.service';

export const getMarketplaceOfferings = async (req: Request, res: Response) => {
    const found = await queryOfferings(""); // "" = all

    if (found) return res.status(200).json(found) 
    else return res.status(404).json({ message: 'Transaction not found' });
};

export const getSingleMarketplaceOffering = async (req: Request, res: Response) => {
    const { id } = req.params;

    const found = await queryOfferings(""); // "" = all
    let foundOffering = undefined;
    if(id) {
        // loop through found and find the one with the id
        foundOffering = found.find(offering => offering.id === id);
    }

    if (foundOffering) return res.status(200).json(foundOffering) 
    else return res.status(404).json({ message: `No offering with id ${id} found.` });
};

export const getMarketplaceRealEstateOfferings = async (req: Request, res: Response) => {
    const found = await queryOfferings(`${process.env.ADDR721_REALESTATE}`); // all from our real estate collection

    if (found) return res.status(200).json(found) 
    else return res.status(404).json({ message: 'Real Estate offerings error' });
};

export const getMarketplacePaintingsOfferings = async (req: Request, res: Response) => {    
    const paintings_found = await queryPaintingOfferings();

    if (paintings_found) return res.status(200).json(paintings_found) 
    else return res.status(404).json({ message: 'Painting offerings error' });
};

export const getMarketplaceFeatured = async (req: Request, res: Response) => {
    // const amount = req.params.num_amount;
    const featured = await queryFeatured(3); // gets top 3 images & real estate

    if (featured) return res.status(200).json(featured) 
    else return res.status(404).json({ message: 'Featured endpoint error' });
};

export const getMarketplaceSpecificContractOffering = async (req: Request, res: Response) => {
    const { parent_contract_address } = req.params;
    const found = await queryOfferings(parent_contract_address);
    if (found) return res.status(200).json(found) 
    else return res.status(404).json({ message: 'Specific contract offerings not found' });
};

export default {
    getMarketplaceOfferings,
    getMarketplaceRealEstateOfferings,    
    getMarketplacePaintingsOfferings,
    getMarketplaceSpecificContractOffering,
    getMarketplaceFeatured,
    getSingleMarketplaceOffering
};