package com.crafteconomy.blockchain.core.request;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

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
import com.crafteconomy.blockchain.wallets.WalletManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.simple.JSONObject;

public class BlockchainRequest {
    
    private static CraftBlockchainPlugin blockchainPlugin = CraftBlockchainPlugin.getInstance();
    private static RedisManager redisDB = blockchainPlugin.getRedis();
    private static String SERVER_ADDRESS = blockchainPlugin.getServersWalletAddress();

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 

    private static WalletManager walletManager = WalletManager.getInstance();

    // -= BALANCES =-
    public static CompletableFuture<Long> getBalance(String craft_address, String denomination) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        if(craft_address == null) {
            future.complete((long) ErrorTypes.NO_WALLET.code);
            // return ErrorTypes.NO_WALLET.code;
        }
        
        Object cacheAmount = Caches.getIfPresent(RequestTypes.BALANCE, craft_address);
        if(cacheAmount != null) { 
            future.complete((long) cacheAmount);
            // return (long) cacheAmount; 
        }

        EndpointQuery.getBalance(craft_address, denomination).thenAcceptAsync(amt -> {
            Caches.put(RequestTypes.BALANCE, craft_address, amt);
            future.complete(amt);
        });

        // String req_url = BALANCES_ENDPOINT.replace("%address%", craft_address).replace("%denomination%", denomination);
        // long amount = Long.parseLong(EndpointQuery.req(req_url, RequestTypes.BALANCE, "Balance Request").toString());

