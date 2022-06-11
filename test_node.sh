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
echo "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry"
craftd keys add $KEY --keyring-backend $KEYRING --algo $KEYALGO --recover
# Set moniker and chain-id for Evmos (Moniker can be anything, chain-id must be an integer)
craftd init $MONIKER --chain-id $CHAINID 

# Set gas limit in genesis
cat $HOME/.craftd/config/genesis.json | jq '.consensus_params["block"]["max_gas"]="100000000"' > $HOME/.craftd/config/tmp_genesis.json && mv $HOME/.craftd/config/tmp_genesis.json $HOME/.craftd/config/genesis.json
cat $HOME/.craftd/config/genesis.json | jq '.app_state["gov"]["voting_params"]["voting_period"]="15s"' > $HOME/.craftd/config/tmp_genesis.json && mv $HOME/.craftd/config/tmp_genesis.json $HOME/.craftd/config/genesis.json

# Allocate genesis accounts (cosmos formatted addresses)
craftd add-genesis-account $KEY 100000000000000000000000000stake,10000000000token --keyring-backend $KEYRING
# Adds token to reece
craftd add-genesis-account craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0 10000000000token --keyring-backend $KEYRING

# Sign genesis transaction
craftd gentx $KEY 1000000000000000000000stake --keyring-backend $KEYRING --chain-id $CHAINID

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

