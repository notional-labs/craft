package com.crafteconomy.blockchain.core.request;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.storage.RedisDB;
import com.crafteconomy.blockchain.utils.Util;

import redis.clients.jedis.Jedis;

public class BlockchainRequest {
    
    private static RedisDB redisDB = CraftBlockchainPlugin.getInstance().getRedis();
    private static String CRAFT_BINARY = CraftBlockchainPlugin.getCraftBinary();
    private static String SEND_TX = CRAFT_BINARY + " tx bank send %from% %to% %amount%token --chain-id craft --generate-only";


    private static final String API_ENDPOINT = "http://localhost:1317/cosmos/bank/v1beta1"; 
    private static final String TOKEN_FAUCET_ENDPOINT = "http://0.0.0.0:4500/";
    
    private static final String BALANCES_ENDPOINT = API_ENDPOINT + "/balances/%address%/by_denom?denom=%denomination%";
    private static final String SUPPLY_ENDPOINT = API_ENDPOINT + "/supply/%denomination%";

    // -= BALANCES =-
    public static long getBalance(String craft_address, String denomination) {
        if(craft_address == null) {
            return ErrorTypes.NO_WALLET.error_code;
        }
        
        Object cacheAmount = Caches.getIfPresent(RequestTypes.BALANCE, craft_address);
        if(cacheAmount != null) { 
            return (long) cacheAmount; 
        }
        
        // String req_url = "http://localhost:1317/cosmos/bank/v1beta1/balances/"+craft_address+"/by_denom?denom="+denomination;
        String req_url = BALANCES_ENDPOINT.replace("%address%", craft_address).replace("%denomination%", denomination);

        long amount = Long.parseLong(EndpointQuery.req(req_url, RequestTypes.BALANCE, "Balance Web Request").toString());

        Caches.put(RequestTypes.BALANCE, craft_address, amount);
        return amount;
    }

    public static long getBalance(String craft_address) {
        return getBalance(craft_address, "token");
    }


    // -= TOTAL SUPPLY =-
    public static long getTotalSupply(String denomination) {
        Object totalSupply = Caches.getIfPresent(RequestTypes.SUPPLY, denomination);        
        if(totalSupply != null) { 
            return (long) totalSupply; 
        }

        String URL = SUPPLY_ENDPOINT.replace("%denomination%", denomination);
        long supply = Long.parseLong(EndpointQuery.req(URL, RequestTypes.SUPPLY, "Total Supply Web Request").toString());

        Caches.put(RequestTypes.SUPPLY, denomination, supply);
        return supply;
    }

    public static long getTotalSupply() {
        return getTotalSupply("token");
    }


    // -= GIVING TOKENS =-
    public static String depositToAddress(String craft_address, long amount) {
        String body = "{  \"address\": \""+craft_address+"\",  \"coins\": [    \""+amount+"token\"  ]}";

        String log = "Faucet: " + craft_address + " " + amount;

        return (String) EndpointQuery.req(TOKEN_FAUCET_ENDPOINT, RequestTypes.FAUCET, body, log);
    }


    // -= TRADING TOKENS BETWEEN 2 =-
    public static ErrorTypes transferTokens(String FROM_ADDRESS, String TO_ADDRESS, long AMOUNT) {
        int minuteTTL = 15;

        if(BlockchainRequest.getBalance(FROM_ADDRESS) < AMOUNT){
            // System.out.println("Not enough tokens to send");
            return ErrorTypes.NOT_ENOUGH_TO_SEND;
        }

        if(BlockchainRequest.getBalance(TO_ADDRESS) < 0) {
            return ErrorTypes.NO_WALLET;
        }

        // String CMD = CRAFT_BINARY + " tx bank send "+FROM_ADDRESS+" "+TO_ADDRESS+" "+AMOUNT+"token --chain-id craft --generate-only";
        String CMD = SEND_TX.replace("%from%", FROM_ADDRESS).replace("%to%", TO_ADDRESS).replace("%amount%" , String.valueOf(AMOUNT));
        String json_output = Util.systemCommand(CMD); 
        

        // save to redis for the webapp to pull from, may use mongo in future. Whichever is easier
        Jedis jedis = redisDB.getRedisConnection();


        String tx_label = "tx_" + FROM_ADDRESS.substring(5) + "_In Game Transfer";

        // set the tx_label to the json_output with an X minute TTL
        jedis.setex(tx_label, minuteTTL*60, json_output);
        Util.log("Tx JSON Saved to redis as " + tx_label + ", "+ json_output);                    
        
        redisDB.getPool().returnResource(jedis);

        return ErrorTypes.NONE;
    }


}
