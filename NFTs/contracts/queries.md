`https://github.com/notional-labs/wasmd/blob/v0.25.0/x/wasm/client/rest/query.go`

```bash
export CRAFTD_NODE=http://65.108.125.182:1317
export CRAFT_721_CONTRACT=craft1qwlgtx52gsdu7dtp0cekka5zehdl0uj3fhp9acg325fvgs8jdzkstnsu5l

# Get information about a contract
curl $CRAFTD_NODE/cosmwasm/wasm/v1/contract/$CRAFT_721_CONTRACT

# Convert a JSON query -> base64, then pass to rest as a request
export MY_QUERY=`printf '{"nft_info":{"token_id":"1"}}' | base64`
curl $CRAFTD_NODE/cosmwasm/wasm/v1/contract/$CRAFT_721_CONTRACT/smart/$MY_QUERY?encoding=base64

# /cosmwasm/wasm/v1/contract/{address}/history
# /cosmwasm/wasm/v1/code/{code_id}/contracts
# curl $CRAFTD_NODE/cosmwasm/wasm/v1/contract/$CRAFT_721_CONTRACT/state
# /cosmwasm/wasm/v1/contract/{address}/raw/{query_data}
# /cosmwasm/wasm/v1/code/{code_id}
# /cosmwasm/wasm/v1/code
# /cosmwasm/wasm/v1/codes/pinned
```