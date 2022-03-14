from util import convert_address_to_craft
from util import GENESIS_VALIDATORS, BLACKLISTED_CENTRAL_EXCHANGES, NETWORKS, headers

import requests

'''
CraftEconomy Airdrops
https://github.com/notional-labs/craft/issues/17
AirDrop Docs:
https://blog.crafteconomy.io/airdrop.html

HOW TO RUN: (Can likely just do this via endpoints, so no need)
sudo wget -qO /usr/local/bin/yq https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64
sudo chmod a+x /usr/local/bin/yq
digd q staking delegations-to digvaloper1ms3k4d9j7rzpzmq3d4jg4j4kffldfnq66wxdpj | yq e -o=json > dig-pbcups.json
'''

# TOTAL_AIRDROP_AMOUNT = 37_500_000
# FORMULA = math.min(multiplier*(Allocated $CRAFT/ElligbleSuppy)*math.sqrt(balanceAmount), maxAmount)

def main():
    WEBSITE = NETWORKS['juno']
    get_all_validators_in_network(WEBSITE) 
    # calculate_bonus_airdrops()
    # get_delegators_of_validator(NETWORKS['dig'], 'digvaloper1ms3k4d9j7rzpzmq3d4jg4j4kffldfnq66wxdpj')



def get_all_validators_in_network(website_link) -> dict:
    '''
    Returns a dict of validators in a given network
    '''
    response = requests.get(f'{website_link}/cosmos/staking/v1beta1/validators', headers=headers).json()
    
    validators = {}
    for i in range(len(response['validators'])):
        bonus_multiplier = 1.0
        validator = response['validators'][i]  
        opp_address = validator['operator_address']

        if opp_address in BLACKLISTED_CENTRAL_EXCHANGES.keys():
            continue # No airdrop for central exchanges (Coinbase, Binance, et.)

        if opp_address in GENESIS_VALIDATORS.keys():
            bonus_multiplier = GENESIS_VALIDATORS[opp_address]

        if bonus_multiplier > 1:
            print(validator['operator_address'] , bonus_multiplier)

        validators[opp_address] = bonus_multiplier

    return validators


def get_delegators_of_validator(website_link, validator_addr):
    delegators = {}
    response = requests.get(f'{website_link}/cosmos/staking/v1beta1/validators/{validator_addr}/delegations', headers=headers).json()
    
    for delegator in response['delegation_responses']:
        # Save to delegators dict. Maybe divide balance amount /1_000_000 to make it whole balances
        print(delegator['delegation']['delegator_address'], delegator['balance']['amount'], delegator['balance']['denom'])


def calculate_bonus_airdrops():
    for opperator_address in GENESIS_VALIDATORS.keys():
        for denom in NETWORKS.keys():
            if opperator_address.startswith(denom):
                link = NETWORKS[denom]

        # print(opperator_address, link)
        get_delegators_of_validator(link, opperator_address)
        break



if __name__ == "__main__":
    main()