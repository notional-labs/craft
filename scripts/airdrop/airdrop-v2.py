from util import AIRDROP_DISTRIBUTIONS, convert_address_to_craft
from util import GENESIS_VALIDATORS, BLACKLISTED_CENTRAL_EXCHANGES, NETWORKS, headers, AIRDROP_RATES

import requests
import json

'''
CraftEconomy Airdrops
https://github.com/notional-labs/craft/issues/17
AirDrop Docs:
https://blog.crafteconomy.io/airdrop.html

HOW TO RUN: (Can likely just do this via endpoints, so no need)
sudo wget -qO /usr/local/bin/yq https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64
sudo chmod a+x /usr/local/bin/yq
digd q staking delegations-to digvaloper1ms3k4d9j7rzpzmq3d4jg4j4kffldfnq66wxdpj | yq e -o=json > dig-pbcups.json

Logic:
- Loop through all cosmos networks we are doing the airdrop for (osmo, dig, etc.)
- Get all validators in that network [applies for: Omos, akash, atom, juno, Osmo LP pool #1 & #561 (luna/osmo)]
  (Airdrop groups 3, 4 and 6)
'''

TOTAL_AIRDROP_AMOUNT = 37_500_000
# FORMULA = math.min(multiplier*(Allocated $CRAFT/ElligbleSuppy)*math.sqrt(balanceAmount), maxAmount)

# holds their craft address & total craft they get for the airdrop
craft_airdrop_amounts = {}

def main(): 
    # d = get_all_validators_and_their_airdrop_bonus('osmo') 
    
    # all_delegates = {}

    # network = "dig"
    for network in ['dig']:
        # delegates = get_delegators_of_validator(network, 'digvaloper1ms3k4d9j7rzpzmq3d4jg4j4kffldfnq66wxdpj') #pbcups validator testing

        # {XXXXvaloperXXXXXXXX: 1.0, XXXXvaloperCHANDRASTATION: 1.20}
        all_validators = get_all_validators_and_their_airdrop_bonus(network) 

        for idx, validator in enumerate(all_validators):
            
            delegations = get_delegators_of_validator(network, validator)

            for address in delegations: 
                # all_delegates[address] = delegations[address]
                craftAddress = convert_address_to_craft(address)
                # are we doing airdrop rates OR a set amount no matter the delegation enom? 
                # (ex. you get same amount of craft if you delegated osmo or dig, since its a per wallet basis)

                # TODO: If above is true, loop through all_validators & get len, then divide total_supply_for_this_group / len() = craftPerAddress
                # add_airdrop_to_account(craftAddress, int(delegations[address]) / AIRDROP_RATES[network])

            if idx == 10:
                break # Testing
    
    total_supply_for_this_group = 10_000_000
    craftPerAddress = (total_supply_for_this_group / len(craft_airdrop_amounts))
    print(craftPerAddress, "craft per address")



def get_all_validators_and_their_airdrop_bonus(name) -> dict:
    '''
    Returns a dict of validators in a given network
    '''
    website_link = NETWORKS[name]
    response = requests.get(f'{website_link}/cosmos/staking/v1beta1/validators', headers=headers).json()
    
    validators = {}
    for i in range(len(response['validators'])-1):
        bonus_multiplier = 1.0
        validator = response['validators'][i]  
        moniker = validator['description']['moniker']
        opp_address = validator['operator_address']
        isJailed = validator['jailed']

        if opp_address in BLACKLISTED_CENTRAL_EXCHANGES.keys():
            continue # No airdrop for central exchanges (Coinbase, Binance, et.)

        if isJailed:
            continue # only active validators

        if opp_address in GENESIS_VALIDATORS.keys():
            bonus_multiplier = GENESIS_VALIDATORS[opp_address]

        # if bonus_multiplier > 1: # Ensure genesis validators get the bonus applied to their lookup
        #     print(f"{validator['operator_address']} {bonus_multiplier}\t{moniker}")
        validators[opp_address] = bonus_multiplier
    return validators


def get_delegators_of_validator(name, validator_addr) -> dict:
    website_link = NETWORKS[name]
    print(f"Getting delegators for {name}: {validator_addr}")

    delegators = {}
    response = requests.get(f'{website_link}/cosmos/staking/v1beta1/validators/{validator_addr}/delegations', headers=headers).json()
    
    boostMultiplier = 1.0
    if validator_addr in GENESIS_VALIDATORS.keys():
        boostMultiplier = GENESIS_VALIDATORS[validator_addr]
        print("BOOSTED to " + str(boostMultiplier))

    for delegator in response['delegation_responses']:
        # Save to delegators dict. Maybe divide balance amount /1_000_000 to make it whole balances
        # print(delegator['delegation']['delegator_address'], delegator['balance']['amount'], delegator['balance']['denom'])

        # if they are delegated to a genesis validator, boost their amount by 5% (or 20% for chandra)
        delegators[delegator['delegation']['delegator_address']] = int(delegator['balance']['amount'])*boostMultiplier

    return delegators # {'digxxxxx': amountHeld}

def add_airdrop_to_account(craft_address, amount):
    global craft_airdrop_amounts
    if craft_address not in craft_airdrop_amounts:
        craft_airdrop_amounts[craft_address] = 0
    craft_airdrop_amounts[craft_address] += amount
    pass

def fairdrop_for_osmosis_pools():
    '''Group #2 - LPs for pool #1 and #561 (luna/osmo)'''
    # TODO: POOL #1 & #561 (luna/osmo) - not done
    # osmosisd export 2138101 > state_export.json
    # osmosisd export-derive-balances state_export.json balances.json --breakdown-by-pool-ids 1,561
    with open('balances.json') as f:
        balances = json.load(f)

    # Gets # of accounts in the pools, and gets gets how much craft everyone gets evenly
    # (their % of 37_500_000/totalAccountsNumber)
    amountOfAccouts = len(balances)
    craft_per_LPer = (AIRDROP_DISTRIBUTIONS[2] / len(amountOfAccouts))

    for account in balances:
        address = account['address'] # something like this
        craftAddress = convert_address_to_craft(address)
        add_airdrop_to_account(craftAddress, int(craft_per_LPer))


# def calculate_bonus_airdrops():
#     for opperator_address in GENESIS_VALIDATORS.keys():
#         for denom in NETWORKS.keys():
#             if opperator_address.startswith(denom):
#                 link = NETWORKS[denom]
#         # print(opperator_address, link)
#         get_delegators_of_validator(link, opperator_address)
#         break



if __name__ == "__main__":
    main()