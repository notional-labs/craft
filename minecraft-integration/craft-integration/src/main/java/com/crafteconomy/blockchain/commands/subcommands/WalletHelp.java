package com.crafteconomy.blockchain.commands.subcommands;

import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WalletHelp implements SubCommand {
    
    String ADMIN_PERM = "blockchain.admin";

    WalletManager walletManager = WalletManager.getInstance();

    private final String[] HELP_MESSAGES = {
        "help",
        "balance <player / craft_address>",
        "set <craft-wallet>",
        "pay <player|UUID|wallet> <amount>", 
        "supply",
        "pending",
    };
    
    private final String[] ADMIN_HELP_MESSAGES = {        
        "faucet <name|wallet> <amount> &7- &fDeposit craft to wallet.",
        "faketx <license/purchase> [item] &7- &4[TEMP].",
        "fakesign <Generated_TxID> &7- &4[TEMP].",
        "allpending &7- &4[TEMP].",
    };


    
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Util.colorMsg(sender, "\n&a&lCRAFT ECONOMY &f&lWALLET");

        if(sender instanceof Player) {
            Player player = (Player) sender;
            String WALLET = walletManager.getAddress(player.getUniqueId());
            if(WALLET == null) {
                Util.clickableWebsite(sender, "https://docs.crafteconomy.io/set-up/wallet", 
                    "&2[!] &a&nClick here to learn how to set up your wallet.",
                    "&7&oSetup your CRAFT wallet with Keplr"    
                );
            } else {
                Util.clickableCopy(sender, WALLET, " &f-> &7%value%", "&7&oClick to copy wallet address");
            }
        }
        

        for(String msg : HELP_MESSAGES){
            Util.colorMsg(sender, "&a/wallet &f" + msg);
        }   

        if(sender.hasPermission(ADMIN_PERM)){
            Util.colorMsg(sender, "\n&c&lADMIN COMMANDS");
            for(String msg : ADMIN_HELP_MESSAGES){
                Util.colorMsg(sender, "&a/wallet &f" + msg);
            }   
        }
        
    }
    
}
