package com.crafteconomy.blockchain.escrow;

import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.transactions.Tx;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EscrowManager {

    // TODO: UNTESTED
    
    private MongoDatabase db = null;

    private static EscrowManager instance;  
    private IntegrationAPI api = null;

    private EscrowManager() { 
        db = CraftBlockchainPlugin.getInstance().getMongo().getDatabase(); 
        api = IntegrationAPI.getInstance();      
    }

    public Long getEscrowBalance(UUID uuid) {
        return getBalanceFromDatabase(uuid);
    }

    public EscrowErrors depositEscrow(UUID uuid, long amount) {
        // pay DAO the balance, the deposit fake tokens into account
        if(!doesPlayerHaveEnoughFundsForEscrow(uuid, amount)) {
            return EscrowErrors.NOT_ENOUGH_CRAFT_FUNDS;
        }
        Tx tx = api.createServerTx(uuid, amount, "ESCROWING " + amount + "FOR " + uuid.toString(), EscrowLogic.depositEscrow(uuid, amount));
        tx.submit(true, true, true);
        return EscrowErrors.SUCCESS;
    }

    
    /**
     * Removes escrow balance & deposits CRAFT into their wallet
     * TODO: UNTESTED SINCE TESTING IS ON OSMOSIS!!!
     * @param uuid
     * @param amount
     * @return False if failed, true if deposited
     */
    public EscrowErrors redeemEscrow(UUID uuid, long amount) {
        // not enough escrow balance to withdraw this much
        // long escrowBal = getBalanceFromDatabase(uuid); // done below
        // if(escrowBal < amount) { return EscrowErrors.NO_ENOUGH_ESCROW_BALANCE; }

        String wallet = api.getWallet(uuid);
        if(wallet == null) { return EscrowErrors.NO_WALLET; }

        // if player has 1 escrow, but wants to withdraw 10. We know the max they can withdraw is 1, so we only withdraw the 1 they have
        amount = Math.min(getBalanceFromDatabase(uuid), amount);        
        removeEscrow(uuid, amount);

        // deposit into wallet failed        
        String returnValue = api.deposit(wallet, amount);
        if(returnValue == null) { return EscrowErrors.FAUCET_DEPOSIT_ERROR; }            

        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            player.sendMessage("You have redeemed " + amount + CraftBlockchainPlugin.getInstance().getTokenDenom(false) + " from your escrow account -> wallet.");
            player.sendMessage("Your new escrow balance is: " + getBalanceFromDatabase(uuid));
        }        
        return EscrowErrors.SUCCESS;
    }
    
    // TODO: Negative balance checking
    public void removeEscrow(UUID uuid, long amount) {
        if(amount > 0) { amount = -amount; }
        changeBalance(uuid, amount);
    }

    private boolean doesPlayerHaveEnoughFundsForEscrow(UUID uuid, long amount) {
        long currentBalance = IntegrationAPI.getInstance().getBalance(uuid);
        if(currentBalance < amount) {
            return false;
        }
        return true;
    }

    public void changeBalance(UUID uuid, long amount) {
        // increase or decrease balance
        long currentBalance = getBalanceFromDatabase(uuid);
        setBalanceToDatabase(uuid, currentBalance+amount);
    }

    // -= Database Functions =-
    private void setBalanceToDatabase(UUID uuid, long amount) {
        Document doc = getUsersDocument(uuid);
        if(doc != null) {
            getCollection().updateOne(Filters.eq("_id", uuid.toString()), Updates.set("amount", amount));
        } else {
            doc = new Document("_id", uuid.toString());
            doc.append("amount", amount);
            getCollection().insertOne(doc);
        }
    }

    private Long getBalanceFromDatabase(UUID uuid) {
        Document doc = getUsersDocument(uuid);
        if(doc == null) { return 0L; } 
        
        Object escrowAmount = doc.get("amount");
        if(escrowAmount != null){
            return (long) escrowAmount;
        } 
        return 0L;       
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
}
