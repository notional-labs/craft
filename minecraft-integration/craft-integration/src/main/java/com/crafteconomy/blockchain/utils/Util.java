package com.crafteconomy.blockchain.utils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.*;
import java.util.logging.Logger;

// Reeces personal Util file for plugins

public class Util {
	static ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

	private static final Logger logger = Logger.getLogger("CraftIntegration");

	public static void log(String message) {
		logger.info(message);		
	}

	public static void logSevere(String message) { logger.severe(message); }

	public static void logWarn(String message) { logger.warning(message); }

	public static void logFine(String message) { logger.fine(message); }

	// ---- CraftIntegration Specific ----	 
	 @SuppressWarnings("deprecation")
	public static void clickableWallet(CommandSender sender, String WALLET_ADDRESS, String fmt) {
		// allows copy paste of wallet address to clipboard
        TextComponent message = new TextComponent(Util.color(fmt.replace("%wallet%", WALLET_ADDRESS)));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, WALLET_ADDRESS));
            // message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, WALLET_ADDRESS));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7§oClick to copy address")));
        sender.spigot().sendMessage(message);
    }

	@SuppressWarnings("deprecation")
	public static void clicableTxID(CommandSender sender, String UUID, String fmt) {
		TextComponent message = new TextComponent(Util.color(fmt.replace("%uuid%", UUID)));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, UUID));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7§oClick to copy TxID")));
            sender.spigot().sendMessage(message);
	}

	@SuppressWarnings("deprecation")
	public static void clickableCommand(CommandSender sender, String command, String msgFormat) {
        TextComponent message = new TextComponent(Util.color(msgFormat.replace("%command%", command)));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7§oClick to run command")));
        sender.spigot().sendMessage(message);
    }

	@SuppressWarnings("deprecation")
    public static void clickableWebsite(CommandSender sender, String URL, String fmt, String hoverText) {
		if(!(URL.startsWith("http://") || URL.startsWith("https://"))) {
			URL = "http://" + URL;
		}
        TextComponent message = new TextComponent(Util.color(fmt.replace("%url%", URL)));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, URL));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Util.color(hoverText))));
        sender.spigot().sendMessage(message);
    }

	public static String systemCommand(String command) { // used for sending transactions		
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();    
    }


	// ---- Other ----
	public static String argsToSingleString(int startPoint, String[] args) {
		StringBuilder str = new StringBuilder();
		for (int i = startPoint; i < args.length; i++) {
			if (i+1 < args.length) {
				str.append(args[i]).append(" ");
			} else {
				str.append(args[i]);
			}
		}
		return str.toString();
	}
		
	public static ItemStack getItemInHand(Player p){
		int slot = p.getInventory().getHeldItemSlot();
		return p.getInventory().getItem(slot);
	}
	

	public static void setTotalExperience(Player player, int exp) {
		player.setExp(0.0F);
		player.setLevel(0);
		player.setTotalExperience(0);
		int amount = exp;
		while (amount > 0) {
			int expToLevel = getExpAtLevel(player.getLevel());
			amount -= expToLevel;
			if (amount >= 0) {
				player.giveExp(expToLevel);
				continue;
			} 
			amount += expToLevel;
			player.giveExp(amount);
			amount = 0;
		} 
	}

	private static int getExpAtLevel(int level) {
		if (level <= 15) {
			return 2 * level + 7;
		}
		if (level <= 30) {
			return 5 * level - 38;
		}

		return 9 * level - 158;
	}

	public static int getTotalExperience(Player player) {
		int exp = Math.round(getExpAtLevel(player.getLevel()) * player.getExp());
		int currentLevel = player.getLevel();
		while (currentLevel > 0) {
			currentLevel--;
			exp += getExpAtLevel(currentLevel);
		} 
		if (exp < 0) {
			exp = Integer.MAX_VALUE;
		}
		return exp;
	}

	public static String formatNumber(double number) {
		Format decimalFormat = new DecimalFormat("###,###.##");
		return decimalFormat.format(number);
	}

	public static String color(String message) {
		if(message == null){
			message = "NULL_ISSUE";
			consoleMSG("NULL ERROR: " + Thread.currentThread().getStackTrace()[2]);
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public static List<String> color(List<String> list) {
		List<String> colored = new ArrayList<>();
		for (String s : list) {
			colored.add(color(s));
		}
		return colored;
	}

	public static void colorMsg(CommandSender sender, String message) {
		if(message == null) { return; }
		if (message.contains("\n")) {
			if (message.endsWith("\n")) {message+= " ";}
			
			for (String line : message.split("\n")) {
				sender.sendMessage(color(line));
			}			
		} else {
			sender.sendMessage(color(message));
		}
	}

	@SuppressWarnings("deprecation")
	public static void coloredBroadcast(String msg) {
		Bukkit.broadcastMessage(Util.color(msg));
	}

	public static void console(String command) {
		Bukkit.dispatchCommand(console, command);
	}

	public static void consoleMSG(String consoleMsg) {		
		Bukkit.getLogger().info(Util.color(consoleMsg));
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException numberFormatException) {
			return false;
		} 
	}  

}
