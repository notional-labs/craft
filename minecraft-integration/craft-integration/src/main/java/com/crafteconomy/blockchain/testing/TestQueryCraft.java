package com.crafteconomy.blockchain.testing;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TestQueryCraft {

    public static void main(String[] args) {
        queryCraftTokenPrice().thenAccept(price -> {
            System.out.println("Craft USD Price: " + price);
        });
    }

    private static JSONParser parser = new JSONParser();
    private static JSONObject json;

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
