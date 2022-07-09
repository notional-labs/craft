package com.crafteconomy.blockchain.commands.escrow.subcommands;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EscrowBalance implements SubCommand {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override // "Usage: /escrow balance"
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        String craftString = "&eEscrow balance is: " + api.escrowGetCraftBalance(player.getUniqueId()) + "craft. ";
        craftString += "&7&o( ucraft = "+ api.escrowGetUCraftBalance(player.getUniqueId()) + " )";
        Util.colorMsg(sender, craftString);
        return;
    }
}
