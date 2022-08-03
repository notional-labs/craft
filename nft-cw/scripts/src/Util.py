import sys
sys.dont_write_bytecode = True
from base64 import b64encode, b64decode
import json
import os
import requests

from dotenv import load_dotenv

current_dir = os.path.dirname(os.path.abspath(__file__))

load_dotenv()
ADDRM = os.getenv('ADDRM') 
CRAFTD_REST = os.getenv('CRAFTD_REST')
if ADDRM == None:
    print('ADDRM is not set. Please set it in the environment variables.')
    exit()

class Contract_Tx:

    DENOM = "ucraft"

    def __init__(self, admin_wallet) -> None:
        self.admin_wallet = admin_wallet

    def transferNFTToMarketplace(self, ADDR721, id, forSalePrice, fileName):        
        '''
        # export NFT_LISTING_BASE64=`printf  $ADDR20 | base64 -w 0` && echo $NFT_LISTING_BASE64
        # # send_nft from 721 -> marketplace contract =  $ADDRM
        # export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"2","msg":"%s"}}' $ADDRM $NFT_LISTING_BASE64`
        # craftd tx wasm execute $ADDR721 $SEND_NFT_JSON --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY

        export NFT_LISTING_BASE64=`printf '{"list_price":"77"}' | base64 -w 0`
        export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"3","msg":"%s"}}' $ADDRM $NFT_LISTING_BASE64`
        craftd tx wasm execute "$ADDR721" "$SEND_NFT_JSON" --gas-prices="0.025ucraft" -y --from $KEY
        '''
            
        # print(f"{listingCraftPrice=}")
        # .replace("{ADMIN_WALLET}", self.admin_wallet).replace("{TOKEN}", Contract_Tx.DENOM)
        listPrice = '{"list_price":"{AMT}"}'.replace("{AMT}", str(forSalePrice))
        print(listPrice)

        SEND_NFT_JSON = '''{"send_nft":{"contract":"{ADDRM}","token_id":"{ID}","msg":"{LIST_PRICE}"}}''' \
            .replace("{ADDRM}", ADDRM) \
            .replace("{ID}", str(id)) \
            .replace("{LIST_PRICE}", b64encode(listPrice.encode('utf-8')).decode('utf-8'))
        # print(SEND_NFT_JSON)


        cmd = f"""craftd tx wasm execute {ADDR721} '{SEND_NFT_JSON}' --gas-prices="0.025ucraft" --gas="auto" -y --from $KEY"""
        path = os.path.join(current_dir, fileName)
        with open(path, 'a') as mintF:
            # print(f"Writing {cmd} to {path}")
            mintF.write(cmd + "\n")