package com.crafteconomy.blockchain.commands.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;

// TODO: Console only in the future when Live

public class WalletFaucet implements SubCommand {

    WalletManager walletManager = WalletManager.getInstance();

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        // if(!(sender instanceof ConsoleCommandSender)) {
        //     Util.colorMsg(sender, "&cOnly console can use this command!");
        //     return;
        // }

        String wallet = null;
        long amount = 0;

        if(args.length >= 3) {            
            wallet = args[1];

            if(!wallet.startsWith("craft")) {
                // not a wallet, check if it is a user. if so, get their wallet
                wallet = walletManager.getAddressFromName(args[1]);

                if(wallet == null) {
                    Util.colorMsg(sender, "&cInvalid wallet / player:  " + args[1]);
                    return;
                }
            }

            try {
                amount = Long.parseLong(args[2]);
                if(amount <= 0) { return; }
            } catch (Exception e) {
                Util.colorMsg(sender, "&cInvalid amount " + args[2]);
                return;
            }
        } else {
            Util.colorMsg(sender, "&cInvalid usage. &f&l/wallet faucet <wallet> <amount>");
            return;
        }
        

        if(wallet == null || wallet.length() != 44 || !wallet.startsWith("craft")) {
            Util.colorMsg(sender, "&cInvalid wallet address " + wallet + " ( length " + wallet.length() + " )");
            return;
        }

        // used only for outputs
        String reducedWallet = wallet.substring(0, 25) + "...";

        Util.colorMsg(sender, "&f&o[!] Faucet request sent for " + reducedWallet);   
        
        String html = BlockchainRequest.depositToAddress(wallet, amount);
        if(html == null) {
            Util.colorMsg(sender, "&c&lERROR: &cCraftd node is not running, please contact a staff.");
            return;
        }

        String output = "&aFauceted &f" + amount + "craft to &a" + reducedWallet;
        if(html.contains("error")) {
            output = "&c&lERROR: &c" + html;
        } 

        Util.colorMsg(sender, output);
        // Util.clickableWallet(sender, wallet, "&7&oTheir Wallet: &n%wallet%"); 
        
    }
    
}
