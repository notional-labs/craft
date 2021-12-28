package com.crafteconomy.blockchain.commands.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// set wallet

public class WalletSet implements SubCommand {

    WalletManager walletManager = WalletManager.getInstance();
    
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if(args.length != 2) {
            Util.colorMsg(sender, "&cInvalid usage. &f&l/wallet set <craft-wallet>");
            return;
        }

        if(!(sender instanceof Player)) {
            Util.colorMsg(sender, "&cOnly players can use this command!");
            return; 
        }

        String newWallet = args[1];

        if(newWallet.length() != 44 || !newWallet.startsWith("craft")) {
            Util.colorMsg(sender, "&cInvalid wallet address " + newWallet + " ( length " + newWallet.length() + " )");
            return;
        } 

        Player player = (Player) sender;
        
        walletManager.setAddress(player.getUniqueId(), newWallet);
        Util.clickableWallet(sender, newWallet, "&fWallet set to: &n%wallet%");
                
    }
    
}
