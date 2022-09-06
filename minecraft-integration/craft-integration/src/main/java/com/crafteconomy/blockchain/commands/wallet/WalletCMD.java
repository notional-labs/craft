package com.crafteconomy.blockchain.commands.wallet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.crafteconomy.blockchain.commands.SubCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class WalletCMD implements CommandExecutor, TabCompleter {

    private Map<String, SubCommand> commands = new HashMap<>();

    private Set<String> tabCompleteArguments = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !commands.containsKey(args[0].toLowerCase())) {
            args = new String[]{ "help" };
        }

        // TODO: if user does not have wallet here, send them api.sendClickableKeplrInstallDocs ?

        commands.get(args[0].toLowerCase()).onCommand(sender, args);
        return true;
    }

    private List<String> result = new ArrayList<String>();    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {        	
        result.clear();

        if(args.length == 1) {			
			for(String a : tabCompleteArguments) {
				if(a.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(a);			
				}
			}
			return result;
		}	        

        if(args.length >= 1) {
            String subCommand = args[0].toLowerCase(); // walelt <SUBCOMMAND>

            if(subCommand.startsWith("bal")) {                
                if(args.length == 2) { return null; }
            }

            if(subCommand.startsWith("set")) {
                if(args.length == 2) { return Arrays.asList("<craft-wallet-address>"); }
            }

            if(subCommand.startsWith("pay") || subCommand.startsWith("send")) {
                if(args.length == 2) { return null; }
                if(args.length == 3) { return Arrays.asList("10", "25", "50"); }
            }
        } 
        
        return new ArrayList<String>();        
    }


    

    public void registerCommand(String cmd, SubCommand subCommand) {
        commands.put(cmd.toLowerCase(), subCommand);
    }

    public void registerCommand(String[] cmds, SubCommand subCommand) {
        for (int i = 0; i < cmds.length; i++) {
            registerCommand(cmds[i], subCommand);
        }        
    }

    public void addTabComplete(String cmd) {
        tabCompleteArguments.add(cmd.toLowerCase());
    }

    public void addTabComplete(String[] cmds) {
        for (int i = 0; i < cmds.length; i++) {

            // only add tab complete to commands we have
            if(commands.containsKey(cmds[i])) {
                addTabComplete(cmds[i]);
            }
        }
    }
    
}
