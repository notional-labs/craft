package com.crafteconomy.blockchain.testing;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.json.simple.JSONObject;

import com.crafteconomy.blockchain.core.types.FaucetTypes;
import com.crafteconomy.blockchain.utils.JavaUtils;

public class TestingPostReq {
    
    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> makePostRequest("craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0", "test-desc", 999999999999L));
    }

    // from BlockchainRequests.java
    private static final String ENDPOINT_SECRET = "7821719493";

    private static FaucetTypes makePostRequest(String craft_address, String description, long ucraft_amount) {
        if(craft_address == null) { 
            return FaucetTypes.NO_WALLET; 
        }

        URL url = null;
        HttpURLConnection http = null;
        OutputStream stream = null;
        String data = "{\"secret\": \""+ENDPOINT_SECRET+"\", \"description\": \""+description+"\", \"wallet\": \""+craft_address+"\", \"ucraft_amount\": "+ucraft_amount+"}";
        System.out.println("depositToAddress data " + data); // TODO: Remove this from production code
        
        try {
            url = new URL("http://api.crafteconomy.io/v1/dao/make_payment");
            http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");

            byte[] out = data.getBytes(StandardCharsets.UTF_8);
            stream = http.getOutputStream();                    
            stream.write(out);

            // get the return value of the POST request
            // {"success":{"wallet":"craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0","ucraft_amount":1,"craft_amount":0.000001,"serverBalLeft":"9955.493893craft"}}
            String response = JavaUtils.streamToString(http.getInputStream());
            if(response.length() == 0) {
                System.err.println("No response from server API (length 0 string)");
                return FaucetTypes.NO_RESPONSE;
            }

            JSONObject json = new JSONObject();
            // parse response
            json = (JSONObject) org.json.simple.JSONValue.parse(response);


            System.out.println("depositToAddress code: " + http.getResponseCode() + " | response: " + json);
            http.disconnect();

            if(http.getResponseCode() != 200) {
                System.out.println("Failed payment!");
                return FaucetTypes.FAILURE;
            }                    

            if(json.keySet().contains("success")) {
                System.out.println("Successful payment!");
                return FaucetTypes.SUCCESS;
                
            } else if (json.keySet().contains("error")) {
                json = (JSONObject) json.get("error");
                String errorCode = (String) json.get("code");
                // https://github.com/cosmos/cosmos-sdk/blob/main/types/errors/errors.go

                String output = "";
                FaucetTypes returnType = FaucetTypes.FAILURE;
                switch (errorCode) {
                    case "5" -> {
                        output = "Server wallet does not have enough funds!";
                        returnType = FaucetTypes.NOT_ENOUGH_FUNDS_IN_SERVER_WALLET;
                    }
                    case "3" -> {
                        output = "Invalid sequence!";
                    }
                    case "19" -> {
                        output = "Transaction is already in the mem pool (duplicate transaction)!";
                    }
                    default -> {
                        output = "No success in response from server API: " + json;
                    }
                }
                System.err.println(output);
                return returnType;           
            }
                
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage().startsWith("Server returned HTTP response code: 502 for URL:")) {
                System.err.println("makePayment API is down!");
                return FaucetTypes.API_DOWN;
            } else {
                System.out.println("Some other failure!");
                return FaucetTypes.FAILURE;
            }
        }
        
        return FaucetTypes.FAILURE;
    }
}
