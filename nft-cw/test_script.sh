# following is taken from commands.md

# TODO: Idea: on init of contract, allow kv stores for placeholders? (ex):
# InitMsg(name, denom, contracts={"realestate": "craft_contract_address"})
# then filter that out with getOffering {} struct if desired by the user

# export KEY="key" # craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
export KEY="validator" # craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh
export KEY2="mykey2" # craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r
export KEYALGO="secp256k1"
export CRAFT_CHAIN_ID="craft-v4"
# export CRAFTD_KEYRING_BACKEND="test"
export CRAFTD_KEYRING_BACKEND="os"
export CRAFTD_NODE="http://65.109.38.251:26657"
export CRAFTD_COMMAND_ARGS="--gas-prices="0.025ucraft" -y --from $KEY"

# craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
echo "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry" | craftd keys add $KEY --keyring-backend $CRAFTD_KEYRING_BACKEND --algo $KEYALGO --recover
# craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r
echo "flag meat remind stamp unveil junior goose first hold atom deny ramp raven party lens jazz tape dad produce wrap citizen common vital hungry" | craftd keys add $KEY2 --keyring-backend $CRAFTD_KEYRING_BACKEND --algo $KEYALGO --recover

# export KEY_ADDR=`craftd keys show $KEY -a`
export KEY_ADDR="craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" # reeces testnet valiadtor address (validaator)
# export KEY_ADDR2=`craftd keys show $KEY2 -a`



# get this diredctory with basedir command
cd $(basedir "$0")