        return future;
    }

    public static CompletableFuture<Long> getUCraftBalance(String craft_address) { // 1_000_000ucraft = 1craft
        return getBalance(craft_address, "ucraft");
    }

    public static CompletableFuture<Float> getCraftBalance(String craft_address) { // 1 craft
        // return (float) (getUCraftBalance(craft_address) / 1_000_000);
        return getBalance(craft_address, "craft").thenApplyAsync(amt -> (float) (amt / 1_000_000));
    }


    // -= TOTAL SUPPLY =-
    public static CompletableFuture<Long> getTotalSupply(String denomination) {
        CompletableFuture<Long> future = new CompletableFuture<>();

        Object totalSupply = Caches.getIfPresent(RequestTypes.SUPPLY, denomination);        
        if(totalSupply != null) { 
            future.complete((long) totalSupply);            
        }
        
        // long supply = Long.parseLong(EndpointQuery.req(URL, RequestTypes.SUPPLY, "Total Supply Request").toString());
        EndpointQuery.getSupply(denomination).thenAcceptAsync(amount -> {
            Caches.put(RequestTypes.SUPPLY, denomination, amount); 
            future.complete(amount);  
        });        

        return future;
    }

    public static CompletableFuture<Long> getTotalSupply() {
        return getTotalSupply("ucraft");
    }

    // -= GIVING TOKENS =-
    private static final String ENDPOINT_SECRET = CraftBlockchainPlugin.getInstance().getSecret();

    private static FaucetTypes makePostRequest(String craft_address, String description, long ucraft_amount) {
        if(craft_address == null) { return FaucetTypes.NO_WALLET; }

        URL url = null;
        HttpURLConnection http = null;
        OutputStream stream = null;
        String endpoint = CraftBlockchainPlugin.getInstance().getApiMakePaymentEndpoint();
        String data = "{\"secret\": \""+ENDPOINT_SECRET+"\", \"description\": \""+description+"\", \"wallet\": \""+craft_address+"\", \"ucraft_amount\": \""+ucraft_amount+"\"}";
        // CraftBlockchainPlugin.log("url: "+endpoint+", depositToAddress data " + data);
        
        try {
            url = new URL(endpoint);
            http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");

            byte[] out = data.getBytes(StandardCharsets.UTF_8);
            stream = http.getOutputStream();                    
            stream.write(out);

            // get the return value of the POST request
            // {"success":{"craft_amount":"1","wallet":"craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0","ucraft_amount":"1000000","serverCraftBalLeft":"999999910.196505craft",
            //      "transactionHash":"EFF47C0977F82CC6533B6CFDDF7E5D93A45D7F955210B457B3CD8DE6E33EA289","height":40486}}
            String response = JavaUtils.streamToString(http.getInputStream());
            if(response.length() == 0) {
                System.err.println("No response from server API (length 0 string)");
                return FaucetTypes.NO_RESPONSE;
            }            

            JSONObject json = new JSONObject();        
            json = (JSONObject) org.json.simple.JSONValue.parse(response);


            CraftBlockchainPlugin.log("API Response: " + http.getResponseCode() + " | response: " + json);
            http.disconnect();

            if(http.getResponseCode() != 200) {
                CraftBlockchainPlugin.log("Failed payment!");
                return FaucetTypes.FAILURE;
            }                    

            if(json.keySet().contains("success")) {
                CraftBlockchainPlugin.log("Successful payment!");
                return FaucetTypes.SUCCESS;
                
            } else if (json.keySet().contains("error")) {
                boolean doSaveToDBForRunLater = true;
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
                        doSaveToDBForRunLater = false;
                    }
                    default -> {
                        output = "No success in response from server API: " + json;
                    }
                }            
                CraftBlockchainPlugin.log(output, Level.SEVERE);
                if(doSaveToDBForRunLater) { saveFailedTransaction(craft_address, description, ucraft_amount, output); }

                return returnType;           
            }
                
        } catch (Exception e) {
            e.printStackTrace();

            saveFailedTransaction(craft_address, description, ucraft_amount, e.getMessage());
            if(e.getMessage().startsWith("Server returned HTTP response code: 502 for URL:")) {                
                CraftBlockchainPlugin.log("makePayment API is down!", Level.SEVERE);
                return FaucetTypes.API_DOWN;
            } else {                
                CraftBlockchainPlugin.log("makePayment API is down!", Level.SEVERE);
                return FaucetTypes.FAILURE;
            }
        }
        
        return FaucetTypes.FAILURE;
    }

    // save a failed transaction to the database to be run later. From EscrowManager.java
    private static MongoDatabase db = CraftBlockchainPlugin.getInstance().getMongo().getDatabase();
    private static String FAILED_TXS = "failedTxs";
    public static void saveFailedTransaction(String craft_address, String description, long ucraft_amount, String failure_reason) {
        CraftBlockchainPlugin.log("Saving failed transaction to database...");
        
        // get current time in human readable format
        Document doc = getUsersDocument(craft_address);
        Document failedTX = createFailedTransaction(craft_address, description, ucraft_amount, failure_reason);

        if(doc == null) { // users first unpaied payment
            // put the document into database as an array
            ArrayList<Document> FaileTxsList = new ArrayList<Document>();
            FaileTxsList.add(failedTX);

            doc = new Document("craft_address", craft_address);
            doc.put(FAILED_TXS, FaileTxsList);

            getCollection().insertOne(doc);

        } else {
            // getCollection().updateOne(Filters.eq("_id", uuid.toString()), Updates.set("ucraft_amount", newBalance));
            Object s = doc.get(FAILED_TXS);
            ArrayList<Document> failedTXs = (ArrayList<Document>) s;             
            if (failedTXs == null) {
                failedTXs = new ArrayList<Document>();
            }

            failedTXs.add(failedTX);
            doc.put(FAILED_TXS, failedTXs);
            getCollection().replaceOne(Filters.eq("craft_address", craft_address), doc);
        }

        // Notify the user that their payment failed but is pending for later send.
        Optional<UUID> uuid = walletManager.getUUIDFromWallet(craft_address);
        if(uuid.isPresent()) {
            // get the uuid & see if player is online
            UUID uuidValue = uuid.get();
            Player p = Bukkit.getPlayer(uuidValue);
            if(p != null) {
                Util.colorMsg(p, "&6[!] Error: Payment failed. Saved to database & will be tried again soon.");
                Util.colorMsg(p, "&e&oReason: " + failure_reason);
                Util.colorMsg(p, "&e&oAmount: " + ucraft_amount/1_000_000 + " craft.");
                Util.colorMsg(p, "&6NOTE: No action is required on your part.");
            }
        }

        CraftBlockchainPlugin.log("Saved failed transaction to database!");
    }    
    private static Document createFailedTransaction(String craft_address, String description, long ucraft_amount, String failure_reason) {        
        Document doc = new Document();
        doc.put("craft_address", craft_address);
        doc.put("description", description);
        doc.put("ucraft_amount", ucraft_amount);
        doc.put("time_epoch", System.currentTimeMillis() / 1000);
        doc.put("time_human", dtf.format(LocalDateTime.now()));
        doc.put("failure_reason", failure_reason);
        return doc;
    }
    private static Document getUsersDocument(String craft_address) {
        Bson filter = Filters.eq("craft_address", craft_address);
        return getCollection().find(filter).first();
    }
    private static MongoCollection<Document> getCollection() {
        return db.getCollection("failedPayments");
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

            // if(BlockchainRequest.getUCraftBalance(transaction.getToWallet()) < 0) {
            //     CraftBlockchainPlugin.log("No wallet balance for address");  
            //     return ErrorTypes.NO_WALLET;
            // }

            // TODO: Thread blocking
            // CompletableFuture<Long> v = BlockchainRequest.getUCraftBalance(transaction.getToWallet()).thenAcceptAsync(amt -> {
            //     CraftBlockchainPlugin.log("No wallet balance for address");  
            //     return 0L; // ErrorTypes.NO_WALLET
            // });

            // get value from BlockchainRequest.getUCraftBalance ffuture
            
            try {
                long amount = BlockchainRequest.getUCraftBalance(transaction.getToWallet()).get();

                if(amount < 0) {
                    CraftBlockchainPlugin.log("No wallet balance for address");  
                    return ErrorTypes.NO_WALLET;

                }else if(amount < transaction.getUCraftAmount()) {
                    CraftBlockchainPlugin.log("Not enough tokens to send");
                    return ErrorTypes.NOT_ENOUGH_TO_SEND;
                }

            } catch (InterruptedException | ExecutionException e) {                
                e.printStackTrace();
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
        // Consumer<UUID> runOnExpire = transaction.getConsumerOnExpire(); // check these are not null,
        // BiConsumer<UUID, UUID> runOnBiExpire = transaction.getBiConsumerOnExpire();

        
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
