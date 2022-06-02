package com.crafteconomy.blockchain.commands.wallet.subcommands;

import java.util.UUID;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.transactions.Tx;
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
    int RedisMinuteTTL = CraftBlockchainPlugin.getRedisMinuteTTL();

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

        // FROM & TO wallet addresses (craftxxxxxxx...)
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
            Util.colorMsg(sender, "&c&lERROR: &fPlease use &a/wallet "+args[0]+" <wallet> &fto set your wallet.");
            return;
        }  
        
        if(TO == null) {
            Util.colorMsg(sender, "&c&lERROR: &f" + args[1] + " &fdoes not have a valid wallet set.");

            Player target = Bukkit.getPlayer(args[1]);
            if(target != null) {
                Util.colorMsg(sender, "\n&4&l[!]] &n" + target.getName() + " &fhas tried sending you money");
                Util.colorMsg(sender, "&4&l[!]] &cBut you do not have an active wallet set!");
                Util.colorMsg(sender, "&4&l[!]] &f&a/wallet "+args[0]+" <wallet>");
            } else {
                Util.colorMsg(sender, "&fInform them too &7&o/wallet set <wallet> next time they are on");
            }
            
            return;
        }
    
        Player player = (Player) sender;

        Tx txInfo = new Tx();
        txInfo.setFromUUID(player.getUniqueId());
        txInfo.setToWallet(TO);
        txInfo.setAmount(AMOUNT);
        txInfo.setDescription(player.getName() + " sent " + AMOUNT + " to " + args[1]);
        txInfo.setFunction(getConsumerMessage("&a&lSUCCESS: &fYou have sent " + AMOUNT + "CRAFT to " + args[1]));

        ErrorTypes error = BlockchainRequest.transaction(txInfo, RedisMinuteTTL);

        if(error != ErrorTypes.NO_ERROR) {
            Util.colorMsg(sender, "&c&lERROR: &f" + error.toString());
            return;
        }

        Util.colorMsg(sender, "\nTx for " + AMOUNT + "craft->" + TO.subSequence(0, 16) + "...");
        IntegrationAPI.getInstance().sendTxIDClickable(sender, txInfo.getTxID().toString());
        IntegrationAPI.getInstance().sendWebappForSigning(sender, FROM);
    }

    private Consumer<UUID> getConsumerMessage(String message) {
        return new Consumer<UUID>() {
            @Override
            public void accept(UUID uuid) {
                Player player = Bukkit.getPlayer(uuid);
                if(player != null) {
                    Util.colorMsg(player, message);
                }                
            }
        };
    }
    
}
