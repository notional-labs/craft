# following is taken from commands.md

# TODO: Idea: on init of contract, allow kv stores for placeholders? (ex):
# InitMsg(name, denom, contracts={"realestate": "craft_contract_address"})
# then filter that out with getOffering {} struct if desired by the user

export KEY="mykey"
export KEY2="mykey2" # craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r
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

# NFT Contract (Code id 3 rn)
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY -y --output json | jq -r '.txhash') && \
CODE_ID_721=$(craftd query tx $TX721 --output json | jq -r '.logs[0].events[-1].attributes[0].value') && \
NFT721_TX_UPLOAD=$(craftd tx wasm instantiate "$CODE_ID_721" '{"name": "craftd-re2","symbol": "ctest","minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}' --label "craft-realestate2" $CRAFTD_COMMAND_ARGS --output json -y --admin $KEY_ADDR | jq -r '.txhash') && \
sleep 1
ADDR721=$(craftd query tx $NFT721_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "ADDR 721: $ADDR721"
export ADDR721=craft1udfs22xpxle475m2nz7u47jfa3vngncdegmczwwdx00cmetypa3s5mr4eq

# SKIN Contract
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY -y --output json | jq -r '.txhash') && \
CODE_ID_721=$(craftd query tx $TX721 --output json | jq -r '.logs[0].events[-1].attributes[0].value') && \
SKIN721_TX_UPLOAD=$(craftd tx wasm instantiate "$CODE_ID_721" '{"name": "craftd-skin3","symbol": "cskin","minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}' --label "craft-skins3" $CRAFTD_COMMAND_ARGS --output json -y --admin $KEY_ADDR | jq -r '.txhash') && \
sleep 1
ADDR721SKIN=$(craftd query tx $SKIN721_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "ADDR SKIN 721: $ADDR721SKIN"
export ADDR721SKIN=craft1qjxu65ucccpg8c5kac8ng6yxfqq85fluwd0p9nt74g2304qw8eyqz8azvt

# ADDR_test721 (testing images)
TX721=$(craftd tx wasm store cw721_base.wasm --from $KEY -y --output json | jq -r '.txhash') && \
CODE_ID_721=$(craftd query tx $TX721 --output json | jq -r '.logs[0].events[-1].attributes[0].value') && \
IMAGE_TX_UPLOAD=$(craftd tx wasm instantiate "$CODE_ID_721" '{"name": "craft-images","symbol": "cimg","minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"}' --label "craft-images" $CRAFTD_COMMAND_ARGS --output json -y --admin $KEY_ADDR | jq -r '.txhash') && \
sleep 1
ADDR721IMAGES=$(craftd query tx $IMAGE_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "ADDR 721 IMAGES (LINKS): $ADDR721IMAGES"
export ADDR721IMAGES=craft10fqy0npt7djm8lg847v9rqlng88kqfdvl8tyt4ge204wf52sy68qfdqfv6

# marketplace
TXM=$(craftd tx wasm store craft_marketplace.wasm --from $KEY -y --output json | jq -r '.txhash') && \
MARKET_CODE_ID=$(craftd query tx $TXM --output json | jq -r '.logs[0].events[-1].attributes[0].value') && \
MARKET_TX_UPLOAD=$(craftd tx wasm instantiate "$MARKET_CODE_ID" '{"name": "m14","denom": "ucraft"}' --label "m11" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR -y --output json | jq -r '.txhash') && \
sleep 1 && \
ADDRM=$(craftd query tx $MARKET_TX_UPLOAD --output json | jq -r '.logs[0].events[0].attributes[0].value') && echo "Marketplace Address: $ADDRM"
export ADDRM=craft1nwp0ynjv84wxysf2f5ctvysl6dpm8ngm70hss6jeqt8q7e7u345sgynrhu



# ==================================PROPERTIES EXAMPLE====================================================
# This is NOT the final version, will be modifying nft mint script in python to do this w/ database values
export JSON_ENCODED=`echo '{"_nft_type":"realestate", "_id": "dbcd78cb-326e-4842-982b-9252f9ca25a7","name": "MyNFTproperty1", "type": "HOME", "description": "This is my NFT1", "image": "https://image.com/1.png"}' | base64 -w 0` #&& echo $JSON_ENCODED
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"1","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT

export JSON_ENCODED=`echo '{"_nft_type":"realestate", "_id": "1fa35e3a-04d7-49a0-9e84-c67cf1f55c7f","name": "MyNFTproperty2", "type": "HOME", "description": "This is my NFT2", "image": "https://image.com/2.png"}' | base64 -w 0` #&& echo $JSON_ENCODED
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"2","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT

export JSON_ENCODED=`echo '{"_nft_type":"realestate", "_id": "2e778ae9-1641-43f6-9b6d-97b349af90dd","name": "MyNFTproperty3", "type": "HOME", "description": "This is my NFT3", "image": "https://image.com/3.png"}' | base64 -w 0` #&& echo $JSON_ENCODED
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"3","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT

export JSON_ENCODED=`echo '{"_nft_type":"realestate", "_id": "884895d7-057d-4a71-96f2-8f2174966110","name": "MyNFTproperty4", "type": "HOME", "description": "This is my NFT4", "image": "https://image.com/4.png"}' | base64 -w 0` #&& echo $JSON_ENCODED
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"4","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT

# ==================================MINT SKINS====================================================
export JSON_ENCODED="eyJfbmZ0X3R5cGUiOiAic2tpbiIsICJ2YWx1ZSI6ICJld29nSUNKMGFXMWxjM1JoYlhBaUlEb2dNVFkxTmpZeE16RXhNRFU1T1N3S0lDQWljSEp2Wm1sc1pVbGtJaUE2SUNKbU5UaGtaV0prTlRsbU5UQTBNakl5T0dZMk1ESXlNakV4WkRSak1UUXdZeUlzQ2lBZ0luQnliMlpwYkdWT1lXMWxJaUE2SUNKMWJuWmxiblJwZG1WMFlXeGxiblFpTEFvZ0lDSnphV2R1WVhSMWNtVlNaWEYxYVhKbFpDSWdPaUIwY25WbExBb2dJQ0owWlhoMGRYSmxjeUlnT2lCN0NpQWdJQ0FpVTB0SlRpSWdPaUI3Q2lBZ0lDQWdJQ0oxY213aUlEb2dJbWgwZEhBNkx5OTBaWGgwZFhKbGN5NXRhVzVsWTNKaFpuUXVibVYwTDNSbGVIUjFjbVV2WkRVeVl6Y3pNVEE1TlRObU1URmlNemd6WmpCaFl6YzNNR1UzT0RrMll6TTFOamhrT1ROa09UQmtPV0poTjJVM00ySXdNRGt6WkRjNU5HVmhZV00ySWl3S0lDQWdJQ0FnSW0xbGRHRmtZWFJoSWlBNklIc0tJQ0FnSUNBZ0lDQWliVzlrWld3aUlEb2dJbk5zYVcwaUNpQWdJQ0FnSUgwS0lDQWdJSDBLSUNCOUNuMD0iLCAic2lnbmF0dXJlIjogIm40bG5FU1Z4OHRNWFFVQ3ovOG93ZHlEaWZhN1J2Q3I4cTJDMlpQa2pFRXBMQzE2SGQwZEQ2RGEzNEdhVGt5NGpyNmlMbmRGeDkwUVhNSnZleExjNm9xbG9zUzdacFV5T0FySThxWTMxbzRqZXZuQmJIbURXcXNWbVlCUWM2eTBka2IxWjg0NTI2UUFTbk9qU09hR3ZhOWQ5MmZBek5KVjJqSFhYaG9SYjFNckxHM0JTbjJEOGZBNVlHUVBQRTd1b2ViWDhVK3dBbXk4Z0VzMjNiem9Vb09GU1cwdlZPd0ZyK2V4dXQrek9WSXFCZS9rREZuS05QSzZ5WUFtR1puTlRLUjJDRlRRTjhIVjdWUkJaVjdKNi9IcHhuSU5icVVOdjl6VGlSM09paGhqRlY2bnh2b3JXaGF5MHVUMGlTMDZmZmMxMWcreUxBZjB3bG9oWm11bmlFTmNPeWozNm9rZ1YxdnFUeVJodVk4MUJlWWRMZzlrMmxtWGF5KzFsbHE4MVc1cXdONzE4Y1Ficnp1dHJNR3o0dlorbXJMQmdYSHk5VGEzczdvUk5xV0MwMjNCNWsvKzRmQkZJcjdsZkFucmhJTmticnIvK3Z6eGpUYWVnN1VUMFVwYTg1cTk4VERRaWlDV1IyaUd3WnJyVTZCMk1pQ0kwdG1URElCbUtLTXMyTllETml3Z3FwbmU4ZnN3cnBRSFRWQ2ViOVUrN0VCNVFEaVBVaGE3R1JRYVorVVp1WGFHejhJbCttdTBITDNjVnJ3TGg3ZGpHSTlyZFk2UmdqNk56K1FURW1SN1BJb21TUURBVkx0MzBjbDFGTHA0cWRjV0JvZjdvem96NXE3OHN1SnhLVWNPY0FhN1o3cHFuV0tHUmJlNnEvKzFmNlBRVWNkbUp3b2NBU3JjPSIsICJ1cmwiOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNTJjNzMxMDk1M2YxMWIzODNmMGFjNzcwZTc4OTZjMzU2OGQ5M2Q5MGQ5YmE3ZTczYjAwOTNkNzk0ZWFhYzYifQ=="
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"1","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721SKIN" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT

export JSON_ENCODED="eyJfbmZ0X3R5cGUiOiAic2tpbiIsICJ2YWx1ZSI6ICJld29nSUNKMGFXMWxjM1JoYlhBaUlEb2dNVFkxTmpZeE1qa3pNRFF4T1N3S0lDQWljSEp2Wm1sc1pVbGtJaUE2SUNJNE9EQmlaV013WVRFME1tTTBZelJsWVRKbFpqbGlNVEZpTVRCa05XTmlOeUlzQ2lBZ0luQnliMlpwYkdWT1lXMWxJaUE2SUNKbVoyRmlJaXdLSUNBaWMybG5ibUYwZFhKbFVtVnhkV2x5WldRaUlEb2dkSEoxWlN3S0lDQWlkR1Y0ZEhWeVpYTWlJRG9nZXdvZ0lDQWdJbE5MU1U0aUlEb2dld29nSUNBZ0lDQWlkWEpzSWlBNklDSm9kSFJ3T2k4dmRHVjRkSFZ5WlhNdWJXbHVaV055WVdaMExtNWxkQzkwWlhoMGRYSmxMelEzT1Rkak9EWmxNMkl5T0RJeFlUa3dNVE0wT1dZME1UVTVNR0l3TnpnNU9ESTJZVFkyWVRRd01qaGlPV0ZtT0dVNE5qazVPRFkzTVRjek1tWTBPV1lpTEFvZ0lDQWdJQ0FpYldWMFlXUmhkR0VpSURvZ2V3b2dJQ0FnSUNBZ0lDSnRiMlJsYkNJZ09pQWljMnhwYlNJS0lDQWdJQ0FnZlFvZ0lDQWdmUW9nSUgwS2ZRPT0iLCAic2lnbmF0dXJlIjogImVIalJ2Z1pXQjg4Yk1BOCsxelJCdlVYUEVnWEUrY3RYRnBweSthWHNGTDVIbk1HUC9ESVJjeHAwYW9ickdOMmNvejR0RVVodkpxdHUvSWw5bGRwc0NZcFpiSlNJalJ2NFNldU9XYjg3RWtWTlJCWnc0bWwwUEZ1Q3pHVmxzOTVOeTM5ZlhQanpVT2h5REhnR0pQcjFTK2Z2ZWZlUVFlMVl1cnduS3JjZkl4RUp2VSs4aEVjZmJteDNDaElMUm1KeE0zOW9LSVZHTlFyWGdOU3dnUGZ6TmtLcDZhYTFvakdQemhKbkxyT0JIcEFKeGRxOXlGRnJOYi8zT0pZR3hXakw0czFmMzI3Wkd5WkNVOWY1YWxlOW5kbVRPajg3YjRFOXRjUUhqMGtORzlSaTFhUEdZcTlTTS9qSE5qTDViREVpeWdJT0tGeXFXN0dGVHFuWkJseDd3WWRzc3hBZ1pManlmWHY0UXovVWdnaHdCNnpWLzM0RnZHWGxtU2dRY0pxRTdJeHZFNFIwVWdWdzZuTk40M3V6bkZ2anpiLzgvRTR4allQUG93cjJBcDVZQVprR012a0hsM1ErbkJSSkN3Yk1UaS81R25Tdm5tTXBobWFjeEhQUmNFajlpSTkzNVZwWHBzRUF4ekR5OXpHZEtmODZsL3RseVdPYkg1bHVocWppTkU2L0s0K2pSd3o5WDdYTWV5QU9FRUJ2OFNmcGdFT3NKZE0rcWJXaU5aY1JFdDg5ZTBxaWE5bm00WGIzLzB5WlgyNnpnYk8xTkpTMU9obk5qUVBhWFhtai92em92SFJCeWV1eW1ndTNtMlhWanVZRXFGMTJnVTBINDluVjFVcTJBV1lwM2pvK0hmbDV2TzVWaWVqL1hONG9VOGNpbkhhRFVwOHkwTmsySndRPSIsICJ1cmwiOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80Nzk3Yzg2ZTNiMjgyMWE5MDEzNDlmNDE1OTBiMDc4OTgyNmE2NmE0MDI4YjlhZjhlODY5OTg2NzE3MzJmNDlmIn0="
export EXECUTED_MINT_JSON=`printf '{"mint":{"token_id":"2","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"%s"}}' $JSON_ENCODED` #&& echo $EXECUTED_MINT_JSON
TXMINT=$(craftd tx wasm execute "$ADDR721SKIN" "$EXECUTED_MINT_JSON" --from $KEY --yes --output json | jq -r '.txhash') && echo $TXMINT



# Query Property check
# CONTRACT_DATA: craftd query wasm contract-state smart $ADDR721 '{"contract_info":{}}'
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"1"}}' --output json) | jq -r '.data.info.token_uri'
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"2"}}' --output json) | jq -r '.data.info.token_uri'
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"3"}}' --output json) | jq -r '.data.info.token_uri'
echo $(craftd q wasm contract-state smart "$ADDR721" '{"all_nft_info":{"token_id":"4"}}' --output json) | jq -r '.data.info.token_uri'
# Skins
echo $(craftd q wasm contract-state smart "$ADDR721SKIN" '{"all_nft_info":{"token_id":"1"}}' --output json) | jq -r '.data.info.token_uri'
echo $(craftd q wasm contract-state smart "$ADDR721SKIN" '{"all_nft_info":{"token_id":"2"}}' --output json) | jq -r '.data.info.token_uri'

