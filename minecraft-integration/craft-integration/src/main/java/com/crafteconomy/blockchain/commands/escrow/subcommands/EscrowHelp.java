package com.crafteconomy.blockchain.commands.escrow.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EscrowHelp implements SubCommand {

    @Override // "Usage: /escrow balance"
    public void onCommand(CommandSender sender, String[] args) {
        Util.colorMsg(sender, "/escrow [balance, deposit, redeem]");
        return;
    }
}

