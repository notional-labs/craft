KEY="mykey"
CHAINID="test-1"
MONIKER="localtestnet"
KEYALGO="secp256k1"
KEYRING="test"
LOGLEVEL="info"
# to trace evm
#TRACE="--trace"
TRACE=""

# validate dependencies are installed
command -v jq > /dev/null 2>&1 || { echo >&2 "jq not installed. More info: https://stedolan.github.io/jq/download/"; exit 1; }

# remove existing daemon
rm -rf ~/.craft*

craftd config keyring-backend $KEYRING
craftd config chain-id $CHAINID

# if $KEY exists it should be deleted
# decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry
# craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl
echo "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry" | craftd keys add $KEY --keyring-backend $KEYRING --algo $KEYALGO --recover
# Set moniker and chain-id for Evmos (Moniker can be anything, chain-id must be an integer)
craftd init $MONIKER --chain-id $CHAINID 

# Function updates the config based on a jq argument as a string
update_test_genesis () {
  # update_test_genesis '.consensus_params["block"]["max_gas"]="100000000"'
  cat $HOME/.craftd/config/genesis.json | jq "$1" > $HOME/.craftd/config/tmp_genesis.json && mv $HOME/.craftd/config/tmp_genesis.json $HOME/.craftd/config/genesis.json
}


# Set gas limit in genesis
update_test_genesis '.consensus_params["block"]["max_gas"]="100000000"'
update_test_genesis '.app_state["gov"]["voting_params"]["voting_period"]="15s"'

# Change chain options to use EXP as the staking denom for craft
update_test_genesis '.app_state["staking"]["params"]["bond_denom"]="uexp"'
update_test_genesis '.app_state["bank"]["params"]["send_enabled"]=[{"denom": "uexp","enabled": true}]'
update_test_genesis '.app_state["staking"]["params"]["min_commission_rate"]="0.050000000000000000"'

# update from token -> ucraft
update_test_genesis '.app_state["mint"]["params"]["mint_denom"]="ucraft"'
update_test_genesis '.app_state["exp"]["params"]["ibc_asset_denom"]="ucraft"'
update_test_genesis '.app_state["gov"]["deposit_params"]["min_deposit"]=[{"denom": "ucraft","amount": "10000000"}],'
update_test_genesis '.app_state["crisis"]["constant_fee"]={"denom": "ucraft","amount": "1000"}'


# Allocate genesis accounts (cosmos formatted addresses)
craftd add-genesis-account $KEY 1000000000000000000000000uexp,10000000000ucraft --keyring-backend $KEYRING
# Adds token to reece
craftd add-genesis-account craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0 8000000uexp,10000000000ucraft --keyring-backend $KEYRING

# Sign genesis transaction
craftd gentx $KEY 1000000000000000000000uexp --keyring-backend $KEYRING --chain-id $CHAINID

# Collect genesis tx
craftd collect-gentxs

# Run this to ensure everything worked and that the genesis file is setup correctly
craftd validate-genesis

if [[ $1 == "pending" ]]; then
  echo "pending mode is on, please wait for the first block committed."
fi

# Opens the RPC endpoint to outside connections
sed -i '/laddr = "tcp:\/\/127.0.0.1:26657"/c\laddr = "tcp:\/\/0.0.0.0:26657"' ~/.craftd/config/config.toml
sed -i 's/cors-allowed-origins = \[\]/cors-allowed-origins = \["\*"\]/g' ~/.craftd/config/config.toml

# # Start the node (remove the --pruning=nothing flag if historical queries are not needed)
craftd start --pruning=nothing  --minimum-gas-prices=0.0001token --mode validator         

