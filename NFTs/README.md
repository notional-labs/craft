### NFTs

This repo is just for CRAFT NFT commands & trying to understand some CosmWasm things from scratch.
Uses a base cw721 contract. Building off the CRAFT v46 chain w/ cosmwasm enabled.


For real estate, we do not use the cw721 spec as intended with a TokenURI.
Instead, it is replaced with a Base64 string which converts to the Properties JSON values.
This is done entirely to reduce needless queries to IPFS from our internal & marketplace WebApp.
Either way, they are still NFTs

## marketplace
The marketplace is a fork of hackatom_v. It needs updating to v0.13.2, which is why it does not share a dir with the base_contracts for now. This merge can be done in the future