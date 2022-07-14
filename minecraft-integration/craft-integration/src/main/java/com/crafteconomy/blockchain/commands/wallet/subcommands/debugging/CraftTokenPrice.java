package com.crafteconomy.blockchain.commands.wallet.subcommands.debugging;

import org.bukkit.command.CommandSender;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;

public class CraftTokenPrice implements SubCommand {
    // getCraftTokenPrice

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    
    @Override
    public void onCommand(CommandSender sender, String[] args) {

        api.getCraftTokenPrice().thenAccept(price -> {
            if(price > 0) {
                Util.colorMsg(sender, "\n&a&lCRAFT TOKEN PRICE: &f$" + price);
            } else {
                Util.colorMsg(sender, "\n&c&lToken price service is currently down");
            }
            
        });    
    }


}
