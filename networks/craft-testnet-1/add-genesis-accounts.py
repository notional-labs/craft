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

    print(f"craftd add-genesis-account {delegator} 1000000uexp")