package com.crafteconomy.blockchain.transactions.listeners;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.transactions.events.ExpiredTransactionEvent;
import com.crafteconomy.blockchain.transactions.events.SignedTransactionEvent;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.Bukkit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

public class RedisKeyListener extends JedisPubSub {

    RedisManager redisManager = RedisManager.getInstance();

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("onPSubscribe " + pattern + " " + subscribedChannels);
    }

    SignedTransactionEvent event = new SignedTransactionEvent(null);
    ExpiredTransactionEvent expiredEvent = new ExpiredTransactionEvent(null);
    
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println("onPMessage pattern:" + pattern + " | channel:" + channel + " | message:" + message);
        // System.out.println(channel.split("signed_")[1]);  

        // When signed_ is set via the API (or fakesign)
        if(message.equalsIgnoreCase("set") && channel.contains("signed_")) {
            // __keyevent@0__:set signed_6a231009-63d9-4a4a-8929-73fa7e59a154 
            // [!]IF KEA is set in redis
            String TransactionID = channel.split("signed_")[1];
            String tendermintHash = null;

            // Gets the value of the signed_<uuid> key (either DEBUGGING or teh tendermint hash)
            try (Jedis jedis = redisManager.getRedisConnection()) {
                tendermintHash = jedis.get("signed_" + TransactionID);
            } catch (JedisException e) {
                Util.logSevere("[RedisKeyListener] JedisException getting value of key: " + e.getMessage());
            }

            UUID TxID = null;
            try {
                TxID = UUID.fromString(TransactionID);

                Util.log("[WalletFakeSign] Firing Event for SignedTX: " + TransactionID);
                event.setTx(TxID);
                event.setTendermintHash(tendermintHash);
                Bukkit.getServer().getPluginManager().callEvent(event);

            } catch (Exception e) {
                System.out.println("The TxID: " + TransactionID + " is not a valid TxID");
                e.printStackTrace();
            }  


        } else if(message.equalsIgnoreCase("expired") && channel.contains("tx_")) { //  & message.contains("expire")
            // this is an expired transaction, we will remove from in game & run any consumers for expired Txs
            System.out.println("HERE IT IS:" + channel);
            String TransactionID = channel.split("tx_")[1].split("_")[1];
            Util.logSevere("Expired Tx: " + TransactionID + ". Message:" + message);
            UUID TxID = null;
            try {
                TxID = UUID.fromString(TransactionID);                  
                expiredEvent.setTx(TxID);
                Util.logWarn("Firing event for ExpiredTxEvent in RedisKeyListener.java");
                Bukkit.getServer().getPluginManager().callEvent(expiredEvent);
            } catch (Exception e) {
                Util.logSevere("[RedisKeyListener.java] Failed to parse TxID from channel: " + channel + " for expired key removal!");
            }
            return;
        }

              
    }
    
}
