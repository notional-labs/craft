package com.crafteconomy.blockchain.commands.wallet.subcommands;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WalletBalance implements SubCommand {

    WalletManager walletManager = WalletManager.getInstance();
    String walletPrefix = CraftBlockchainPlugin.getInstance().getWalletPrefix();
    int walletLength = CraftBlockchainPlugin.getInstance().getWalletLength();

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        String username = null;
        UUID uuid = null;
        String wallet = null;

        // set UUID to user by default to current player querying request
        if(sender instanceof Player) {
            Player player = (Player) sender;
            uuid = player.getUniqueId();
            username = player.getName();
        }

        // if they request another players address, get their offline UUID if they joined before
        if(args.length >= 2) {            
            Player player = Bukkit.getPlayer(args[1]);
            
            if(args[1].length() == walletLength && args[1].startsWith(walletPrefix)) {
                wallet = args[1];

            } else if(player == null) {
                // Player not found, get offline player info if they are cached
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(args[1]);

                if(offlinePlayer != null) {
                    uuid = offlinePlayer.getUniqueId();
                    username = offlinePlayer.getName();
                }
            } else {
                // player is online, get their UUID
                uuid = player.getUniqueId();
                username = player.getName();
            }  
        }

        if(wallet != null) {
            String walletBalance = getWalletBalanceOutput(args[1]);
            Util.colorMsg(sender, walletBalance);

        } else if(uuid == null) {
            String output = "&c&lERROR: &f&lInvalid wallet address.";
            if(args.length >= 2) { 
                output = getWalletBalanceOutput(args[1]);
            } 

            Util.colorMsg(sender, "\n" + output);

        } else {
            wallet = walletManager.getAddress(uuid);

            Util.colorMsg(sender, getPlayerBalanceOutput(wallet, username));
            if(wallet != null) {
                Util.clickableCopy(sender, wallet, "&7&oWallet: &n%value%", "&7&oClick to copy wallet address");
            }
            
        }
        
    }

    private String getPlayerBalanceOutput(String wallet, String otherUser){        
        if(wallet == null) {
            return "&c"+otherUser+" does not have a wallet set!";
        }

        float amount;
        try {
            amount = BlockchainRequest.getCraftBalance(wallet).get();
            if(otherUser != null) {
                return otherUser + " has " + amount + walletPrefix;
            } else {
                return "You have " + amount + "craft";
            }            
        } catch (InterruptedException | ExecutionException e) {            
            e.printStackTrace();
        }

        return "&c&lERROR: &f&lCould not get balance for " + otherUser;        
    }

    private String getWalletBalanceOutput(String wallet){
        if(!wallet.startsWith(walletPrefix)){
            return "";
        }

        // long amount = BlockchainRequest.getUCraftBalance(wallet);
        // float craft_amount = BlockchainRequest.getCraftBalance(wallet);

        // TODO: Correct value here?
        float craft_amount = 0;
        try {
            craft_amount = BlockchainRequest.getCraftBalance(wallet).get();
        } catch (InterruptedException | ExecutionException e) {            
            e.printStackTrace();
        }

        // negative number checks
        if(craft_amount == ErrorTypes.NO_TOKENS_FOR_WALLET.code || craft_amount == ErrorTypes.NO_WALLET.code) {
            return "&c[!] That wallet is not apart of the BlockchainChain!";

        } else if(craft_amount == ErrorTypes.NODE_DOWN.code) {            
            return "&c[!] Blockchain is currently down, please try again later.";
        }

        return wallet + " has " + craft_amount + "craft";
    }
    
}
