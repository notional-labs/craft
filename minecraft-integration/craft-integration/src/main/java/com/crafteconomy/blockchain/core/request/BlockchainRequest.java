package com.crafteconomy.blockchain.core.request;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.FaucetTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.core.types.TransactionType;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.JavaUtils;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.Bukkit;
import org.json.JSONException;
import org.json.simple.JSONObject;

public class BlockchainRequest {
    
    private static CraftBlockchainPlugin blockchainPlugin = CraftBlockchainPlugin.getInstance();
    private static RedisManager redisDB = blockchainPlugin.getRedis();
    private static String SERVER_ADDRESS = blockchainPlugin.getServersWalletAddress();

    // http://65.108.125.182:1317/cosmos/bank/v1beta1
    private static final String API_ENDPOINT = blockchainPlugin.getApiEndpoint();

    // Found via https://v1.cosmos.network/rpc/v0.41.4
    private static final String BALANCES_ENDPOINT = API_ENDPOINT + "cosmos/bank/v1beta1/balances/%address%/by_denom?denom=%denomination%";
    private static final String SUPPLY_ENDPOINT = API_ENDPOINT + "cosmos/bank/v1beta1/supply/by_denom?denom=%denomination%";
    private static final String ACCOUNT_ENDPOINT = API_ENDPOINT + "cosmos/auth/v1beta1/accounts/%address%";

    // -= BALANCES =-
    public static long getBalance(String craft_address, String denomination) {
        if(craft_address == null) {
            return ErrorTypes.NO_WALLET.code;
        }
        
        Object cacheAmount = Caches.getIfPresent(RequestTypes.BALANCE, craft_address);
        if(cacheAmount != null) { 
            return (long) cacheAmount; 
        }
        
        // TODO: Add uexp as well, only show if >0 OR just loop through directly:
        // http://65.108.125.182:1317/cosmos/bank/v1beta1/balances/craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0

        String req_url = BALANCES_ENDPOINT.replace("%address%", craft_address).replace("%denomination%", denomination);

        long amount = Long.parseLong(EndpointQuery.req(req_url, RequestTypes.BALANCE, "Balance Request").toString());

        Caches.put(RequestTypes.BALANCE, craft_address, amount);
        return amount;
    }

    public static long getUCraftBalance(String craft_address) { // 1_000_000ucraft = 1craft
        return getBalance(craft_address, "ucraft");
    }

    public static float getCraftBalance(String craft_address) { // 1 craft
        return (float) (getUCraftBalance(craft_address) / 1_000_000);
    }


    // -= TOTAL SUPPLY =-
    public static long getTotalSupply(String denomination) {
        Object totalSupply = Caches.getIfPresent(RequestTypes.SUPPLY, denomination);        
        if(totalSupply != null) { 
            return (long) totalSupply; 
        }

        String URL = SUPPLY_ENDPOINT.replace("%denomination%", denomination);
        long supply = Long.parseLong(EndpointQuery.req(URL, RequestTypes.SUPPLY, "Total Supply Request").toString());

        Caches.put(RequestTypes.SUPPLY, denomination, supply);
        return supply;
    }

    public static long getTotalSupply() {
        return getTotalSupply("ucraft");
    }

    public static String getAccountSequence(String craft_address) {
        // curl -X GET "https://api.cosmos.network/cosmos/auth/v1beta1/accounts/cosmos10r39fueph9fq7a6lgswu4zdsg8t3gxlqvvvyvn" -H "accept: application/json"

        String req_url = ACCOUNT_ENDPOINT.replace("%address%", craft_address);
        CraftBlockchainPlugin.log(req_url);
        return EndpointQuery.req(req_url, RequestTypes.ACCOUNT, "Account Sequence Request").toString();
    }

    // -= GIVING TOKENS =-
    private static final String ENDPOINT_SECRET = CraftBlockchainPlugin.getInstance().getSecret();

    private static FaucetTypes makePostRequest(String craft_address, String description, long ucraft_amount) {
        if(craft_address == null) { return FaucetTypes.NO_WALLET; }

        URL url = null;
        HttpURLConnection http = null;
        OutputStream stream = null;
        String data = "{\"secret\": \""+ENDPOINT_SECRET+"\", \"description\": \""+description+"\", \"wallet\": \""+craft_address+"\", \"ucraft_amount\": "+ucraft_amount+"}";
        CraftBlockchainPlugin.log("depositToAddress data " + data); // TODO: Remove this from production code
        
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

            CraftBlockchainPlugin.log("response from API string: " + response);

            JSONObject json = new JSONObject();
            // parse response
            json = (JSONObject) org.json.simple.JSONValue.parse(response);


            CraftBlockchainPlugin.log("depositToAddress code: " + http.getResponseCode() + " | response: " + json);
            http.disconnect();

            if(http.getResponseCode() != 200) {
                CraftBlockchainPlugin.log("Failed payment!");
                return FaucetTypes.FAILURE;
            }                    

            if(json.keySet().contains("success")) {
                CraftBlockchainPlugin.log("Successful payment!");
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
                CraftBlockchainPlugin.log("Some other failure!");
                return FaucetTypes.FAILURE;
            }
        }
        
