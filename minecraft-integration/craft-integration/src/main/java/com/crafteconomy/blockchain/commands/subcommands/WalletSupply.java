package com.crafteconomy.blockchain.commands.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;

public class WalletSupply implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String formatedCraft = Util.formatNumber(BlockchainRequest.getTotalSupply("token"));
        String formatedStake = Util.formatNumber(BlockchainRequest.getTotalSupply("stake"));

        Util.colorMsg(sender, "Total CRAFT supply is " + formatedCraft);   
        Util.colorMsg(sender, "Total STAKE supply is " + formatedStake);          
    }
}
