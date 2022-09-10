package com.crafteconomy.blockchain.commands.wallet.subcommands.debugging;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.core.types.TransactionType;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.transactions.function_testing.Examples;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import redis.clients.jedis.Jedis;

public class WalletMultipleTxTesting implements SubCommand {

    // [/wallet genfaketxstest]

    RedisManager redis = CraftBlockchainPlugin.getInstance().getRedis();

    String webapp = CraftBlockchainPlugin.getInstance().getWebappLink();

    int RedisMinuteTTL = CraftBlockchainPlugin.getRedisMinuteTTL();

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        // if (args.length < 2) {
        //     player.sendMessage("Usage: /wallet genfaketxstest");
        //     return;
        // }

        String walletAddress = WalletManager.getInstance().getAddress(player.getUniqueId());
        if(walletAddress == null) {
            Util.colorMsg(sender, "&c[!] You do not have a wallet address. Please create one with KEPLR wallet");
            return;
        }

        // , "test TX Method", Examples.purchaseBusinessLicense()

        int num_to_make = 10;
        for(int i = 0; i < num_to_make; i++) {
            Tx TxInfo = new Tx();
            TxInfo.setFromUUID(player.getUniqueId());
            TxInfo.setToWallet(walletAddress);
            TxInfo.setRedisMinuteTTL(RedisMinuteTTL);
            TxInfo.setFunction(Examples.TEST_youPaidXCraft());
            TxInfo.setTxType(TransactionType.DEFAULT);    

            // get a random number between 5billion  and 20 billion
            long randomNum = (long) (Math.random() * 15000000000L) + 5000000000L;
            TxInfo.setUCraftAmount(randomNum);
            TxInfo.setDescription("" + randomNum + " ucraft");
            TxInfo.submit();  
        }

        Util.colorMsg(sender, "&a[!] &f" + num_to_make + " fake TXs have been created. They will expire in " + RedisMinuteTTL + " minutes. You can view them at: the webapp");
           
    }    
}
