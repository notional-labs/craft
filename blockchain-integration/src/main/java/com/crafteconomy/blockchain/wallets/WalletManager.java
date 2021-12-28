package com.crafteconomy.blockchain.wallets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class WalletManager {
    
    private MongoDatabase db = CraftBlockchainPlugin.getInstance().getMongo().getDatabase();

    private Map<UUID, String> walletCache = new HashMap<UUID, String>();

    private static WalletManager instance;  
    
    public WalletManager() {
        instance = this;
    }

    public static WalletManager getInstance() {
        return instance;
    }

    public void cacheWalletOnJoin(UUID uuid) {
        String wallet = getAddress(uuid);

        if(wallet != null){
            addToCache(uuid, wallet);
        }        
    }

    // Cache Functions
    public void setAddress(UUID uuid, String wallet){
        walletCache.put(uuid, wallet);
        setAddressToDatabase(uuid, wallet);
    }

    private void addToCache(UUID uuid, String wallet){
        walletCache.put(uuid, wallet);
    }
    
    public String getAddress(UUID uuid) {
        String wallet = walletCache.get(uuid);

        if(wallet == null) {
            wallet = getAddressFromDatabase(uuid);
        }

        return wallet;
    }

    /**
     * Get the wallet address from a username.
     * (Gets offline cache via paper cache, checks DB/Cache for wallet)
     * 
     * @param username
     * @return String Wallet
     */
    public String getAddressFromName(String username) {    
        String wallet = null;
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(username);
        
        if(player != null) {
            wallet = getAddress(player.getUniqueId());
        }

        return wallet;        
    }

    public void removeFromCache(UUID uuid) {
        walletCache.remove(uuid);
    }

    // -= Database Functions =-
    public void setAddressToDatabase(UUID uuid, String wallet) {
        Bson filter = Filters.eq("uuid", uuid.toString());
        Document doc = getCollection().find(filter).first();

        if(doc != null) {
            getCollection().updateOne(filter, Updates.set("address", wallet));
        } else {
            doc = new Document("uuid", uuid.toString());
            doc.append("address", wallet);
            getCollection().insertOne(doc);
        }
    }

    public String getAddressFromDatabase(UUID uuid) {
        Bson filter = Filters.eq("uuid", uuid.toString());
        Document doc = getCollection().find(filter).first();

        if(doc != null) { 
            // Util.log("[WalletManager] doc: " + doc.toJson());
            Object wallet = doc.get("address");
            if(wallet != null){
                return (String) wallet;
            } 
        }

        return null;        
    }

    private MongoCollection<Document> getCollection() {
        return db.getCollection("wallets");
    }
}
