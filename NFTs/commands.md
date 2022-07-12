
# Replaced by test_script.sh in ./already_compiled/

```bash
export KEY="mykey"
export KEY2="mykey2"
export KEY_ADDR=`craftd keys show $KEY -a`
export KEY_ADDR2=`craftd keys show $KEY2 -a`
export KEYALGO="secp256k1"
export CRAFT_CHAIN_ID="test-1"
export CRAFTD_KEYRING_BACKEND="test"
export CRAFTD_NODE="tcp://65.108.125.182:26657"
export CRAFTD_COMMAND_ARGS="--gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY"

# This is the same key we use in ./testnode.sh. The 2nd is just a random key generated ONLY for testing with craft NFTs
# craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
echo "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry" | craftd keys add $KEY --keyring-backend $CRAFTD_KEYRING_BACKEND --algo $KEYALGO --recover
# craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r
echo "flag meat remind stamp unveil junior goose first hold atom deny ramp raven party lens jazz tape dad produce wrap citizen common vital hungry" | craftd keys add $KEY2 --keyring-backend $CRAFTD_KEYRING_BACKEND --algo $KEYALGO --recover
```

```bash
cd base_contacts/artifacts/
TX20=$(craftd tx wasm store cw20_base.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')

cd ../../marketplace/artifacts/
TXM=$(craftd tx wasm store nftext_manager.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')
```

```bash
# jq -r '.logs[0].events[-1].attributes[0].value'
C20=$(craftd q tx $TX20 --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
C721=$(craftd q tx $TX721 --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
CM=$(craftd q tx $TXM --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
```

```bash
export C20=3
export C721=2
export CM=5
echo $C20
echo $C721
echo $CM
```

# now we need to init them

```bash
craftd tx wasm instantiate $C20 '{
  "name": "craft-cw-20-placeholder2",
  "symbol": "CRAFTR",
  "decimals": 6,
  "initial_balances": [
    {
      "address": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl",
      "amount": "10000"
    }
  ],
  "mint": {
    "minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
  }
}' --label "cw20-base2" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR
# gets latest instantiated contract address
# export ADDR20=$(craftd q wasm list-contract-by-code $C20 --output json | jq -r '.contracts[-1]')
# export ADDR20=craft1qg5ega6dykkxc307y25pecuufrjkxkaggkkxh7nad0vhyhtuhw3shge3vd
export ADDR20=craft1q23d30x94cm8ve243pxdc52m398r4l5ecgcfp8tud3vggcsq8s2qz0py27
```

```bash
craftd tx wasm instantiate $C721 '{
  "name": "craftd-realestate-6",
  "symbol": "CRE",
  "minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
}' --label "cw721-realestate-craft" $CRAFTD_COMMAND_ARGS -y --admin $KEY_ADDR

craftd tx wasm instantiate $C721 '{
  "name": "craftd-skins-2",
  "symbol": "CSKINS",
  "minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
}' --label "cw721-realestate-craft" $CRAFTD_COMMAND_ARGS -y --admin $KEY_ADDR

craftd tx wasm instantiate $C721 '{
  "name": "craftd-test721",
  "symbol": "ctest2",
  "minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
}' --label "cw721-base-test" $CRAFTD_COMMAND_ARGS -y --admin $KEY_ADDR

export ADDR721=craft187zds75uenfxht2zqz7e0wxn3ushcawvf2ndrns6q63hgfn6ptqqsuflma
export ADDR721_SKINS=craft15jzyzklz8rq9gy38p4kcall0nqr8exglqg5870gglaq4c976vlxsa5uznc
export ADDR721_TEST_NORMAL=craft1zjd5lwhch4ndnmayqxurja4x5y5mavy9ktrk6fzsyzan4wcgawnq7d4srp
```

```bash
craftd tx wasm instantiate $CM '{
  "name": "craft-marketplace-nfts6"
}' --label "marketplace" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR
export ADDRM=craft13h9k5rsrgveg6sdtzg34qg499ns0e5kku74kapnskegtwyfspf6qhxcdfh
```

---
init an NFT w/ data.
THIS IS NOW DONE IN THE MintNFTsFromMongodb.py script for automation

