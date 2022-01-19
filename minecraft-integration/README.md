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
    <version>3.1.6</version>
    <scope>provided</scope>
</dependency>


plugin.yml -> depend: ["craft-integration"]

IntegrationAPI api = CraftBlockchainPlugin.getAPI();


Standard Request:
String wallet   = api.getWallet(uuid);
long balance    = api.getBalance(uuid);
String value    = api.deposit(sender, uuid, longAmount);
String swallet  = api.getServerWallet();


Transactions:

Tx txInfo = api.createNewTx(uuid, to_wallet, amt, desc, Consumer<UUID> Function);
Tx txInfo2 = api.createNewTx(uuid, to_wallet, amt, desc, BiConsumer<UUID, UUID> Function);

OR

Tx txInfo = new Tx();
txinfo.setPlayerUUID(uuid);
txinfo.setToWallet(to_wallet);
txinfo.setAmount(10);
txinfo.setDescription("Memo");

txinfo.setFunction((Consumer<UUID>) Logic.purchaseBusinessLicense()); // single payments
OR
txinfo.setBiFunction(Logic.trade(Player1UUID, Player2UUID)); // p2p

UUID txID = txInfo.getTxID();

// submits transaction for user to sign
ErrorTypes error = api.submit(txInfo); 
ErrorTypes.NO_ERROR // Successful submit for a pending transaction [not yet signed]


Messaging:
    // Link to sign on webapp, useful when a transaction has been submitted
    api.sendWebappForSigning(CommandSender sender, String fromWallet);

    // Link to take user to documents page (useful when getWallet==null)
    api.sendClickableKeplrInstallDocs(sender);

    **Format = "%value%" as placeholder

    // Sends user a TxID which can be clicked and coppied to keyboard
    api.sendTxIDClickable(sender, TxIDString, format, hoverMessage);
    api.sendTxIDClickable(sender, TxIDString, format);
    api.sendTxIDClickable(sender, TxIDString);

    // Sends user their wallet & allows for them to click copy it
    api.sendWalletClickable(sender, wallet, format, hoverMessage);
    api.sendWalletClickable(sender, wallet, format);
    api.sendWalletClickable(sender, wallet);



```
