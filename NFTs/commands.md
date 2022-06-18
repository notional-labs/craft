<!-- This is the same key used for the test_node validator
https://docs.cosmwasm.com/docs/1.0/smart-contracts/deployment/ -->


### Setup basic info matching the ./test_node.sh script
```bash
export KEY="mykey"
export KEYALGO="secp256k1"
export CRAFT_CHAIN_ID="test-1"
export CRAFT_KEYRING_BACKEND="test"
export CRAFT_NODE="tcp://65.108.125.182:26657"

# test contract
# craft1qg5ega6dykkxc307y25pecuufrjkxkaggkkxh7nad0vhyhtuhw3shge3vd

echo "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry" | craftd keys add $KEY --keyring-backend $CRAFT_KEYRING_BACKEND --algo $KEYALGO --recover
```

### Clone the cosmwasm nft repo & build the contracts
*Note, this uses an updated workspace-optimizer version compared to the default cw-nfts impl.*
```bash
git clone git@github.com:CosmWasm/cw-nfts.git
cd cw-nfts

docker run --rm -v "$(pwd)":/code \
  --mount type=volume,source="$(basename "$(pwd)")_cache",target=/code/target \
  --mount type=volume,source=registry_cache,target=/usr/local/cargo/registry \
  cosmwasm/workspace-optimizer:0.12.6
```

### Change to where the compiled files are, store bytecode on chain
```bash
cd artifacts

# Gets the TXHASH from the output as a raw string (no quotes) so we can query & get the code id
TX_HASH=$(craftd tx wasm store cw721_base.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')
CODE_ID=$(craftd q tx $TX_HASH --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
echo $CODE_ID
```

---
```bash
#craftd q tx $TX_HASH # OLD manual way of doing it
#craftd q wasm list-code
craftd q wasm code-info $CODE_ID
```

```sh
# This file shows how the msgs should be contrstructed
https://github.com/CosmWasm/cw-nfts/blob/main/contracts/cw721-base/src/msg.rs
```

```bash
# init the contract as the code it is with the JSON values matching the msg.rs file
INIT_TX_HASH=$(craftd tx wasm instantiate 3 '{"name":"craft-re","symbol":"craftt1","minter":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}' --label="This is example craft realestate" --admin craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl --from $KEY --yes --output json | jq -r '.txhash')
```

```bash
# This gives us our _contract_address
CONTRACT_ADDRESS=$(craftd q tx $INIT_TX_HASH --output json | jq -r '.logs[].events[] | select(.type == "instantiate").attributes[] | select(.key == "_contract_address").value')
```

---

Create a JSON file with the values needed, then upload to app.pinata.cloud
https://gateway.pinata.cloud/ipfs/QmXkGh665GVjCCs3cbLLWYwjc3kug1EBGvdyVmhuZRMgNE
- *Im not really sure how to property store token_uri data atm, need to look into more.*
- *Can probably automate these uplaods with python pinata API?*

<!-- https://docs.cosmwasm.com/docs/1.0/getting-started/interact-with-contract/ -->

```bash
# how is token URI meant to be, supposed to be ipfs json?
TX_INFO=$(craftd tx wasm execute $CONTRACT_ADDRESS '{"mint":{"token_id":"2","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"https://gateway.pinata.cloud/ipfs/QmXkGh665GVjCCs3cbLLWYwjc3kug1EBGvdyVmhuZRMgNE"}}' --from $KEY --yes --output json | jq -r '.txhash')
```
`craftd q tx $TX_INFO`

---

```
craftd q wasm contract-state smart $CONTRACT_ADDRESS '{"all_nft_info":{"token_id":"1"}}'

craftd q wasm contract-state smart $CONTRACT_ADDRESS '{"nft_info":{"token_id":"1"}}'
```


Need to find how to do the above queries via RPC
http://65.108.125.182:26657/abci_query?path=_&data=_&height=_&prove=_