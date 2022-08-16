import json, os

FOLDER_NAME="gentxs_from_strings"
GENTXS = [
    """{"body":{"messages":[{"@type":"/cosmos.staking.v1beta1.MsgCreateValidator","description":{"moniker":"pbcups","identity":"","website":"https://reece.sh","security_contact":"reece@crafteconomy.io","details":""},"commission":{"rate":"0.050000000000000000","max_rate":"0.200000000000000000","max_change_rate":"0.050000000000000000"},"min_self_delegation":"1","delegator_address":"craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh","validator_address":"craftvaloper13vhr3gkme8hqvfyxd4zkmf5gaus840j5v27flg","pubkey":{"@type":"/cosmos.crypto.ed25519.PubKey","key":"Uv/iDHTCTjKtOFhV8wnUquCBl3x3CePTkGDK+lcZGgU="},"value":{"denom":"uexp","amount":"1000000"}}],"memo":"442b5eca3de3557ba24a2928553e701241bb5dae@65.109.38.251:26656","timeout_height":"0","extension_options":[],"non_critical_extension_options":[]},"auth_info":{"signer_infos":[{"public_key":{"@type":"/cosmos.crypto.secp256k1.PubKey","key":"AwS2eOmDNypUhlwWCj/b7JwiszaY/YiUMZa9E1xFX3Gf"},"mode_info":{"single":{"mode":"SIGN_MODE_DIRECT"}},"sequence":"0"}],"fee":{"amount":[],"gas_limit":"200000","payer":"","granter":""},"tip":null},"signatures":["rfhG2MSSPf8ubNcTXyptAO/cK+qAwXG2QbJuGfPl8kE26gXKMMba734Kl8GJuBjwp3jaDTJ/TvYPQKaRkl5wYw=="]}""",
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