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
        // async under the hood for actual payment
        // long redeemedAmount = api.escrowCraftRedeem(player.getUniqueId(), redeemAmount); // we do this notification in the redeem section now     
        api.escrowCraftRedeem(player.getUniqueId(), redeemAmount);        
    }
}