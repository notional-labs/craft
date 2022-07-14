package com.crafteconomy.blockchain.commands.escrow.subcommands;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EscrowRedeem implements SubCommand {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        if(args.length < 2) {
            Util.colorMsg(player, "Usage: /escrow redeem <amount>");
            return;
        }

        int redeemAmount = 0;
        // ensure argument 1 is a number
        try {
            redeemAmount = Integer.valueOf(args[1]);
        } catch(NumberFormatException e) {
            Util.colorMsg(player, "Usage: /escrow redeem <amount>");
            return;
        }

        // takes in game CRAFT held balance & deposits into their CRAFTA wallet address account
        // if redeemAmount > what they have in escrow, we make the Tx match their escrow balance
        long redeemedAmount = api.escrowRedeem(player.getUniqueId(), redeemAmount);
        
        Util.colorMsg(player, "&aYou redeemed " + redeemedAmount/1_000_000 + "craft tokens to your blockchain wallet from escrow");
        if(redeemedAmount < redeemAmount) {
            Util.colorMsg(player, "This was less than you requested "+redeemAmount+", since you only had " + redeemedAmount/1_000_000 + "craft tokens in escrow");
        }
    }
}