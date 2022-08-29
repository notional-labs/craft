// Express
import { Request, Response } from 'express';
import { getAllEndpoints, makePayment, getTotalSupply, getAssets, getTotalUSDValue, getExpValueCalculation, getServersEscrowAccountInfo } from '../services/dao.service';
import { getCraftUSDPrice } from '../services/pricing.service';

export const makePaymentToPlayer = async (req: Request, res: Response) => {
    const {secret, wallet, ucraft_amount, description} = req.body;

    // console.log("makePayment:", secret, wallet, ucraft_amount, description);

    const response = await makePayment(secret, wallet, ucraft_amount, description);
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'No Real Estate NFTs found for this wallet' });
};

export const getAll = async (req: Request, res: Response) => {
    const response = await getAllEndpoints();
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'ERROR: No wallets found...' });
};

export const getServerEscrowWallet = async (req: Request, res: Response) => {
    const response = await getServersEscrowAccountInfo();
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'ERROR: No escrow account found in config...' });
};

export const getSupply = async (req: Request, res: Response) => {
    const response = await getTotalSupply("uexp");
    const response2 = await getTotalSupply("ucraft");
    if (response && response2) return res.status(200).json({"uexp": Number(response), "exp": Number(response)/1_000_000, "ucraft": Number(response2), "craft": Number(response2)/1_000_000});
    else return res.status(404).json({ message: 'ERROR: getTotalSupply() "uexp" & "ucraft" function call returned -1, chain may be down...' });
};

export const getUSDValueOfDAO = async (req: Request, res: Response) => {
    const response = await getTotalUSDValue();
    if (response) return res.status(200).json({"dao_usd_nav": Number(response)});
    else return res.status(404).json({ message: 'ERROR:...' });
};

export const getTotalAssets = async (req: Request, res: Response) => {
    const response = await getAssets();
    if (response) return res.status(200).json(response);
    else return res.status(404).json({ message: 'ERROR:...' });
};

// get EXP price
export const getEXPPrice = async (req: Request, res: Response) => {
    const response = await getExpValueCalculation();
    if (response) return res.status(200).json(
        {
            "exp_price": Number(response),
            "uexp_price": Number(response)/1_000_000
        }
    );
    else return res.status(404).json({ message: 'ERROR:...' });
};

export const getCraftPrice = async (req: Request, res: Response) => {
    const response = await getCraftUSDPrice();
    if (response) return res.status(200).json(
        {
            "craft_price": Number(response),
            "ucraft_price": Number(response)/1_000_000,
        }
    );
    else return res.status(404).json({ message: 'ERROR:...' });
};

export default {
    getAll,
    getSupply,
    getUSDValueOfDAO,
    getTotalAssets,
    getServerEscrowWallet,
    getEXPPrice,
    getCraftPrice,
    makePaymentToPlayer
};