package com.crafteconomy.blockchain.testing;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;

public class TestQueryCraft {

    private static final String API_ENDPOINT = "https://craft-rest.crafteconomy.io/";
    private static final String BALANCES_ENDPOINT = API_ENDPOINT + "cosmos/bank/v1beta1/balances/%address%/by_denom?denom=%denomination%";
    private static final String SUPPLY_ENDPOINT = API_ENDPOINT + "cosmos/bank/v1beta1/supply/by_denom?denom=%denomination%";    

    public static void main(String[] args) {
        String craft_address = "craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0";

        queryCraftTokenPrice().thenAccept(price -> {
            CraftBlockchainPlugin.log("Craft USD Price: " + price);
        });

        getUCraftBalance(craft_address).thenAcceptAsync(balance -> {
            // CraftBlockchainPlugin.log("Craft Balance: " + balance);    
            System.out.println("Current thread: " + Thread.currentThread().getName());                    
            System.out.println("ucraft Balance: " + balance);
        });

        getCraftBalance(craft_address).thenAcceptAsync(balance -> {        
            // CraftBlockchainPlugin.log("Craft Balance: " + balance);
            System.out.println("craft Balance: " + balance);
        });
        
        // System.out.println(suply_url);
        getSupply("ucraft").thenAcceptAsync(amount -> {
            System.out.println("Current thread: " + Thread.currentThread().getName());                    
            System.out.println("ucraft Supply: " + amount);
        });

        getSupply("uexp").thenAcceptAsync(amount -> {
            System.out.println("Current thread: " + Thread.currentThread().getName());                    
            System.out.println("uexp Supply: " + amount);
        });

    }

    private static JSONParser parser = new JSONParser();
    private static JSONObject json;

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

        String url_link = BALANCES_ENDPOINT.replace("%address%", craft_addr).replace("%denomination%", "ucraft");
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
                    System.out.println(amount);
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
