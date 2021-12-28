package com.crafteconomy.blockchain.signedtxs;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SignedTransactionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    private String TxID;
    private String Metadata;

    public SignedTransactionEvent(String TxID, String Metadata) {
        this.TxID = TxID;
        this.Metadata = Metadata; // "craftcompanies_purchase-company-license"
    }

    public SignedTransactionEvent() { 
                
    }

    public void setTx(String tx) {
        this.TxID = tx;
    }

    public String getTxID() {
        return TxID;
    }
    
    public void setMetaData(String Metadata) {
        this.Metadata = Metadata;
    }

    public String getMetaData() {
        return Metadata;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
