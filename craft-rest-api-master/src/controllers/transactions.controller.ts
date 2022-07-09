import { Request, Response } from 'express';
import { getTransaction, getAllTransactions, signTx, deleteTx } from '../services/transactions.service';

export const getTransactionData = async (req: Request, res: Response) => {
    const { uuid } = req.params;

    const found = await getTransaction(uuid);

    if (found) return res.status(200).json(JSON.parse(found)) 
    else return res.status(404).json({ message: 'Transaction not found' });
};

export const getAllTransactionsData = async (req: Request, res: Response) => {
    const { wallet } = req.params;

    const found = await getAllTransactions(wallet);

    if (found) return res.status(200).json(found);
    else return res.status(404).json({ message: 'Wallet not found' });
};

export const signTransaction = async (req: Request, res: Response) => {
    const { uuid, tenderminthash } = req.params;

    // Handle signing
    const doc = await signTx(uuid, tenderminthash)
    if (doc) return res.status(200).json(doc);
    else return res.status(400).json({ message: 'Transaction not found' });
};

export const removeTransaction = async (req: Request, res: Response) => {
    const { uuid } = req.params;

    // remove the uuid from the redis instance
    const wasDeleted = await deleteTx(uuid);    
   
    if (wasDeleted) {        
        return res.status(200).json({ message: 'Transaction removed' });
    } else {
        return res.status(404).json({ message: 'Transaction not found' });
    }
};


export default {
    getTransactionData,
    getAllTransactionsData,
    signTransaction,
    removeTransaction
};