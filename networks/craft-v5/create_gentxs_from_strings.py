import json, os

FOLDER_NAME="gentx"
GENTXS = [
    # pbcups
    '''{"body":{"messages":[{"@type":"/cosmos.staking.v1beta1.MsgCreateValidator","description":{"moniker":"pbcups","identity":"","website":"https://reece.sh","security_contact":"reece@crafteconomy.io","details":""},"commission":{"rate":"0.050000000000000000","max_rate":"0.200000000000000000","max_change_rate":"0.050000000000000000"},"min_self_delegation":"1","delegator_address":"craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh","validator_address":"craftvaloper13vhr3gkme8hqvfyxd4zkmf5gaus840j5v27flg","pubkey":{"@type":"/cosmos.crypto.ed25519.PubKey","key":"KknYgJv/blBFiu+5zqJoV0rBXU596D8BpgQNFawLkbM="},"value":{"denom":"uexp","amount":"1000000"}}],"memo":"e57fc7232493744712f492f1a00695d9b92329e9@65.109.38.251:26656","timeout_height":"0","extension_options":[],"non_critical_extension_options":[]},"auth_info":{"signer_infos":[{"public_key":{"@type":"/cosmos.crypto.secp256k1.PubKey","key":"AwS2eOmDNypUhlwWCj/b7JwiszaY/YiUMZa9E1xFX3Gf"},"mode_info":{"single":{"mode":"SIGN_MODE_DIRECT"}},"sequence":"0"}],"fee":{"amount":[],"gas_limit":"200000","payer":"","granter":""},"tip":null},"signatures":["+k07OUrBO44/7UtaeTrrDz9izbQcV1vxl3k27f1RCaoY5nO+Nip9A3AOAgrYIvmPPU24Fl8a4dvSGNcagycFJA=="]}''',
    
]

os.makedirs(FOLDER_NAME, exist_ok=True)

for gentx in GENTXS:
    v = json.loads(gentx)
    moniker = v['body']['messages'][0]['description']['moniker']
    memo = v['body']['memo']
    if '@' in memo: print(f"{moniker} - {memo}")

    # create file named moniker.json
    with open(f"{FOLDER_NAME}/{moniker}.json", "w") as f:
        json.dump(v, f)
        # print(f"{moniker}.json created")

print(f"ALL GENTXS CREATED FROM STRINGS. MOVE {FOLDER_NAME} INTO 'gentx' folder. Then run add-genesis-accounts.py")