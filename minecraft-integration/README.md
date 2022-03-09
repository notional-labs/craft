# CRAFT Integration


## Setup
```
Load plugin(s)

Set MongoDB server, Redis-Server, and other information in craft-integration/config.yml

Install keplr, visit: http://65.108.71.66/ [or any testnet with :1317 and :26657]

Copy address for CRAFT wallet

Then in game do “/wallet set <craft-address>”

/wallet faucet ADDRESS 500 [will be disabled]
```

## Commands
```
/wallet help
/wallet balance <player/address>
/wallet set <address>
/wallet pay <player/wallet> <amount>
/wallet supply (shows stake and craft)
/wallet pending

ADMIN:
/wallet faucet <name/wallet> <amount> [Will be firewalled in the future]
/wallet faketx <license/purchase> [item]
/wallet fakesign <generated_TxID>
/wallet allpending
```

## Testing Commands
```
  /test-balanceapi (Show balance of current wallet)
  /test-walletapi (Get current wallet as a WalletClickable)
  /test-exampleapi (Generates a Tx which prints out a user bought something. Requires /wallet fakesign <id>)
  /test-tokensapi (Show total CRAFT and STAKE tokens in supply)
  /test-trade (Req. 2 players. Hold items, confirm trade amount, items are taken. On Blockchain sign trade items as agreed upon)
  /test-keplr (Send user link to KEPLR wallet documentation)
  /test-escrowspend (pays 1 craft for some dirt to ensure it works correctly)
```

## API
```
https://repsy.io/reecepbcups/maven/craft-integration/artifacts/com.crafteconomy

<repository>
    <id>repsy</id>
    <name>craft-integration</name>
    <url>https://repo.repsy.io/mvn/reecepbcups/craft-integration</url>
</repository>

<dependency>
    <groupId>com.crafteconomy</groupId>
    <artifactId>craft-integration</artifactId>
    <version>3.3.1</version>
    <scope>provided</scope>
</dependency>


plugin.yml -> depend: ["craft-integration"]

IntegrationAPI api = CraftBlockchainPlugin.getAPI();


Standard Request:
String wallet   = api.getWallet(uuid);
long balance    = api.getBalance(uuid);
String swallet  = api.getServerWallet();
String webapp   = api.getWebAppAddress();

String value    = api.deposit(uuid, longAmount); // sends funds to player from faucet
String value    = api.deposit(wallet_address, longAmount);


Formating:
String denom     = api.getTokenDenomination(boolean getSmallerValue);   // true = ucraft, false = craft
float readableCraftValue = api.convertUCRAFTtoBeReadable(long ucraft);  // 1000000ucraft = 1 craft 
long craftAmount = api.convertCraftToUCRAFT(long craft_amount);         // 0.1craft -> 100,000ucraft, for submitting to chain

Account Wallets:
boolean hasAcc = api.hasAccount(uuid);
boolean valid = api.isValidWallet(String wallet);
boolean validSet = api.setWallet(uuid, wallet);

-

Escrow Accounts: Returns EscrowErrors.SUCCESS if successful
EscrowErrors err = api.escrowDeposit(UUID, amount); 
err = api.escrowRedeem(UUID, amount); // in game back to wallet
err = api.escrowSpend(UUID, cost); // purchase something in game with balance if any
err = api.escrowGetBalance(UUID);

-

Transactions:

Tx tx1 = api.createNewTx(uuid, to_wallet, amt, desc, Consumer<UUID> Function);
Tx tx2 = api.createNewTx(uuid, to_wallet, amt, desc, BiConsumer<UUID, UUID> Function);

OR

Tx tx = new Tx();
tx.setFromUUID(fromUUID);
tx.setToUUID(toUUID); // biConsumer only
tx.setToWallet(to_wallet);
tx.setAmount(10);
tx.setDescription("Memo here");

tx.setFunction((Consumer<UUID>) Logic.purchaseBusinessLicense()); // single payments
OR
tx.setBiFunction(Logic.trade(Player1UUID, Player2UUID)); // p2p

// submits transaction for user to sign via webapp, returns ErrorTypes.NO_ERROR if successful
ErrorTypes error = api.submit(txInfo); 
OR
txInfo.submit(boolean includeTxClickable, boolean sendDescMessage, boolean sendWebappLink)

Getting values from a Tx
    UUID fromUUID    = tx.getFromUUID();
    UUID toUUID      = tx.getToUUID();
    UUID txID        = tx.getTxID();
    long amt         = tx.getAmount();
    String desc      = tx.getDescription();
    String toWallet  = tx.getToWallet();
    Consumer c       = tx.getFunction(); || tx.getBiFunction();

-

Messaging:
    // Link to sign on webapp, useful when a transaction has been submitted OR just tx.submit(false, false, true)
    api.sendWebappForSigning(CommandSender sender, String fromWallet);
    api.sendWebappForSigning(Player player);

    // Link to take user to documents page (useful when getWallet==null)
    api.sendClickableKeplrInstallDocs(sender);

    **Format = "%value%" as placeholder

    // Sends user a TxID which can be clicked and coppied to keyboard
    api.sendTxIDClickable(sender, TxIDString, format, hoverMessage);
    api.sendTxIDClickable(sender, TxIDString, format);
    api.sendTxIDClickable(sender, TxIDString);
    api.sendTxIDClickable(player, TxIDString);

    // Sends user their wallet & allows for them to click copy it
    api.sendWalletClickable(sender, wallet, format, hoverMessage);
    api.sendWalletClickable(sender, wallet, format);
    api.sendWalletClickable(sender, wallet);
```
