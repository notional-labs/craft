from operator import ge
import os
import json
from pathlib import Path

# cd networks/craft-v4

LAUNCH_TIME = "2022-08-19T20:00:00Z"
CHAIN_ID = "craft-v4"
EXP_SEND = [{"denom": "uexp","enabled": False}]
GENESIS_FILE=f"{Path.home()}/.craftd/config/genesis.json" # Home Dir of the genesis
FOLDER = "gentx" # no need to change

CUSTOM_GENESIS_ACCOUNT_VALUES = {
    # Add some extra to notional / chandra station.
    "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh": "10000000uexp,1000000000000000ucraft #pbcups",  

    # TESTNET V4 - DAO ACCOUNT (multisig for mainnet)
    "craft1n3a53mz55yfsa2t4wvdx3jycjkarpgkf07zwk7": "1000000000uexp,1000000000000000ucraft #dao itself",    
    # TESTNET V4 - GAME PAYMENTS (multisig for mainnet)
    "craft14svh76rr38wkj9d3g5qmsxyjm7dhxk34v57ygy": "1000000000000000ucraft # game payments addr",    

    # https://github.com/notional-labs/craft/pull/12/files
    "craft1lxh0u07haj646pt9e0l2l4qc3d8htfx5se2ntp": "3000000uexp,250000000ucraft # daomember",
    "craft1fpsv3uk2tqq362zvh82777gjexjduu79t8z29v": "3000000uexp,250000000ucraft # daomember",
    "craft1f4sjvl8ujk9g6vtdvratlzmz7k7a5d9dnm325l": "3000000uexp,250000000ucraft # daomember",
    "craft165qvsanfcnm075vld6r90haypxwe27rjlzw6r7": "3000000uexp,250000000ucraft # daomember",
    "craft1uc8a9f43fqc4pum8ejfr3n69l87c87r2ja2xwq": "3000000uexp,250000000ucraft # daomember",
    "craft1nzd8jnu69w8eux3dcg4axxyarm7age86p4pjd4": "3000000uexp,250000000ucraft # daomember",
    "craft1ray0cavvxaa92xp08affex2casrekddgxt2lcl": "3000000uexp,250000000ucraft # daomember",
    "craft12sczd7vmheqat355txqducgm6fk86ye4s5lkz8": "3000000uexp,250000000ucraft # daomember",
    "craft1h2kjnnnryh9ezgzj6yrax4snzfner9qaqjfqr9": "3000000uexp,250000000ucraft # daomember",
    "craft1pcal3gqemz4g9e6p52had37azx2p9hg64rapfk": "3000000uexp,250000000ucraft # daomember",
    "craft13t0vcrdlj3vju5pqgwzlyr7lcw9s96kahfgla4": "3000000uexp,250000000ucraft # daomember",
    "craft1f0043tu4clcs7skhzs7760hw095xzv6tflvvfd": "3000000uexp,250000000ucraft # daomember",
    "craft18r6j04h3pa49kmhazdqz6plt5t35jxswdv4y93": "3000000uexp,250000000ucraft # daomember",
    "craft1j8s87w3fctz7nlcqtkl5clnc805r2404eu8xvq": "3000000uexp,250000000ucraft # daomember",
    "craft1gmgck2kytg9tj60m2c3m9gdaavencp7l77nwd6": "3000000uexp,250000000ucraft # daomember",
    "craft1s42j67d3f6julvx4nhjgmcxf74xfph26t9vcn2": "3000000uexp,250000000ucraft # daomember",
    "craft1r8qt0k0t7kywdndjs2udlem4j7m2yk29ua47mm": "3000000uexp,250000000ucraft # daomember",
    "craft1fasl4wc76fxxxmvkrkzre9cejyn0x2lmgckyqz": "3000000uexp,250000000ucraft # daomember",
    "craft1hg49kyr022qvj9hq6esvm5g9gtax4c262hutn2": "3000000uexp,250000000ucraft # daomember",
    "craft1f0l4wt43gyktrveku2aqc3mw9tz3dk9j7nwese": "3000000uexp,250000000ucraft # daomember",
    "craft14l4g4lvwl0tg6thmpl5q9337drs3he44mr0795": "3000000uexp,250000000ucraft # daomember",
    "craft1dv3v662kd3pp6pxfagck4zyysas82ads89ldp8": "3000000uexp,250000000ucraft # daomember",
    "craft1w9rugshphy0a849yp56klt5ul8y55mne7g7vf0": "3000000uexp,250000000ucraft # daomember",
    "craft1ddd9vf56hv5jntdqkd85dv6je6xes25g3ykyn4": "3000000uexp,250000000ucraft # daomember",
    "craft145r7j5u2868er6ylj3nt9zzg5lnc9gyt4d6282": "3000000uexp,250000000ucraft # daomember",
    "craft1me3g0a2nr24sjykmhvpl687f6zt66nlvhv0y9h": "3000000uexp,250000000ucraft # daomember",
    "craft1ugjgu3hg7jcmafq3tr6g857950vuyj0kua0hka": "3000000uexp,250000000ucraft # daomember",
}

