# https://github.com/notional-labs/craft/blob/master/NFTs/commands.md

# NOTE TO SELF, UPDATE CW721 to support sending nfts & offerings

```bash
export KEY="mykey"
export KEYALGO="secp256k1"
export CRAFT_CHAIN_ID="test-1"
export CRAFT_KEYRING_BACKEND="test"
export CRAFTD_NODE="tcp://65.108.125.182:26657"
```

```bash
cd base_contacts/artifacts
TX20=$(craftd tx wasm store cw20_base.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')

cd ../../marketplace/artifacts
TXM=$(craftd tx wasm store nftext_manager.wasm --from $KEY --gas auto -y --output json | jq -r '.txhash')
```

```bash
C20=$(craftd q tx $TX20 --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
C721=$(craftd q tx $TX721 --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
CM=$(craftd q tx $TXM --output json | jq -r '.logs[].events[] | select(.type=="store_code").attributes[].value')
```

```bash
echo $C20  # id: 6
echo $C721 # id: 3
echo $CM   # id: 5
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

export JSON_ENCODED=`echo '{"uuid": "12345", "name": "My NFT", "type": "HOME", "description": "This is my NFT", "image": "https://image.com/1.png"}' | base64`

TXMINT=$(craftd tx wasm execute $ADDR721 '{"mint":{"token_id":"11","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"eyJ1dWlkIjogIjEyMzQ1IiwgIm5hbWUiOiAiTXkgTkZUIiwgInR5cGUiOiAiSE9NRSIsICJkZXNjcmlwdGlvbiI6ICJUaGlzIGlzIG15IE5GVCIsICJpbWFnZSI6ICJodHRwczovL2ltYWdlLmNvbS8xLnBuZyJ9Cg=="}}' --from $KEY --yes --output json | jq -r '.txhash')

export QUERY_JSON='{"all_nft_info":{"token_id":"11"}}'
# craftd q wasm contract-state smart $ADDR721 $QUERY_JSON

# Export Base64 encoded JSON as a raw string (no quotes)
export JSON_VALUES=`echo $(craftd q wasm contract-state smart $ADDR721 $QUERY_JSON --output json) | jq -r '.data.info.token_uri'`
echo $JSON_VALUES | base64 --decode | jq '.uuid'
```

----
https://github.com/BlockscapeNetwork/hackatom_v/tree/master/contracts/marketplace

# sell token
```sh
# Example of conversion
echo '{"address":"$ADDR20","amount":"1"}}' | base64
--> eyJhZGRyZXNzIjoiJEFERFIyMCIsImFtb3VudCI6IjEifX0K
```

```bash
# ADDR20
# export NFT_LISTING=`echo -e '{"list_price":{"address":"$ADDR20","amount":"1","denom":"CRAFTR"}}' | base64`

export LISTING_FORMAT='{"list_price":{"address":"%s","amount":"1","denom":"CRAFTR"}}'
export NFT_LISTING_BASE64=`printf $LISTING_FORMAT $ADDR20 | base64 -w 0`

# send_nft contract =  $ADDRM
craftd tx wasm execute $ADDR721 '{
  "send_nft": {
    "contract": "craft1999u8suptza3rtxwk7lspve02m406xe7l622erg3np3aq05gawxs2rh8r2", 
    "token_id": "11",
    "msg":"eyJsaXN0X3ByaWNlIjp7ImFkZHJlc3MiOiJjcmFmdDF0cXd3eXRoMzQ1NTBsZzI0MzdtMDVtam5qcDh3N2g1a2E3bTcwanR6cHhuNHVoMmt0c21xdWQwdzgyIiwiYW1vdW50IjoiMSIsImRlbm9tIjoiQ1JBRlRSIn19"
  }
}' --gas-prices="0.025ucosm" --gas="auto" --gas-adjustment="1.2" -y --from $KEY
```




# all offerings
```bash
craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}'

data:
  offerings:
  - contract_addr: craft1qwlgtx52gsdu7dtp0cekka5zehdl0uj3fhp9acg325fvgs8jdzkstnsu5l
    id: "1"
    list_price:
      amount: "1"
      denom: CRAFTR
    seller: craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
    token_id: "11"
  
```