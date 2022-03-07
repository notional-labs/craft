package com.crafteconomy.blockchain.commands.wallet.subcommands;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WalletSet implements SubCommand {
    // wallet set <craft-wallet-address>

    WalletManager walletManager = WalletManager.getInstance();    

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if(!(sender instanceof Player)) {
            Util.colorMsg(sender, "&cOnly players can use this command!");
            return; 
        }

        if(args.length != 2) {
            Util.colorMsg(sender, "&cUsage: &f/wallet set <craft-wallet>");
            Util.clickableWebsite(sender, "https://docs.crafteconomy.io/set-up/wallet", 
                "&2[!] &a&nClick here to learn how to set up your wallet.",
                "&7&oView the crafteconomy documentation"    
            );  
            

            return;       
        } 

        // gets last argument which is the craft wallet address
        String newWallet = args[1]; 

        if(!WalletManager.isValidWallet(newWallet)) {
            Util.colorMsg(sender, "&cInvalid wallet address " + newWallet + " ( length " + newWallet.length() + " )");
            return;
        } 
        
        setWallet(sender, newWallet);                                
    }

    

    private void setWallet(CommandSender sender, String wallet) {
        walletManager.setAddress(((Player) sender).getUniqueId(), wallet);
        Util.clickableCopy(sender, wallet, "&fWallet set to: &n%value%", "&7&oClick to copy wallet address");
    }
    
}
