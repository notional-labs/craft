package com.crafteconomy.blockchain.core.request;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.core.types.TransactionType;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.JSONException;
import org.json.JSONObject;

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
        System.out.println(req_url);
        return EndpointQuery.req(req_url, RequestTypes.ACCOUNT, "Account Sequence Request").toString();
    }

    // -= GIVING TOKENS =-
    private static final String ENDPOINT_SECRET = CraftBlockchainPlugin.getInstance().getSecret();
    public static CompletableFuture<String> depositToAddress(String craft_address, long ucraft_amount) {   
        CompletableFuture<String> future = new CompletableFuture<>();

        if(craft_address == null) {            
            // throw new Exception("NO WALLET");
            future.complete("NO WALLET");
        }

        // curl --data '{"secret": "7821719493", "wallet": "craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0", "amount": 50000}' -X POST -H "Content-Type: application/json"  http://api.crafteconomy.io/v1/dao/make_payment 
        // {"success":"Wallet: craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0 Amount: 50000"}

        Bukkit.getServer().getScheduler().runTaskAsynchronously(CraftBlockchainPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                URL url = null;
                HttpURLConnection http = null;
                OutputStream stream = null;
                String msg = "";

                String data = "{\"secret\": \""+ENDPOINT_SECRET+"\", \"wallet\": \""+craft_address+"\", \"amount\": "+ucraft_amount+"}";
                System.out.println("depositToAddress data " + data);
                
                try {
                    url = new URL("http://api.crafteconomy.io/v1/dao/make_payment");
                    http = (HttpURLConnection)url.openConnection();
                    http.setRequestMethod("POST");
                    http.setDoOutput(true);
                    http.setRequestProperty("Content-Type", "application/json");
        
                    byte[] out = data.getBytes(StandardCharsets.UTF_8);
                    stream = http.getOutputStream();                    
                    stream.write(out);
        
                    msg = http.getResponseMessage();
                    System.out.println("depositToAddress code: " + http.getResponseCode() + " | msg: " + msg);
                    http.disconnect();

                    if(http.getResponseCode() == 200) {
                        System.out.println("Successful payment!");
                        future.complete("SUCCESS");
                    } else {
                        System.out.println("Failed payment!");
                        future.complete("FAILED");
                    }                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return future;
    }


    private static PendingTransactions pTxs = PendingTransactions.getInstance();

    public static ErrorTypes transaction(Tx transaction, int RedisMinuteTTL) {
        // int minuteTTL = 30;

        // IF we are in dev mode, don't try to send request to the blockchain, just do the transactions
        if(CraftBlockchainPlugin.getIfInDevMode() == false) {
            // we check how much ucraft is in the transaction data since its on chain, so get the ucraft from the Tx

            if(BlockchainRequest.getUCraftBalance(transaction.getToWallet()) < 0) {
                System.out.println("No wallet balance for address");  
                return ErrorTypes.NO_WALLET;
            }

            if(BlockchainRequest.getUCraftBalance(transaction.getFromWallet()) < transaction.getUCraftAmount()){
                System.out.println("Not enough tokens to send");
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


        JSONObject jsonObject;
        try {
            // we submit the uCraft amount -> the redis for the webapp to sign it directly
            String transactionJson = generateTxJSON(from, to, ucraftAmount, desc, txType);
            jsonObject = new JSONObject(transactionJson);
       }catch (JSONException err) {
            Util.logSevere("EBlockchainRequest.java Error " + err.toString());
            Util.logSevere("Description: " + transaction.getDescription());
            return ErrorTypes.JSON_PARSE_TRANSACTION;
       }
       
        pTxs.addPending(transaction.getTxID(), transaction);     
        redisDB.submitTxForSigning(from, TxID, jsonObject.toString(), RedisMinuteTTL);
        
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
        // System.out.println(v);
        return json;
    }
}
