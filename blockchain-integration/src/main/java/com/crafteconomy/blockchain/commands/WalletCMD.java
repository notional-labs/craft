package com.crafteconomy.blockchain.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WalletCMD implements CommandExecutor {

    private Map<String, SubCommand> commands = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0 || !commands.containsKey(args[0].toLowerCase())) {
            args = new String[]{ "help" };
        }

        commands.get(args[0]).onCommand(sender, args);
        return true;
    }


    public void registerCommand(String cmd, SubCommand subCommand) {
        commands.put(cmd, subCommand);
    }

    public void registerCommand(String[] cmds, SubCommand subCommand) {
        for (int i = 0; i < cmds.length; i++) {
            commands.put(cmds[i], subCommand);
        }        
    }
    
}
