package com.crafteconomy.blockchain.commands.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Send Tokens to another player/wallet
 */

public class WalletSend implements SubCommand {

    WalletManager walletManager = WalletManager.getInstance();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if(sender instanceof ConsoleCommandSender) {
            Util.colorMsg(sender, "&cOnly players can use this command!");
            return;
        }

        if(args.length < 3) {
            Util.colorMsg(sender, "&cUsage: /wallet send <player|UUID|wallet> <amount>");
            return;
        }

        final String FROM = walletManager.getAddress(((Player)sender).getUniqueId());
        final String TO = walletManager.getAddressFromName(args[1]);

        final long AMOUNT;
        try {
            AMOUNT = Long.parseLong(args[2]);
            if(AMOUNT <= 0) { return; }

        } catch (Exception e) {
            Util.colorMsg(sender, "&c&lInvalid amount " + args[2]);
            return;
        }         
        
        if(FROM == null) {
            Util.colorMsg(sender, "&c&lERROR: &fPlease use &a/wallet setwallet <wallet> &fto set your wallet.");
            return;
        }  
        
        if(TO == null) {
            Util.colorMsg(sender, "&c&lERROR: &f" + args[1] + " &fdoes not have a valid wallet set.");

            Player target = Bukkit.getPlayer(args[1]);
            if(target != null) {
                Util.colorMsg(sender, "\n&4&l[!]] &n" + target.getName() + " &fhas tried sending you money");
                Util.colorMsg(sender, "&4&l[!]] &cBut you do not have an active wallet set!");
                Util.colorMsg(sender, "&4&l[!]] &f&a/wallet setwallet <wallet>");
            } else {
                Util.colorMsg(sender, "&fInform them too &7&o/wallet set <wallet> next time they are on");
            }
            
            return;
        }
    
        // saves to redis instance for webapp to grab
        BlockchainRequest.transferTokens(FROM, TO, AMOUNT);

        Util.colorMsg(sender, "\nTx for " + AMOUNT + "craft->" + TO.subSequence(0, 16) + "...");
        Util.clickableWebsite(sender, "https://crafteconomy.com/sign_tx/"+FROM.substring(5), "%url%");
    }
    
}
