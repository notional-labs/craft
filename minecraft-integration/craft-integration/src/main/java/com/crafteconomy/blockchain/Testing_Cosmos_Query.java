package com.crafteconomy.blockchain;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;


public class Testing_Cosmos_Query {
    
    // craftd tx bank send mykey craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl 100ucraft --note "My Test Description_Reece"
    private static String tendermintHash = "0054F78B96E3E690EAC85E13088BC05EE19DCEC9330D66A6FF04D98943E09F01";
    private static String to_address = "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl";
    private static long amount = 100; // this is in ucraft

    public static void main(String[] args) {
        doesDataMatchTransaction(tendermintHash, to_address, amount, "My Test Description_Reece");
    }

    // https://api.crafteconomy.io/v1/tx/confirm/craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl/100/My%20Test%20Description_Reece/0054F78B96E3E690EAC85E13088BC05EE19DCEC9330D66A6FF04D98943E09F01
    protected static boolean IS_DEV_MODE = false; // TODO: Remove
    private static boolean doesDataMatchTransaction(String tendermintHash, String expectedToAddress, long expectedUCraftAmount, String expectedMemo) {
        boolean transactionDataMatches = false;    
        try {
            String fmt_url = "http://localhost:4000/v1/tx/confirm/{TO_ADDR}/{EXPECTED_UCRAFT_AMT}ucraft/{EXPECTED_MEMO}/{TM_HASH}"
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
            JSONObject jo = new JSONObject(response);

            // check if jo contains key doesDataMatch
            if(!jo.has("doesDataMatch")) {
                System.out.println("Error: doesDataMatch key not found in response.");
                return false;
            }

            transactionDataMatches = jo.getBoolean("doesDataMatch");
                       
            System.out.println("Does data match: " + transactionDataMatches);
            return transactionDataMatches;
        } catch (IOException e) {
            // Maybe the tx hash was not there? recall this with a runnable in X seconds?
            e.printStackTrace();
        }
        return transactionDataMatches;
    }
}
