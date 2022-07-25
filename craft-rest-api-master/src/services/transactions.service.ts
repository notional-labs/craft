import { redisClient } from './database.service';

import {SigningStargateClient} from '@cosmjs/stargate'
import {decodeTxRaw} from '@cosmjs/proto-signing'

import { config } from 'dotenv';
config()

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
        // 5 day expire time incase the signed doesn't go through
        redisClient.setEx(key, 60*60*24*5, tenderminthash);

        // None
        return {
            message: `Successfully submited signed transaction to redis with id ${uuid} and tx hash ${tenderminthash}`
        };
    }

    // Else bad request
    return undefined;
};



const RPC = `${process.env.CRAFTD_NODE}`;
// http://localhost:4000/v1/tx/confirm/craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl/100/My%20Test%20Description_Reece/0054F78B96E3E690EAC85E13088BC05EE19DCEC9330D66A6FF04D98943E09F01
/*
export const confirmTransactionDataMatches = async (to_address: string, ucraft_amt: string, description: string, tendermint_hash: string) => {
    console.log(`Confirming transaction data matches with ${RPC}`);
    console.log(`To: ${to_address}, Amount: ${ucraft_amt}, Description: ${description}, Tendermint Hash: ${tendermint_hash}`);
    
    
    // query the RPC for a given transaction hash
    const client = await SigningStargateClient.connect(`${RPC}`).catch(err => {
        console.log(err);
        return undefined;
    });
    if(!client) { return undefined; }

    // use client to query a transaction on chain
    let tx = await client.getTx(tendermint_hash).catch(err => {
        console.log(err);
    });
    if(!tx) { return undefined; }

    // create an Object which converts the CosmJS in a better readable format
    let modifiedTx = {doesDataMatch: false, height: 0, code: 0, rawLog: "", memo: "", txs: {}};
    modifiedTx.height = tx.height;
    modifiedTx.code = tx.code;
    modifiedTx.rawLog = tx.rawLog;
    modifiedTx.memo = decodeTxRaw(tx?.tx).body.memo

    // convert modifiedTx.rawLog to JSON
    const JSONTxs = JSON.parse(modifiedTx.rawLog);
    modifiedTx.txs = JSONTxs;

    let doesMemoMatch = modifiedTx.memo === description;
    modifiedTx.doesDataMatch = doesMemoMatch;
    if(modifiedTx.doesDataMatch === false) {
        console.log("The transaction memo does not match, no reason to do any further computation");        
        return modifiedTx;
    } else {
        console.log("The transaction memo matches, checking the amount's in messages to find one which does match");
    }

    let doesAmountMatch = false;
    let doesToAddressMatch = false;
    for(const txEvent of JSONTxs) { // is an array of objects, if it doesn't match, check for next index in array
        doesAmountMatch = false;
        doesToAddressMatch = false;

        for(const events of txEvent.events) {
            if(events.type === "coin_received") {
                console.log(`Found coin_received event, checking if it matches`);                
                for(const attrs of events.attributes) {
                    // console.log("attrs", attrs)
                    if(attrs.key === "amount") {
                        doesAmountMatch = attrs.value === ucraft_amt;
                        console.log(`- Amount ${attrs.value} matches ${ucraft_amt}: ${doesAmountMatch}`);

                    } 
                    if(attrs.key === "receiver") {
                        doesToAddressMatch = attrs.value === to_address;
                        console.log(`- To Address ${attrs.value} matches ${to_address}: ${doesToAddressMatch}`);
                    }

                    // if amount matches & the address it was sent to match, we can stop checking & set it = true.
                    // This way in game we know this transaction matches exactly & we can push through the code in game to run
                    modifiedTx.doesDataMatch = (doesAmountMatch && doesToAddressMatch);
                    if(modifiedTx.doesDataMatch === true) {
                        console.log("The transaction data matches! Returning True");
                        return modifiedTx;
                    }
                }
            }
        }
    }

    return modifiedTx;
};
*/