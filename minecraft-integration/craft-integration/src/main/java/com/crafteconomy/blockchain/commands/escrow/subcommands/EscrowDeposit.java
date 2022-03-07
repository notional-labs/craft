package com.crafteconomy.blockchain.commands.escrow.subcommands;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EscrowDeposit implements SubCommand {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        if(args.length < 2) {
            Util.colorMsg(player, "Usage: /escrow deposit <amount>");
            return;
        }

        int depositAmount = 0;
        // ensure argument 1 is a number
        try {
            depositAmount = Integer.valueOf(args[1]);
        } catch(NumberFormatException e) {
            Util.colorMsg(player, "Usage: /escrow deposit <amount>");
            return;
        }

        if(depositAmount > 0) { // depositing into players escrow account
            api.escrowDeposit(player.getUniqueId(), depositAmount);
        } else {
            Util.colorMsg(player, "You must deposit > 0craft tokens");
        }

        return;
    }
}
