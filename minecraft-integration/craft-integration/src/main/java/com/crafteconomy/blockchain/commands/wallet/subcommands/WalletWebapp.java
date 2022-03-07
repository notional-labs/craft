package com.crafteconomy.blockchain.commands.wallet.subcommands;

import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.SubCommand;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class WalletWebapp implements SubCommand {

    private IntegrationAPI api = IntegrationAPI.getInstance();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        
        // check if sender instanceOf console
        if(sender instanceof ConsoleCommandSender) {
            Util.colorMsg(sender, "&cYou must be a player to use this command.");
            return;
        }

        Player player = (Player) sender;
        String wallet = api.getWallet(player.getUniqueId());

        if(wallet == null) {
            Util.colorMsg(sender, "&cYou do not have a wallet. Install one:");
            api.sendClickableKeplrInstallDocs(sender);
            return;
        }

        // sends link to the webapp so user can sign all Txs
        api.sendWebappForSigning(sender, wallet, "\n&6&l[!] &e&nClick here to access the webapp for your account\n");

    }
}