def main():
    outputDetails()
    resetGenesisFile()
    createGenesisAccountsCommands()
    pass

def resetGenesisFile():
    # load genesis.json & remove all values for accounts & supply
    with open(GENESIS_FILE) as f:
        genesis = json.load(f)
        genesis["genesis_time"] = LAUNCH_TIME
        genesis["chain_id"] = str(CHAIN_ID)

        genesis["app_state"]['auth']["accounts"] = []
        genesis["app_state"]['bank']["balances"] = []
        genesis["app_state"]['bank']["supply"] = []
        genesis["app_state"]['bank']["params"]["send_enabled"] = EXP_SEND

        genesis["app_state"]['genutil']["gen_txs"] = []

        genesis["app_state"]['gov']["deposit_params"]['min_deposit'][0]['denom'] = 'uexp'

        genesis["app_state"]['slashing']['params']["signed_blocks_window"] = "10000"
        genesis["app_state"]['slashing']['params']["min_signed_per_window"] = '0.050000000000000000' # 5% * 10,000
        genesis["app_state"]['slashing']['params']["slash_fraction_double_sign"] = '0.050000000000000000' # 5% if you SlashLikeMo
        genesis["app_state"]['slashing']['params']["slash_fraction_downtime"] = '0.01000000000000000' # 0.01% for downtime, like Juno


        genesis["app_state"]['staking']['params']["bond_denom"] = 'uexp' 


        genesis["app_state"]['mint']["minter"]["inflation"] = '0.150000000000000000' # 15% inflation
        genesis["app_state"]['mint']["params"]["mint_denom"] = 'ucraft' # exp pays in ucraft        

        # wasm = permissionless for now.

        genesis["app_state"]['exp']["params"]['max_coin_mint'] = str(10_000_000_000)

        # - multisig here? Maybe we just do reece's account for testnet? or should we do this one so anyone can push through
        genesis["app_state"]['exp']["params"]['daoAccount'] = "craft1n3a53mz55yfsa2t4wvdx3jycjkarpgkf07zwk7" 

        genesis["app_state"]['exp']["params"]['close_pool_period'] = "6000s"
        genesis["app_state"]['exp']["params"]['vesting_period_end'] = "60s"
        genesis["app_state"]['exp']["params"]['burn_exp_period'] = "60s"
        genesis["app_state"]['exp']["params"]['ibc_asset_denom'] = "uexp" # TODO: Is this ucraft or uexp?

        # Maybe in the whitelist, we add some validators / accounts just to test it

        # Is this initial price?
        genesis["app_state"]['exp']["dao_asset"]['dao_token_price'] = "1.000000000000000000"
        genesis["app_state"]['exp']["dao_asset"]['asset_dao'] = []

        genesis["app_state"]['exp']['port_id'] = "ibc-exp"

    # save genesis.json
    with open(GENESIS_FILE, 'w') as f:
        json.dump(genesis, f, indent=4)
    print(f"# RESET: {GENESIS_FILE}\n")


def outputDetails() -> str:
    # get the seconds until LAUNCH_TIME
    launch_time = int(os.popen("date -d '" + LAUNCH_TIME + "' +%s").read())
    now = int(os.popen("date +%s").read())
    seconds_until_launch = launch_time - now

    # convert seconds_until_launch to hours, minutes, and seconds
    hours = seconds_until_launch // 3600
    minutes = (seconds_until_launch % 3600) // 60

    print(f"# {LAUNCH_TIME} ({hours}h {minutes}m) from now\n# {CHAIN_ID}\n# {EXP_SEND}\n# GenesisFile: {GENESIS_FILE}")



def createGenesisAccountsCommands():
    gentx_files = os.listdir(FOLDER)
    # give validators their amounts in the genesis (1uexp & some craft)
    for file in gentx_files:
        f = open(FOLDER + "/" + file, 'r')
        data = json.load(f)

        validatorData = data['body']['messages'][0]
        moniker = validatorData['description']['moniker']
        val_addr = validatorData['delegator_address'] # craftxxxxx
        amt = validatorData['value']['amount']

        if val_addr not in CUSTOM_GENESIS_ACCOUNT_VALUES.keys():
            print(f"craftd add-genesis-account {val_addr} {amt}uexp,10000000000ucraft #{moniker}")
            continue # 
                
    for account in CUSTOM_GENESIS_ACCOUNT_VALUES:
        print(f"craftd add-genesis-account {account} {CUSTOM_GENESIS_ACCOUNT_VALUES[account]}")

    print(f"# [!] COPY-PASTE-RUN THE ABOVE TO CREATE THE GENESIS ACCOUNTS")


if __name__ == "__main__":
    main()