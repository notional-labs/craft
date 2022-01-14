package com.crafteconomy.blockchain.transactions.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

// NOTE: OUT OF DATE

public class SignedTransactionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    private UUID TxID;

    public SignedTransactionEvent(UUID TxID) {
        super(true); // event is async woo!
        this.TxID = TxID;
    }

    public SignedTransactionEvent() { 
        super(true); // event is async woo!   
    }

    public void setTx(UUID TxID) {
        this.TxID = TxID;
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
