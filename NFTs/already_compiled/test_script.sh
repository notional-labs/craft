# following is taken from commands.md

export KEY="mykey"
export KEY2="mykey2"
export KEYALGO="secp256k1"
export CRAFT_CHAIN_ID="test-1"
export CRAFTD_KEYRING_BACKEND="test"
export CRAFTD_NODE="http://65.108.125.182:26657"
export CRAFTD_COMMAND_ARGS="--gas-prices="0.025ucraft" -y --from $KEY"

# craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
echo "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry" | craftd keys add $KEY --keyring-backend $CRAFTD_KEYRING_BACKEND --algo $KEYALGO --recover
# craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r
echo "flag meat remind stamp unveil junior goose first hold atom deny ramp raven party lens jazz tape dad produce wrap citizen common vital hungry" | craftd keys add $KEY2 --keyring-backend $CRAFTD_KEYRING_BACKEND --algo $KEYALGO --recover

export KEY_ADDR=`craftd keys show $KEY -a`
export KEY_ADDR2=`craftd keys show $KEY2 -a`





# get this diredctory with basedir command
cd $(basedir "$0")

# NFT Contract
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY -y --output json | jq -r '.txhash') && \
CODE_ID_721=$(craftd query tx $TX721 --output json | jq -r '.logs[0].events[-1].attributes[0].value') && \
NFT721_TX_UPLOAD=$(craftd tx wasm instantiate "$CODE_ID_721" '{"name": "craftd-test5","symbol": "ctest","minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}' --label "craft-test4" $CRAFTD_COMMAND_ARGS --output json -y --admin $KEY_ADDR | jq -r '.txhash') && \
sleep 1
ADDR721=$(craftd query tx $NFT721_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "ADDR 721: $ADDR721"
export ADDR721=craft12njsx22ne73swjqxxn5e7xtc2n95y2aw8r73cqdth0g86way24cq75c0c7

# marketplace
TXM=$(craftd tx wasm store craft_marketplace.wasm --from $KEY -y --output json | jq -r '.txhash') && \
MARKET_CODE_ID=$(craftd query tx $TXM --output json | jq -r '.logs[0].events[-1].attributes[0].value') && \
MARKET_TX_UPLOAD=$(craftd tx wasm instantiate "$MARKET_CODE_ID" '{"name": "m10","denom": "ucraft"}' --label "m9" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR -y --output json | jq -r '.txhash') && \
sleep 1
ADDRM=$(craftd query tx $MARKET_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "Marketplace Address: $ADDRM"
export ADDRM=craft1rcmvfsn77pd6m04ctqj3wcu66pvrw9p265cdl72w4zarfup2rv7q6xyshc



# mint Properties -> CW721 as the admin address
export JSON_ENCODED=`echo '{"uuid": "11111","name": "MyNFTproperty", "type": "HOME", "description": "This is my NFT", "image": "https://image.com/1.png"}' | base64 -w 0` #&& echo $JSON_ENCODED
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"1","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT

export JSON_ENCODED=`echo '{"uuid": "22222","name": "MyNFTproperty", "type": "HOME", "description": "This is my NFT", "image": "https://image.com/2.png"}' | base64 -w 0` #&& echo $JSON_ENCODED
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"2","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT

export JSON_ENCODED=`echo '{"uuid": "33333","name": "MyNFTproperty", "type": "HOME", "description": "This is my NFT", "image": "https://image.com/2.png"}' | base64 -w 0` #&& echo $JSON_ENCODED
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"3","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT



# Query Property check
# CONTRACT_DATA: craftd query wasm contract-state smart $ADDR721 '{"contract_info":{}}'
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"1"}}' --output json) | jq -r '.data.info.token_uri'
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"2"}}' --output json) | jq -r '.data.info.token_uri'
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"3"}}' --output json) | jq -r '.data.info.token_uri'

# Query 721 Owned Tokens
craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","start_after":"0","limit":50}}'
# craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r","start_after":"0","limit":50}}' # 0 until they buy 1


# Query Marketplace Holdings
craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}' 


# list the NFT for sale in ucraft
export NFT_LISTING_BASE64=`printf '{"list_price":"11"}' | base64 -w 0`
export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"1","msg":"%s"}}' $ADDRM $NFT_LISTING_BASE64`
craftd tx wasm execute "$ADDR721" "$SEND_NFT_JSON" --gas-prices="0.025ucraft" -y --from $KEY



# withdraw NFT so it is no longer for sale
craftd tx wasm execute $ADDRM '{"withdraw_nft":{"offering_id":"1"}}' $CRAFTD_COMMAND_ARGS -y



# buy the NFT with mykey2 & with ucraft
# offering_id should match with {"get_offerings": {}} id:
export SEND_FUNDS_PURCHASE_NFT=`printf '{"buy_nft":{"offering_id":"2"}}' $OFFERING_ID_MSG_BASE64`
craftd tx wasm execute $ADDRM $SEND_FUNDS_PURCHASE_NFT --gas-prices="0.025ucraft" --amount 11ucraft -y --from $KEY2