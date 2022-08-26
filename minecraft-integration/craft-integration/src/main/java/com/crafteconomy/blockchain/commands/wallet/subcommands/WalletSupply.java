package com.crafteconomy.blockchain.commands.wallet.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;

public class WalletSupply implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        BlockchainRequest.getTotalSupply("ucraft").thenAcceptAsync(amount -> {
            Util.colorMsg(sender, "ucraft Supply: " + Util.formatNumber(amount));
        });

        BlockchainRequest.getTotalSupply("uexp").thenAcceptAsync(amount -> {
            Util.colorMsg(sender, "uexp Supply: " + Util.formatNumber(amount));
        });

        // String formattedCraft = Util.formatNumber(BlockchainRequest.getTotalSupply("ucraft"));
        // String formattedStake = Util.formatNumber(BlockchainRequest.getTotalSupply("uexp"));
        // Util.colorMsg(sender, "Total ucraft supply is " + formattedCraft);   
        // Util.colorMsg(sender, "Total uexp supply is " + formattedStake);          
    }
}