# NFT Contract (Code id 3 rn)
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY -y --output json | jq -r '.txhash') && sleep 1
CODE_ID_721=$(craftd query tx $TX721 --output json | jq -r '.logs[0].events[-1].attributes[0].value') && sleep 1
# NFT721_TX_UPLOAD=$(craftd tx wasm instantiate "$CODE_ID_721" '{"name": "craftd-re1","symbol": "ctest","minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}' --label "craft-realestate" $CRAFTD_COMMAND_ARGS --output json -y --admin $KEY_ADDR | jq -r '.txhash') && sleep 1
NFT721_TX_UPLOAD=$(craftd tx wasm instantiate "$CODE_ID_721" '{"name": "craftd-re2","symbol": "ctest","minter": "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh"}' --label "craft-realestate" $CRAFTD_COMMAND_ARGS --output json -y --admin $KEY_ADDR | jq -r '.txhash') && sleep 1
sleep 3
ADDR721=$(craftd query tx $NFT721_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "ADDR 721: $ADDR721"
# export ADDR721=craft1ltd0maxmte3xf4zshta9j5djrq9cl692ctsp9u5q0p9wss0f5lmsh738wk

# ADDR_test721 (testing images)
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY -y --output json | jq -r '.txhash') && sleep 1
CODE_ID_721=$(craftd query tx $TX721 --output json | jq -r '.logs[0].events[-1].attributes[0].value') && sleep 1
# IMAGE_TX_UPLOAD=$(craftd tx wasm instantiate "$CODE_ID_721" '{"name": "craft-images1","symbol": "cimg","minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}' --label "craft-images" $CRAFTD_COMMAND_ARGS --output json -y --admin $KEY_ADDR | jq -r '.txhash') && sleep 1
IMAGE_TX_UPLOAD=$(craftd tx wasm instantiate "$CODE_ID_721" '{"name": "craft-images1","symbol": "cimg","minter": "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh"}' --label "craft-images" $CRAFTD_COMMAND_ARGS --output json -y --admin $KEY_ADDR | jq -r '.txhash') && sleep 1
sleep 3
ADDR721IMAGES=$(craftd query tx $IMAGE_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "ADDR 721 IMAGES (LINKS): $ADDR721IMAGES"
# export ADDR721IMAGES=craft1suhgf5svhu4usrurvxzlgn54ksxmn8gljarjtxqnapv8kjnp4nrs8k85qj

# marketplace
TXM=$(craftd tx wasm store craft_marketplace.wasm --from $KEY -y --output json --broadcast-mode block | jq -r '.txhash')
MARKET_CODE_ID=$(craftd query tx $TXM --output json | jq -r '.logs[0].events[-1].attributes[0].value')
# fee_receive_address should = DAO wallet / multisig
# MARKET_TX_UPLOAD=$(craftd tx wasm instantiate "$MARKET_CODE_ID" '{"name":"marketplace-2","denom":"ucraft","fee_receive_address":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","platform_fee":"5"}' --label "marketplace" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR -y --output json | jq -r '.txhash')
MARKET_TX_UPLOAD=$(craftd tx wasm instantiate "$MARKET_CODE_ID" '{"name":"marketplace-1","denom":"ucraft","fee_receive_address":"craft1n3a53mz55yfsa2t4wvdx3jycjkarpgkf07zwk7","platform_fee":"5"}' --label "marketplace" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR -y --output json | jq -r '.txhash')
sleep 3
ADDRM=$(craftd query tx $MARKET_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "Marketplace Address: $ADDRM"
# export ADDRM=craft1xr3rq8yvd7qplsw5yx90ftsr2zdhg4e9z60h5duusgxpv72hud3sc3plyl

function mintToken() {
    CONTRACT_ADDR=$1
    TOKEN_ID=$2
    OWNER=$3
    TOKEN_URI=$4

    export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"%s","owner":"%s","token_uri":"%s"}}' $TOKEN_ID $OWNER $TOKEN_URI`
    TXMINT=$(craftd tx wasm execute "$CONTRACT_ADDR" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT
}

# ==================================PROPERTIES EXAMPLE====================================================
# base64 is from the Mint_RealEstate.py script

# mintToken $ADDR721 1 "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl" "eyJfaWQiOiAiNDY2YjJjMjEtNTY4MC00Mzg2LTk3ZDUtYzM0MDcwZjU0NjI0IiwgIm5hbWUiOiAiTWVnYS1NYW5zaW9uICMxIiwgImRlc2NyaXB0aW9uIjogIkEgbHV4dXJpb3VzIGFuZCBsYXJnZSBtYW5zaW9uIGxvY2F0ZWQgb24gdGhlIGVkZ2Ugb2YgY2l0eS4iLCAiaW1hZ2VMaW5rIjogImh0dHBzOi8vaS5pbWd1ci5jb20vTXc3OGp4dS5wbmciLCAiZmxvb3JBcmVhIjogMjA4NTYsICJ0b3RhbFZvbHVtZSI6IDYyNTY4LCAid29ybGROYW1lIjogIndvcmxkIiwgImNpdHlJZCI6ICJiZjcxM2RkOS03ZTFhLTRjYTUtYTA3Ny0yNTFjNTc0ZDQ5YjMiLCAiYnVpbGRpbmdJZCI6IG51bGwsICJ0eXBlIjogIlJFU0lERU5USUFMIiwgImNpdHlOYW1lIjogIkNyYWZ0IENpdHkiLCAiYnVpbGRpbmdOYW1lIjogIiIsICJfbmZ0X3R5cGUiOiAicmVhbF9lc3RhdGUifQ=="
mintToken $ADDR721 1 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "eyJfaWQiOiAiNDY2YjJjMjEtNTY4MC00Mzg2LTk3ZDUtYzM0MDcwZjU0NjI0IiwgIm5hbWUiOiAiTWVnYS1NYW5zaW9uICMxIiwgImRlc2NyaXB0aW9uIjogIkEgbHV4dXJpb3VzIGFuZCBsYXJnZSBtYW5zaW9uIGxvY2F0ZWQgb24gdGhlIGVkZ2Ugb2YgY2l0eS4iLCAiaW1hZ2VMaW5rIjogImh0dHBzOi8vaS5pbWd1ci5jb20vTXc3OGp4dS5wbmciLCAiZmxvb3JBcmVhIjogMjA4NTYsICJ0b3RhbFZvbHVtZSI6IDYyNTY4LCAid29ybGROYW1lIjogIndvcmxkIiwgImNpdHlJZCI6ICJiZjcxM2RkOS03ZTFhLTRjYTUtYTA3Ny0yNTFjNTc0ZDQ5YjMiLCAiYnVpbGRpbmdJZCI6IG51bGwsICJ0eXBlIjogIlJFU0lERU5USUFMIiwgImNpdHlOYW1lIjogIkNyYWZ0IENpdHkiLCAiYnVpbGRpbmdOYW1lIjogIiIsICJfbmZ0X3R5cGUiOiAicmVhbF9lc3RhdGUifQ=="
mintToken $ADDR721 2 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "eyJfaWQiOiAiN2UzNzQyN2QtODZhNy00MmZjLThiZGMtMjZlNzczNjBmNDhkIiwgIm5hbWUiOiAiTWlkLU1hbnNpb24gIzEiLCAiZGVzY3JpcHRpb24iOiAiQSBtaWQtc2l6ZWQgbWFuc2lvbiBsb2NhdGVkIGFjcm9zcyBmcm9tIHRoZSBiZWFjaC4iLCAiaW1hZ2VMaW5rIjogImh0dHBzOi8vaS5pbWd1ci5jb20vWnlPWHJWay5qcGVnIiwgImZsb29yQXJlYSI6IDc2MzIsICJ0b3RhbFZvbHVtZSI6IDMxMjkxMiwgIndvcmxkTmFtZSI6ICJ3b3JsZCIsICJjaXR5SWQiOiAiYmY3MTNkZDktN2UxYS00Y2E1LWEwNzctMjUxYzU3NGQ0OWIzIiwgImJ1aWxkaW5nSWQiOiBudWxsLCAidHlwZSI6ICJSRVNJREVOVElBTCIsICJjaXR5TmFtZSI6ICJDcmFmdCBDaXR5IiwgImJ1aWxkaW5nTmFtZSI6ICIiLCAiX25mdF90eXBlIjogInJlYWxfZXN0YXRlIn0="
mintToken $ADDR721 3 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "eyJfaWQiOiAiZjZhMjA4OGUtZmRjMi00M2Q5LTlmOTQtMDVjZjE0OWIwM2ExIiwgIm5hbWUiOiAiU3VidXJiYW4gSG9tZSAxIiwgImRlc2NyaXB0aW9uIjogIkEgc21hbGwgc3VidXJiYW4gaG9tZSBsb2NhdGVkIGp1c3Qgb3V0c2lkZSB0aGUgYnVzaW5lc3MgZGlzdHJpY3QuIiwgImltYWdlTGluayI6ICJodHRwczovL2kuaW1ndXIuY29tL0N4N0hLRkQucG5nIiwgImZsb29yQXJlYSI6IDgxMCwgInRvdGFsVm9sdW1lIjogOTcyMCwgIndvcmxkTmFtZSI6ICJ3b3JsZCIsICJjaXR5SWQiOiAiYmY3MTNkZDktN2UxYS00Y2E1LWEwNzctMjUxYzU3NGQ0OWIzIiwgImJ1aWxkaW5nSWQiOiBudWxsLCAidHlwZSI6ICJSRVNJREVOVElBTCIsICJjaXR5TmFtZSI6ICJDcmFmdCBDaXR5IiwgImJ1aWxkaW5nTmFtZSI6ICIiLCAiX25mdF90eXBlIjogInJlYWxfZXN0YXRlIn0="
mintToken $ADDR721 4 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "eyJfaWQiOiAiMWIyYmZkMWItM2ZhMi00ZDhlLTgyMjUtZDM0YTU0M2ZkZGM0IiwgIm5hbWUiOiAiQmVhY2ggU2hvcCAjMSIsICJkZXNjcmlwdGlvbiI6ICJBIGNvenkgbGl0dGxlIHNob3AgbG9jYXRlZCBvbiB0aGUgYmVhY2guIiwgImltYWdlTGluayI6ICJodHRwczovL2kuaW1ndXIuY29tL1RrYWFEYVQucG5nIiwgImZsb29yQXJlYSI6IDM4NCwgInRvdGFsVm9sdW1lIjogNjE0NCwgIndvcmxkTmFtZSI6ICJ3b3JsZCIsICJjaXR5SWQiOiAiYmY3MTNkZDktN2UxYS00Y2E1LWEwNzctMjUxYzU3NGQ0OWIzIiwgImJ1aWxkaW5nSWQiOiBudWxsLCAidHlwZSI6ICJCVVNJTkVTUyIsICJjaXR5TmFtZSI6ICJDcmFmdCBDaXR5IiwgImJ1aWxkaW5nTmFtZSI6ICIiLCAiX25mdF90eXBlIjogInJlYWxfZXN0YXRlIn0="
mintToken $ADDR721 5 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "eyJfaWQiOiAiOTc3OGQ0ZjctNzA4ZS00YWYwLTllNWQtZjM2ZDc0ZTQ0YWNiIiwgIm5hbWUiOiAiR2FzIFN0YXRpb24gIzEiLCAiZGVzY3JpcHRpb24iOiAiQW4gdXBrZXB0IGdhcyBzdGF0aW9uIGxvY2F0ZWQgaW4gdGhlIGhlYXJ0IG9mIHRoZSBjaXR5LiIsICJpbWFnZUxpbmsiOiAiaHR0cHM6Ly9pLmltZ3VyLmNvbS9rMVhsM3JELnBuZyIsICJmbG9vckFyZWEiOiA1MDk3LCAidG90YWxWb2x1bWUiOiA3NjQ1NSwgIndvcmxkTmFtZSI6ICJ3b3JsZCIsICJjaXR5SWQiOiAiYmY3MTNkZDktN2UxYS00Y2E1LWEwNzctMjUxYzU3NGQ0OWIzIiwgImJ1aWxkaW5nSWQiOiBudWxsLCAidHlwZSI6ICJSRVNJREVOVElBTCIsICJjaXR5TmFtZSI6ICJDcmFmdCBDaXR5IiwgImJ1aWxkaW5nTmFtZSI6ICIiLCAiX25mdF90eXBlIjogInJlYWxfZXN0YXRlIn0="
mintToken $ADDR721 6 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "eyJfaWQiOiAiNWFjNWIyYWItZWMxNS00OGVhLTllYzEtZjU0OTJiZjBlMmRiIiwgIm5hbWUiOiAiR2lmdCBTaG9wICMxIiwgImRlc2NyaXB0aW9uIjogIkEgc21hbGwgc2hvcCBsb2NhdGVkIGFjcm9zcyBmcm9tIHRoZSBiZWFjaC4iLCAiaW1hZ2VMaW5rIjogImh0dHBzOi8vaS5pbWd1ci5jb20vcTU2ZVpaQS5wbmciLCAiZmxvb3JBcmVhIjogMzIyLCAidG90YWxWb2x1bWUiOiA1NDc0LCAid29ybGROYW1lIjogIndvcmxkIiwgImNpdHlJZCI6ICJiZjcxM2RkOS03ZTFhLTRjYTUtYTA3Ny0yNTFjNTc0ZDQ5YjMiLCAiYnVpbGRpbmdJZCI6IG51bGwsICJ0eXBlIjogIkJVU0lORVNTIiwgImNpdHlOYW1lIjogIkNyYWZ0IENpdHkiLCAiYnVpbGRpbmdOYW1lIjogIiIsICJfbmZ0X3R5cGUiOiAicmVhbF9lc3RhdGUifQ=="
mintToken $ADDR721 7 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "eyJfaWQiOiAiMTM0MjgxNmQtM2E4ZS00Yzk0LTgwYmUtMzE1ZjIwNWViODc0IiwgIm5hbWUiOiAiQ2FzaW5vIE1DIiwgImRlc2NyaXB0aW9uIjogIlRoZSBvbmUgYW5kIG9ubHkgQ2FzaW5vIE1DLiIsICJpbWFnZUxpbmsiOiAiaHR0cHM6Ly9pLmltZ3VyLmNvbS94dEEwclNDLmpwZWciLCAiZmxvb3JBcmVhIjogOTQ2MCwgInRvdGFsVm9sdW1lIjogNjE0OTAwLCAid29ybGROYW1lIjogIndvcmxkIiwgImNpdHlJZCI6ICJiZjcxM2RkOS03ZTFhLTRjYTUtYTA3Ny0yNTFjNTc0ZDQ5YjMiLCAiYnVpbGRpbmdJZCI6IG51bGwsICJ0eXBlIjogIkdPVkVSTk1FTlQiLCAiY2l0eU5hbWUiOiAiQ3JhZnQgQ2l0eSIsICJidWlsZGluZ05hbWUiOiAiIiwgIl9uZnRfdHlwZSI6ICJyZWFsX2VzdGF0ZSJ9"
mintToken $ADDR721 8 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "eyJfaWQiOiAiZDliYjE2M2YtOGViZS00NDBiLWE5NGEtZWJiYzAxN2M4MGRmIiwgIm5hbWUiOiAiREFPIENvdXJ0aG91c2UiLCAiZGVzY3JpcHRpb24iOiAiQ291cnRob3VzZSBvZiB0aGUgREFPLiIsICJpbWFnZUxpbmsiOiAiaHR0cHM6Ly9pLmltZ3VyLmNvbS9qRDV6UGIyLnBuZyIsICJmbG9vckFyZWEiOiAxMjY1NiwgInRvdGFsVm9sdW1lIjogMTA3NTc2MCwgIndvcmxkTmFtZSI6ICJ3b3JsZCIsICJjaXR5SWQiOiAiYmY3MTNkZDktN2UxYS00Y2E1LWEwNzctMjUxYzU3NGQ0OWIzIiwgImJ1aWxkaW5nSWQiOiBudWxsLCAidHlwZSI6ICJHT1ZFUk5NRU5UIiwgImNpdHlOYW1lIjogIkNyYWZ0IENpdHkiLCAiYnVpbGRpbmdOYW1lIjogIiIsICJfbmZ0X3R5cGUiOiAicmVhbF9lc3RhdGUifQ=="

# reece mint - craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0
mintToken $ADDR721 10 "craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0" "eyJfaWQiOiAiZDliYjE2M2YtOGViZS00NDBiLWE5NGEtZWJiYzAxN2M4MGRmIiwgIm5hbWUiOiAiREFPIENvdXJ0aG91c2UiLCAiZGVzY3JpcHRpb24iOiAiQ291cnRob3VzZSBvZiB0aGUgREFPLiIsICJpbWFnZUxpbmsiOiAiaHR0cHM6Ly9pLmltZ3VyLmNvbS9qRDV6UGIyLnBuZyIsICJmbG9vckFyZWEiOiAxMjY1NiwgInRvdGFsVm9sdW1lIjogMTA3NTc2MCwgIndvcmxkTmFtZSI6ICJ3b3JsZCIsICJjaXR5SWQiOiAiYmY3MTNkZDktN2UxYS00Y2E1LWEwNzctMjUxYzU3NGQ0OWIzIiwgImJ1aWxkaW5nSWQiOiBudWxsLCAidHlwZSI6ICJHT1ZFUk5NRU5UIiwgImNpdHlOYW1lIjogIkNyYWZ0IENpdHkiLCAiYnVpbGRpbmdOYW1lIjogIiIsICJfbmZ0X3R5cGUiOiAicmVhbF9lc3RhdGUifQ=="


# ====================================NORMAL LINKS =======================================
mintToken $ADDR721IMAGES 1 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "https://ipfs.io/ipfs/QmNLijobERK4VhSDZdKjt5SrezdRM6k813qcSHd68f3Mqg"
mintToken $ADDR721IMAGES 2 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "https://ipfs.io/ipfs/QmNLjZSFV3GUMcusj8keEqVtToEE3ceTSguNom7e4S6pbJ"
mintToken $ADDR721IMAGES 3 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ"
mintToken $ADDR721IMAGES 4 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ"
mintToken $ADDR721IMAGES 5 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ"
mintToken $ADDR721IMAGES 6 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ"
# mintToken $ADDR721IMAGES 26 "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl" "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ"
mintToken $ADDR721IMAGES 7 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ"
mintToken $ADDR721IMAGES 8 "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh" "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ"




# Query Property check
# CONTRACT_DATA: craftd query wasm contract-state smart $ADDR721 '{"contract_info":{}}'

echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"1"}}' --output json) | jq -r '.data.info.token_uri' | base64 --decode
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"2"}}' --output json) | jq -r '.data.info.token_uri' | base64 --decode
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"3"}}' --output json) | jq -r '.data.info.token_uri' | base64 --decode
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"4"}}' --output json) | jq -r '.data.info.token_uri' | base64 --decode

