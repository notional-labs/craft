package com.crafteconomy.blockchain.core.request;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.core.types.TransactionType;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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

    public static int acc_seq_override = 0;

    // -= GIVING TOKENS =-
    public static String depositToAddress(String craft_address, long ucraft_amount) {        
        if(craft_address == null) {
            return "NO_WALLET";
        }
        // TODO: Hook into our API for payment of ucraft -> player
        
        /*
        TO NOT USE THESE MONIKERS, THEY ARE ONLY FOR PRIVATE TESTING
        # Server Wallet to give players funds
        craftd keys add daowallet --recover --keyring-backend test 
        multiply security charge attack minor logic staff belt want mixture sick rebuild sadness canvas twelve mango embark emotion bulb popular remind rebel circle blouse
        # craft1s4yczg3zgr4qdxussx3wpgezangh2388xgkkz9 -- 

        # Testing that the send actually worked
        craftd keys add playerwallet --recover --keyring-backend test
        treat draft tip empty disorder gain symptom harbor pride motion twin right pony car bubble fantasy tube supply amused nut strong service useless expose
        # craft12gr56gv009h8hq30k5xfv7agl2d8xa8u2ag7e4

        
        craftd tx bank send daowallet craft12gr56gv009h8hq30k5xfv7agl2d8xa8u2ag7e4 4ucraft --keyring-backend test --yes --chain-id craft-v4 --node http://65.108.125.182:26657 --sequence $(craftd q account craft1s4yczg3zgr4qdxussx3wpgezangh2388xgkkz9 --node http://65.108.125.182:26657 --output json | jq '.sequence' | sed 's/\"//g')
        craftd q bank balances craft12gr56gv009h8hq30k5xfv7agl2d8xa8u2ag7e4 --node http://65.108.125.182:26657
        */

        // when overridden, its because the sequence is off due to pending txs
        int acc_seq;
        if(acc_seq_override == 0) {
            acc_seq = Integer.valueOf(getAccountSequence("craft1s4yczg3zgr4qdxussx3wpgezangh2388xgkkz9")); // DAO wallet. make sure to set in config
        } else {
            // override blockchain query to the correct value (if a code 32 happens)
            acc_seq = acc_seq_override;
            acc_seq_override = 0;
        }
        System.out.println("Account Sequence for DAO Wallet craft1s4yczg3zgr4qdxussx3wpgezangh2388xgkkz9: " + acc_seq);
         
       

        // pay the user & set sequence as 1 higher than the wallets current sequence.
        String[] args = ("craftd tx bank send daowallet " + craft_address + " " + ucraft_amount + "ucraft --keyring-backend test --yes --chain-id craft-v4 --node http://65.108.125.182:26657 --sequence " + (acc_seq)).split(" ");
        try {
            // get the Tx hash here to prove it worked?
            Process process = new ProcessBuilder(args).start();
            String result = new String(process.getInputStream().readAllBytes());
            System.out.println(result);

            // if the account seq is off (32) then set it to the raw logs correct value of what it expected
            if(result.startsWith("code: 32")) {
                // raw_log: 'account sequence mismatch, expected 13, got 12: incorrect account sequence'
                acc_seq_override = Integer.valueOf(StringUtils.substringBetween(result, ", expected ", ", got "));
                depositToAddress(craft_address, ucraft_amount);
            }
            // code: 19 - already in mem pool

            String r2 = new String(process.getErrorStream().readAllBytes());
            System.out.println(r2);

            return "SUCCESS"; // return txhash in future            
        } catch (IOException e) {
            e.printStackTrace();            
        }
        return "FAILED";
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
