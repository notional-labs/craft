package com.crafteconomy.blockchain.testing;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;


public class Testing_Cosmos_Query {
    
    // craftd tx bank send mykey craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl 100ucraft --note "My Test Description_Reece"

    // used in SignedTxCheckListener.java

    public static void main(String[] args) {
        String tendermintHash = "0054F78B96E3E690EAC85E13088BC05EE19DCEC9330D66A6FF04D98943E09F01";
        String to_address = "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl";
        long amount = 100;

        boolean matches = doesDataMatchTransaction(tendermintHash, to_address, amount, "My Test Description_Reece");
        System.out.println("Does the data match the transaction? " + matches);
    }
    
    protected static boolean IS_DEV_MODE = false;

    protected static String TX_ENDPOINT = "http://65.108.125.182:1317/cosmos/tx/v1beta1/txs/{TENDERMINT_HASH}";
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
