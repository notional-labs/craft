package com.crafteconomy.blockchain.wallets;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    // [NOTE] Only admins should use this command. Players will do it via the connection page.
    // This is soley to ensure that admins can easily test accounts via public bech32 addresses.
    
    private MongoDatabase db = null;

    private Map<UUID, String> walletCache = new HashMap<UUID, String>();

    private static WalletManager instance;  

    private static String walletPrefix = CraftBlockchainPlugin.getInstance().getWalletPrefix();
    private static int walletLength = CraftBlockchainPlugin.getInstance().getWalletLength();    
    
    private WalletManager() { 
        db = CraftBlockchainPlugin.getInstance().getMongo().getDatabase();       
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

    public static boolean isValidWallet(String walletAddress) {
        return walletAddress.length() == walletLength && walletAddress.startsWith(walletPrefix);
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
        Bson filter = Filters.eq("minecraftId", uuid.toString());
        Document doc = getCollection().find(filter).first();

        if(doc != null) {
            getCollection().updateOne(filter, Updates.set("keplrId", wallet));
        } else {
            doc = new Document("minecraftId", uuid.toString());
            doc.append("keplrId", wallet);
            getCollection().insertOne(doc);
        }
    }

    public String getAddressFromDatabase(UUID uuid) {
        Bson filter = Filters.eq("minecraftId", uuid.toString());
        Document doc = getCollection().find(filter).first();

        if(doc != null) { 
            Object wallet = doc.get("keplrId");
            if(wallet != null){
                return (String) wallet;
            } 
        }
        return null;        
    }

    public Optional<UUID> getUUIDFromWallet(String wallet) {
        Bson filter = Filters.eq("keplrId", wallet);
        Document doc = getCollection().find(filter).first();

        if(doc != null) { 
            Object uuid = doc.get("minecraftId");
            if(uuid != null){
                String myUUID = (String) uuid;
                return Optional.of(UUID.fromString(myUUID));                
            } 
        }        
        return Optional.empty();        
    }

    private MongoCollection<Document> getCollection() {
        // This is where all the connections are stored w/ the webapp
        return db.getCollection("connections");
    }

    public static WalletManager getInstance() {
        if(instance == null) {
            instance = new WalletManager();
        }
        return instance;
    }
}
