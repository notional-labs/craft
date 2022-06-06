package com.crafteconomy.blockchain.transactions;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.TransactionType;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString 
public class Tx implements Serializable {

    private static IntegrationAPI api = IntegrationAPI.getInstance();
    private static WalletManager walletManager = WalletManager.getInstance();
    
    private UUID fromUUID;
    private UUID toUUID; // for BiConsumer only

    private UUID TxID;
    private String description;

    private TransactionType txType = TransactionType.DEFAULT; // used for webapp

    private Consumer<UUID> function = null;
    private BiConsumer<UUID, UUID> biFunction = null;

    private String toWallet;
    private long amount;

    // used when submitting a tx. Done like a builder
    // Tx tx = api.createServerTx(uuid, amount, "ESCROWING " + amount + "FOR " + uuid.toString(), depositEscrowLogic(uuid, amount));        
    // tx = tx.sendDescription().sendTxIDClickable().sendWebappLink();
    // tx.submit();
    private boolean includeTxClickable = false;
    private boolean sendDescMessage = false;
    private boolean sendWebappLink = false;

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

    public Tx setDescription(String description) {
        // By doing this, we can ensure a compromised webapp attacker can't reuse the same TxHash to match a similar Tx description
        // Read about it in docs/README.md -> Security Considerations
        this.description = description + " time_" + System.currentTimeMillis();
        return this;
    }

    public String getFromWallet() {
        return walletManager.getAddress(this.fromUUID);
    }

    public void setToWalletAsServer() {
        this.toWallet = api.getServerWallet();
    }

    public Double getTotalTaxAmount() {
        return api.getTaxRate() * this.amount;
    }

    public void complete() {
        if(biFunction != null) {
            this.getBiFunction().accept(this.fromUUID, this.toUUID);
            
        } else if(function != null) {
            this.getFunction().accept(this.fromUUID);
        }
    }

    // TODO Does this mess with the @getter and setter stuff? or override
    public Tx setTxType(TransactionType txType) {
        // sets the type of transaction for the webapp to better sort each ID.
        // (optional)
        this.txType = txType;
        return this;
    }
    public TransactionType getTxType() {
        return this.txType;
    }

    public Tx sendTxIDClickable() {
        includeTxClickable = true;
        return this;
    }
    public Tx sendDescription() {
        sendDescMessage = true;
        return this;
    }
    public Tx sendWebappLink() {
        sendWebappLink = true;
        return this;
    }

    /**
     * Submit the transaction to redis for the webapp to sign & send link to sign it
     */
    public ErrorTypes submit() {
        ErrorTypes returnType = api.submit(this);
        if(returnType == ErrorTypes.NO_ERROR) {
            Player player = Bukkit.getPlayer(this.fromUUID);
            if(player != null) {
                if(includeTxClickable) {
                    api.sendTxIDClickable(player, this.TxID.toString());
                }
                if(sendDescMessage) {
                    player.sendMessage(this.getDescription());
                }    
                if(sendWebappLink) {
                    api.sendWebappForSigning(player);
                }                
            }            
        }
        return returnType;
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