package com.crafteconomy.blockchain.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {
    void onCommand(CommandSender sender, String[] args);
}
