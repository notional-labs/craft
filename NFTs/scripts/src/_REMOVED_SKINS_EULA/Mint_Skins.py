import sys
sys.dont_write_bytecode = True

from dotenv import load_dotenv
from base64 import b64encode, b64decode

import requests, os, json, time
import redis # python3 -m pip install redis==4.1.4

# IPFS / Rendering
import ipfsApi # pip install ipfs-api
import shutil
from PIL import Image
from MinePI import render_3d_skin # pip install MinePI
import asyncio
api = ipfsApi.Client('https://ipfs.infura.io', 5001)

from Util import Contract_Tx

# FUTURE: AI Generated skins? - https://github.com/saltysnacc/SkinGAN

# https://github.com/MineSkin/api.mineskin.org
# https://rest.wiki/?https://api.mineskin.org/openapi

'''
Get the 721 contract code (2 in this case)

craftd tx wasm instantiate $C721 '{
  "name": "craftd-skins-1",
  "symbol": "CSKINS",
  "minter": "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
}' --label "cw721-realestate-craft" $CRAFTD_COMMAND_ARGS -y --admin $KEY_ADDR
# craftd q tx <hash>
'''


START_INDEX = 50 # 1 by default
SKIN_MINT_PRICE = 5_000_000 # ucraft
MINT_SKIN_SLEEP_TIME = 1 # in seconds. So ipfs doesn't bitch
MAX_NFTS_TO_MINT_TEST = 5

load_dotenv()
MINESKINS_API_KEY = os.getenv("MINESKINS_API_KEY")
MINESKINS_API_SECRET = os.getenv("MINESKINS_API_SECRET")

current_dir = os.path.dirname(os.path.abspath(__file__))
skins_dir = f"{current_dir}/skins"
renderings = f"{current_dir}/skin_renderings" # saved ipfs renderings of walking pose

os.makedirs(skins_dir, exist_ok=True)
os.makedirs(renderings, exist_ok=True)


ADDRM = os.getenv('ADDRM') 
CRAFTD_REST = os.getenv('CRAFTD_REST')
ADDR721_SKINS = os.getenv("ADDR721_SKINS")
CRAFT_ADMIN_WALLET = os.getenv("CRAFT_ADMIN_WALLET")

# Cache System (Removed for now)
# r = redis.Redis.from_url(os.getenv("REDIS_CACHE"))  # print(r.keys()); exit()

headers = { 'accept': 'application/json', 'User-Agent': 'MyFancyUserAgent/1.0',}
params_base64 = {'encoding': 'base64'}

def main():
    # asyncio.run(step1_save_skins_to_file())  # runs 1 and 2
    input("Run tx_mint_skins_commands.txt..."); step3_sendToMarketplace()

    # v = s.getSkinValues(1623065198); print(v)
    pass

class Skins:
    def getPage(self, page) -> list: # list of dicts
        if page < 0:
            return []
        response = requests.get(f'https://api.mineskin.org/get/list/{page}', headers=headers).json()
        if 'skins' in response:
            return response['skins']
        return []

    async def render_skin(self, texture_url):
        filename = texture_url.split("/")[-1] + ".png"
        file_path = renderings + "/" + filename

        r = requests.get(texture_url, stream = True)
        if r.status_code == 200:
            # Set decode_content value to True, otherwise the downloaded image file's size will be zero.
            r.raw.decode_content = True
            # Open a local file with wb ( write binary ) permission.
            with open(file_path,'wb') as f:
                shutil.copyfileobj(r.raw, f)            
            print('Image successfully Downloaded: ',filename)
        else:
            print('Image Couldn\'t be retrieved')

        skin_image = Image.open(file_path)
        image = await render_3d_skin(skin_image=skin_image, hr=30, vr=-20, aa=True,
            vrll=20, vrrl=-20, vrla=-25, vrra=25) # walking motion
        
        # show an Image.Image
        # image.show()
        # delete old file (texture skin)
        os.remove(file_path)
        # save image to the filename
        image.save(file_path)

        
        # res = api.add("skin_renderings/" + filename)
        os.chdir(renderings)
        res = api.add(filename)
        # print(f"{filename} https://ipfs.infura.io/ipfs/{res['Hash']}")
        # exit()
        os.chdir("..")
        return f"https://ipfs.infura.io/ipfs/{res['Hash']}"

    async def getSkinValues(self, id: int):
        # we dont save to cache here, we do that in the TS query
        # REDIS_CACHE_KEY = "cache:skins_textures" # speeds up query time for past events
        # value = r.hget(REDIS_CACHE_KEY, str(id))
        # if value:
        #     value = json.loads(value)
        #     if len(value) > 0:
        #         print(f"[Redis Cache] {id} found")
        #         return value # if not, we need to re-query the API

        response = requests.get(f'https://api.mineskin.org/get/id/{id}', headers=headers).json()
        if 'data' in response:
            texture = response['data']['texture']
            textureData =  {
                "_nft_type": "skin", # helps with sorting
                "value": texture['value'], 
                "signature": texture['signature'],
                "url": texture['url'],
                "rendering": await self.render_skin(texture['url']),
            }

            # TODO: Render here, update textureData to that rendering
            print(f'Downloaded {id}')
            # r.hset(REDIS_CACHE_KEY, str(id), json.dumps(textureData))
            return textureData
        return {}

