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
<repository>
    <id>repsy</id>
    <name>craft-integration</name>
    <url>https://repo.repsy.io/mvn/reecepbcups/craft-integration</url>
</repository>

<dependency>
    <groupId>com.crafteconomy</groupId>
    <artifactId>craft-integration</artifactId>
    <version>3.0.1</version>
    <scope>provided</scope>
</dependency>

mvn clean
mvn compile
mvn install


plugin.yml -> depend: ["craft-integration"]

IntegrationAPI api = CraftBlockchainPlugin.getAPI();

String wallet   = api.getWallet(uuid);
long balance    = api.getBalance(uuid);
String value    = api.deposit(sender, uuid, longAmount);

Transactions:

Tx txInfo = api.createNewTx(uuid, to_wallet_string, amount, description, Consumer<UUID> Function);
ErrorTypes error = api.submit(txInfo);

OR

Tx txInfo = new Tx();
txinfo.setPlayerUUID(uuid);
txinfo.setToWallet(to_wallet);
txinfo.setAmount(10);
txinfo.setDescription("Memo");
txinfo.setFunction((Consumer<UUID>) Logic.purchaseBusinessLicense());

UUID txID = txInfo.getTxID();

ErrorTypes error = api.submit(txInfo);
ErrorTypes.NO_ERROR -> Successful submit for a pending transaction [not yet signed]

```
