import { redisClient } from './database.service';

// Structure to use for transaction keys
const KEY_STRUCTURE = 'tx_WALLET_UUID';

/**
 * Get a transaction by UUID
 * 
 * @param uuid The UUID
 * @returns Transaction info
 */
export const getTransaction = async (uuid: string) => {
    const key = KEY_STRUCTURE.replace('UUID', uuid).replace('WALLET', '*');
    const foundKey = (await redisClient.scanIterator({
        MATCH: key,
        COUNT: 1
    }));

    // Return first result
    for await (const member of foundKey) {
        return redisClient.get(member);
    }

    // Not found
    return undefined;
};

/**
 * Deletes a Tx by its UUID name
 * 
 * @param uuid The transaction UUID
 * @returns If it was successful or not
 */
export const deleteTx = async (uuid: string) => {
    const key = KEY_STRUCTURE.replace('UUID', uuid).replace('WALLET', '*');
    const foundKey = (await redisClient.scanIterator({
        MATCH: key,
        COUNT: 1
    }));

    // Delete first result
    for await (const member of foundKey) {
        await redisClient.del(member);
        return true;
    }

    // Not found
    return undefined;
};

/**
 * Get all transactions from wallet
 * 
 * @param wallet The wallet address
 * @returns All transactions info
 */
export const getAllTransactions = async (wallet: string) => {
    const key = KEY_STRUCTURE.replace('UUID', '*').replace('WALLET', wallet);
    const foundKeys = await redisClient.scanIterator({
        MATCH: key,
    });

    const results: {} = {};

    // Return first result
    for await (const member of foundKeys) {
        const foundValue = await redisClient.get(member) || ''
        results[member.split('_')[2]] = JSON.parse(foundValue);
    }

    return results;
};

/**
 * Sign a transaction by its uuid
 * 
 * @param uuid TxID
 */
export const signTx = async (uuid: string, tenderminthash: string) => {
    const lookupKey = KEY_STRUCTURE.replace('UUID', uuid).replace('WALLET', '*');
    const key = `signed_${uuid}`

    const foundKeys = await redisClient.scanIterator({
        MATCH: lookupKey,
        COUNT: 1
    });

    // Will execute this code if key is found
    for await (const member of foundKeys) {
        redisClient.set(key, tenderminthash);

        // None
        return {
            message: `Successfully submited signed transaction to redis with id ${uuid} and tx hash ${tenderminthash}`
        };
    }

    // Else bad request
    return undefined;
};