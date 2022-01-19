package com.crafteconomy.blockchain.api;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class IntegrationAPI {

    WalletManager walletManager = WalletManager.getInstance();

    // singleton, sets wallet to the server wallet in config
    private final String SERVER_WALLET; 
    private IntegrationAPI() {     
        SERVER_WALLET = CraftBlockchainPlugin.getInstance().getConfig().getString("SERVER_WALLET_ADDRESS");
        if(SERVER_WALLET == null) {
            throw new IllegalStateException("SERVER_WALLET_ADDRESS is not set in config.yml");
        }
    }

    /**
     * Gets the server wallet, used for paying the server for transactions (ex. taxes) 
     * @return String Wallet
     */
    public String getServerWallet() {
        return SERVER_WALLET;
    }

    /**
     * Gets a players String wallet address (starts with 'craft' & is 44 char long)
     * @param player_uuid
     * @return
     */
    public String getWallet(UUID player_uuid) {
        return walletManager.getAddress(player_uuid);
    }

    /**
     * Gets the balance of a player based on their wallet address
     * @param player_uuid
     * @return
     */
    public long getBalance(UUID player_uuid) {
        String walletAddr = getWallet(player_uuid);
        if(walletAddr == null) {
            return 0;
        }
        return BlockchainRequest.getBalance(walletAddr);
    }


    /**
     * Send Tokens to another player/wallet. Blockchain integration will run the callback
     * @param from_uuid     Who it is from
     * @param to_wallet     Who to send the CRAFT tokens too
     * @param amount        Amount of craft to send
     * @param description   What this transaction is for
     * @param callback      Function to run for the sender once completed
     * @return Tx
     */
    public Tx createNewTx(UUID playerUUID, @NotNull String to_wallet, long amount, String description, Consumer<UUID> callback) {
        Tx tx = new Tx();
        tx.setFromUUID(playerUUID);
        tx.setToWallet(to_wallet);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setFunction(callback);
        return tx;
    } 

    /**
     * Allows for 2 players to be involved in a transaction, useful for trading between players
     * @param playerUUID
     * @param recipientUUID
     * @param to_wallet
     * @param amount
     * @param description
     * @param callback
     * @return Tx
     */
    public Tx createNewTx(UUID playerUUID, UUID recipientUUID, @NotNull String to_wallet, long amount, String description, BiConsumer<UUID, UUID> biCallback) {
        Tx tx = new Tx();
        tx.setFromUUID(playerUUID);
        tx.setToUUID(recipientUUID);
        tx.setToWallet(to_wallet);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setBiFunction(biCallback);
        return tx;
    } 
    
    /**
     * Creates a transaction which pays tokens back to the servers main wallet
     * @param from_uuid     Who it is from
     * @param amount        Amount of craft to send
     * @param description   What this transaction is for
     * @param callback      Function to run for the sender once completed
     * @return              The Transaction (Tx) object
     */
    public Tx createServerTx(UUID from_uuid, long amount, String description, Consumer<UUID> callback) {
        return createNewTx(from_uuid, SERVER_WALLET, amount, description, callback);
    }
    
    /**
     * Submits a transaction message to the redis instance (To get signed from webapp)
     * @param tx    Transaction to submit
     * @return      The ErrorTypes of the transaction status
     */
    public ErrorTypes submit(Tx tx) {
        return BlockchainRequest.transaction(tx);
    }

    /**
     * Gives a player's wallet some tokens (CRAFT) via Console only
     * @param consoleSender
     * @param player_uuid
     * @param amount
     * @return  null or json {"transfers":[{"coin":"1token","status":"ok"}]}
     */
    public String deposit(CommandSender consoleSender, UUID player_uuid, long amount) {
        if(!(consoleSender instanceof ConsoleCommandSender)) {
            return null;
        }

        // {"transfers":[{"coin":"1token","status":"ok"}]}
        return BlockchainRequest.depositToAddress(walletManager.getAddress(player_uuid), amount);
    }

    // --------------------------------------------------
    // clickable links / commands / TxId's to make user life better
    public void sendWebappForSigning(CommandSender sender, String fromWallet) {
		Util.clickableWebsite(sender, "https://crafteconomy.com/sign?"+fromWallet, 
            "&6&l[!] &e&nClick here to sign your transaction(s)",
            "&7&oSign your transaction(s) with the KEPLR wallet"
        );
	}

	public void sendClickableKeplrInstallDocs(CommandSender sender) {
		Util.clickableWebsite(sender, "https://docs.crafteconomy.io/set-up/wallet", 
            "&2[!] &a&nClick here to learn how to set up your wallet.",
            "&7&oSetup your CRAFT wallet with Keplr"    
        );
	}

    public void sendTxIDClickable(CommandSender sender, String TxID, String format, String hoverMessage) {
		Util.clickableCopy(sender, TxID, format, hoverMessage);
	}

    public void sendTxIDClickable(CommandSender sender, String TxID, String format) {
		Util.clickableCopy(sender, TxID, format, "&7&oClick to copy TxID");
	}

	public void sendTxIDClickable(CommandSender sender, String TxID) {
		sendTxIDClickable(sender, TxID, "&7&oTxID: &n%value%");
	}


    public void sendWalletClickable(CommandSender sender, String wallet, String format, String hoverMessage) {
		Util.clickableCopy(sender, wallet, format, hoverMessage);
	}

    public void sendWalletClickable(CommandSender sender, String wallet, String format) {
		sendWalletClickable(sender, wallet, format, "&7&oClick to copy TxID");
	}

    public void sendWalletClickable(CommandSender sender, String wallet) {
		sendWalletClickable(sender, wallet, "&7&oWallet: &n%value%");
	}



    private static IntegrationAPI instance = null;
    public static IntegrationAPI getInstance() {
        if(instance == null) {
            instance = new IntegrationAPI();
        }
        return instance;
    }
}
