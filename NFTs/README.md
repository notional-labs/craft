### NFTs

This repo is just for CRAFT NFT commands & trying to understand some CosmWasm things from scratch.
Uses a base cw721 contract. Building off the CRAFT v46 chain w/ cosmwasm enabled.


For realestate, we do not use the cw721 spec as intended with a TokenURI.
Instead if is replaced with a Base64 string whih converts to the Properties JSON values.
This is done entirely to reduce needless queries to IPFS from our internal & marketplace webapp.
Either way, they are still NFTs

## CW20
This is a temp project and will be replaced with the Bank module features in the future. THis way CRAFT directly interfaces with the 721/marketplace instead of an intermediate, unless this somehow is easier to do? unsure

## marketplace
Marketplace is a fork of hackatom_v. It needs updating to v0.13.2, which is why it does not share a dir with the base_contracts for now. This merge can be done in the future