package sh.reece.testingintegration.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import sh.reece.testingintegration.Logic;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// Shows how P2P should work [when blockchain is required in transaction]

// !If the server holds items for the pending transaction, make sure to set a reasonable time limit & logic for if the user doesn't sign
// !IMPORTANT: View example in ExpiredTransaction.java

// Only 1 user should sign (the 'net' sender of craft coins)
// Ex: Reece trades 1 craft + 1 diamond to Chalabi
// Chalabi trades 2 craft & 1 dirt to Reece.
// This means net, Chalabi sends 1 craft to reece, so he is the signer of the Tx.
// If net = 0, no signing is needed the transaction can just happen.

public class TradeCommand implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    private Map<UUID, Tx> pendingTradeRequest = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    
        
        Player from = (Player) sender;

        if(args.length == 1) {
            // test-trade accept
            if(args[0].equalsIgnoreCase("accept")) {
                if(!pendingTradeRequest.containsKey(from.getUniqueId())) {
                    Util.colorMsg(from, "You do not have any pending trade request...");
                    return true;
                }
                // checks already made, so just actually do the trade here + Tx
                acceptTrade(from.getUniqueId());
                return true;
            } 

        } 

        if(args.length != 2) {
            Util.colorMsg(sender, "&7&o(( Trades item in hands + sends CRAFT from sender to receiver ))");
            Util.colorMsg(sender, "Usage: /test-trade <player> <amount>");
            Util.colorMsg(sender, "Usage: /test-trade accept");
            return true;
        }
        
        Player receiver = Bukkit.getPlayer(args[0]);
        if(receiver == null) {
            Util.colorMsg(sender, "&c&oPlayer not found to trade with");
            return true;
        }

        // Checks to be sure both users have wallets
        UUID fromUUID = from.getUniqueId();
        String fromWallet = api.getWallet(fromUUID);

        UUID receiverUUID = receiver.getUniqueId();        
        String toWallet = api.getWallet(receiverUUID);

        if(fromWallet == null) {
            Util.colorMsg(sender, "&c&oYou do not have an account");
            return true;
        }

        if(toWallet == null) {
            Util.colorMsg(sender, "&c&oPlayer does not have an account");
            return true;
        }

        // Check to ensure user has enough CRAFT to actually send that much
        float craft_amount = Float.parseFloat(args[1]);
        if(api.getCraftBalance(from.getUniqueId()) < craft_amount) {
            Util.colorMsg(sender, "&c&oYou do not have enough CRAFT");
            return true;
        }

        // [!] save backup of those items incase they log off / server goes down, etc
        
        // Create unfinished template for Transaction (Since we will not know items trading OR description until user accepts)
        Tx txInfo = api.createNewTx(fromUUID, receiverUUID, toWallet, craft_amount, null, null);

        // Saves to others UUID for when they /trade accept, real projects would do /trade accept <user>
        pendingTradeRequest.put(receiverUUID, txInfo);

        // Inform users the trade request was sent & how to accept it
        Util.colorMsg(sender, "&2[!] &aSent trade request to &e&o" + receiver.getName());        
        Util.colorMsg(receiver, "\n&6[!] &eYou have been sent a trading request by " + from.getName());  
        Util.colorMsg(receiver, "&7&o(( /test-trade accept ))"); 
        return true; 
    }

    // run when the other user accepts the trade request
    // pushes pending
    private void acceptTrade(UUID playerWhoAcceptedTrade) {
        // get the unfinished transaction (created in method above)
        Tx tx = pendingTradeRequest.get(playerWhoAcceptedTrade);


        Player from = Bukkit.getPlayer(tx.getFromUUID());
        Player receiver = Bukkit.getPlayer(playerWhoAcceptedTrade);
        String toWallet = api.getWallet(playerWhoAcceptedTrade);

        // just get the first item in each inventory (real would be a chest)
        ItemStack p1Item = from.getInventory().getItemInMainHand().clone();
        ItemStack p2Item = receiver.getInventory().getItemInMainHand().clone();

        // remove items from each & update inventory
        from.getInventory().setItemInMainHand(null);
        from.updateInventory();
        receiver.getInventory().setItemInMainHand(null);
        receiver.updateInventory();
        
        // set values for transaction now that we know the user accepted the trade
        tx.setDescription("Traded " + p1Item.getType() + " +" + tx.getCraftAmount() + "craft FOR " + receiver.getName() + "'s " + p2Item.getType());
        tx.setBiFunction(Logic.trade(tx.getFromUUID(), playerWhoAcceptedTrade, p1Item, p2Item, tx.getDescription()));
        tx.setToWallet(toWallet);

        // submit transaction to wait and be signed from the initiator
        api.submit(tx);


        // Notify both users the initiator needs to sign the transaction
        Util.colorMsg(from, "\n&6[!] &eTrade Successful! Just need to sign it now:");
        api.sendTxIDClickable((CommandSender) from, tx.getTxID().toString());
        api.sendWebappForSigning((CommandSender) from, api.getWallet(from.getUniqueId()));
        from.sendMessage("");

        Util.colorMsg(receiver, "&e&oTransaction created, waiting for " + from.getName() + " to sign it");
        
    }
    
}
