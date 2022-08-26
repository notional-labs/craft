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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class EndpointQuery {

    private static CraftBlockchainPlugin blockchainPlugin = CraftBlockchainPlugin.getInstance();

    private static JSONParser parser = new JSONParser();
    private static JSONObject json;

    // http://65.108.125.182:1317/cosmos/bank/v1beta1
    private static final String API_ENDPOINT = blockchainPlugin.getApiEndpoint();
    // Found via https://v1.cosmos.network/rpc/v0.41.4
    private static final String BALANCES_ENDPOINT = API_ENDPOINT + "cosmos/bank/v1beta1/balances/%address%/by_denom?denom=%denomination%";
    private static final String SUPPLY_ENDPOINT = API_ENDPOINT + "cosmos/bank/v1beta1/supply/by_denom?denom=%denomination%";
    // private static final String ACCOUNT_ENDPOINT = API_ENDPOINT + "cosmos/auth/v1beta1/accounts/%address%";

    // TODO: Convert to CompletableFuture
    // public static Object req(String URL, RequestTypes type, String body, String logMSG) {
    //     Object value = 0L; 
        
    //     try {     
    //         CraftBlockchainPlugin.log(logMSG);

    //         Request request;

    //         if(body != null) {
    //             request = Request.Post(URL);                
    //             request.bodyString(body, ContentType.APPLICATION_JSON);
    //         } else {
    //             request = Request.Get(URL);
    //         }
            
    //         request.setHeader("Accept", "application/json");
    //         request.setHeader("Content-Type", "application/json");

    //         HttpResponse httpResponse = request.execute().returnResponse();
    //         if(httpResponse.getStatusLine().getStatusCode() != 200){
    //             CraftBlockchainPlugin.log(httpResponse.getStatusLine().toString());   
    //         }

    //         if (httpResponse.getEntity() != null) {                
    //             String html = EntityUtils.toString(httpResponse.getEntity());            

    //             json = (JSONObject) parser.parse(html);
                
    //             if(type == RequestTypes.ACCOUNT) {
    //                 // http://65.108.125.182:1317/cosmos/auth/v1beta1/accounts/craft1s4yczg3zgr4qdxussx3wpgezangh2388xgkkz9
    //                 CraftBlockchainPlugin.log("JSON " + json);
    //                 json = (JSONObject) json.get("account");                    
    //                 return json.get("sequence").toString();
    //             }

    //             // add CRAFT_PRICE query here

    //             // gets the key based on the type's name, 
    //             // so TotalSupply = amount. balance = balance
    //             json = (JSONObject) json.get(type.json_key);

    //             if(json != null) {
    //                 value = json.get("amount").toString();
    //             } else {
    //                 value = ErrorTypes.NO_TOKENS_FOR_WALLET.code;
    //             }      
    //         }

    //     } catch (Exception e) {
    //         String error_msg = "Error in BlochchainReq.makeRequest()";

    //         if(e.getMessage().contains("Connection refused")) {
    //             error_msg = "Error BlockchainAPI Connection refused, is the server running?";
    //             value = ErrorTypes.NODE_DOWN.code;
    //         } else {                
    //             e.printStackTrace();
    //             value = ErrorTypes.NETWORK_ERROR.code;
    //         }

    //         CraftBlockchainPlugin.log(error_msg);
    //     } 
    //     return value;
    // }
    
    // public static Object req(String URL, RequestTypes type, String logMSG){
    //     return req(URL, type, null, logMSG);
    // }



    public static CompletableFuture<Float> queryCraftTokenPrice() {        
        CompletableFuture<Float> future = new CompletableFuture<>();

        makeGetRequests("http://api.crafteconomy.io/v1/dao/craft_price").thenAcceptAsync(jsonString -> {
            if(jsonString.isEmpty()) {
                future.complete(-1.0f);
            } else {
                try {
                    json = (JSONObject) parser.parse(jsonString.get());
                    Float price = Float.parseFloat(json.get("craft_price").toString());
                    future.complete(price);
                } catch (Exception e) {
                    future.complete(-1.0f);
                }
            }
        });
        return future;
    }


    public static CompletableFuture<Float> getCraftBalance(String craft_addr) {
        // get the value from getUCraftBalance, and divide it by 1000000
        CompletableFuture<Float> future = new CompletableFuture<>();
        
        getUCraftBalance(craft_addr).thenAccept(balance -> {
            // future.complete(balance / 1_000_000);
            if(balance < 0) {
                future.complete(-1.0f);
            } else {
                future.complete(Float.valueOf(balance / 1_000_000));
            }
        });
        return future;
    }

    // long amount = Long.parseLong(EndpointQuery.req(req_url, RequestTypes.BALANCE, "Balance Request").toString());
    public static CompletableFuture<Long> getUCraftBalance(String craft_addr) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        //  { "balance": { "denom": "ucraft", "amount": "999919751059492" } }

        // String url_link = BALANCES_ENDPOINT.replace("%address%", craft_addr).replace("%denomination%", "ucraft");
        // makeGetRequests(url_link).thenAcceptAsync(response -> {
        //     try {
        //         if(response.isEmpty()) {
        //             future.complete(-1L);
        //         } else {
        //             json = (JSONObject) parser.parse(response.get());
        //             JSONObject balance = (JSONObject) json.get("balance");
        //             long amount = Long.parseLong(balance.get("amount").toString());
        //             future.complete(amount);                
        //         }
        //     } catch (Exception e) {
        //         future.complete(-1L);
        //     }
        // });    
        getBalance(craft_addr, "ucraft").thenAcceptAsync(balance -> {
            // if(balance < 0) {
            //     future.complete(-1L);
            // } else {
            //     future.complete(balance);
            // }
            System.out.println("9289 balance - " + balance);
            future.complete(balance);
        });

        return future;
    }

    public static CompletableFuture<Long> getBalance(String craft_addr, String denomination) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        //  { "balance": { "denom": "ucraft", "amount": "999919751059492" } }

        String url_link = BALANCES_ENDPOINT.replace("%address%", craft_addr).replace("%denomination%", denomination);
        makeGetRequests(url_link).thenAcceptAsync(response -> {
            try {
                if(response.isEmpty()) {
                    future.complete(-1L);
                } else {
                    json = (JSONObject) parser.parse(response.get());
                    JSONObject balance = (JSONObject) json.get("balance");
                    long amount = Long.parseLong(balance.get("amount").toString());
                    future.complete(amount);                
                }
            } catch (Exception e) {
                future.complete(-1L);
            }
        });        
        return future;
    }


    public static CompletableFuture<Long> getSupply(String denomination) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        //  { "balance": { "denom": "ucraft", "amount": "999919751059492" } }        
        
        // long supply = Long.parseLong(EndpointQuery.req(URL, RequestTypes.SUPPLY, "Total Supply Request").toString());
        String url_link = SUPPLY_ENDPOINT.replace("%denomination%", denomination);
        makeGetRequests(url_link).thenAcceptAsync(response -> {
            try {
                if(response.isEmpty()) {
                    future.complete(-1L);
                } else {
                    json = (JSONObject) parser.parse(response.get());
                    System.out.println(json);
                    JSONObject amt = (JSONObject) json.get("amount");
                    long amount = Long.parseLong(amt.get("amount").toString());
                    // System.out.println(amount);
                    future.complete(amount);                
                }
            } catch (Exception e) {
                future.complete(-1L);
            }
        });        
        return future;
    }

    private static CompletableFuture<Optional<String>> makeGetRequests(String url_link) {
        CompletableFuture<Optional<String>> future = new CompletableFuture<>();

        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;        
        try {
            url = new URL(url_link);
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
            // System.out.println(jsonString);
            future.complete(Optional.of(jsonString));
        } catch (Exception e) {
            // future.complete(balance);
            future.complete(Optional.empty());
        }
        return future;        
    }

}
