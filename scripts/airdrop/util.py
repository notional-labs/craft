import bech32

headers = {
    'accept': 'application/json',
}

NETWORKS = {  
    # https://github.com/Reecepbcups/CosmosGovernanceProposalBot/blob/main/GovBot.py
    # Change to use same name as opperator addresses
    'dig': "https://api-1-dig.notional.ventures", 
    'osmo': "https://lcd-osmosis.blockapsis.com",
    'atom': "https://api.cosmos.network",
    'akash': "https://akash.api.ping.pub",
    'juno': "https://lcd-juno.itastakers.com",
}

GENESIS_VALIDATORS = { # When looping through delegators, if they are in this .keys(), ignore
    'akashvaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx5kk698d': 1.2, # ChandraStation
    'osmovaloper10ymws40tepmjcu3a2wuy266ddna4ktas0zuzm4': 1.2,  # ChandraStation
    'junovaloper106y6thy7gphzrsqq443hl69vfdvntgz260uxlc': 1.2,  # ChandraStation
    'sentvaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx543ss9m': 1.2,  # ChandraStation
    'emoneyvaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx5ev9y8d': 1.2,  # ChandraStation
    'comdexvaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx59hp5ft': 1.2,  # ChandraStation
    'gravityvaloper1728s3k0mgzmc38eswpu9seghl0yczupyhc695s': 1.2,  # ChandraStation
    'digvaloper1dv3v662kd3pp6pxfagck4zyysas82adspfvtw4': 1.2,   # ChandraStation
    'chihuahuavaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx5pd5hur': 1.2,  # ChandraStation

    'digvaloper1ms3k4d9j7rzpzmq3d4jg4j4kffldfnq66wxdpj': 1.05, # pbcups
}

BLACKLISTED_CENTRAL_EXCHANGES = {
    'cosmosvaloper156gqf9837u7d4c4678yt3rl4ls9c5vuursrrzf': "Binance Staking",
    'cosmosvaloper1a3yjj7d3qnx4spgvjcwjq9cw9snrrrhu5h6jll': "Coinbase Custody",
    'cosmosvaloper1nm0rrq86ucezaf8uj35pq9fpwr5r82clzyvtd8': "Kraken",
}

def convert_address_to_craft(address) -> str:
    if address.startswith('0x'):
        return None  # TODO: DIG 0x addresses? how do we convert to beh32
    _, data = bech32.bech32_decode(address)
    return bech32.bech32_encode('craft', data)