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
            System.out.println("SignedTxChecklistener Redis Error");
            throw new JedisException(e);
        }
    }

    // TODO: CompleteableFuture
    // Testing_Cosmos_Query.java
    private static boolean doesDataMatchTransaction(String tendermintHash, String expectedToAddress, long expectedUCraftAmount, String expectedMemo) {
        boolean transactionDataMatches = false;
        ;        
        try {
            String fmt_url = "http://api.crafteconomy.io/v1/tx/confirm/{TO_ADDR}/{EXPECTED_UCRAFT_AMT}ucraft/{EXPECTED_MEMO}/{TM_HASH}"
                .replace("{TO_ADDR}", expectedToAddress)
                .replace("{EXPECTED_UCRAFT_AMT}", String.valueOf(expectedUCraftAmount))
                .replace("{EXPECTED_MEMO}", expectedMemo.replace(" ", "%20"))
                .replace("{TM_HASH}", tendermintHash);

            // prefix with 0x bc that's how it likes it
            URL url = new URL(fmt_url);
            // URL url = new URL(TX_ENDPOINT.replace("{TENDERMINT_HASH}", tendermintHash);
            System.out.println("URL: " + url.toString());
                        
            if(IS_DEV_MODE) {
                System.out.println("Dev mode is enabled, so we will sign the tx given this & broadacast to ensure devs know.");
                return true;
            }            

            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("accept", "application/json");

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2 ? httpConn.getInputStream() : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            s.close(); 
            // Get the memo string from the transcaction
            transactionDataMatches = new JSONObject(response).getBoolean("doesDataMatch");
                       
            System.out.println("Does data match: " + transactionDataMatches);
            return transactionDataMatches;
        } catch (IOException e) {
            // Maybe the tx hash was not there? recall this with a runnable in X seconds?
            e.printStackTrace();
        }
        return transactionDataMatches;
    }
}
