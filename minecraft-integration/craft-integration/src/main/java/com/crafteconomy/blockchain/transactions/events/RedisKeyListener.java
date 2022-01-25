package com.crafteconomy.blockchain.transactions.events;

import java.util.UUID;

import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.Bukkit;

import redis.clients.jedis.JedisPubSub;

public class RedisKeyListener extends JedisPubSub {

    RedisManager redisManager = RedisManager.getInstance();

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("onPSubscribe " + pattern + " " + subscribedChannels);
    }

    SignedTransactionEvent event = new SignedTransactionEvent(null);
    
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println("onPMessage pattern " + pattern + " " + channel + " " + message);

        // System.out.println(channel.split("signed_")[1]);

        // __keyevent@0__:set signed_6a231009-63d9-4a4a-8929-73fa7e59a154 
        // [!]IF KEA is set in redis
        String TransactionID = channel.split("signed_")[1];

        UUID TxID = null;
        try {
            TxID = UUID.fromString(TransactionID);

            Util.log("[WalletFakeSign] Firing Event for SignedTX: " + TransactionID);
            event.setTx(TxID);
            Bukkit.getServer().getPluginManager().callEvent(event);

        } catch (Exception e) {
            System.out.println("The TxID: " + TransactionID + " is not a valid TxID");
            e.printStackTrace();
        }        
    }
    
}
