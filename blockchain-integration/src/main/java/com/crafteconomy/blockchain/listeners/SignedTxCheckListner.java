package com.crafteconomy.blockchain.listeners;

import com.crafteconomy.blockchain.signedtxs.SignedTransactionEvent;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SignedTxCheckListner implements Listener {

    @EventHandler
    public void onSignedTxCheck(SignedTransactionEvent event) {
        String TxID = event.getTxID();
        String metaData = event.getMetaData();
        
        Util.logSevere("[DEBUG] FOUND " + TxID + " : " + metaData);

        // do actions here if plugin has generated that TxID
    }

}
