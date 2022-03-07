package com.crafteconomy.blockchain.commands.wallet.subcommands;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import redis.clients.jedis.Jedis;

// Shows a user their Tx's which are pending (tx_CRAFT-ADDR*)

public class WalletMyPendingTxs implements SubCommand {

    // [/wallet mypending [wallet]
    // ^^ what the web app will see

    RedisManager redis = CraftBlockchainPlugin.getInstance().getRedis();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        
        if(sender instanceof ConsoleCommandSender) {
            Util.colorMsg(sender, "&c[!] This command cannot be run from the console.");
            return;
        }

        Player player = (Player) sender;

        String walletAddress = WalletManager.getInstance().getAddress(player.getUniqueId());
        if(walletAddress == null) {
            Util.colorMsg(sender, "&c[!] You do not have a wallet address. Please create one with KEPLR wallet");
            return;
        }

        // Get all keys from redis
        Set<String> keys = new HashSet<String>();

        try (Jedis jedis = redis.getRedisConnection()) {
            System.out.println("[DEBUG] Getting keys from redis for MyPending: " + "tx_" + walletAddress + "*");
            keys = jedis.keys("tx_" + walletAddress + "*");
        } catch (Exception e) {
            Util.logSevere("[WalletMyPendingTxs] error with jedis.keys fetch");
        }
 
        if(keys.size() > 0) {
            Util.colorMsg(sender, "&b[!] Your pending transactions:");
            // TODO: add signing website here
            
            String walletFormat = "tx_" + walletAddress + "_";

            for(String key : keys) {

                // valid UUID excluding redis excluding "tx_<wallet>_"
                UUID uuid = null;
                try {
                    uuid = UUID.fromString(key.replace(walletFormat, ""));
                } finally {

                }

                // Here shows all transactions, including ones where the TxInfo is not present

                if(uuid != null) {
                    Tx txinfo = PendingTransactions.getInstance().getTxFromID(uuid);
                    if(txinfo != null) {
                        // Util.clicableTxID(sender, key.replace(walletFormat, ""), "&7- &f%uuid%");

                        IntegrationAPI.getInstance().sendTxIDClickable(sender, key.replace(walletFormat, ""), "&7- &f%value%");
                        Util.colorMsg(sender, "&7&o   " + txinfo.getDescription() + "\n");
                    }
                }

            }

        } else {
            Util.colorMsg(sender, "&c[!] You have no pending transactions");
        }
    }
    
}
