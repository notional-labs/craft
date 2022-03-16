package com.crafteconomy.blockchain.escrow;

import java.util.UUID;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.request.Caches;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.wallets.WalletManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EscrowManager {
    
    private MongoDatabase db = null;

    private static EscrowManager instance;  
    private WalletManager walletManager;
    private IntegrationAPI api = null;
    private RequestTypes escrow = RequestTypes.ESCROW;

    // TODO: 
    public void unloadCachedPlayer(UUID uuid) {
        Caches.invalidate(escrow, uuid);
    }
    
    public void loadCachedPlayer(UUID uuid) {
        Caches.put(escrow, uuid, getBalance(uuid));
    }

    private EscrowManager() { 
        db = CraftBlockchainPlugin.getInstance().getMongo().getDatabase(); 
        api = IntegrationAPI.getInstance();    
        walletManager = WalletManager.getInstance();  
    }

    public long getBalance(UUID uuid) {
        // get cached value
        Object value = Caches.getIfPresent(escrow, uuid);
        if (value != null) {
            return (long) value;
        }

        // cache time ran out, get document if any & cache current value
        Document doc = getUsersDocument(uuid);
        if(doc == null) { 
            Caches.put(escrow, uuid, 0L);
            return 0L; 
        } 

        Object escrowAmount = doc.get("amount");
        if(escrowAmount != null){
            long amt = (long) escrowAmount;
            Caches.put(escrow, uuid, amt);
            return amt;
        } 
        return 0L; 
    }
    
    public EscrowErrors deposit(UUID uuid, long amount) {
        // Get the most the user can withdraw from CRAFT wallet or what they want, which would be the minimum of the 2
        amount = Math.min(api.getBalance(uuid), amount);

        Tx tx = api.createServerTx(uuid, amount, "ESCROWING " + amount + "FOR " + uuid.toString(), depositEscrowLogic(uuid, amount));
        tx.submit(true, true, true); // submit & send TxClickable, Description, & WebappLink
        return EscrowErrors.SUCCESS;
    }

    /**
     * Redeems from in game held CRAFT balance to their actual CRAFT wallet address
     * @param uuid
     * @param redeemAmt
     * @return Amount they redeemed, or -1 if they do not have a wallet
     */
    public long redeem(UUID uuid, long redeemAmt) {
        String wallet = api.getWallet(uuid);
        if(wallet == null) { 
            return ErrorTypes.NO_WALLET.code; 
        }

        redeemAmt = Math.abs(redeemAmt); // only positive redeems
            
        // max withdraw is the redeemAmt OR total amount in their wallet, which ever is less
        long mostTheyCanRedeem = Math.min(getBalance(uuid), redeemAmt);
        removeBalance(uuid, mostTheyCanRedeem);
        System.out.println("Redeeming " + mostTheyCanRedeem + " from in game -> wallet via deposit");
        // deposits the tokens to their actual wallet
        // api.faucet();
        BlockchainRequest.depositToAddress(walletManager.getAddress(uuid), mostTheyCanRedeem);

        Player player = Bukkit.getPlayer(uuid);
            if(player != null) {
                player.sendMessage("You have redeemed " + redeemAmt + CraftBlockchainPlugin.getInstance().getTokenDenom(false) + " from your escrow account -> wallet.");
                player.sendMessage("Your new escrow balance is: " + getBalance(uuid));
            }  

        return mostTheyCanRedeem; 
    }

    private void setBalance(UUID uuid, long newBalance) {
        // TODO: Move MongoDB logic only on close, call from onDisable() for all online
        Document doc = getUsersDocument(uuid);
        if(doc != null) {
            getCollection().updateOne(Filters.eq("_id", uuid.toString()), Updates.set("amount", newBalance));
        } else {
            doc = new Document("_id", uuid.toString());
            doc.append("amount", newBalance);
            getCollection().insertOne(doc);
        }
        Caches.put(escrow, uuid, newBalance);
    }

    public EscrowErrors spend(UUID uuid, long cost) {
        // player not enough escrow.
        if(getBalance(uuid) < cost) {
            return EscrowErrors.INSUFFICIENT_FUNDS;
        }
        removeBalance(uuid, cost);
        return EscrowErrors.SUCCESS;
    }

    

    private Document getUsersDocument(UUID uuid) {
        Bson filter = Filters.eq("_id", uuid.toString());
        return getCollection().find(filter).first();
    }

    private MongoCollection<Document> getCollection() {
        return db.getCollection("escrow");
    }

    public static EscrowManager getInstance() {
        if(instance == null) {
            instance = new EscrowManager();
        }
        return instance;
    }


    /**
     * On successful signing, this function will be called to update the balance of the player
     * @param player_uuid
     * @param amount
     * @return Consumer<UUID>
     */
    private static Consumer<UUID> depositEscrowLogic(UUID player_uuid, long amount) {        
        Consumer<UUID> deposit = (uuid) -> {  
            instance.addBalance(uuid, amount);

            Player player = Bukkit.getPlayer(player_uuid);
            if(player != null) {
                player.sendMessage("You have deposited " + amount + "CRAFT into your escrow account.");
            }        
        };
        return deposit;
    }  


    private void addBalance(UUID uuid, long addAmount) {
        setBalance(uuid, getBalance(uuid) + addAmount);
    }
    private void removeBalance(UUID uuid, long removeAmount) {
        // "Adds" negative absolute value amount (ensures we don't add via this func)
        addBalance(uuid, -Math.abs(removeAmount));
    }


}
