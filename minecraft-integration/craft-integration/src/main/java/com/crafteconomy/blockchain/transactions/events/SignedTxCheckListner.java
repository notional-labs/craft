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
        String desc = tx.getDescription();
        String tendermintHash = getMemoFromHash(event.getTednermintHash());

        // Checks that the desc of our Tx object matches that of the signed Memo on chain.
        boolean doesMatch = doTxMemoAndDescriptionMatch(desc, tendermintHash);
        boolean isDebugging = tendermintHash.equalsIgnoreCase("DEBUGGING");

        System.out.println("[SignedTransactionEvent] Comparing our tx description -> the memo in the body of the transaction");
        if(!doesMatch) {
            if(isDebugging) {
                System.out.println("In debugging mode (tendermintHash = DEBUGGING), fake a testing generated faketx");
            } else {
                System.out.println("[DEBUG] Memo & Actual Description do not match for:" + TxID + "\n- desc: " + desc + "\n- memo:" + tendermintHash);
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

    // TODO: Finish this to ping the hash endpoint for craft
    private boolean doTxMemoAndDescriptionMatch(String tendermintDesc, String desc) {
        // https://www.mintscan.io/cosmos/txs/3EFA66F9613EF5E215942257C08904392195FCA1C8A9367704AEF97FCAD6FEAA
        // My test tx hash ^

        if(tendermintDesc == null) {
            System.out.println("[DEBUG] TxID: " + tendermintDesc + " tendermintDesc is null (not found)");
            return false;
        }

        if (tendermintDesc.equalsIgnoreCase("DEBUGGING")) {
            return true; // wallet fakesign command
        }

        // if the memo matches the tx description, the tx is valid & secure
        return tendermintDesc.equalsIgnoreCase(desc);
    }

    private String getMemoFromHash(String tendermintHash) {
        URL url = null;
        String myMemo = null;
        try {
            // TODO: Change this to get value from config
            // curl -X GET "https://api.cosmos.network/cosmos/tx/v1beta1/txs/3EFA66F9613EF5E215942257C08904392195FCA1C8A9367704AEF97FCAD6FEAA" -H "accept: application/json"
            // url = new URL("https://api.cosmos.network/cosmos/tx/v1beta1/txs/" + tendermintHash);
            String myUrl = TX_ENDPOINT.replace("{TENDERMINT_HASH}", tendermintHash);
            
            if(tendermintHash.equalsIgnoreCase("debugging")) {
                System.out.println("TendermintHash = DEBUGGING, so we will sign the tx given this.");
                return tendermintHash;
            }

            System.out.println("MYURL: " + myUrl);
            url = new URL(myUrl);

            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");

            httpConn.setRequestProperty("accept", "application/json");

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                    ? httpConn.getInputStream()
                    : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";

            JSONObject myObject = new JSONObject(response).getJSONObject("tx");
            myObject = myObject.getJSONObject("body");

            myMemo = myObject.getString("memo");
            System.out.println(myMemo);        
            s.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return myMemo;
    }

}