```bash
# TXMINT=$(craftd tx wasm execute $ADDR721 '{"mint":{"token_id":"1","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"https://gateway.pinata.cloud/ipfs/QmXkGh665GVjCCs3cbLLWYwjc3kug1EBGvdyVmhuZRMgNE"}}' --from $KEY --yes --output json | jq -r '.txhash')
## craftd q wasm contract-state smart $ADDR721 '{"all_nft_info":{"token_id":"1"}}'

export JSON_ENCODED=`echo '{"uuid": "11111","name": "MyNFTproperty", "type": "HOME", "description": "This is my NFT", "image": "https://image.com/1.png"}' | base64 -w 0` #&& echo $JSON_ENCODED
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"2","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` && echo $EXECUTED_MINT_JSON

TXMINT=$(craftd tx wasm execute $ADDR721 $EXECUTED_MINT_JSON --from $KEY --yes --output json | jq -r '.txhash')


# Export Base64 encoded JSON as a raw string (no quotes)
export JSON_VALUES=`echo $(craftd q wasm contract-state smart $ADDR721 '{"all_nft_info":{"token_id":"2"}}' --output json) | jq -r '.data.info.token_uri'`
echo $JSON_VALUES | base64 --decode #| jq '.uuid'
```

----
https://github.com/BlockscapeNetwork/hackatom_v/tree/master/contracts/marketplace

# sell token
```bash
# ADDR20 address required
export NFT_LISTING_BASE64=`printf '{"list_price":{"address":"%s","amount":"13","denom":"token"}}' $ADDR20 | base64 -w 0` && echo $NFT_LISTING_BASE64
# send_nft from 721 -> marketplace contract =  $ADDRM
export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"2","msg":"%s"}}' $ADDRM $NFT_LISTING_BASE64`
craftd tx wasm execute $ADDR721 $SEND_NFT_JSON --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY
```

# Query all NFTs you are selling
```bash
craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}' 
```

# Query all NFTs you hold in the 721
```bash
# `owner_of`, `approval`, `approvals`, `all_operators`, `num_tokens`, `contract_info`, `nft_info`, `all_nft_info`, `tokens`, `all_tokens`, `minter`
craftd query wasm contract-state smart $ADDR721 '{"contract_info":{}}' # name and symbol
craftd query wasm contract-state smart $ADDR721 '{"all_nft_info":{"token_id":"1"}}' # owner & metadata on an NFT in 1 query

# If they are offering it for sale, they don't show here / it would be removed from them.
craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","start_after":"0","limit":50}}' # owners token IDs
```

<!-- The below don't work with current nftext_manager::state::Offering not found -->
# cancel selling
```bash
# If unknown request, double check the offering id. it is not the same as the actuasl token id.
# nftext_manager::state::Offering not found: execute wasm contract failed: unknown request. Should update this msg
craftd tx wasm execute $ADDRM '{"withdraw_nft":{"offering_id":"2"}}' $CRAFTD_COMMAND_ARGS -y
```

# Buying an NFT from the marketplace
```bash
export OFFERING_ID_MSG_BASE64=`printf '{"offering_id":"1"}' | base64 -w 0` && echo $OFFERING_ID_MSG

# We execute on the CW20, the contract is the marketplace $ADDRM
# The contract address (ADDRM) should match the get_offerings{} query
export SEND_ADDR20_FOR_NFT_JSON=`printf '{"send": {"contract": "%s","amount": "15","msg": "%s"}}' $ADDRM $OFFERING_ID_MSG_BASE64`
craftd tx wasm execute $ADDR20  --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY2
```

# Sending cw20 balance from KEY -> KEY2 for testing above
```bash
craftd tx wasm execute $ADDR20 '{
  "transfer": {
    "recipient": "craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r",
    "amount": "5"
  }
}' --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY

craftd tx bank send $KEY $KEY_ADDR2 10000token -y

# Gets the balance of the new key
craftd query wasm contract-state smart $ADDR20 '{"token_info": {}}' #CRAFTR
craftd query wasm contract-state smart $ADDR20 '{"balance": {"address": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}}' 
craftd query wasm contract-state smart $ADDR20 '{"balance": {"address": "craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r"}}' 
```