package com.crafteconomy.blockchain.commands.wallet.subcommands;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;

// TODO: Remove when live

public class WalletFaucet implements SubCommand {

    WalletManager walletManager = WalletManager.getInstance();
    int requiredWalletLength = CraftBlockchainPlugin.getInstance().getWalletLength();

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        String wallet = null;
        long amount = 0;

        if(args.length < 3) {
            Util.colorMsg(sender, "&cInvalid usage. &f&l/wallet faucet <wallet> <amount>");
            return;
        }
               
        wallet = args[1];

        // If they are requesting to give to a player
        if(!wallet.startsWith("craft")) {
            // not a wallet, check if it is a user. if so, get their wallet
            wallet = walletManager.getAddressFromName(args[1]);

            if(wallet == null) {
                Util.colorMsg(sender, "&cInvalid wallet / player:  " + args[1]);
                return;
            }
        }

        if(wallet == null || wallet.length() != requiredWalletLength) {
            Util.colorMsg(sender, "&cInvalid wallet address " + wallet + " ( length " + wallet.length() + " )");
            return;
        }

        try {
            amount = Long.parseLong(args[2]);
            if(amount <= 0) { return; }

            if(amount > CraftBlockchainPlugin.MAX_FAUCET_AMOUNT) {
                Util.logSevere("\n\n&cConsole just tried to faucet an amount too high: " + amount + "\n\n");
                return;
            }

        } catch (Exception e) {
            Util.colorMsg(sender, "&cInvalid amount " + args[2]);
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
