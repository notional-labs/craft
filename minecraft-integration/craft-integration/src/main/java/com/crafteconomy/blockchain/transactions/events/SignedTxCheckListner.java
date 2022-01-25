package com.crafteconomy.blockchain.transactions.events;

import java.util.Set;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class SignedTxCheckListner implements Listener {

    RedisManager redis = CraftBlockchainPlugin.getInstance().getRedis();

    @EventHandler
    public void onSignedTxCheck(SignedTransactionEvent event) {
        UUID TxID = event.getTxID();
        
        Util.logSevere("[DEBUG] SignedTransactionEvent FIRED FOR TxID:" + TxID);

        // Check if Integration has a TxID which matches the TxID fired
        // If it does, we can complete the method and remove the TxID from the pending list&cache
        Tx tx = PendingTransactions.getInstance().getTxFromID(TxID);

        if(tx == null) {
            return;
        }

        Util.logFine("SignedTransactionEvent found for " + TxID.toString().substring(0, 15) + "... Completing.\n");
        tx.complete();

        // remove that TxID from the pending list
        PendingTransactions.getInstance().removePending(TxID);
        System.out.println("[DEBUG] TxID: " + TxID + " removed from pending list");


        try (Jedis jedis = redis.getRedisConnection()) {
            // gets 1 key which matches the wallets address due to unique TxID
            Set<String> keyString = jedis.keys("tx_*_"+TxID);

            for(String key : keyString) {
                jedis.del(key);
                System.out.println("[DEBUG-REDIS] DELETED " + key);
            }

            jedis.del("signed_"+TxID);
            System.out.println("[DEBUG-REDIS] DELETED signed_"+TxID );

        } catch (Exception e) {
            System.out.println("SignedTxChecklistener Redis Error");
            throw new JedisException(e);
        } 
    }

    

}
