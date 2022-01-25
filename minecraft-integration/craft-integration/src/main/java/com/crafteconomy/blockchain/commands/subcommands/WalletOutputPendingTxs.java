package com.crafteconomy.blockchain.commands.subcommands;

import java.util.Set;
import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;

// Acts in place of the webapp assuming it has signed the transaction

public class WalletOutputPendingTxs implements SubCommand {

    // [/wallet allpending ]

    RedisManager redis = CraftBlockchainPlugin.getInstance().getRedis();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Set<UUID> keys = PendingTransactions.getInstance().getKeys();      
        Util.colorMsg(sender, "&b[!] All Pending keys: \n" + keys.toString());     
    }
    
}
