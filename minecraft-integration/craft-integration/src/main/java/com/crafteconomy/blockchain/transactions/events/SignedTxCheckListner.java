package com.crafteconomy.blockchain.transactions.events;

import java.util.Set;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;

public class SignedTxCheckListner implements Listener {

    RedisManager redis = CraftBlockchainPlugin.getInstance().getRedis();

    private static String TX_ENDPOINT = CraftBlockchainPlugin.getTxQueryEndpoint();
    private static Boolean IS_DEV_MODE = CraftBlockchainPlugin.getIfInDevMode();

    @EventHandler
    public void onSignedTxCheck(SignedTransactionEvent event) {
        UUID TxID = event.getTxID();

        Util.logSevere("[DEBUG] SignedTransactionEvent FIRED FOR TxID:" + TxID);

        // Check if Integration has a TxID which matches the TxID fired
        // If it does, we can complete the method and remove the TxID from the pending
        // list&cache
        Tx tx = PendingTransactions.getInstance().getTxFromID(TxID);
        if (tx == null) { return; }

        // Gets the Memos/Descriptions of each transaction (on chain query & our local object)
        String expectedDesc = tx.getDescription();
        long expected_ucraft = tx.getAmount() * 1_000_000; 
        String expectedToWallet = tx.getToWallet();

        boolean doesMatch = doesDataMatchTransaction(event.getTednermintHash(), expectedToWallet, expected_ucraft, expectedDesc);

        System.out.println("[SignedTransactionEvent] Comparing our tx description -> the memo in the body of the transaction");
        if(!doesMatch) {
            if(IS_DEV_MODE) {
                System.out.println("In debugging mode (tendermintHash = DEBUGGING), fake a testing generated faketx");
            } else {
                System.out.println("[DEBUG] TxData did not match for:" + TxID + " - " + event.getTednermintHash());
                System.out.println("[DEBUG] ACTUAL: desc: " + expectedDesc + "  amount (ucraft): " + expected_ucraft + "  toWallet: " + expectedToWallet);
                return;
            }            
        }
        System.out.println("[DEBUG] TxID: " + TxID + " desc matches the tendermint hash memo!");
        

        Util.logFine("SignedTransactionEvent found for " + TxID.toString().substring(0, 15) + "... Completing.\n");
        tx.complete();

        // remove that TxID from the pending list
        PendingTransactions.getInstance().removePending(TxID);
        System.out.println("[DEBUG] TxID: " + TxID + " removed from pending list");

        try (Jedis jedis = redis.getRedisConnection()) {
            // gets 1 key which matches the wallets address due to unique TxID
            Set<String> keyString = jedis.keys("tx_*_" + TxID);

            for (String key : keyString) {
                jedis.del(key);
                System.out.println("[DEBUG-REDIS] DELETED " + key);
            }

            jedis.del("signed_" + TxID);
            System.out.println("[DEBUG-REDIS] DELETED signed_" + TxID);

        } catch (Exception e) {
            System.out.println("SignedTxChecklistener Redis Error");
            throw new JedisException(e);
        }
    }

    private static boolean doesDataMatchTransaction(String tendermintHash, String expectedToAddress, long expectedAmount, String expectedMemo) {
        boolean transactionDataMatches = false;        
        try {
            // TODO: Change this to get value from config
            // curl -X GET "https://api.cosmos.network/cosmos/tx/v1beta1/txs/3EFA66F9613EF5E215942257C08904392195FCA1C8A9367704AEF97FCAD6FEAA" -H "accept: application/json"
            // URL url = new URL("https://api.cosmos.network/cosmos/tx/v1beta1/txs/" + tendermintHash);
            URL url = new URL(TX_ENDPOINT.replace("{TENDERMINT_HASH}", tendermintHash));
            
            if(tendermintHash.equalsIgnoreCase("debugging")) {
                System.out.println("TendermintHash = 'debugging', so we will sign the tx given this.");
                return true;
            }            

            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("accept", "application/json");

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2 ? httpConn.getInputStream() : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";

            // Get the memo string from the transcaction
            JSONObject myObject = new JSONObject(response).getJSONObject("tx");
            myObject = myObject.getJSONObject("body");
            String txMemo = myObject.getString("memo");

            // Loops through the Tx's messages trying to find one which matches to_address & amount 
            // [!] (amount is in ucraft)
            for(Object msg :  myObject.getJSONArray("messages")) {
                JSONObject msgObject = (JSONObject) msg;
                String msgToAddress = msgObject.getString("to_address");
                boolean doesAmountMatchExpected = false;
                // System.out.println(msgObject.toString());
                
                // get amounts array & check until the expected amount = the amount they sent            
                for(Object amounts : msgObject.getJSONArray("amount")) {
                    JSONObject tempAmount = (JSONObject) amounts;
                    long msgAmount = tempAmount.getLong("amount");                    
                    if(msgAmount == expectedAmount) {
                        // Util.log("TXHASH - Found a matching amount of " + msgAmount);
                        doesAmountMatchExpected = true;
                        break;
                    }
                }

                // if (amount=Expected) & (to_address=expected) & (memo=expected), then this was the actual transaction& is valid
                if(doesAmountMatchExpected && msgToAddress.equalsIgnoreCase(expectedToAddress) && txMemo.equalsIgnoreCase(expectedMemo)) {
                    // Util.log("Data DOES match transaction!!!");
                    transactionDataMatches = true;
                } else {
                    // Util.logSevere("Data does NOT match this transaction's expected outcome!");
                }
            }
            s.close();
        } catch (IOException e) {
            // Maybe the tx hash was not there? recall this with a runnable in X seconds?
            e.printStackTrace();
        }
        return transactionDataMatches;
    }
}
