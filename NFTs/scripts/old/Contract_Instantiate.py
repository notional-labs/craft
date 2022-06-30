"""
class Contract_Instantiate:   
    # After uploading to the chain, we call this class to actually get get an instance of them 
    @staticmethod
    def C20():
        '''
        Uploads CW20 contract & returns the address
        '''
        init_cw20_balances = [
            {"address":f"{admin_wallet}","amount": "100"}, # test account (admin)
            {"address":"craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0","amount": "100"}, # reece
        ]
        cmd = '''craftd tx wasm instantiate $C20 '{"name": "{CW_CONTRACT_NAME}","symbol": "{SYMBOL}","decimals": 6,"initial_balances": {INIT_BALS},"mint": {"minter": "{ADMIN_WALLET}"}}' --label "{LABEL}" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR --output json | jq -r '.txhash'''' \
            .replace("{INIT_BALS}", json.dumps(init_cw20_balances, separators=(',',":"))) \
            .replace("{CW_CONTRACT_NAME}", "craft-cw-20-placeholder") \
            .replace("{SYMBOL}", "CRAFTR") \
            .replace("{LABEL}", "cw20-base") \
            .replace("{ADMIN_WALLET}", admin_wallet)
        TX_HASH = os.popen(cmd).read().strip()
        # print(TX_HASH)
        cmd2 = f'''craftd q tx {TX_HASH} --output json | jq -r '.logs[].events[].attributes[] | select(.key=="_contract_address").value''''
        # print(cmd2)
        contractID = os.popen(cmd2).read().strip()
        # print(contractID) # craft1qg5ega6dykkxc307y25pecuufrjkxkaggkkxh7nad0vhyhtuhw3shge3vd
        return {"tx_hash": TX_HASH, "contract_address": contractID}

    @staticmethod
    def C721():
        '''
        Uploads CW721 contract & returns the address
        '''
        cmd = '''craftd tx wasm instantiate {C721_ID} '{"name": "{CW_CONTRACT_NAME}","symbol": "{SYMBOL}","minter": "{ADMIN_WALLET}"}' --label "{LABEL}" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR --output json | jq -r '.txhash'''' \
            .replace("{CW_CONTRACT_NAME}", "craftd-realestate4") \
            .replace("{SYMBOL}", "CRE") \
            .replace("{C721_ID}", f"{CODE_721}") \
            .replace("{LABEL}", "cw721-base-craft2") \
            .replace("{ADMIN_WALLET}", admin_wallet)
        print(cmd)
        TX_HASH = os.popen(cmd).read().strip()
        # print(TX_HASH)
        time.sleep(0.5)
        cmd2 = f'''craftd q tx {TX_HASH} --output json | jq -r '.logs[].events[].attributes[] | select(.key=="_contract_address").value''''
        # print(cmd2)
        contractID = os.popen(cmd2).read().strip()
        values = {"tx_hash": TX_HASH, "contract_address": contractID}
        print(values)
        return values

    @staticmethod
    def CM():
        '''
        Uploads CW721 contract & returns the address
        '''
        # init_cw20_balances = [
        #     {"address":f"{admin_wallet}","amount": "100"}, # test account (admin)
        #     {"address":"craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0","amount": "100"}, # reece
        # ]
        cmd = '''craftd tx wasm instantiate $CM '{"name": "{CW_CONTRACT_NAME}"}' --label "{LABEL}" $CRAFTD_COMMAND_ARGS --admin $KEY_ADDR --output json | jq -r '.txhash'''' \
            .replace("{CW_CONTRACT_NAME}", "craft-marketplace-nfts") \
            .replace("{LABEL}", "marketplace")
        TX_HASH = os.popen(cmd).read().strip()
        print(TX_HASH)
        cmd2 = f'''craftd q tx {TX_HASH} --output json | jq -r '.logs[].events[].attributes[] | select(.key=="_contract_address").value''''
        # print(cmd2)
        contractID = os.popen(cmd2).read().strip()
        # print(contractID) # craft1xr3rq8yvd7qplsw5yx90ftsr2zdhg4e9z60h5duusgxpv72hud3sc3plyl
        return {"tx_hash": TX_HASH, "contract_address": contractID}
"""
# cw20_contract_address = Contract_Instantiate.C20()
# print(f"{cw20_contract_address}")
# cw721_contract_address = Contract_Instantiate.C721()
# print(f"{cw721_contract_address}")
# cm_contract_address = Contract_Instantiate.CM()
# print(f"{cm_contract_address}")