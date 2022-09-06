package com.crafteconomy.blockchain.commands.wallet.subcommands;

import java.util.UUID;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WalletClearPending implements SubCommand {

    WalletManager walletManager = WalletManager.getInstance();
     

    @Override
    public void onCommand(CommandSender sender, String[] args) {        
        UUID uuid = null;         

        // set UUID to user by default to current player querying request
        if(sender instanceof Player) {
            Player player = (Player) sender;
            uuid = player.getUniqueId();            
        } else {
            Util.colorMsg(sender, "&cYou must be a player to use this command.");
            return;
        }

        
        String wallet = walletManager.getAddress(uuid);
        if(wallet == null) {            
            Util.colorMsg(sender, "&cYou do not have a wallet yet. You must set one first.");
            return;
        }

        PendingTransactions.clearTransactionsFromWallet(wallet);
        Util.colorMsg(sender, "&aSuccessfully cleared all your pending transactions.");  
    }          
}
