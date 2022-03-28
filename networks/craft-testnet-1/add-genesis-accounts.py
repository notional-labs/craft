from operator import ge
import os
import json
from pathlib import Path

# cd networks/craft-testnet-1/

GENESIS_FILE=f"{Path.home()}/.craftd/config/genesis.json"

# load genesis.json & remove all values for accounts & supply
with open(GENESIS_FILE) as f:
    genesis = json.load(f)
    genesis["app_state"]['auth']["accounts"] = []
    genesis["app_state"]['bank']["balances"] = []
    genesis["app_state"]['bank']["supply"] = []

# save genesis.json
with open(GENESIS_FILE, 'w') as f:
    json.dump(genesis, f, indent=4)


gentx_files = os.listdir('gentx')
for file in gentx_files:
    f = open('gentx/' + file, 'r')
    data = json.load(f)

    validatorData = data['body']['messages'][0]
    moniker = validatorData['description']['moniker']
    delegator = validatorData['delegator_address']
    amt = validatorData['value']['amount']

    if delegator == "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh":
        print(f"craftd add-genesis-account {delegator} 100000000000000ucraft,30000000000uexp #pbcups")
    else:
        print(f"craftd add-genesis-account {delegator} {amt}uexp #{moniker}")