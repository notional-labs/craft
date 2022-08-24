# Implementation

# Overview

The Integration plugin creates a simple solution for generating blockchain transactions to be signed. The API & Tx builder gives just enough to create Minecraft functions without having to deal with all the backed understanding of a cosmos & signing.

You can read a rough overview [HERE](https://github.com/notional-labs/craft/blob/master/minecraft-integration/docs/security.md) which includes the backend, security mitigation techniques, and infrastructure.

# Maven Import

```html
<!-- [https://repsy.io/reecepbcups/maven/craft-integration/artifacts/com.crafteconomy](https://repsy.io/reecepbcups/maven/craft-integration/artifacts/com.crafteconomy) -->
<repository>
    <id>repsy</id>
    <name>craft-integration</name>
    <url>https://repo.repsy.io/mvn/reecepbcups/craft-integration</url>
</repository>

<dependency>
    <groupId>com.crafteconomy</groupId>
    <artifactId>craft-integration</artifactId>
    <version>4.4.1</version>
    <scope>provided</scope>
</dependency>
```

# Plugin Implementation

Example in production: [CRAFT PRIVATE GITLAB LINK](https://gitlab.com/craft-economy/plugins/craft-2fa-authentication/-/blob/master/src/main/java/com/crafteconomy/authentication/command/RequestAuthenticateCommand.java)

*& also in github → notional-labs/craft Integration → test plugin*

## plugin.yml

`depend: ["craft-integration"]`

## API

`IntegrationAPI api = CraftBlockchainPlugin.getAPI();`

---

## Blockchain Request

[https://github.com/notional-labs/craft/blob/master/minecraft-integration/craft-integration/src/main/java/com/crafteconomy/blockchain/api/IntegrationAPI.java](https://github.com/notional-labs/craft/blob/master/minecraft-integration/craft-integration/src/main/java/com/crafteconomy/blockchain/api/IntegrationAPI.java)

```java
String wallet   = api.getWallet(uuid);
boolean result  = api.setWallet(uuid, craftwallet)

// Gets balance from chain query
long ubalance    = api.getUCraftBalance(uuid);
float balance    = api.getCraftBalance(uuid);

// The DAO's wallet
String swallet  = api.getServerWallet();

// Will deposit money from DAO wallet -> a Players wallet
CompletableFuture<FaucetTypes> cf = api.faucetUCraft(addr, description, ucraft);
... cf = api.faucetUCraft(uuid, description, ucraft);
... cf = api.faucetCraft(wallet_addr, description, craft);
... cf = api.faucetCraft(uuid, description, craft);
// thenAccept(status -> {... logic here});

// Gets the link to the webapp signing location
String webappAddr = api.getWebAppAddress()

// Get the USD price as a float, just cosmetic nice to have
CompletableFuture<Float> usd_price = api.getCraftUSDPrice();

// Gets a decimal tax rate. This is automatically added to Txs after generation
Double rate = api.getTaxRate();

// manually removes a transaction from the server & redis AND runs the logic which
// expires a transaction (if set). This is useful to revert a change without waiting
// the full time for the Tx to expire (such as wagering in minigame lobby)
boolean wasSuccessful = api.expireTransaction(txinfo.getTxID());
```

# Escrow

```java
// converts CRAFT on chain -> escrow account in game 1 for 1 rate
EscrowErrors escrowUCraftDeposit(UUID playerUUID, long ucraft_amount)
EscrowErrors escrowCraftDeposit(UUID playerUUID, float craft_amount)

// converts escrow -> on chain money upon request IF they have enough
long escrowRedeem(UUID playerUUID, float craft_amount)

// remove balance & return Success if they can spend
EscrowErrors escrowUCraftSpend(UUID playerUUID, long ucraft_cost)
EscrowErrors escrowCraftSpend(UUID playerUUID, float craft_cost)

// pay between accounts
EscrowErrors escrowPayPlayerUCraft(from_uuid, to_uuid, long ucraft_amount)
EscrowErrors escrowPayPlayerCraft(from_uuid, to_uuid, float craft_amount)

long escrowGetUCraftBalance(UUID uuid)
float escrowGetCraftBalance(UUID uuid)

long escrowUCraftRedeem(uuid, float craft_amount)
long escrowCraftRedeem(uuid, long ucraft_amount)

// TODO:
// Get DAO Account (all 9's from string) & allow people to pay -> it.

// /escrow pay <OnlinePlayerName> <FloatCraftAmount>
// /escrow balance
// /escrow deposit <CraftAmount>
// /escrow withdraw <CraftAmountFromEscrow>
```

# Transactions

[https://github.com/notional-labs/craft/blob/master/minecraft-integration/craft-integration/src/main/java/com/crafteconomy/blockchain/transactions/Tx.java](https://github.com/notional-labs/craft/blob/master/minecraft-integration/craft-integration/src/main/java/com/crafteconomy/blockchain/transactions/Tx.java)

## INLINE - Tx for 1 user

```java
Tx tx = api.createNewTx(uuid, to_wallet, amt, memo, Consumer<UUID> function);
```

## INLINE - Tx for 2 users (Trading)

```java
Tx tx2 = api.createNewTx(uuid, to_wallet, amt, desc, BiConsumer<UUID, UUID> function);
```

## Query Values from a generated Tx

```java
UUID fromUUID    = tx.getFromUUID();
UUID toUUID      = tx.getToUUID();
UUID txID        = tx.getTxID();

long ttl         = tx.getRedisMinuteTTL();

long amt         = tx.getUCraftAmount();
float craft      = tx.getCraftAmount();

String desc      = tx.getDescription();
String toWallet  = tx.getToWallet();
Consumer c       = tx.getFunction();
BiConsumer b     = tx.getBiFunction();
```

## Create Tx from Scratch

```java
Tx tx = new Tx();
tx.setFromUUID(fromUUID);
// tx.setToUUID(toUUID); // biConsumer only

tx.setToWallet(to_wallet);
// OR
tx.setToWalletAsServer();

tx.setTxType(TransactionType.DEFAULT);
tx.setCraftAmount(10);
tx.setDescription(fromUUID + " paid " + toUUID + " 10 for their trade of items");

tx.setRedisMinuteTTL(3); // OPTIONAL: expires in 1 minutes

// OPTIONAL: On expire, we may want to run code (such as giving items back from a middleman trade)
tx.setConsumerOnExpire(Examples.revertSomeActionOnExpire());
tx.setBiConsumerOnExpire(Examples.revertSomeActionOnExpireFor2People());

tx.setFunction((Consumer<UUID>) Logic.purchaseBusinessLicense());
// OR
tx.setBiFunction(Logic.trade(Player1UUID, Player2UUID)); 
```

## Set Transaction Messages

*optional*

```java
// You can automatically set sending messages to user on submit
// These are false by default.
tx.setIncludeTxClickable(true);
tx.setSendDescMessage(true);
tx.setSendWebappLink(true);
```

## Submit Transaction to be signed

```java
// Subit the Tx to be signed by the user
ErrorTypes err = tx.submit()
if(err != ErrorTypes.SUCCESS) { // I should really change this to SUCCESS
  p.sendMessage("Error submitting auth transaction to chain: " + err.toString());
  return;
}

// inform user link to sign. Not required if you used tx.sendWebappLink(true);
// Internal
TextCraftComponent tcc = new TextCraftComponent("auth.code.website", "\n&6[!] &fClick here to unlock your account (2fa)!\n");
tcc.clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl(api.getWebAppAddress()));
tcc.send(player);
// Other
p.sendMessage("Sign your Tx at " + api.getWebAppAddress());
```

# Clickables / Messages

```java
/* 
NOTE: This is required if you want more specific messages.
If you want this done automatically when building a Tx, set it on the Tx.
- tx.setIncludeTxClickable(true);
- tx.sendDescMessage(true);
- tx.sendWebappLink(true);
before .submit()
*/

sendWebappForSigning(CommandSender sender, String message, String hoverMsg)
sendWebappForSigning(CommandSender sender, String message)
sendWebappForSigning(CommandSender sender)
sendWebappForSigning(Player player)

// Link to take user to documents page (useful when getWallet==null)
sendClickableKeplrInstallDocs(CommandSender sender)

// Format requires "%value%" as the replace placeholder
sendTxIDClickable(CommandSender sender, String TxID, String format, String hoverMessage)
sendTxIDClickable(CommandSender sender, String TxID, String format)
sendTxIDClickable(CommandSender sender, String TxID)

// Send a users wallet too them, on click it coppies. 
// Format requires "%value%" as the replace placeholder
sendWalletClickable(CommandSender sender, String wallet, String format, String hoverMessage)
sendWalletClickable(CommandSender sender, String wallet, String format)
sendWalletClickable(CommandSender sender, String wallet)
```

# Custom Events

NOTE: YOU WILL LIKELY NEVER NEED TO TOUCH THESE

```jsx
public void onExpiredTxEvent(ExpiredTransactionEvent event)
event.getTxID(); // with this, you could get the pending Tx from PendingTransactions

public void onSignedTransactionEvent(SignedTransactionEvent event)
event.getTxID();
```