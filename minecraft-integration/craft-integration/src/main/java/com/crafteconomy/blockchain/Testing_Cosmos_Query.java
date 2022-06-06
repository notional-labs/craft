package com.crafteconomy.blockchain;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONObject;


public class Testing_Cosmos_Query {
    
    private static String tendermintHash = "3EFA66F9613EF5E215942257C08904392195FCA1C8A9367704AEF97FCAD6FEAA";

    public static void main(String[] args) {
        URL url = null;
        String myMemo = null;
        try {
            // curl -X GET "https://api.cosmos.network/cosmos/tx/v1beta1/txs/3EFA66F9613EF5E215942257C08904392195FCA1C8A9367704AEF97FCAD6FEAA" -H "accept: application/json"
            url = new URL("https://api.cosmos.network/cosmos/tx/v1beta1/txs/" + tendermintHash);

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
    }

    

}
