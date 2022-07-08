package com.crafteconomy.blockchain.commands.escrow.subcommands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.escrow.EscrowErrors;
import com.crafteconomy.blockchain.utils.Util;

public class EscrowPay implements SubCommand {
    
    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override // "Usage: /escrow pay <Player> <craft_amount>"
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length < 3) {
            Util.colorMsg(sender, "Usage: /escrow pay <Player> <craft_amount>");
            return;
        }


        Player receiver = Bukkit.getPlayer(args[1]);
        if (receiver == null) {
            Util.colorMsg(player, "&cPlayer is not online to pay.");
            return;
        }
        UUID reciver_uuid = receiver.getUniqueId();

        float craft_cost = Math.abs(Float.parseFloat(args[2]));

        EscrowErrors err = api.escrowPayPlayerCraft(player.getUniqueId(), reciver_uuid, craft_cost);
        if(err == EscrowErrors.INSUFFICIENT_FUNDS) {
            Util.colorMsg(sender, "&cYou do not have enough funds to pay this amount.");
            return;
        } 

        Util.colorMsg(receiver, "&a" + player.getName() + " paid you " + craft_cost + "craft.");
        Util.colorMsg(sender, "&a[!] Success! Paid : '" + args[1] + "'' " + craft_cost + "craft.");        
        return;
    }
}
