package com.crafteconomy.blockchain.commands.wallet.subcommands.debugging;

import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;

import redis.clients.jedis.Jedis;

// Acts in place of the webapp assuming it has signed the transaction

public class WalletFakeSign implements SubCommand {

    // [/wallet fakesign txUUIDHere -> writes signed_txID to redis -> Bukkit event sees -> Method is run]

    // The transaction is NOT being signed here, the webapp does that and broadcast.
    // This is purely just to generate the transactions & simulate as if they were signed.

    RedisManager redis = CraftBlockchainPlugin.getInstance().getRedis();

    

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if(args.length < 2) {
            Util.colorMsg(sender, "&cUsage: /wallet fakesign txUUIDHere");
            return;
        }
        
        UUID TxID = null;
        try {
            TxID = UUID.fromString(args[1]);
        } catch (Exception e) {
            Util.colorMsg(sender, "The TxID: " + args[1] + " is not a valid TxID");
            return;
        }

        try (Jedis jedis = redis.getRedisConnection()) {
            // if DEBUGGING is the value, we will allow it through the tendermint hash check
            jedis.set("signed_" + TxID.toString(), "DEBUGGING");
            Util.log("signed_" + TxID.toString() + " added to redis, firing event");
        } catch (Exception e) {
            Util.logSevere("[WalletFakeSign] Error setting signed_" + TxID.toString() + " in redis");
        }            
        
    }
    
}
