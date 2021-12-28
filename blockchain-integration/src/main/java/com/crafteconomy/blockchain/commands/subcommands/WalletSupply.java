package com.crafteconomy.blockchain.commands.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;

public class WalletSupply implements SubCommand {

    // TODO: NFTs?

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        
        String token = null; // stake / supply

        if(args.length == 1) {
            token = args[0];
        } else {
            Util.colorMsg(sender, "&cUsage: /wallet <stake / token>");
            return;
        }

        if(token == null || !token.equalsIgnoreCase("stake") && !token.equalsIgnoreCase("token")) {
            Util.colorMsg(sender, "&cInvalid usage. &f&l/wallet <stake / token>");
            return;
        }

        supplyMessage(sender, token);
        
    }

    private void supplyMessage(CommandSender sender, String denom) {
        String formatedAmount = Util.formatNumber(BlockchainRequest.getTotalSupply(denom));
        String suffix = "";
        switch (denom) {
            case "stake" -> suffix = denom;                
            case "token" -> suffix = "craft";
        }

        Util.colorMsg(sender, "Total " + denom + " supply is " + formatedAmount + " " + suffix);        
    }
}
