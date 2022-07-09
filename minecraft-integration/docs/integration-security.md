# Integration Documentation

## Intro
The role of integration is to combine a cosmos based blockchain with the popular game, Minecraft, on the CraftEconomy server. This allows for transactions to be generated in game which when signed, will run the functions and in game features related to said Tx. This must be done with the consideration in mind that one of more services could be compromised, which is where the cosmos based blockchain helps. This documentation outlines these design considerations and how they are implemented securely.

---
## Infrastructure Requirements
Bare:
- Minecraft server running Spigot or any fork using version 1.18.X
- A v46 chain (CRAFT in this case)
- A redis database (stores transaction temp transaction data)
- A webapp to sign transactions
- An API to make interfacing with the webapp <-> redis database easier.

API:
- POST /api/signed/:txuuid/:tenderminthash 
    - when a transaction is signed from the webapp successfully (CosmJS)
    - writes the key signed_{txuuid} with value {tenderminthash} to redis
- GET /api/tx/:txuuid
    - gets transaction specific information (TO, MEMO, AMOUNT, etc)
    - Stored as tx_*_{txuuid}, where * is their wallet address
- GET /api/all/:walletaddress
    - returns all transactions for a given wallet address
    - returns a list from redis via the following query: "tx_{walletaddress}_*"
        - Where * is all the UUIDs found pending

---
## Implementation
The integration plugin & API is used to allow CRAFT developers to create and store pending blockchain transactions. The goal is to ensure that the developer does not need to learn how the blockchain works, but rather just use the provided builder to create the transaction.

This is done by having the Integration plugin create a key value pair between a Unique ID (uuid) & the transaction data from the Transaction Builder. By having a Transaction Builder, a developer can specify the specifics of the transaction requirements & include a Java (Bi)Consumer lambda function to run after completion. 

For example:

UserA wants to purchase a business license from the server for 100 $CRAFT
A developer can do the following (pseudocode):

```java
Tx tx = new Tx();
tx.sendToServerWallet();
tx.setMemo("UserA bought a Business License");
tx.setAmount(100);
tx.setFunction(MyClass.GiveUserBusinessLicense(UserA.getUUID()))

tx.submit();
```
And that is it! The blockchain-integration takes care of the rest. 
This is done by having the API auto generate the required data behind the scenes such as the:
- unique transaction id
- generate the tax amount & where to send it too
- saving data to redis 
- listening for signed transaction uuids (per server basis, based on where they were generated)
- running the setFunction() lambda once a signed transaction comes through
all without the developer having to worry about the details.

Once a transaction is submit()'ed, its in a queue waiting for UserA to sign it.

UserA gets a link to go to the webapp & connects his keplr wallet (CRAFT address).
UserA presses on on the transaction, and expands seeing the details match the above generation
(They are sending the funds to the SERVER, it cost 100, and the Memo is what they wanted to do)

They then click sign transaction. Within 6 seconds CosmJS will return a tenderminthash (Tx hash) which confirms the Tx was successful. Now the webapp POST to the API with the :txuuid & this new :tenderminthash.

Integration sees this POST & checks if that :txuuid is in the servers cache. This ensures we only run the function on the server which the user requested it. If this txuuid was on this server, we grab that Tx object we saved from submit() & also grab the tendermint Tx hash.
We now query the Tx hash from the minecraft server & confirm the Tx requirements match with the signed transaction.
If they do, we run the function for UserA and they get their business license.

---
## Security Considerations
Integration was written to ensure that the webapp, redis, and API can ALL be compromised without impacting the state of the minecraft server. This is why the webapp must POST the txUUID & the tendermint tx hash together.

If we only passed through the txuuid and the webapp was compromised, an attacker could just POST any txUUID they generate in game. This would NOT allow for any privilege escalation, but would allow for them to gain CRAFT tokens (via fake trades) & gain free access/items from the server.

To resolve this, when integration sees a POST request, it will check the value of the BankSend messages -> the messages we generated in game with the Tx Builder. Given the state a tendermint Tx hash is immutable, we can confirm this is valid so long as 67% of the chain is not compromised (BFT Consensus limitation). This ensures we confirm the TO, AMOUNT, MEMO,  TAX_TO, and TAX_AMOUNT all are the same. If they are not, we know this transaction does not match the UUID it was generated for. Allowing us to skip running any in game code for it.

Now assuming the webapp IS compromised, the above solution STILL would allow for an attacker to generate a transaction 1 time, and re-run this transaction as many times as they want (Double signing in game functions).
Example:
```
User A compromises the webapp, redis server, AND our API. (We assume all in this case)

User A runs feature in game to buy 10 bread for 1CRAFT.
User A signs this in the webapp (This is a valid sign just as any player would do).
- The user then knows this returned a Tx hash which = "1A2B3CD4" from CosmJS.

The user now generates this same transaction in game: "10 bread for 1 craft".
With them now controlling the API, they can send through a POST for this new txUUID & the SAME tendermint txhash as their first transaction, since the AMOUNT, TO, FROM, and DESCRIPTION have not changed.
```

To resolve this, Integration will append the unix timestamp (in milliseconds) to the end of every description from the TxBuilder. This way the same Tendermint TxHash would not work with any other transactions since the description time is different, even if it is the same function in game. (Refer to paragraph 3 of this section "To resolve this")

By doing all of the above, CRAFT is able to securely implement a solution to allow for Minecraft to work with a Cosmos based blockchain!