package com.crafteconomy.blockchain.commands.wallet.subcommands;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

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
            String walletBalance;
            try {
                walletBalance = getWalletBalanceOutput(args[1]).get();
                Util.colorMsg(sender, walletBalance);
            } catch (InterruptedException | ExecutionException e) {                
                e.printStackTrace();
            }            

        } else if(uuid == null) {            
            if(args.length >= 2) { 
                try {                    
                    Util.colorMsg(sender, "\n" + getWalletBalanceOutput(args[1]).get());
                } catch (InterruptedException | ExecutionException e) {                    
                    Util.colorMsg(sender, "\n&c&lERROR: &f&lInvalid wallet address.");
                    e.printStackTrace();
                }
            }             

        } else {
            wallet = walletManager.getAddress(uuid);

            // Util.colorMsg(sender, getPlayerBalanceOutput(wallet, username));

            final String their_wallet = wallet;
            final String their_username = username;
            Bukkit.getScheduler().runTaskAsynchronously(CraftBlockchainPlugin.getInstance(), () -> {
                try {
                    if(their_wallet == null) {
                        Util.colorMsg(sender, "&c"+their_username+" does not have a wallet set!");
                    }
            
                    float amount = BlockchainRequest.getCraftBalance(their_wallet).get();
            
                    if(their_username != null) {
                        Util.colorMsg(sender, their_username + " has " + amount + walletPrefix);
                    } else {
                        Util.colorMsg(sender, "You have " + amount + "craft");
                    }

                    if(their_wallet != null) {
                        Util.clickableCopy(sender, their_wallet, "&7&oWallet: &n%value%", "&7&oClick to copy wallet address");
                    } 

                } catch (InterruptedException | ExecutionException e) {                
                    e.printStackTrace();
                }
            });              
        }   
    }

    private CompletableFuture<String> getWalletBalanceOutput(String wallet){        
        return CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                if(!wallet.startsWith(walletPrefix)){
                    return "";
                }

                long amount = -1;
                try {
                    amount = BlockchainRequest.getUCraftBalance(wallet).get();
                } catch (InterruptedException | ExecutionException e) {                    
                    e.printStackTrace();
                }
                float craft_amount = amount / 1_000_000;          

                // negative number checks
                if(craft_amount == ErrorTypes.NO_TOKENS_FOR_WALLET.code || craft_amount == ErrorTypes.NO_WALLET.code) {
                    return "&c[!] That wallet is not apart of the BlockchainChain!";

                } else if(craft_amount == ErrorTypes.NODE_DOWN.code) {            
                    return "&c[!] Blockchain is currently down, please try again later.";
                }

                return wallet + " has " + craft_amount + "craft";
            }
        });        
    }
    
}
