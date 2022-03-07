package com.crafteconomy.blockchain.commands.wallet.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;

public class WalletSupply implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        String formattedCraft = Util.formatNumber(BlockchainRequest.getTotalSupply("token"));
        String formattedStake = Util.formatNumber(BlockchainRequest.getTotalSupply("stake"));

        Util.colorMsg(sender, "Total CRAFT supply is " + formattedCraft);   
        Util.colorMsg(sender, "Total STAKE supply is " + formattedStake);          
    }
}
