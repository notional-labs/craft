package com.crafteconomy.blockchain.transactions.listeners;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.transactions.events.ExpiredTransactionEvent;
import com.crafteconomy.blockchain.utils.Util;

public class ExpiredTransactionListener implements Listener {    

    // private static String TX_ENDPOINT = CraftBlockchainPlugin.getTxQueryEndpoint();
    private static Boolean IS_DEV_MODE = CraftBlockchainPlugin.getIfInDevMode();

    @EventHandler
    public void onExpiredTxEvent(ExpiredTransactionEvent event) {
        UUID TxID = event.getTxID();

        // check if it is in pending
        Tx tx = PendingTransactions.getInstance().getTxFromID(TxID);
        if(tx == null) {
            CraftBlockchainPlugin.log("[ExpiredTransactionListener.java] TxID " + TxID + " is not in pending transactions on this server, ignore");
            return;
        }
        
        Consumer<UUID> consumer = tx.getConsumerOnExpire();
        BiConsumer<UUID, UUID> biConsumer = tx.getBiConsumerOnExpire();
        if(consumer != null) {
            consumer.accept(tx.getFromUUID());
        } else if(biConsumer != null) {
            biConsumer.accept(tx.getFromUUID(), tx.getToUUID());
        }  

        CraftBlockchainPlugin.log("[DEBUG] ExpiredTransactionEvent FIRED FOR TxID:" + TxID + "\n Consumer logic completed");
    }
    
}
