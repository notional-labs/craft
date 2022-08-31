package com.crafteconomy.blockchain.commands.wallet.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;

public class WalletSupply implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {        
        BlockchainRequest.getTotalSupply("ucraft").thenAccept((supply) -> {
            Util.colorMsg(sender, "Total ucraft supply is " + Util.formatNumber(supply));        
        });
        BlockchainRequest.getTotalSupply("uexp").thenAccept((supply) -> {
            Util.colorMsg(sender, "Total uexp supply is " + Util.formatNumber(supply));        
        });
    }
}
