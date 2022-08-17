import json, os

FOLDER_NAME="gentxs_from_strings"
GENTXS = [
    # testdedi
    '''{"body":{"messages":[{"@type":"/cosmos.staking.v1beta1.MsgCreateValidator","description":{"moniker":"mytestdedi","identity":"","website":"https://google.com","security_contact":"dedi@email.io","details":""},"commission":{"rate":"0.069000000000000000","max_rate":"1.000000000000000000","max_change_rate":"0.250000000000000000"},"min_self_delegation":"1","delegator_address":"craft1r8sd9cgfnwzyt0dm06hnajy9mkljhl6suczml5","validator_address":"craftvaloper1r8sd9cgfnwzyt0dm06hnajy9mkljhl6s8uqjkt","pubkey":{"@type":"/cosmos.crypto.ed25519.PubKey","key":"0ixMUdga80raj26sO80xd5FAhg1zProoAsQwoWvu1pc="},"value":{"denom":"uexp","amount":"1000000"}}],"memo":"b51c7a36fc212392c33d20c3e8e848bafffdb859@95.217.113.126:26656","timeout_height":"0","extension_options":[],"non_critical_extension_options":[]},"auth_info":{"signer_infos":[{"public_key":{"@type":"/cosmos.crypto.secp256k1.PubKey","key":"AjNfUYKrWXhVuv5d7isaPR1iVckVFap7Or2CcZnj3EP1"},"mode_info":{"single":{"mode":"SIGN_MODE_DIRECT"}},"sequence":"0"}],"fee":{"amount":[],"gas_limit":"200000","payer":"","granter":""},"tip":null},"signatures":["RjSO+335bs9r3YlmxufMG9LnCRDNlllaCnTSbPqGBMJ5BVfc6rObjeoD/sJUJRW4IzvtneWh+RCGNXrk94IMtA=="]}''',
    # craft-validator machine
    '''{"body":{"messages":[{"@type":"/cosmos.staking.v1beta1.MsgCreateValidator","description":{"moniker":"craft-validator","identity":"","website":"https://reece.sh","security_contact":"validator@email.io","details":""},"commission":{"rate":"0.050000000000000000","max_rate":"0.200000000000000000","max_change_rate":"0.050000000000000000"},"min_self_delegation":"1","delegator_address":"craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh","validator_address":"craftvaloper13vhr3gkme8hqvfyxd4zkmf5gaus840j5v27flg","pubkey":{"@type":"/cosmos.crypto.ed25519.PubKey","key":"WXiOnJpTAfZy/l/9L4W1IkHzfgr1NvuOO8fEa8RPV9E="},"value":{"denom":"uexp","amount":"1000000"}}],"memo":"589efdbd55b593869b83f123e34930573fdf904b@65.109.38.251:26656","timeout_height":"0","extension_options":[],"non_critical_extension_options":[]},"auth_info":{"signer_infos":[{"public_key":{"@type":"/cosmos.crypto.secp256k1.PubKey","key":"AwS2eOmDNypUhlwWCj/b7JwiszaY/YiUMZa9E1xFX3Gf"},"mode_info":{"single":{"mode":"SIGN_MODE_DIRECT"}},"sequence":"0"}],"fee":{"amount":[],"gas_limit":"200000","payer":"","granter":""},"tip":null},"signatures":["ZWJROQlOreN33Vysuy+B5KvdS1SglWkhsX554W0+cwZMJ3CAxAdqwNGe/0SpWJDzoCKg/CLODSwgBnuzq0OXLQ=="]}''',    
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