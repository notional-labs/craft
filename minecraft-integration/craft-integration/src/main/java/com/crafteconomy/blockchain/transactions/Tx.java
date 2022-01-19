package com.crafteconomy.blockchain.transactions;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.wallets.WalletManager;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString 
public class Tx implements Serializable {
    
    private UUID fromUUID;
    private UUID toUUID; // for BiConsumer only

    private UUID TxID;
    private String description;

    private Consumer<UUID> function = null;
    private BiConsumer<UUID, UUID> biFunction = null;

    private String toWallet;
    private long amount;

    public Tx(UUID playerUUID, String TO_WALLET, int amount, String description, Consumer<UUID> function){
        this.setFromUUID(playerUUID);        
        this.setDescription(description);
        this.setToWallet(toWallet);
        this.setAmount(amount);     
        this.setFunction(function);

        this.TxID = UUID.randomUUID(); // random for each Tx for signing time
    }

    public Tx(UUID playerUUID, UUID recipientUUID, String TO_WALLET, int amount, String description, BiConsumer<UUID, UUID> biFunction){
        this(playerUUID, TO_WALLET, amount, description, null);
        this.setToUUID(recipientUUID);
        this.setBiFunction(biFunction);        
    }

    public Tx() {    
        this.TxID = UUID.randomUUID();    
    }

    public String getFromWallet() {
        return WalletManager.getInstance().getAddress(this.fromUUID);
    }

    public void setToWalletAsServer() {
        this.toWallet = IntegrationAPI.getInstance().getServerWallet();
    }

    public void complete() {
        if(biFunction != null) {
            this.getBiFunction().accept(this.fromUUID, this.toUUID);
            
        } else if(function != null) {
            this.getFunction().accept(this.fromUUID);
        }
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