# Query 721 Owned Tokens
craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","start_after":"0","limit":50}}'
craftd query wasm contract-state smart $ADDR721IMAGES '{"tokens":{"owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","start_after":"0","limit":50}}'

# Query Marketplace Holdings
craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}'
craftd query wasm contract-state smart $ADDRM '{"get_offerings": {"filter_seller":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}}' # optional query specific

craftd query wasm contract-state smart $ADDRM '{"get_collection_volume": {"address": "craft1suhgf5svhu4usrurvxzlgn54ksxmn8gljarjtxqnapv8kjnp4nrs8k85qj"}}'

# list real estate NFT for sale
function ListTokenForSale() {
    CONTRACT_ADDR=$1
    TOKEN_ID=$2
    # export NFT_LISTING_BASE64=`printf '{"list_price":"1000000"}' | base64 -w 0` # 10 craft
    # export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"26","msg":"%s"}}' $ADDRM $NFT_LISTING_BASE64`
    # # craftd tx wasm execute "$ADDR721" "$SEND_NFT_JSON" --gas-prices="0.025ucraft" -y --from $KEY
    # craftd tx wasm execute "$ADDR721IMAGES" "$SEND_NFT_JSON" --gas-prices="0.025ucraft" -y --from $KEY

    export NFT_LISTING_BASE64=`printf '{"list_price":"1000000"}' | base64 -w 0` # 10 craft
    export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"%s","msg":"%s"}}' $ADDRM $TOKEN_ID $NFT_LISTING_BASE64`    
    craftd tx wasm execute "$CONTRACT_ADDR" "$SEND_NFT_JSON" --gas-prices="0.025ucraft" -y --from $KEY
}
# IMAGES
ListTokenForSale $ADDR721IMAGES 1
ListTokenForSale $ADDR721IMAGES 2
ListTokenForSale $ADDR721IMAGES 3
ListTokenForSale $ADDR721IMAGES 4
ListTokenForSale $ADDR721IMAGES 21
ListTokenForSale $ADDR721IMAGES 22

