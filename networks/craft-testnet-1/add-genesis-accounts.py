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

    if delegator == "craft1zt9q62x909nz0w55nlnd7l8h4vhyzrgkxly9q4":
        continue # chandra station, we add later

    print(f"craftd add-genesis-account {delegator} 1000000uexp")

# chandra station
print("craftd add-genesis-account craft1zt9q62x909nz0w55nlnd7l8h4vhyzrgkxly9q4 200000000uexp")

# pbcups craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh is already added from gentx