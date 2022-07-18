package com.crafteconomy.blockchain.transactions.listeners;

import java.util.Set;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.transactions.events.SignedTransactionEvent;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;

public class SignedTxCheckListener implements Listener {

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
        long expected_ucraft = tx.getUCraftAmount(); 
        String expectedToWallet = tx.getToWallet();
        boolean doesMatch = doesDataMatchTransaction(event.getTednermintHash(), expectedToWallet, expected_ucraft, expectedDesc);

        System.out.println("[SignedTransactionEvent] Comparing our tx description -> the memo in the body of the transaction");        
        if(doesMatch == false) {
            Util.logWarn("[DEBUG] TxData did not match for:" + TxID + " - " + event.getTednermintHash());
            Util.logWarn("[DEBUG] ACTUAL: desc: " + expectedDesc + "  amount (ucraft): " + expected_ucraft + "  toWallet: " + expectedToWallet);
            return;
        }                
        Util.logFine("SignedTransactionEvent [DATA MATCH] found for " + TxID.toString().substring(0, 15) + "... Completing\n");
        tx.complete();

        // remove that TxID from the pending list
        PendingTransactions.getInstance().removePending(TxID);
        // System.out.println("[DEBUG] TxID: " + TxID + " removed from pending list");

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
            System.out.println("SignedTxCheckListener Redis Error");
            throw new JedisException(e);
        }
    }


    // protected static String CRAFT_URL = "http://65.108.125.182:1317/cosmos/tx/v1beta1/txs/{TENDERMINT_HASH}";
    private static boolean doesDataMatchTransaction(String tendermintHash, String expectedToAddress, long expectedAmount, String expectedMemo) {
        boolean transactionDataMatches = false;        
        boolean doesTxMemoMatch = false;

        if(IS_DEV_MODE) {
            System.out.println("Dev mode is enabled, so we will sign the tx given this & broadcast to ensure developers know.");
            return true;
        }
        
        JSONObject txObject = getTransactionObject(tendermintHash); // tx key of the above link
        if(txObject == null) {
            // System.out.println("Error: myObject is null");
            return false;
        }

        txObject = txObject.getJSONObject("body");
        String memo = txObject.getString("memo");
        doesTxMemoMatch = memo.equalsIgnoreCase(expectedMemo);

        // Loops through the Tx's messages trying to find one which matches to_address & amount                 
        for(Object msg : txObject.getJSONArray("messages")) {
            JSONObject msgObject = (JSONObject) msg;
            // System.out.println(msgObject.toString());

            // Check that the to_address matches who we expected to send it too, if not we check the next.
            String to_address = msgObject.getString("to_address");
            boolean doesToAddressMatch = to_address.equalsIgnoreCase(expectedToAddress);
            if(doesToAddressMatch == false) {
                continue; // if who we were sending it too doesn't match, this is not the transaction.
            }
            System.out.println("to_address matches expected address" + expectedToAddress );

           
            // Check there is a message which has the correct amount, this only runs after we checked for to_address
            // So if this finds a match, it means that amount was sent to the user.
            // If memo is correct, then the Tx will run!
            for(Object amounts : msgObject.getJSONArray("amount")) { // [!] (amount is in ucraft)
                JSONObject tempAmount = (JSONObject) amounts;
                Long msgAmount = tempAmount.getLong("amount");                    
                if(msgAmount == expectedAmount) {
                    System.out.println("TXHASH - Found a matching amount of " + msgAmount +"ucraft. This makes it a valid Tx if memo is correct: " + doesTxMemoMatch);
                    transactionDataMatches = true;
                    break;
                }
            }
        }
        return transactionDataMatches && doesTxMemoMatch;
    }

    private static JSONObject getTransactionObject(String tendermintHash) {
        JSONObject myObject = null;
        try {
            URL url = new URL(TX_ENDPOINT.replace("{TENDERMINT_HASH}", tendermintHash));            
                            
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("accept", "application/json");

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2 ? httpConn.getInputStream() : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";

            // Get the memo string from the transaction
            myObject = new JSONObject(response).getJSONObject("tx");
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myObject;
    }
}
