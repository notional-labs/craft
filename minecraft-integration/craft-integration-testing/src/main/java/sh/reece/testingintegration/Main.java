package sh.reece.testingintegration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import sh.reece.testingintegration.onlyconsole.GiveTokens;
import sh.reece.testingintegration.simple.Balance;
import sh.reece.testingintegration.simple.HowToKepler;
import sh.reece.testingintegration.simple.MyExampleTransaction;
import sh.reece.testingintegration.simple.Wallet;
import sh.reece.testingintegration.trade.TradeCommand;


public class Main extends JavaPlugin {


    private Main instance;

    // generate onenable and disable
    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getPluginCommand("test-balanceapi").setExecutor(new Balance());
        Bukkit.getPluginCommand("test-walletapi").setExecutor(new Wallet());
        Bukkit.getPluginCommand("test-exampleapi").setExecutor(new MyExampleTransaction());
        Bukkit.getPluginCommand("test-tokensapi").setExecutor(new GiveTokens());
        Bukkit.getPluginCommand("test-trade").setExecutor(new TradeCommand());
        Bukkit.getPluginCommand("test-keplr").setExecutor(new HowToKepler());
    }


    @Override
    public void onDisable() { }


    public Main getInstance() {
        return instance;
    }

}
