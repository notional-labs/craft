package com.crafteconomy.blockchain.core.request;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class EndpointQuery {

    private static JSONParser parser = new JSONParser();
    private static JSONObject json;
    
    // Only run these from completeable futures async
    public static Object req(String URL, RequestTypes type, String body, String logMSG) {
        Object value = 0L; 
        
        try {     
            CraftBlockchainPlugin.log(logMSG);

            Request request;

            if(body != null) {
                request = Request.Post(URL);                
                request.bodyString(body, ContentType.APPLICATION_JSON);
            } else {
                request = Request.Get(URL);
            }
            
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");

            HttpResponse httpResponse = request.execute().returnResponse();
            if(httpResponse.getStatusLine().getStatusCode() != 200){
                CraftBlockchainPlugin.log(httpResponse.getStatusLine().toString());   
            }

            if (httpResponse.getEntity() != null) {                
                String html = EntityUtils.toString(httpResponse.getEntity());            

                json = (JSONObject) parser.parse(html);
                
                if(type == RequestTypes.ACCOUNT) {
                    // http://65.108.125.182:1317/cosmos/auth/v1beta1/accounts/craft1s4yczg3zgr4qdxussx3wpgezangh2388xgkkz9
                    CraftBlockchainPlugin.log("JSON " + json);
                    json = (JSONObject) json.get("account");                    
                    return json.get("sequence").toString();
                }

                // add CRAFT_PRICE query here

                // gets the key based on the type's name, 
                // so TotalSupply = amount. balance = balance
                json = (JSONObject) json.get(type.json_key);

                if(json != null) {
                    value = json.get("amount").toString();
                } else {
                    value = ErrorTypes.NO_TOKENS_FOR_WALLET.code;
                }      
            }

        } catch (Exception e) {
            String error_msg = "Error in BlochchainReq.makeRequest()";

            if(e.getMessage().contains("Connection refused")) {
                error_msg = "Error BlockchainAPI Connection refused, is the server running?";
                value = ErrorTypes.NODE_DOWN.code;
            } else {                
                e.printStackTrace();
                value = ErrorTypes.NETWORK_ERROR.code;
            }

            CraftBlockchainPlugin.log(error_msg);
        } 
        return value;
    }    
    public static Object req(String URL, RequestTypes type, String logMSG){
        return req(URL, type, null, logMSG);
    }



    public static CompletableFuture<Float> queryCraftTokenPrice() {        
        CompletableFuture<Float> future = new CompletableFuture<>();

        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        Float price = -1.0f;
        try {
            // { "craft_price": 1.79 }
            url = new URL("http://api.crafteconomy.io/v1/dao/craft_price");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            is = conn.getInputStream();

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024]; // is a small requests
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            String jsonString = result.toString(StandardCharsets.UTF_8);
            // get the float price from the object which the key is craft_price
            json = (JSONObject) parser.parse(jsonString);
            price = Float.parseFloat(json.get("craft_price").toString());

            future.complete(price);
        } catch (Exception e) {
            future.complete(-1.0f);
        }    
        return future;    
    }

}