# Query 721 Owned Tokens
craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","start_after":"0","limit":50}}'
craftd query wasm contract-state smart $ADDR721SKIN '{"tokens":{"owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","start_after":"0","limit":50}}'


# Query Marketplace Holdings
craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}' 


# list real estate NFT for sale
export NFT_LISTING_BASE64=`printf '{"list_price":"77"}' | base64 -w 0`
export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"3","msg":"%s"}}' $ADDRM $NFT_LISTING_BASE64`
craftd tx wasm execute "$ADDR721" "$SEND_NFT_JSON" --gas-prices="0.025ucraft" -y --from $KEY
# list skins nft for sale
export NFT_LISTING_BASE64=`printf '{"list_price":"69"}' | base64 -w 0`
export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"2","msg":"%s"}}' $ADDRM $NFT_LISTING_BASE64`
craftd tx wasm execute "$ADDR721SKIN" "$SEND_NFT_JSON" --gas-prices="0.025ucraft" -y --from $KEY



# withdraw NFT so it is no longer for sale
craftd tx wasm execute $ADDRM '{"withdraw_nft":{"offering_id":"1"}}' $CRAFTD_COMMAND_ARGS -y


# gets all contracts which are CW721
# craftd q wasm list-contract-by-code 3 --output json | jq '.contracts'
# So our API could query this list, check which a user owns, so we get ALL iamges they own.


# buy the NFT with mykey2 & with ucraft
# offering_id should match with {"get_offerings": {}} id:
export SEND_FUNDS_PURCHASE_NFT=`printf '{"buy_nft":{"offering_id":"2"}}' $OFFERING_ID_MSG_BASE64`
craftd tx wasm execute $ADDRM $SEND_FUNDS_PURCHASE_NFT --gas-prices="0.025ucraft" --amount 11ucraft -y --from $KEY2