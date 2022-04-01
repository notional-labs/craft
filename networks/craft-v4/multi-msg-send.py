import os
import json

'''
Generates all CRAFT addresses and sends them some coins, excluding reece's address via 1 MsgSend Tx
'''

# cd networks/craft-testnet-1/

default = """{"body":{"messages":[{"@type":"/cosmos.bank.v1beta1.MsgSend","from_address":"craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh","to_address":"craft1p79gj0hcg7wp74df8xaf969tr37gt0t9f0x30m","amount":[{"denom":"ucraft","amount":"2000000000"}]}],"memo":"","timeout_height":"0","extension_options":[],"non_critical_extension_options":[]},"auth_info":{"signer_infos":[],"fee":{"amount":[{"denom":"ucraft","amount":"4000"}],"gas_limit":"200000","payer":"","granter":""},"tip":null},"signatures":[]}"""

output = []
# {"@type":"/cosmos.bank.v1beta1.MsgSend","from_address":"craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh","to_address":"craft1p79gj0hcg7wp74df8xaf969tr37gt0t9f0x30m","amount":[{"denom":"ucraft","amount":"2000000000"}]}
gentx_files = os.listdir('gentx')

idx = 0
for i, file in enumerate(gentx_files):
    f = open('gentx/' + file, 'r')
    data = json.load(f)

    validatorData = data['body']['messages'][0]
    delegator = validatorData['delegator_address']
    amt = validatorData['value']['amount']

    if delegator == "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh": # skip reece
        # print(f"craftd add-genesis-account {delegator} 100000000000000ucraft,1000000uexp")
        continue
    else:

        if 23+idx >= 54: # craft1wycekpw7ut6c5f80890a436r7k2qmjqcd24wnr
            # print(f"craftd tx bank send reece {delegator} 2000000000ucraft --fees 4000ucraft --chain-id craft-testnet-1 --yes --sequence {23+idx}")
            output.append("""{"@type":"/cosmos.bank.v1beta1.MsgSend","from_address":"craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh","to_address":\""""+delegator+"""\","amount":[{"denom":"ucraft","amount":"2000000000"}]}""")
        
    idx += 1


finalOutput = ",".join(output)  
finalOutput += """],"memo":"","timeout_height":"0","extension_options":[],"non_critical_extension_options":[]},"auth_info":{"signer_infos":[],"fee":{"amount":[{"denom":"ucraft","amount":"4000"}],"gas_limit":"200000","payer":"","granter":""},"tip":null},"signatures":[]}"""
print("""{"body":{"messages":[""" + finalOutput)

