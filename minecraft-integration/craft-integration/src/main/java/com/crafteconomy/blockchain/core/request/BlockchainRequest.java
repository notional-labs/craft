package com.crafteconomy.blockchain.core.request;

import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class BlockchainRequest {
    
    private static RedisManager redisDB = CraftBlockchainPlugin.getInstance().getRedis();

    // http://IP:PORT/cosmos/bank/v1beta1
    private static final String API_ENDPOINT = CraftBlockchainPlugin.getInstance().getApiEndpoint();
    // osmosis endpoints (https://osmo.api.ping.pub/). Found via https://v1.cosmos.network/rpc/v0.41.4
    private static final String BALANCES_ENDPOINT = API_ENDPOINT + "/balances/%address%/by_denom?denom=%denomination%";
    private static final String SUPPLY_ENDPOINT = API_ENDPOINT + "/supply/%denomination%";
    // TODO: For denominations in uosmo/ucraft, ensure to *1000000

    private static final String TOKEN_DENOMINATION = CraftBlockchainPlugin.getInstance().getTokenDenom(true);

    // -= BALANCES =-
    public static long getBalance(String craft_address, String denomination) {
        if(craft_address == null) {
            return ErrorTypes.NO_WALLET.code;
        }
        
        Object cacheAmount = Caches.getIfPresent(RequestTypes.BALANCE, craft_address);
        if(cacheAmount != null) { 
            return (long) cacheAmount; 
        }
        
        String req_url = BALANCES_ENDPOINT.replace("%address%", craft_address).replace("%denomination%", denomination);

        long amount = Long.parseLong(EndpointQuery.req(req_url, RequestTypes.BALANCE, "Balance Web Request").toString());

        Caches.put(RequestTypes.BALANCE, craft_address, amount);
        return amount;
    }

    public static long getBalance(String craft_address) {
        return getBalance(craft_address, TOKEN_DENOMINATION);
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
        return getTotalSupply(TOKEN_DENOMINATION);
    }

    // -= GIVING TOKENS =-
    public static String depositToAddress(String craft_address, long amount) {
        String body = "{  \"address\": \""+craft_address+"\",  \"coins\": [    \""+amount+"token\"  ]}";

        String log = "Faucet: " + craft_address + " " + amount;

        return (String) EndpointQuery.req(CraftBlockchainPlugin.getInstance().getTokenFaucet(), RequestTypes.FAUCET, body, log);
    }


    private static PendingTransactions pTxs = PendingTransactions.getInstance();
    public static ErrorTypes transaction(Tx transaction) {
        int minuteTTL = 30;

        if(BlockchainRequest.getBalance(transaction.getFromWallet()) < transaction.getAmount()){
            System.out.println("Not enough tokens to send");
            return ErrorTypes.NOT_ENOUGH_TO_SEND;
        }

        if(BlockchainRequest.getBalance(transaction.getToWallet()) < 0) {
            System.out.println("No wallet balance for address");  
            return ErrorTypes.NO_WALLET;
        }

        String from = transaction.getFromWallet();
        String to = transaction.getToWallet();
        long amount = transaction.getAmount();
        UUID TxID = transaction.getTxID();


        JSONObject jsonObject;
        try {
            String transactionJson = generateJSONAminoTx(from, to, amount, transaction.getDescription());
            jsonObject = new JSONObject(transactionJson);
       }catch (JSONException err){
            Util.logSevere("Error" + err.toString());
            return ErrorTypes.JSON_PARSE_TRANSACTION;
       }
       
        pTxs.addPending(transaction.getTxID(), transaction);     
        redisDB.submitTxForSigning(from, TxID, jsonObject.toString(), minuteTTL);
        
        return ErrorTypes.NO_ERROR;
    }


    /**
     * Generates a JSON object for a transaction used by the blockchain
     * @param FROM
     * @param TO
     * @param AMOUNT
     * @param DESCRIPTION
     * @return String JSON Amino (Readable by webapp)
     */
    // TODO: Old craft tx format
    // private static String generateJSONAminoTx(String FROM, String TO, long AMOUNT, String DESCRIPTION) {
    //     return "{\"body\":{\"messages\":[{\"@type\":\"/cosmos.bank.v1beta1.MsgSend\",\"from_address\":\""+FROM+"\",\"to_address\":\""+TO+"\",\"amount\":[{\"denom\":\"token\",\"amount\":\""+AMOUNT+"\"}]}],\"memo\":\""+DESCRIPTION+"\",\"timeout_height\":\"0\",\"extension_options\":[],\"non_critical_extension_options\":[]},\"auth_info\":{\"signer_infos\":[],\"fee\":{\"amount\":[],\"gas_limit\":\"200000\",\"payer\":\"\",\"granter\":\"\"}},\"signatures\":[]}";
    // }

    private static String generateJSONAminoTx(String FROM, String TO, long AMOUNT, String DESCRIPTION) {
        // since it is uosmo, we multiple amount x 1mil
        return "{\"txBody\":{\"messages\":[{\"fromAddress\":\""+FROM+"\",\"toAddress\":\""+TO+"\",\"amount\":[{\"denom\":\"uosmo\",\"amount\":\""+(AMOUNT*1000000)+"\"}]}],\"memo\":\""+DESCRIPTION+"\"},\"authInfo\":{\"signerInfos\":[{\"publicKey\":{\"type_url\":\"/cosmos.crypto.secp256k1.PubKey\",\"value\":\"CiEClb3dZ44jTqORGRy3N4CIxVFFKF1v4h3GbFr7etcCV6Q=\"},\"modeInfo\":{\"single\":{\"mode\":\"SIGN_MODE_DIRECT\"}},\"sequence\":\"111\"}],\"fee\":{\"amount\":[{\"denom\":\"uosmo\",\"amount\":\"0\"}],\"gasLimit\":\"200000\"}},\"chainId\":\"osmosis-1\",\"accountNumber\":\"126268\"}";
    }
}
