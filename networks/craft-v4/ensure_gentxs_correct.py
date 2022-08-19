import os
import json

# cd networks/craft-testnet-1/

FOLDER="gentx"
if not os.path.exists(FOLDER):
    print('gentx folder not found')
    exit(1)

# get all files within the gentx folder
gentx_files = os.listdir(FOLDER)

invalids = ""
output = ""

for file in gentx_files:
    f = open('gentx/' + file, 'r')
    data = json.load(f)

    validatorData = data['body']['messages'][0]
    moniker = validatorData['description']['moniker']
    rate = float(validatorData['commission']['rate']) * 100
    valop = validatorData['validator_address']
    exp = validatorData['value']
    amt = int(exp["amount"])/1_000_000

    if exp['denom'] != 'uexp':
        invalids += f'[!] Invalid denomination for validator: {moniker} {exp["denom"]} \n'
    elif amt > 1.0:
        invalids += f'[!] Invalid exp amount for validator: {moniker} {amt}\n'
    else:
        output += (f"{valop} {rate}%\texp: {amt}, {moniker.strip()}\n")    

print(output)
print(f"{invalids}")