        return FaucetTypes.FAILURE;
    }
    

    public static CompletableFuture<FaucetTypes> depositUCraftToAddress(String craft_address, String description, long ucraft_amount) {   
        // curl --data '{"secret": "7821719493", "wallet": "craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0", "amount": 50000}' -X POST -H "Content-Type: application/json"  http://api.crafteconomy.io/v1/dao/make_payment
        return CompletableFuture.supplyAsync(() -> makePostRequest(craft_address, description, ucraft_amount)).completeOnTimeout(FaucetTypes.ENDPOINT_TIMEOUT, 45, TimeUnit.SECONDS);

    }
    public static CompletableFuture<FaucetTypes> depositCraftToAddress(String craft_address, String description, float craft) {           
        return depositUCraftToAddress(craft_address, description, (long)(craft*1_000_000));
    }


    private static PendingTransactions pTxs = PendingTransactions.getInstance();

    public static ErrorTypes transaction(Tx transaction) {
        // int minuteTTL = 30;

        // IF we are in dev mode, don't try to send request to the blockchain, just do the transactions
        if(CraftBlockchainPlugin.getIfInDevMode() == false) {
            // we check how much ucraft is in the transaction data since its on chain, so get the ucraft from the Tx

            if(BlockchainRequest.getUCraftBalance(transaction.getToWallet()) < 0) {
                CraftBlockchainPlugin.log("No wallet balance for address");  
                return ErrorTypes.NO_WALLET;
            }

            if(BlockchainRequest.getUCraftBalance(transaction.getFromWallet()) < transaction.getUCraftAmount()){
                CraftBlockchainPlugin.log("Not enough tokens to send");
                return ErrorTypes.NOT_ENOUGH_TO_SEND;
            }            
        } else {
            String name = Bukkit.getPlayer(transaction.getFromUUID()).getName().toUpperCase();
            Util.coloredBroadcast("&cDEV MODE IS ENABLED FOR THIS TRANSACTION "+name+" (config.yml, no blockchain request)");
        }

        

        String from = transaction.getFromWallet();
        String to = transaction.getToWallet();
        long ucraftAmount = transaction.getUCraftAmount();
        UUID TxID = transaction.getTxID();
        String desc = transaction.getDescription();
        TransactionType txType = transaction.getTxType(); // used for webapp

        int redisMinuteTTL = transaction.getRedisMinuteTTL(); // minutes till this transaction should be: rm from redis, rm from pending, run the following:
        Consumer<UUID> runOnExpire = transaction.getConsumerOnExpire(); // check these are not null,
        BiConsumer<UUID, UUID> runOnBiExpire = transaction.getBiConsumerOnExpire();

        
        org.json.JSONObject jsonObject;
        try {
            // we submit the uCraft amount -> the redis for the webapp to sign it directly
            String transactionJson = generateTxJSON(from, to, ucraftAmount, desc, txType);
            jsonObject = new org.json.JSONObject(transactionJson);            
       }catch (JSONException err) {
            CraftBlockchainPlugin.log("EBlockchainRequest.java Error " + err.toString());
            CraftBlockchainPlugin.log("Description: " + transaction.getDescription());
            return ErrorTypes.JSON_PARSE_TRANSACTION;
       }
       
        pTxs.addPending(transaction.getTxID(), transaction);     
        redisDB.submitTxForSigning(from, TxID, jsonObject.toString(), redisMinuteTTL);
        
        return ErrorTypes.SUCCESS;
    }

    // private static String tokenDenom = blockchainPlugin.getTokenDenom(true);

    /**
     * Generates a JSON object for a transaction used by the blockchain
     * @param FROM
     * @param TO
     * @param AMOUNT
     * @param DESCRIPTION
     * @return String JSON Amino (Readable by webapp)
     */
    private static String generateTxJSON(String FROM, String TO, long UCRAFT_AMOUNT, String DESCRIPTION, TransactionType txType) {    
        double taxAmount = UCRAFT_AMOUNT * blockchainPlugin.getTaxRate();
        long now = Instant.now().getEpochSecond();

        // EX: {"amount":"2","description":"Purchase Business License for 2","to_address":"osmo10r39fueph9fq7a6lgswu4zdsg8t3gxlqyhl56p","tax":{"amount":0.1,"address":"osmo10r39fueph9fq7a6lgswu4zdsg8t3gxlqyhl56p"},"denom":"uosmo","from_address":"osmo10r39fueph9fq7a6lgswu4zdsg8t3gxlqyhl56p"}
        
        // ",\"timestamp\": "+variable.toString()+

        // Tax is another message done via webapp to pay a fee to the DAO. So the total transaction cost = amount + tax.amount
        String json = "{\"from_address\": "+FROM+",\"to_address\": "+TO+",\"description\": "+DESCRIPTION+",\"tx_type\": "+txType.toString()+",\"timestamp\": "+now+",\"amount\": \""+UCRAFT_AMOUNT+"\",\"denom\": \"ucraft\",\"tax\": { \"amount\": "+taxAmount+", \"address\": "+SERVER_ADDRESS+"}}";
        // CraftBlockchainPlugin.log(v);
        return json;
    }
}
