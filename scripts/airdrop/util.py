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

AIRDROP_DISTRIBUTIONS = { 
    # TODO: NOT REAL VALUES, FOR TESTING ONLY
    # https://github.com/notional-labs/craft/issues/17
    1: 0, # stakers of [akash, osmo, atom, and juno]
    2: 0, # LP's of Pool 1 & 561
    3: 0, # Delegators to Atom relayers
    4: 0, # ChandraStation Deleagators
    5: 0, # ION Holders & LP's
    6: 37_500_000, # Genesis Validators
    7: 0, # Beta Players
}

AIRDROP_RATES = { # TODO: NOT REAL VALUES, FOR TESTING ONLY
    "dig": 10_000_000,   # 10dig = 1 craft
    "osmo": 100_000      # 0.1 osmo = 1 craft
}

GENESIS_VALIDATORS = { 
    # pbcups TODO: Testing
    'digvaloper1ms3k4d9j7rzpzmq3d4jg4j4kffldfnq66wxdpj': 1.07, 

    # ChandraStation - 20% boost
    'akashvaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx5kk698d': 1.2, 
    'osmovaloper10ymws40tepmjcu3a2wuy266ddna4ktas0zuzm4': 1.2,
    'junovaloper106y6thy7gphzrsqq443hl69vfdvntgz260uxlc': 1.2,
    'sentvaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx543ss9m': 1.2,
    'emoneyvaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx5ev9y8d': 1.2,
    'comdexvaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx59hp5ft': 1.2,
    'gravityvaloper1728s3k0mgzmc38eswpu9seghl0yczupyhc695s': 1.2,
    'digvaloper1dv3v662kd3pp6pxfagck4zyysas82adspfvtw4': 1.2, 
    'chihuahuavaloper1lxh0u07haj646pt9e0l2l4qc3d8htfx5pd5hur': 1.2,

    # Kingnodes - Chihuahua Chain
    'chihuahuavaloper10wxn2lv29yqnw2uf4jf439kwy5ef00qdqj3xsc': 1.05,

    # cros-nest - Crypto.org | Atom | Osmosis
    'cosmosvaloper1fsg635n5vgc7jazz9sx5725wnc3xqgr7awxaag': 1.05,
    'osmovaloper1u6jr0pztvsjpvx77rfzmtw49xwzu9kas05lk04': 1.05,
    'crocncl15m2ae4c2ajpkz6hw0d4ucvwfyuwq8ns5z369u8': 1.05,

    # NosNodes - Juno | Osmosis
    'osmovaloper10jm8fvdyqlj78w0j5nawc76wsn4pqmdxgzgh4c': 1.05,
    'junovaloper10jm8fvdyqlj78w0j5nawc76wsn4pqmdxnpxsgg': 1.05,

    # imperator_co - Atom | Osmosis | Juno
    'cosmosvaloper1vvwtk805lxehwle9l4yudmq6mn0g32px9xtkhc': 1.05,
    'osmovaloper1t8qckan2yrygq7kl9apwhzfalwzgc2429p8f0s': 1.05,
    'junovaloper17n3w6v5q3n0tws4xv8upd9ul4qqes0nlg7q0xd': 1.05,

    # SpacePotato - Akash | Juno | Osmosis
    'osmovaloper16gm3cvhluf9xfurkx9qgxq7pldvd479l0j6zms': 1.05,
    'akashvaloper1r4sevyvum9ppqlw75t6h0ex9j4j9dzydu8hczm': 1.05,
    'junovaloper16gm3cvhluf9xfurkx9qgxq7pldvd479l5359xq': 1.05,

    # SkyNetValidator - Osmosis | Akash | Juno
    'osmovaloper1zfcmwh56kmz4wqqg2t8pxrm228dx2c6hhzhtx7': 1.05,
    'akashvaloper1zfcmwh56kmz4wqqg2t8pxrm228dx2c6hzh0ewm': 1.05,
    # 'juno': 1.05,  ??

    # ThePizzaTech - Desmos | G-Bridge
    'desmosvaloper1hwfp4kcuauvutjdhvm9qpputreywl8tyrnx0y3': 1.05,
    'gravityvaloper1lu9p4tl02nl979l9huk33x8rgnwzwmysap0s60': 1.05,

    # AltcoinPsyco - Akash | Sentinel | Persistence
    'akashvaloper1d4sctjlcqjrfkxpp0a5hwsntfhzy7mz2lhujx8': 1.05,
    'sentvaloper12kz3rd47mpljfzqc9ygrpelj5kp6tx0dl8ylrk': 1.05,
    'persistencevaloper17egy2hqqrutajzvf4f6qe2qzc3uey7ysvkeftp': 1.05,

    # notionaldao - Osmosis | Huahua | Juno
    'osmovaloper1083svrca4t350mphfv9x45wq9asrs60c6rv0j5': 1.05,
    'chihuahuavaloper1h6vcu4r2hx70x5f0l3du3ey2g98u9ut2tafnnv': 1.05,
    'junovaloper1083svrca4t350mphfv9x45wq9asrs60cpqzg0y': 1.05,

    # Sentinel_co - Osmosis | Atom | Comdex
    'cosmosvaloper1u6ddcsjueax884l3tfrs66497c7g86skn7pa0u': 1.05,
    'osmovaloper1cyw4vw20el8e7ez8080md0r8psg25n0cq98a9n': 1.05,
    'comdexvaloper1ndslxsucavg3eglqe4mzge74tdx67rcnd7dawq': 1.05,

    # forbole - Akash | Atom | Crypto.org| Osmosis
    'cosmosvaloper14kn0kk33szpwus9nh8n87fjel8djx0y070ymmj': 1.05,
    'osmovaloper14kn0kk33szpwus9nh8n87fjel8djx0y0fhtak5': 1.05,
    'crocncl15xphw2m025acwnjd2ucq9t5ku4ggaqyecekzqa': 1.05,
    'akashvaloper14kn0kk33szpwus9nh8n87fjel8djx0y0uzn073': 1.05,

    # nodes_smart -  Osmosis | Juno | Atom
    'osmovaloper16j5hsdrcaa6950ks0rf944rgmncukl74cs7yw6': 1.05,
    'cosmosvaloper1hdrlqvyjfy5sdrseecjrutyws9khtxxaux62l7': 1.05,
    'junovaloper1q3jsx9dpfhtyqqgetwpe5tmk8f0ms5qywje8tw': 1.05,

    # vitwit_ (WitVal) - Atom | Akash | Osmosis | Juno | Regen
    'cosmosvaloper1ddle9tczl87gsvmeva3c48nenyng4n56nghmjk': 1.05,
    'akashvaloper1qwlcuf2c2dhtgy8z5y7xxqev96km0n5mw30ls2': 1.05,
    'osmovaloper1ddle9tczl87gsvmeva3c48nenyng4n56yscals': 1.05,
    'junovaloper16msryt3fqlxtvsy8u5ay7wv2p8mglfg9g70e3q': 1.05,
    'regenvaloper1h5z08rzvrwt3pzdjc03upvuh2x0j3yskldyqvj': 1.05,

    # OmniFlixNetwork - Juno | Stargaze | Osmosis
    'osmovaloper12zwq8pcmmgwsl95rueqsf65avfg5zcj047ucw6': 1.05,
    'starsvaloper1fhznrvfyv25f27se8pqw79ytfcwh45j0ppy6lz': 1.05,
    'junovaloper1afhtjur8js4589xymu346ca7a5y5293x7p64ca': 1.05,

    # StrangeLove -  TBD

    # EcoStake - Chihuahua
    'chihuahuavaloper19vwcee000fhazmpt4ultvnnkhfh23ppwxll8zz': 1.05,

    # Akashlytics - Akash
    'akashvaloper14mt78hz73d9tdwpdvkd59ne9509kxw8yj7qy8f': 1.05,

    # Lavender.Five - Juno | Atom | Osmo | Secret
    'cosmosvaloper140l6y2gp3gxvay6qtn70re7z2s0gn57zfd832j': 1.05,
    'secretvaloper1t5wtcuwjkdct9qkw2h6m48zu2hectpd6ulmekk': 1.05,
    'junovaloper1wd02ktcvpananlvd9u6jm3x3ap3vmw59jv9vez': 1.05,
    'osmovaloper1glmy88g0uf6vmw29pyxu3yq0pxpjqtzqr5e57n': 1.05,
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