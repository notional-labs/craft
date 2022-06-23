<!--
order: 1
-->

# Concepts
This chain is a Dao, governed by a transferable token called exp. User joins dao by adding ibc token (possibly stable coin) to the system and get gov token back. These tokens can be used to vote in system changes. IBC token and its price are added to the system by dao address (after being voted by the dao members)


## How to get gov token
There are 2 ways to join Dao and get gov tokens: 
- Use ibc tokens (of other chains in the ecosystem like OSMO, JUNO,...) 
- Use non ibc tokens (ETH, BTC, or any other payment method)

## Non IBC tokens
- After the user pays (currently we have not processed non ibc payments on the chain). Send a request for the amount to be minted to DAO.
- The DAO address will execute requests, mint gov tokens to the user's address. Also take some fees to the community pool for validators.

## IBC tokens
- Fully processed on-chain
- User sends request, with attached IBC tokens in exchange for gov tokens
- After each block, the system will check the requests and execute request, mint the amount of gov corresponding to the amount of locked IBC tokens, based on the price stored in DaoAssetInfo
- Also take some fees to the community pool for validators.
- Later users can burn gov tokens to get their locked IBC tokens back. 28 days unlock

## Fees

Craft uses a fee model that differs from most Cosmos SDK-based blockchains.  There are no base gas fees on transactions. Instead, Craft uses custom fee logic implementations with fees only for certain transactions.  This fee model enables certain transactions to occur for free to provide a 0-barrier-to-entry experience for users.

Fees always exist on the following transactions:
- Request mint, burn gov token
- Join DAO
- Vote

Fees are optional on the recipe execution transaction.

## Accounts

Since there are no fees on the Craft chain, accounts need to be created for users with no coins in their balances.  Also limit the maximum amount of gov tokens per user