<!-- This is the same key used for the test_node validator
https://docs.cosmwasm.com/docs/1.0/smart-contracts/deployment/ -->


### Setup basic info matching the ./test_node.sh script
```bash
export KEY="mykey"
export KEYALGO="secp256k1"
export CRAFT_CHAIN_ID="test-1"
export CRAFT_KEYRING_BACKEND="test"
export CRAFT_NODE="tcp://65.108.125.182:26657"

echo "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry" | craftd keys add $KEY --keyring-backend $CRAFT_KEYRING_BACKEND --algo $KEYALGO --recover
```

### Clone the cosmwasm nft repo & build the contracts
*Note, this uses an updated workspace-optimizer version compared to the default cw-nfts impl.*
```bash
git clone git@github.com/CosmWasm/cw-nfts.git
cd cw-nfts

docker run --rm -v "$(pwd)":/code \
  --mount type=volume,source="$(basename "$(pwd)")_cache",target=/code/target \
  --mount type=volume,source=registry_cache,target=/usr/local/cargo/registry \
  cosmwasm/workspace-optimizer:0.12.6
```

### Change to where the compiled files are, store bytecode on chain
```bash
cd artifacts
RES=$(craftd tx wasm store cw721_base.wasm --from $KEY --gas auto -y)
```
*We need to query this txhash to get the code_id*

---
```
craftd q tx 3B6383A93244C7D7E2A2EBAD43F3CE254E7C02BE3C960BB7378E89613BA4E00B *code id 3*
craftd q wasm list-code
craftd q wasm code-info 3
```

```sh
# This file shows how the msgs should be contrstructed
https://github.com/CosmWasm/cw-nfts/blob/main/contracts/cw721-base/src/msg.rs
```

```bash
# init the contract as the code it is with the JSON values matching the msg.rs file
craftd tx wasm instantiate 3 '{"name":"craft-re","symbol":"craftt1","minter":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}' --label="This is example craft realestate" --admin craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl --from $KEY --yes
```

```bash
# This gives us our _contract_address
craftd q tx 01F85E3089EED495A2175017173C0E8994B463A4A93309D289F11826BFD3FC8E
# craft1qg5ega6dykkxc307y25pecuufrjkxkaggkkxh7nad0vhyhtuhw3shge3vd
```

---

Create a JSON file with the values needed, then upload to app.pinata.cloud
- https://gateway.pinata.cloud/ipfs/QmXkGh665GVjCCs3cbLLWYwjc3kug1EBGvdyVmhuZRMgNE
*Im not really sure how to property store token_uri data atm, need to look into more.*


<!-- https://docs.cosmwasm.com/docs/1.0/getting-started/interact-with-contract/ -->

```bash
# how is token URI meant to be, supposed to be ipfs json?
craftd tx wasm execute craft1qg5ega6dykkxc307y25pecuufrjkxkaggkkxh7nad0vhyhtuhw3shge3vd '{"mint":{"token_id":"1","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"https://gateway.pinata.cloud/ipfs/QmXkGh665GVjCCs3cbLLWYwjc3kug1EBGvdyVmhuZRMgNE"}}' --from $KEY --yes
```
`craftd q tx D9EF58605BB02C6513D29347EE25B1E8383150828B22B21D20BB1C2A22F6337D`

---

```
CONTRACT="craft1qg5ega6dykkxc307y25pecuufrjkxkaggkkxh7nad0vhyhtuhw3shge3vd"
craftd q wasm contract-state smart $CONTRACT '{"all_nft_info":{"token_id":"1"}}'

craftd q wasm contract-state smart $CONTRACT '{"nft_info":{"token_id":"1"}}'
```