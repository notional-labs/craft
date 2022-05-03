package com.crafteconomy.blockchain.core.request;

import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.utils.Util;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class EndpointQuery {

    private static JSONParser parser = new JSONParser();
    private static JSONObject json;

    public static Object req(String URL, RequestTypes type, String body, String logMSG) {
        Object value = 0L; 
        
        try {     
            Util.log(logMSG);

            Request request;

            if(body != null) {
                request = Request.Post(URL);                
                request.bodyString(body, ContentType.APPLICATION_JSON);
            } else {
                request = Request.Get(URL);
            }
            
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");
            // add Proxy-Authorization ?

            HttpResponse httpResponse = request.execute().returnResponse();
            if(httpResponse.getStatusLine().getStatusCode() != 200){
                System.out.println(httpResponse.getStatusLine());   
            }

            if (httpResponse.getEntity() != null) {                
                String html = EntityUtils.toString(httpResponse.getEntity());

                
                if (type == RequestTypes.FAUCET) {
                    Util.log("Faucet Request " + html);
                    return html; // {"transfers":[{"coin":"1token","status":"ok"}]}
                }

                json = (JSONObject) parser.parse(html);            
                
                if(type == RequestTypes.ACCOUNT) {
                    // http://65.108.125.182:1317/cosmos/auth/v1beta1/accounts/craft1s4yczg3zgr4qdxussx3wpgezangh2388xgkkz9
                    System.out.println("JSON " + json);
                    json = (JSONObject) json.get("account");                    
                    return json.get("sequence").toString();
                }

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

            Util.logSevere(error_msg);
        } 
        return value;
    }
    
    public static Object req(String URL, RequestTypes type, String logMSG){
        return req(URL, type, null, logMSG);
    }
}