async def step1_save_skins_to_file():    
    s = Skins()
    for i in range(0, 1):
        for idx, mySkin in enumerate(s.getPage(i)):
            _id = int(mySkin['id'])
            skin_values[_id] = await s.getSkinValues(_id) 
            time.sleep(MINT_SKIN_SLEEP_TIME)
            print(f"Only making {MAX_NFTS_TO_MINT_TEST} nfts")
            if idx == MAX_NFTS_TO_MINT_TEST-1: break;

    with open(skins_dir + "/mint_skin_values.json", 'w') as f:
        json.dump(skin_values, f, indent=4)

    step2_saveMintTxsToFile();

# not going to use BASE64 & see if it works out easier
def step2_saveMintTxsToFile(): 
    fileName = skins_dir + "/tx_mint_skins_commands.txt"
    print(f"Step 2: Skins from Step1 -> {fileName}")

    skin_values = json.load(open(skins_dir + '/mint_skin_values.json', 'r'))

    for idx, (id, data) in enumerate(skin_values.items(), START_INDEX):
        b64Data = b64encode(json.dumps(data).encode('utf-8')).decode('utf-8')

        mintJSON = '''{"mint":{"token_id":"{IDX}","owner":"{ADMIN_WALLET}","token_uri":"{BASE64_DATA}"}}''' \
            .replace("{IDX}", str(idx)) \
            .replace("{ADMIN_WALLET}", CRAFT_ADMIN_WALLET) \
            .replace("{BASE64_DATA}", b64Data)

        mintCmd = f"""craftd tx wasm execute {ADDR721_SKINS} '{mintJSON}' --from $KEY --output json -y"""
        with open(os.path.join(current_dir, f"{fileName}"), 'a') as mintF:
            mintF.write(mintCmd + "\n")

    print(f"Commands to mint saved to file {fileName}. Please run these before continuing to Step3.")

def step3_sendToMarketplace():
    nft_token_list = list(Contract_Query.getUserOwnedNFTsALL(address=f"{CRAFT_ADMIN_WALLET}", decodeBase64=False).keys())
    print(nft_token_list)

    cTx = Contract_Tx(CRAFT_ADMIN_WALLET)
    for tokenId in nft_token_list:
        
        # ADDR721, id, forSalePrice, fileName
        cTx.transferNFTToMarketplace(ADDR721_SKINS, int(tokenId), SKIN_MINT_PRICE, "skins/SKINS_txSendToMarketplace.txt")




class Contract_Query:
    # All moved to official rest API + redis caching.
    @staticmethod
    def getNFTContractInfo(): # CACHE THIS
        # craftd query wasm contract-state smart $ADDR721 '{"contract_info":{}}'
        url = f'{CRAFTD_REST}/cosmwasm/wasm/v1/contract/{ADDR721_SKINS}'
        response = requests.get(url).json()
        print(response)
        return response
    
    @staticmethod
    def queryToken(token_id, decodeBase64=True): # CACHE THIS (24 hour TTL?). Meta data never changes
        # cmd = '''craftd q wasm contract-state smart {ADDR721} '{"all_nft_info":{"token_id":"{ID}"}}' --output json'''.replace("{ID}", str(id)).replace("{ADDR721}", ADDR721)        
        query = '{"nft_info":{"token_id":"{TOKEN_ID}"}}'.replace("{TOKEN_ID}", str(token_id))
        b64query = b64encode(query.encode('utf-8')).decode('utf-8')

        url = f'{CRAFTD_REST}/cosmwasm/wasm/v1/contract/{ADDR721_SKINS}/smart/{b64query}'#; print(url)
        response = requests.get(url, params=params_base64)  
        if response.status_code != 200:
            print(f"Status code {response.status_code}. This NFT '{token_id}' likely does not exist...")
            return {}

        response = response.json()['data']['token_uri']
        if decodeBase64: # convert it to the dict
            response = b64decode(response).decode('utf-8')

        # print(response)
        return response

    def getUsersOwnedNFTs(address, INIT_START_IDX=0): # CACHE THIS? (maybe like 15/30 second TTL)
        # craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","start_after":"0","limit":50}}'
        query = '{"tokens":{"owner":"{address}","start_after":"{INIT_START}","limit":100}}'.replace("{address}", str(address)).replace("{INIT_START}", str(INIT_START_IDX))
        b64query = b64encode(query.encode('utf-8')).decode('utf-8')

        url = f'{CRAFTD_REST}/cosmwasm/wasm/v1/contract/{ADDR721_SKINS}/smart/{b64query}'#; print(url)
        response = requests.get(url, params=params_base64)        
        if response.status_code != 200:
            print("Status code " + str(response.status_code) + ". Returning {}")
            return []
        return response.json()['data']['tokens']

    @staticmethod
    def getUserOwnedNFTsALL(address, decodeBase64=True):
        # Gets all users tokenIDS AND their base64 values
        '''{"tokenID": "base64Value",}'''
        myNFTs = Contract_Query.getUsersOwnedNFTs(address, INIT_START_IDX=0)
        newOutput = {}
        for nftID in myNFTs:
            base64Value = Contract_Query.queryToken(nftID, decodeBase64=decodeBase64)
            newOutput[nftID] = base64Value
        # print(newOutput)
        return newOutput

    @staticmethod
    def queryOfferings(debugPrint=True):
        # craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}'
        query = '{"get_offerings":{}}'
        b64query = b64encode(query.encode('utf-8')).decode('utf-8')

        url = f'{CRAFTD_REST}/cosmwasm/wasm/v1/contract/{ADDRM}/smart/{b64query}'#; print(url)
        response = requests.get(url, params=params_base64)        
        if response.status_code != 200:
            print("Status code " + str(response.status_code) + ". Returning {}")
            return {}

        response = response.json()['data']['offerings']
        if debugPrint: print(response)
        return response






skin_values = {} # {idNumber: {value, signature, url}}
if __name__ == '__main__':
    main()