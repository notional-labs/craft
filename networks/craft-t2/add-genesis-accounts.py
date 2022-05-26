from operator import ge
import os
import json
from pathlib import Path

'''
Resets the genesis.json file to the default values & clears all balances / accounts
Then will generate all the commands to add balances for all gentxs
'''

# cd networks/craft-t2
LAUNCH_TIME = "2022-05-16T10:15:00Z"
CHAIN_ID = "craft-t2"
EXP_SEND = [{"denom": "uexp","enabled": True}]

GENESIS_FILE=f"{Path.home()}/.craftd/config/genesis.json" # home dir

validatorAddresses = { 
    # taken from craft-v4 gentxs. Hardcoded now. see craft-v4 folder for code to auto generate this
    'craft14qekdkj2nmmwea4ufg9n002a3pud23y8y5nu5v': ' Lavender.Five Nodes 🐝', 
    'craft1zakl4cpuht2aeh85txamdjxf2fa5ujn3dtt79k': 'stakingcabin', 
    'craft10jm8fvdyqlj78w0j5nawc76wsn4pqmdxmcqn33': 'NosNode⛏️', 
    'craft1uhnewhpjm7fz054p7clsfw8myyftf5kau4v86e': 'Blockfend Genesis Labs', 
    'craft15zm7fcjl6vry2ufdxv3vhttst6qdyml678rxyd': 'premet', 
    'craft1w387qauu9a237sd6pkdpxsmg7unj6v8umcj530': 'silent', 
    'craft1p79gj0hcg7wp74df8xaf969tr37gt0t9f0x30m': 'cyberG', 
    'craft1g0ggan7f94rnrkat4ql07rym5gkwa3j4fgsfa3': 'Lex_Prime', 
    'craft1edx0xs08q26lc250yv09645hhtaqn77vyf2t50': 'TienThuatToan Capital', 
    'craft14rmsffw6fsaq82fsrw6g22utn6ss5039phgj3k': 'carbonator', 
    'craft1hcm76ufl3weqflkf88tt7clmf9jdge4vva9dlc': 'TM.Interchainers', 
    'craft1w0dpqj64wz8v8t0u6l3jj6a9l4dhnyl5emr7cx': 'panxinyang', 
    'craft1pmjtg54j3ev65249jp2twf6vhsu9mzvsrfna0l': 'Imperator', 
    'craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh': 'pbcups', 
    'craft1a8ddr30zx8tees0pq90vkapfmhll0u5j42j2ej': 'osmosis', 
    'craft1mu8gf6j38unc6zzry9a8q5n0czl9q30zkt9h83': 'pulsar', 
    'craft1pfwu20k8eaw4qydzdz05sjl5a00v9z60haw0pp': 'Cros-Nest', 
    'craft1jt9w26mpxxjsk63mvd4m2ynj0af09cslzhd3vd': 'polkachu.com', 
    'craft189v3uk0ek76ffswzdt052sln9c30hg9k8yyneh': 'Crypto Compass',
    'craft1f3hlyeaqclwx6a6aurw8cquusxapuxpmjcgs7a': 'PFC', 
    'craft12vtd3cydrkgumm668qa6ccjl6drskfad9hsj8n': 'ZenChainLabs', 
    'craft12lh9mwuadacywsujjtd088y6j35k8x874rkgk9': 'Forbole', 
    'craft1zt9q62x909nz0w55nlnd7l8h4vhyzrgkxly9q4': 'Chandra Station', 
    'craft1up3slj4nr5y23gxkvqn8h5ra9mv0pj2v84g0me': 'SpacePotato',
    'craft1wtqqettvaahq78lem9usp690s28jy06aeawnra': 'SkyNet | Validators', 
    'craft1nw0j25kv6r2fk2t7vnnevx08eg950p2snf6csz': 'Infinity | carbonZERO🌲', 
    'craft1zl4vt84hya03e8hu7dx4q4cvn2ts2xdrtn29r2': 'Stake Frites (🥩 , 🍟)', 
    'craft1h3sjjuvvs6957awj5dlp0ulv6gnfaky0agxgx8': 'donadel', 
    'craft1pxqawx4kst06jj7wntmjejl7k036xstnpu2ft4': 'mcb', 
    'craft1nv48u6dguupmg8dp8ktnmmhsxgqs0f792ydg2d': 'seltonstake', 
    'craft1etx55kw7tkmnjqz0k0mups4ewxlr324t5rxcm6': 'NodeStake.top', 
    'craft1yyelzq8hlc7qaufyn3uywfygmncjw3sazwfv9e': 'web34ever', 
    'craft122nlntdquyrsjcwvxvh8acse6fer98y6ak432f': 'Stakers', 
    'craft198zayzplu4tq5gyx55awuwva69ut9r6kpvjw7e': 'Chill Validation', 
    'craft1xap0xc4p83x2scpnplv5k3c6lkr9amgy7zlx6f': 'Bloqhub', 
    'craft14838ulgmg07pmuse4ujmy3ljv4h8q0s8g50p2y': 'RED', 
    'craft1ddgtjj5dkakq2f20sgy76aay33gvep8mf6ysmr': 'hailbiafra', 
    'craft1n33gppg3v99yhgdjs327gj75kthxfdyfg8wf4k': 'stake98', 
    'craft19w2488ntfgpduzqq3sk4j5x387zynwknj8rxy3': 'kingnodes', 
    'craft1wycekpw7ut6c5f80890a436r7k2qmjqcd24wnr': 'Analytic Dynamix', 
    'craft1vclem3jshzgdtuwapcalurhz7jveqzumqfts72': 'RHINO', 
    'craft13ggl3l8fgc60xndx3sztygn897x757vlsnva2n': 'The Pizza Tech', 
    'craft1cm6a5ffwddm3lfj3l9rtfyx88shuvzhcwu6zgc': 'ECO Stake 🌱', 
    'craft17n56v5xsdf80lfncr3jq34ct49pstegyz8sn0h': 'Enigma', 
    'craft1kauwk8402rqywk547xv9tq3e7j50057t9r8j8g': 'smyr'
    }

def main():
    outputDetails()
    # resetGenesisFile()
    # createGenesisAccountsCommands()
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
    for address in validatorAddresses.keys():
        coins = f"10000000000ucraft,1000000uexp"
        moniker = validatorAddresses[address]

        if address == "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh":
            coins = "100000000000000ucraft,30000000000uexp" # pbcups

        print(f"craftd add-genesis-account {address} {coins} #{moniker}")


if __name__ == "__main__":
    main()