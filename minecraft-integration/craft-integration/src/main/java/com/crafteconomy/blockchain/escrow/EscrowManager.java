package com.crafteconomy.blockchain.escrow;

import java.util.UUID;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.request.Caches;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.FaucetTypes;
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
        ucraft_amount = Math.min(api.getUCraftBalance(uuid), ucraft_amount);

        Tx tx = new Tx();
        tx.setFromUUID(uuid);
        tx.setToWalletAsServer();
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
        if (wallet == null) {
            return ErrorTypes.NO_WALLET.code;
        }

        ucraft_amount = Math.abs(ucraft_amount);

        // max withdraw is the redeemAmt OR total amount in their wallet, which ever is less
        long mostTheyCanRedeemUCraft = Math.min(getUCraftBalance(uuid), ucraft_amount);
        removeUCraftBalance(uuid, mostTheyCanRedeemUCraft);
        CraftBlockchainPlugin.log("Redeeming " + (mostTheyCanRedeemUCraft / 1_000_000) + "craft from in game -> wallet via deposit");
        
        // deposits the tokens to their actual wallet
        final String description = "Escrow redeem via EscrowManager.java (Integration)";
        
        BlockchainRequest.depositUCraftToAddress(walletManager.getAddress(uuid), description, mostTheyCanRedeemUCraft).thenAccept(status_type -> {
            Player player = Bukkit.getPlayer(uuid);

            String messages = "";
            if (status_type == FaucetTypes.SUCCESS) {
                String amt = (mostTheyCanRedeemUCraft/1_000_000) + "craft";
                messages = "You have redeemed " + amt + " from your escrow account -> wallet.\n";
                messages += "Your new escrow balance is: " + getCraftBalance(uuid);
            } else {
                messages = "An error occurred while redeeming your escrow account -> wallet.\n";
                messages += "Error: " + status_type.toString();
            }

            if(player != null) {
                player.sendMessage(messages);
            }
        });
            
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