ListTokenForSale $ADDR721 1
ListTokenForSale $ADDR721 2
ListTokenForSale $ADDR721 3
ListTokenForSale $ADDR721 4
ListTokenForSale $ADDR721 5
ListTokenForSale $ADDR721 6



# withdraw NFT so it is no longer for sale
craftd tx wasm execute $ADDRM '{"withdraw_nft":{"offering_id":"4"}}' $CRAFTD_COMMAND_ARGS -y


# gets all contracts which are CW721
# craftd q wasm list-contract-by-code 1 --output json | jq '.contracts'
# http://95.217.113.126:1317/cosmwasm/wasm/v1/code/1/contracts?pagination.limit=100
# So our API could query this list, check which a user owns, so we get ALL iamges they own.


# buy the NFT with mykey2 & with ucraft
# offering_id should match with {"get_offerings": {}} id:
craftd tx wasm execute $ADDRM '{"buy_nft":{"offering_id":"2"}}' --gas-prices="0.025ucraft" --amount 1000000ucraft -y --from $KEY2


# update token priceing
craftd tx wasm execute $ADDRM '{"update_listing_price":{"offering_id":"1","new_price":"4000000"}}' --gas-prices="0.025ucraft" -y --from $KEY

# update params as the DAO
# update_fee_receiver_address, update_platform_fee, force_withdraw_all
craftd query wasm contract-state smart $ADDRM '{"get_contract_info": {}}' # 'dao' key for testnet

craftd tx wasm execute $ADDRM '{"update_platform_fee":{"new_fee":"0"}}' --gas-prices="0.025ucraft" -y --from $KEY
craftd tx wasm execute $ADDRM '{"force_withdraw_all":{}}' --gas-prices="0.025ucraft" -y --from $KEY

# FUTURE TO DO
craftd tx wasm migrate $ADDRM 3 '{"migrate_msg":{}}' --gas-prices="0.025ucraft" -y --from $KEY