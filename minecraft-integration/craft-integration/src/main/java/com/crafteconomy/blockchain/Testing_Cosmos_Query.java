package com.crafteconomy.blockchain;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;


public class Testing_Cosmos_Query {
    
    private static String tendermintHash = "3EFA66F9613EF5E215942257C08904392195FCA1C8A9367704AEF97FCAD6FEAA";
    private static String to_address = "cosmos10r39fueph9fq7a6lgswu4zdsg8t3gxlqvvvyvno";
    private static long amount = 10000; // this is in uatom

    public static void main(String[] args) {
        doesDataMatchTransaction(tendermintHash, to_address, amount, "My Test Memo - Reece");
    }

    private static boolean doesDataMatchTransaction(String tendermintHash, String expectedToAddress, long expectedAmount, String expectedMemo) {
        boolean transactionDataMatches = false;        
        try {
            // TODO: Change this to get value from config
            // curl -X GET "https://api.cosmos.network/cosmos/tx/v1beta1/txs/3EFA66F9613EF5E215942257C08904392195FCA1C8A9367704AEF97FCAD6FEAA" -H "accept: application/json"
            URL url = new URL("https://api.cosmos.network/cosmos/tx/v1beta1/txs/" + tendermintHash);
            // URL url = new URL(TX_ENDPOINT.replace("{TENDERMINT_HASH}", tendermintHash);
            
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
