package com.crafteconomy.blockchain.commands.subcommands.debugging;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.transactions.function_testing.Examples;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import redis.clients.jedis.Jedis;

public class WalletGenerateFakeTx implements SubCommand {

    // [/wallet genfaketx <TxIDToUse>-> writes tx_txID to redis + default tx]

    RedisManager redis = CraftBlockchainPlugin.getInstance().getRedis();

    String webapp = CraftBlockchainPlugin.getInstance().getWebappLink();

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("Usage: /wallet genfaketx <license/purchase>");
            player.sendMessage("license = business license example");
            player.sendMessage("purchase = purchase item example");
            return;
        }

        String walletAddress = WalletManager.getInstance().getAddress(player.getUniqueId());
        if(walletAddress == null) {
            Util.colorMsg(sender, "&c[!] You do not have a wallet address. Please create one with KEPLR wallet");
            return;
        }

        // , "test TX Method", Examples.purchaseBusinessLicense()
        Tx TxInfo = new Tx();
        TxInfo.setFromUUID(player.getUniqueId());

        String desc = null;
        String itemToPurchase = "<PlaceholderItemText>";

        if(args[1].equalsIgnoreCase("license")) {
            TxInfo.setFunction(Examples.purchaseBusinessLicense());
            desc = "Purchase Business License for 2";
            TxInfo.setAmount(2);
        } else {
            if(args.length >= 3) { 
                itemToPurchase = Util.argsToSingleString(2, args); 
            }

            TxInfo.setFunction(Examples.purchaseSomeItem(itemToPurchase));
            desc = "Purchasing item " + itemToPurchase + " for 1";
            TxInfo.setAmount(1);
        }

        TxInfo.setDescription(desc);

        TxInfo.setToWallet(walletAddress);
        
       
        
        try (Jedis jedis = redis.getRedisConnection()) {
            ErrorTypes error = BlockchainRequest.transaction(TxInfo);
            // if(error != ErrorTypes.NO_ERROR) {
            //     // code
            // }
                        
            Util.colorMsg(sender, "\n&a&l[✓] &aAdded following Tx to redis:");
            Util.colorMsg(sender, "&f&otx_"+walletAddress.subSequence(0, 10)+"..._" +TxInfo.getTxID());
            // Util.colorMsg(sender, "&7&o(( View your pending TxIDs with /wallet mypending ))");
            Util.clickableCommand(sender, "/wallet mypending", "&7&o(( View your pending TxIDs with %command% ))");

            // TODO:
            // Util.clickableWebsite(sender, "https://crafteconomy.com/sign?"+walletAddress, 
            //     "\n&6&l[!] &e&nClick here to sign your transaction(s)",
            //     "&7&oSign this transaction with your KEPLR wallet");

            Util.clickableWebsite(sender, webapp, 
                "\n&6&l[!] &e&nClick here to sign your transaction(s)",
                "&7&oSign this transaction with your KEPLR wallet");

        } catch (Exception e) {
            Util.logSevere("[WalletGEnFakeTx] Error setting tx_" + TxInfo.getTxID() + " in redis");
        }
    }    
}
