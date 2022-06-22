```bash
export KEY="mykey"
export KEYALGO="secp256k1"
export CRAFT_CHAIN_ID="test-1"
export CRAFT_KEYRING_BACKEND="test"
export CRAFTD_NODE="tcp://65.108.125.182:26657"

# This is the same key we use in ./testnode.sh
echo "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry" | craftd keys add $KEY --keyring-backend $CRAFT_KEYRING_BACKEND --algo $KEYALGO --recover
```

```bash
cd base_contacts/artifacts/
TX20=$(craftd tx wasm store cw20_base.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')

cd ../../marketplace/artifacts/
TXM=$(craftd tx wasm store nftext_manager.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')
```

```bash
C20=$(craftd q tx $TX20 --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
C721=$(craftd q tx $TX721 --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
CM=$(craftd q tx $TXM --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
```

```bash
echo $C20  # id: 8
echo $C721 # id: 9
echo $CM   # id: 10
```

# now we need to init them

```bash
craftd tx wasm instantiate $C20 '{
  "name": "craft-cw-20-placeholder",
  "symbol": "CRAFTR",
  "decimals": 6,
  "initial_balances": [
    {
      "address": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl",
      "amount": "100"
    }
  ],
  "mint": {
    "minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
  }
}' --label "cw20-base" --gas-prices="0.025ucosm" --gas="auto" --gas-adjustment="1.2" -y --from $KEY --admin $(craftd keys show $KEY -a)

export ADDR20=craft1tqwwyth34550lg2437m05mjnjp8w7h5ka7m70jtzpxn4uh2ktsmqud0w82
```

```bash
craftd tx wasm instantiate $C721 '{
  "name": "craftd-realestate-nfts",
  "symbol": "CRE",
  "minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
}' --label "cw721-base" --gas-prices="0.025ucosm" --gas="auto" --gas-adjustment="1.2" -y --admin $(craftd keys show $KEY -a) --from $KEY
export ADDR721=craft1gurgpv8savnfw66lckwzn4zk7fp394lpe667dhu7aw48u40lj6jshnd885
```

```bash
craftd tx wasm instantiate $CM '{
  "name": "craft-marketplace-nfts"
}' --label "marketplace" --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY --admin $(craftd keys show $KEY -a)
export ADDRM=craft1999u8suptza3rtxwk7lspve02m406xe7l622erg3np3aq05gawxs2rh8r2
```

---
init an NFT w/ data

```bash
# TXMINT=$(craftd tx wasm execute $ADDR721 '{"mint":{"token_id":"1","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"https://gateway.pinata.cloud/ipfs/QmXkGh665GVjCCs3cbLLWYwjc3kug1EBGvdyVmhuZRMgNE"}}' --from $KEY --yes --output json | jq -r '.txhash')
## craftd q wasm contract-state smart $ADDR721 '{"all_nft_info":{"token_id":"1"}}'

export JSON_ENCODED=`echo '{"uuid": "12345", "name": "My NFT", "type": "HOME", "description": "This is my NFT", "image": "https://image.com/1.png"}' | base64 -w 0` && echo $JSON_ENCODED

TXMINT=$(craftd tx wasm execute $ADDR721 '{"mint":{"token_id":"13","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"eyJ1dWlkIjogIjEyMzQ1IiwgIm5hbWUiOiAiTXkgTkZUIiwgInR5cGUiOiAiSE9NRSIsICJkZXNjcmlwdGlvbiI6ICJUaGlzIGlzIG15IE5GVCIsICJpbWFnZSI6ICJodHRwczovL2ltYWdlLmNvbS8xLnBuZyJ9Cg=="}}' --from $KEY --yes --output json | jq -r '.txhash')

export QUERY_JSON='{"all_nft_info":{"token_id":"13"}}'
# OLD -> craftd q wasm contract-state smart $ADDR721 $QUERY_JSON

# NEW -> Export Base64 encoded JSON as a raw string (no quotes)
export JSON_VALUES=`echo $(craftd q wasm contract-state smart $ADDR721 $QUERY_JSON --output json) | jq -r '.data.info.token_uri'`
echo $JSON_VALUES | base64 --decode #| jq '.uuid'
```

----
https://github.com/BlockscapeNetwork/hackatom_v/tree/master/contracts/marketplace

# sell token
```bash
# ADDR20 address required
export NFT_LISTING_BASE64=`printf '{"list_price":{"address":"%s","amount":"3","denom":"CRAFTR"}}' $ADDR20 | base64 -w 0` && echo $NFT_LISTING_BASE64

# send_nft from 721 -> marketplace contract =  $ADDRM
craftd tx wasm execute $ADDR721 '{
  "send_nft": {
    "contract": "craft1999u8suptza3rtxwk7lspve02m406xe7l622erg3np3aq05gawxs2rh8r2", 
    "token_id": "13",
    "msg":"eyJsaXN0X3ByaWNlIjp7ImFkZHJlc3MiOiJjcmFmdDF0cXd3eXRoMzQ1NTBsZzI0MzdtMDVtam5qcDh3N2g1a2E3bTcwanR6cHhuNHVoMmt0c21xdWQwdzgyIiwiYW1vdW50IjoiMyIsImRlbm9tIjoiQ1JBRlRSIn19"
  }
}' --gas-prices="0.025ucosm" --gas="auto" --gas-adjustment="1.2" -y --from $KEY
```

<!-- The below don't work with current nftext_manager::state::Offering not found -->
# cancel selling
```bash
craftd tx wasm execute $ADDRM '{
  "withdraw_nft": {
    "offering_id": "12"
  }
}' --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY
```

# Buying an NFT from the marketplace
```bash
export OFFERING_ID_MSG=`printf '{"offering_id":"12"}' | base64 -w 0` && echo $OFFERING_ID_MSG

# We execute on the CW20, the contract is the marketplace $ADDRM
craftd tx wasm execute $ADDR20 '{
  "send": {
    "contract": "craft1999u8suptza3rtxwk7lspve02m406xe7l622erg3np3aq05gawxs2rh8r2",
    "amount": "2",
    "msg": "eyJvZmZlcmluZ19pZCI6IjEyIn0="
  }
}' --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY
```


# all offerings
```bash
craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}'  
```