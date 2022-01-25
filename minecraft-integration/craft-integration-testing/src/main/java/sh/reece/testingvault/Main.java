package sh.reece.testingvault;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


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
    }


    @Override
    public void onDisable() { }


    public Main getInstance() {
        return instance;
    }

}
