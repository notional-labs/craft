package com.crafteconomy.blockchain.api;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.core.request.BlockchainRequest;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.escrow.EscrowErrors;
import com.crafteconomy.blockchain.escrow.EscrowManager;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class IntegrationAPI {

    WalletManager walletManager = WalletManager.getInstance();
    private int RedisMinuteTTL = CraftBlockchainPlugin.getRedisMinuteTTL();

    private CraftBlockchainPlugin blockchainPlugin;

    // singleton, sets wallet to the server wallet in config
    private final String SERVER_WALLET; 
    private final String webappAddress;
    private IntegrationAPI() {    
        blockchainPlugin = CraftBlockchainPlugin.getInstance();

        SERVER_WALLET = blockchainPlugin.getServersWalletAddress();
        if(SERVER_WALLET == null) {
            throw new IllegalStateException("SERVER_WALLET_ADDRESS is not set in config.yml");
        }
        webappAddress = blockchainPlugin.getConfig().getString("SIGNING_WEBAPP_LINK");
        if(webappAddress == null) {
            throw new IllegalStateException("SIGNING_WEBAPP_LINK is not set in config.yml");
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
     * Gets where the user will actually be signing the transaction.
     * @return String Wallet
     */
    public String getWebAppAddress() {
        return webappAddress;
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
     * Sets a players wallet if True, False is an an incorrect wallet address
     * @param player_uuid
     * @return
     */
    public boolean setWallet(UUID player_uuid, String craftWallet) {    
        if(isValidWallet(craftWallet)) {
            walletManager.setAddress(player_uuid, craftWallet);
            return true;
        }
        return false;        
    }

    /**
     * Checks if a wallet is valid
     * @param wallet
     * @return True if valid, False if incorrect
     */
    public boolean isValidWallet(String address) {
        return WalletManager.isValidWallet(address);
    }

    /**
     * Checks if a player has a wallet set in the database / cache
     * @param player_uuid
     * @return
     */
    public boolean hasAccount(UUID player_uuid) {
        return walletManager.getAddress(player_uuid) != null;
    }

    /**
     * Best to be used for actual transactions since we don't like floats
     * @param craft_amount
     * @return
     */
    public long convertCraftToUCRAFT(long craft_amount) {
        return (long)craft_amount * 1_000_000;
    }

    /**
     * Best to be used for displaying only
     * @return Float of CRAFT amount to be human readable
     */
    public float convertUCRAFTtoBeReadable(long ucraft){
        return ((float)ucraft / 1_000_000);
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
     * Gets either CRAFT (false) or UCRAFT (true)
     * @param getSmallerValue
     * @return craft | ucraft
     */
    public String getTokenDenomination(boolean getSmallerValue) {
        return blockchainPlugin.getTokenDenom(getSmallerValue);
    }

    /**
     * Gets the tax rate of the server (ex. 0.05 = 5% rate * any transaction amount (so 105% total)).
     * Is done via the webapp for you
     * @return
     */
    public Double getTaxRate() {
        return blockchainPlugin.getTaxRate();
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
        return BlockchainRequest.transaction(tx, RedisMinuteTTL);
    }

    /**
     * Gives a wallet some tokens (CRAFT) 
     * @param consoleSender
     * @param player_uuid
     * @param amount
     * @return  "" if faucet is disabled, 
     *          or {"transfers":[{"coin":"1token","status":"ok"}]}
     *          or "NO_WALLET" if wallet is null
     */
    public String faucet(String wallet_address, long amount) {
        // {"transfers":[{"coin":"1token","status":"ok"}]}
        return BlockchainRequest.depositToAddress(wallet_address, amount);
    }

    /**
     * Gives a player's wallet some tokens (CRAFT)
     * @param consoleSender
     * @param player_uuid
     * @param amount
     * @return  "" if faucet is disabled, 
     *          or {"transfers":[{"coin":"1token","status":"ok"}]}
     *          or "NO_WALLET" if wallet is null
     */
    public String faucet(UUID player_uuid, long amount) {
        // {"transfers":[{"coin":"1token","status":"ok"}]}
        return faucet(walletManager.getAddress(player_uuid), amount);
    }


    // --------------------------------------------------
    // clickable links / commands / TxId's to make user life better
    public void sendWebappForSigning(CommandSender sender, String message, String hoverMsg) {
		Util.clickableWebsite(sender, 
            getWebAppAddress(), // link which we have the webapp redirect to
            message,
            hoverMsg
        );
	}
    public void sendWebappForSigning(CommandSender sender, String message) {
        sendWebappForSigning(sender, 
            message, 
            "&7&oSign your transaction(s) with the KEPLR wallet"
        );
	}
    public void sendWebappForSigning(CommandSender sender) {
        sendWebappForSigning(sender, 
            "&6&l[!] &e&nClick here to sign your transaction(s)", 
            "&7&oSign your transaction(s) with the KEPLR wallet"
        );
	}
    public void sendWebappForSigning(Player player) {
        sendWebappForSigning((CommandSender)player);
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

    public void sendTxIDClickable(Player player, String TxID) {
		sendTxIDClickable((CommandSender)player, TxID, "&7&oTxID: &n%value%");
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



    // ESCROW ACCOUNTS
    
    /**
     * Deposits CRAFT into an in game account (Escrow). 
     * Each escrow is redeemable for 1x craft, its deposit rate
     */
    public EscrowErrors escrowDeposit(UUID playerUUID, long amount) {
        // creates a Tx to send CRAFT to DAO. On sign, player gets escrow balance
        return EscrowManager.getInstance().deposit(playerUUID, amount);
    }

    public long escrowRedeem(UUID playerUUID, long amount) {
        // If player has enough escrow, their wallet is paid in CRAFT & escrow is subtracted
        return EscrowManager.getInstance().redeem(playerUUID, amount);
    }

    public EscrowErrors escrowSpend(UUID playerUUID, long cost) {
        // Will remove balance & return Success if they can spend
        return EscrowManager.getInstance().spend(playerUUID, cost);
    }

    public long escrowGetBalance(UUID uuid) {
        return EscrowManager.getInstance().getBalance(uuid);
    }



    private static IntegrationAPI instance = null;
    public static IntegrationAPI getInstance() {
        if(instance == null) {
            instance = new IntegrationAPI();
        }
        return instance;
    }
}
