from util import convert_address_to_craft
from util import GENESIS_VALIDATORS, BLACKLISTED_CENTRAL_EXCHANGES, NETWORKS, headers, AIRDROP_DISTRIBUTIONS

'''
Testing to ensure each function works
'''

def main():
    test_address_conversions()
    test_all_airdrop_equals_100_percent()



def test_address_conversions():
    # terra, kava, and evmos would fail. Add dig 0x addresses here to fail too?
    for addr in [
            'cosmos10r39fueph9fq7a6lgswu4zdsg8t3gxlqvvvyvn', 
            'dig10r39fueph9fq7a6lgswu4zdsg8t3gxlq5c90wg', 
            'osmo10r39fueph9fq7a6lgswu4zdsg8t3gxlqyhl56p', 
            'juno10r39fueph9fq7a6lgswu4zdsg8t3gxlq670lt0',
            'akash10r39fueph9fq7a6lgswu4zdsg8t3gxlqphpr4f',
            'craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0',
            'comdex10r39fueph9fq7a6lgswu4zdsg8t3gxlqtrwx4y',
            'chihuahua10r39fueph9fq7a6lgswu4zdsg8t3gxlq0ep2d3',
            ]:
        assert convert_address_to_craft(addr) == 'craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0' 

def test_all_airdrop_equals_100_percent():
    assert sum(AIRDROP_DISTRIBUTIONS.values()) == 37_500_000


main()