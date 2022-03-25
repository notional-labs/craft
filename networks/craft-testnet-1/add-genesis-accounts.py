import os
import json
from pydoc import cram

# cd networks/craft-testnet-1/

gentx_files = os.listdir('gentx')
for file in gentx_files:
    f = open('gentx/' + file, 'r')
    data = json.load(f)

    validatorData = data['body']['messages'][0]
    delegator = validatorData['delegator_address']
    amt = validatorData['value']['amount']

    if delegator == "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh":
        print(f"craftd add-genesis-account {delegator} 100000000000000ucraft,1000000uexp")
    else:
        print(f"craftd add-genesis-account {delegator} {amt}uexp")