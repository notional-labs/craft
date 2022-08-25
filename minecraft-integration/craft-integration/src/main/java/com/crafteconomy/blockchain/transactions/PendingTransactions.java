package com.crafteconomy.blockchain.transactions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.utils.Util;

import redis.clients.jedis.Jedis;

public class PendingTransactions {
    
    // Randomly Generated TxUUID, Tx
    private static Map<UUID, Tx> pending = new HashMap<>();

    private static PendingTransactions instance = null;

    private PendingTransactions() {  
        // singleton      
    }

    public void addPending(UUID TxID, Tx tx) {
        // ID, TxInfo
        pending.put(TxID, tx);
    }

    public void removePending(UUID TxID) {
        pending.remove(TxID);
    }

    public Tx getTxFromID(UUID txID) {
        return pending.get(txID);
    }

    public Set<UUID> getKeys() {
        return pending.keySet();
    }

    public boolean expireTransaction(UUID txID) {
        if(getKeys().contains(txID)) {            
            try (Jedis jedis = RedisManager.getInstance().getRedisConnection()) {
            
                // deletes redis keys which are in pending keys since we do not save to DB
                String key = "tx_*_"+ txID.toString();
                // String value = jedis.get(key);

                jedis.keys(key).forEach(k -> {
                    // jedis.unlink(k); // deletes the key
                    jedis.setex(k, 1, "expired");
                    CraftBlockchainPlugin.log("[PendingTxs.java] Expired " + key + " from redis");
                });  

                // removePending(txID);
                return true;

            } catch (Exception e) {
                CraftBlockchainPlugin.log("[PendingTxs.java] Failed to clear transactions. Make sure pool is open");
            }                        
        }
        return false;
    }

    public static void clearUncompletedTransactionsFromRedis() {
        try (Jedis jedis = RedisManager.getInstance().getRedisConnection()) {
            
            // deletes redis keys which are in pending keys since we do not save to DB
            for(UUID TxID : pending.keySet()) {

                String key = "tx_*_"+ TxID.toString();
                // String value = jedis.get(key);

                jedis.keys(key).forEach(k -> {
                    jedis.unlink(k); // deletes the key
                    CraftBlockchainPlugin.log("[PendingTxs.java] Removed " + key + " from redis");
                });  
            }

        } catch (Exception e) {
            CraftBlockchainPlugin.log("[PendingTxs.java] Failed to clear transactions. Make sure pool is open");
        }
    }

    public static PendingTransactions getInstance() {       
        if(instance == null) {
            instance = new PendingTransactions();            
        }
        
        return instance;
    }

}
