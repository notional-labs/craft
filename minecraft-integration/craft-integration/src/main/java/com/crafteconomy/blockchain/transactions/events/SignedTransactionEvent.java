package com.crafteconomy.blockchain.transactions.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

// NOTE: OUT OF DATE

public class SignedTransactionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    private UUID TxID;
    private String tednermintHash = null;

    public SignedTransactionEvent(UUID TxID) {
        super(true);  // async for RedisKeylistener being async   
        this.TxID = TxID;
    }

    public SignedTransactionEvent() { 
        super(true); // async for RedisKeylistener being async   
    }

    public void setTx(UUID TxID) {
        this.TxID = TxID;
    }

    public void setTendermintHash(String tednermintHash) {
        this.tednermintHash = tednermintHash;
    }

    public String getTednermintHash() {
        return tednermintHash;
    }

    public UUID getTxID() {
        return TxID;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
