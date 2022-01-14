package com.crafteconomy.blockchain.transactions;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.wallets.WalletManager;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString 
public class Tx implements Serializable {
    
    private UUID playerUUID;
    private UUID TxID;
    private Consumer<UUID> function;
    private String description;

    private String toWallet;
    private long amount;

    public Tx(UUID playerUUID, String TO_WALLET, int amount, String description, Consumer<UUID> function){
        this.setPlayerUUID(playerUUID);
        this.setFunction(function);
        this.setDescription(description);
        this.setToWallet(toWallet);
        this.setAmount(amount);

        // generated randomly for each Tx
        this.TxID = UUID.randomUUID();
    }

    public Tx() {    
        this.TxID = UUID.randomUUID();    
    }

    public String getFromWallet() {
        return WalletManager.getInstance().getAddress(playerUUID);
    }

    public void complete() {
        this.getFunction().accept(this.playerUUID);
        // remove TxID from signed_<TxID> in redis
    }

    @Override
    public boolean equals(Object o) {        
        if(o instanceof Tx) {
            Tx tx = (Tx) o;
            return tx.getTxID().equals(this.getTxID());
        }        
        return false;
    }
}