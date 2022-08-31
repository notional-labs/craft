package com.crafteconomy.blockchain.escrow;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.request.Caches;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.FaucetTypes;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;
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

    // This collection saves as ucraft -> the collection.
    // TODO: make sure this logic is correct with saving & spending in ucraft.

    private MongoDatabase db = null;

    private static EscrowManager instance;
    private WalletManager walletManager;
    private IntegrationAPI api = null;
    private RequestTypes escrow = RequestTypes.ESCROW;

    public void unloadCachedPlayer(UUID uuid) {
        Caches.invalidate(escrow, uuid);
    }

    public void loadCachedPlayer(UUID uuid) {
        Caches.put(escrow, uuid, getUCraftBalance(uuid));
    }

    private EscrowManager() {
        db = CraftBlockchainPlugin.getInstance().getMongo().getDatabase();
        api = IntegrationAPI.getInstance();
        walletManager = WalletManager.getInstance();
    }

    public long getUCraftBalance(UUID uuid) {
        // get cached value
        Object value = Caches.getIfPresent(escrow, uuid);
        if (value != null) {
            return (long) value;
        }

        // cache time ran out, get document if any & cache current value
        Document doc = getUsersDocument(uuid);
        if (doc == null) {
            Caches.put(escrow, uuid, 0L);
            return 0L;
        }

        Object escrowAmount = doc.get("ucraft_amount");
        if (escrowAmount != null) {
            long amt = (long) escrowAmount;
            Caches.put(escrow, uuid, amt);
            return amt;
        }
        return 0L;
    }

    public float getCraftBalance(UUID uuid) {
        return (float) getUCraftBalance(uuid) / 1_000_000;
    }

    public EscrowErrors depositUCraft(UUID uuid, long ucraft_amount) {
        // Get the most the user can withdraw from their chain ucraft wallet or what
        // they want, which would be the minimum of the 2
        try {
            ucraft_amount = Math.min(api.getUCraftBalance(uuid).get(), ucraft_amount);
        } catch (InterruptedException | ExecutionException e) {            
            e.printStackTrace();
        }

        Tx tx = new Tx();
        tx.setFromUUID(uuid);        
        tx.setToWalletAsEscrowRestAPIAccount(); // pays the escrow account (cosmjs in the API)
        tx.setUCraftAmount(ucraft_amount);
        tx.setDescription("ESCROWING " + ucraft_amount / 1_000_000 + "craft (" + ucraft_amount + "ucraft) FOR "
                + uuid.toString());
        tx.setFunction(depositEscrowLogic(uuid, ucraft_amount));

        // tx = tx.sendDescription().sendTxIDClickable().sendWebappLink();
        tx = tx.sendTxIDClickable().sendWebappLink();
        tx.submit();

        return EscrowErrors.SUCCESS;
    }

    /**
     * Redeems from in game held CRAFT balance to their actual CRAFT wallet address
     * 
     * @param uuid
     * @param redeemAmt
     * @return Amount they redeemed, or -1 if they do not have a wallet
     */
    public long redeemUCraft(UUID uuid, long ucraft_amount) {
        String wallet = api.getWallet(uuid);
        if (wallet == null) { return ErrorTypes.NO_WALLET.code; }

        ucraft_amount = Math.abs(ucraft_amount);

        // max withdraw is the redeemAmt OR total amount in their wallet, which ever is less
        long mostTheyCanRedeemUCraft = Math.min(getUCraftBalance(uuid), ucraft_amount);        

        final String description = "Escrow redeem via Escrow Manager (Craft Integration) for " + mostTheyCanRedeemUCraft/1_000_000 + "craft.";

        // We make them sign a transaction for 1ucraft to confirm they are themselfs to redeem & launch the redeem process from the chain
        Tx tx = new Tx();
        tx.setFromUUID(uuid);
        tx.setToUUID(uuid); // sending to themself
        tx.setUCraftAmount(1);
        tx.setDescription(description + " from " + uuid.toString());                
        tx.setFunction((user_uuid) -> {
            // TODO: This needs cleanup to only remove craft from escrow IF payment was successful.
            // ^ if payment fails, it currently saves that to DB. Which would = double withdraw.
            removeUCraftBalance(uuid, mostTheyCanRedeemUCraft);
            CraftBlockchainPlugin.log(description);

            BlockchainRequest.depositUCraftToAddress(walletManager.getAddress(uuid), description, mostTheyCanRedeemUCraft).thenAccept(status_type -> {
                Player player = Bukkit.getPlayer(uuid);

                String messages = "";
                if (status_type == FaucetTypes.SUCCESS) {
                    

                    String amt = (mostTheyCanRedeemUCraft/1_000_000) + "craft";
                    messages = "&aYou have redeemed &f" + amt + "&a from your escrow account -> wallet.\n";
                    messages += "&f&oYour new escrow balance is: &f&n" + getCraftBalance(uuid);
                } else {
                    // Since the faucet already post the error now, not required here.
                    // messages = "\n\n\n\n&6Escrow Error: "+status_type.toString()+"(Ignore above messages)\n&7 - &f Your balance is still here in escrow, please try again later.\n";                                
                }

                if(player != null) {                
                    Util.colorMsg(player, messages);
                }
            });
        });

        tx = tx.sendTxIDClickable().sendWebappLink();         
        tx.submit();
        
        // notify user to sign tx to get their payment
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {            
            Util.colorMsg(player, "&7You have requested &f" + ucraft_amount/1_000_000 + " craft &7tokens to your wallet from escrow");
            Util.colorMsg(player, "&7&o((Please sign the above transaction to process this requests))\n");
            if(mostTheyCanRedeemUCraft < ucraft_amount) {
                Util.colorMsg(player, "&6[!] &eNOTE&7: &fThis was less than you requested "+ucraft_amount/1000000+", since you only had " + mostTheyCanRedeemUCraft/1_000_000 + " in escrow");
            }
        }
                   
        return mostTheyCanRedeemUCraft;
    }

    public long redeemCraft(UUID uuid, float craft_amount) {
        return redeemUCraft(uuid, (long) (craft_amount * 1_000_000));
    }

    private void setBalance(UUID uuid, long newBalance) {
        Document doc = getUsersDocument(uuid);
        if (doc != null) {
            getCollection().updateOne(Filters.eq("_id", uuid.toString()), Updates.set("ucraft_amount", newBalance));
        } else {
            doc = new Document("_id", uuid.toString());
            doc.append("ucraft_amount", newBalance);
            getCollection().insertOne(doc);
        }
        Caches.put(escrow, uuid, newBalance);
    }

    public EscrowErrors spendUCraft(UUID uuid, long ucraft_cost) {
        // player not enough escrow.
        if (getUCraftBalance(uuid) < ucraft_cost) {
            return EscrowErrors.INSUFFICIENT_FUNDS;
        }
        removeUCraftBalance(uuid, ucraft_cost);
        return EscrowErrors.SUCCESS;
    }

    public EscrowErrors spendCraft(UUID uuid, float craft_cost) {
        return spendUCraft(uuid, (long) (craft_cost * 1_000_000));
    }

    public EscrowErrors escrowPayPlayerUCraft(UUID from_uuid, UUID to_uuid, long ucraft_cost) {
        // player not enough escrow.
        if (getUCraftBalance(from_uuid) < ucraft_cost) {
            return EscrowErrors.INSUFFICIENT_FUNDS;
        }

        removeUCraftBalance(from_uuid, ucraft_cost);
        addUCraftBalance(to_uuid, ucraft_cost);
        return EscrowErrors.SUCCESS;
    }

    public EscrowErrors escrowPayPlayerCraft(UUID from_uuid, UUID to_uuid, float craft_cost) {
        return escrowPayPlayerUCraft(from_uuid, to_uuid, (long) (craft_cost * 1_000_000));
    }

    private Document getUsersDocument(UUID uuid) {
        Bson filter = Filters.eq("_id", uuid.toString());
        return getCollection().find(filter).first();
    }

    private MongoCollection<Document> getCollection() {
        return db.getCollection("escrow");
    }

    public static EscrowManager getInstance() {
        if (instance == null) {
            instance = new EscrowManager();
        }
        return instance;
    }

    /**
     * On successful signing, this function will be called to update the balance of
     * the player
     * 
     * @param player_uuid
     * @param amount
     * @return Consumer<UUID>
     */
    private static Consumer<UUID> depositEscrowLogic(UUID player_uuid, long ucraft_amount) {
        Consumer<UUID> deposit = (uuid) -> {
            instance.addUCraftBalance(uuid, ucraft_amount);

            Player player = Bukkit.getPlayer(player_uuid);
            if (player != null) {
                player.sendMessage(
                        "You have deposited " + ucraft_amount / 1_000_000 + "craft into your escrow account.");
            }
        };
        return deposit;
    }

    private void addUCraftBalance(UUID uuid, long addUCraftAmount) {
        setBalance(uuid, getUCraftBalance(uuid) + addUCraftAmount);
    }

    private void removeUCraftBalance(UUID uuid, long removeUCraftAmount) {
        // "Adds" negative absolute value amount (ensures we don't add via this func)
        addUCraftBalance(uuid, -Math.abs(removeUCraftAmount));
    }

    // private void addCraftBalance(UUID uuid, long addCraftAmount) {
    // addUCraftBalance(uuid, (long) (addCraftAmount * 1_000_000));
    // }
    // private void removeCraftBalance(UUID uuid, long removeCraftAmount) {
    // removeUCraftBalance(uuid, (long) (removeCraftAmount * 1_000_000));
    // }
}
