'''
Reece Williams | June 27, 2022.
Use this NFT script to generate CRAFT real estate NFTs for the 721/marketplace.
Commands are from commands.md. & list prices are based on the floor volume & type of property.

[!] This script REQUIRES that you already have the 20, 721, and marketplace NFTs instantiated on chain
[!] This requires that you set the values of chain, keys, node, etc via commands.md top export commands
'''

# ---- Configuration --------------------------------------------------------------------------------------------------
START_IDX = 1 # put at 1 for mainnet mint

MINT_PRICES = { # price per sqBlock (floor volume)
    # src/main/java/com/crafteconomy/realestate/property/PropertyType.java
    "GOVERNMENT": -1, # not for sale
    "RESIDENTIAL": 1,
    "BUSINESS": 3, # (500 floorArea * 3 = 1500craft list price on marketplace)
}

mintCommands = {}
removeKeys = ["state", "restrictions", "restrictionTemplate", "rentingPlayer", "lastPayment", "lastFailedPayment", "price", "ownerId", "region", "rentalPeriod"]

DENOM = "ucraft"

# ---- Imports ---------------------------------------------------------------------------------------------------------
import os
import json
import time
import requests
from base64 import b64decode, b64encode
from dotenv import load_dotenv
from pymongo import MongoClient

from Util import Contract_Tx

# --- User Defined Variables -------------------------------------------------------------------------------------------
CRAFTD_REST = "http://65.108.125.182:1317"
CODE_20=3 # code ids on chain after upload
CODE_721=4
CODE_M=5

# Hardcoded once you Contract_Initialize
load_dotenv()
ADDR20=os.getenv("ADDR20")
ADDR721=os.getenv("ADDR721_REALESTATE")
ADDRM=os.getenv("ADDRM")

if ADDR721 == None:
    print("Please set the ADDR721 variable in the script.")
    exit()


# --- Initialization ---------------------------------------------------------------------------------------------------
load_dotenv()
uri = os.getenv("CRAFT_MONGO_DB")
admin_wallet = os.getenv("CRAFT_ADMIN_WALLET")
current_dir = os.path.dirname(os.path.abspath(__file__))
params_base64 = {'encoding': 'base64'}

# --- Database ---------------------------------------------------------------------------------------------------------
client = MongoClient(uri)
db = client['crafteconomy']
reProperties = db['reProperties']
reCities = db['reCities']
reBuildings = db['reBuildings']

# Classes --------------------------------------------------------------------------------------------------------------
class Utils:
    @staticmethod
    def getCityFromID(region_id):
        doc = reCities.find_one(filter={'_id': region_id})
        if doc == None:
            return ""    
        return doc.get('name', "") 

    @staticmethod
    def getBuildingFromID(region_id):
        doc = reBuildings.find_one(filter={'_id': region_id})
        if doc == None:
            return ""    
        return doc.get('name', "")

    @staticmethod
    def _calcListingPrice(property_type: str, floor_area: int) -> int:
        mintMultiplier = int(MINT_PRICES[property_type])
        floorArea = int(floor_area)
        product = floorArea * mintMultiplier
        print(f"{mintMultiplier=}, {floorArea=}, {product=}")
        return product



class Contract_Query:
    # All moved to official rest API + redis caching.
    @staticmethod
    def getNFTContractInfo(): # CACHE THIS
        # craftd query wasm contract-state smart $ADDR721 '{"contract_info":{}}'
        url = f'{CRAFTD_REST}/cosmwasm/wasm/v1/contract/{ADDR721}'
        response = requests.get(url).json()
        print(response)
        return response
    
    @staticmethod
    def queryToken(token_id, decodeBase64=True): # CACHE THIS (24 hour TTL?). Meta data never changes
        # cmd = '''craftd q wasm contract-state smart {ADDR721} '{"all_nft_info":{"token_id":"{ID}"}}' --output json'''.replace("{ID}", str(id)).replace("{ADDR721}", ADDR721)        
        query = '{"nft_info":{"token_id":"{TOKEN_ID}"}}'.replace("{TOKEN_ID}", str(token_id))
        b64query = b64encode(query.encode('utf-8')).decode('utf-8')

        url = f'{CRAFTD_REST}/cosmwasm/wasm/v1/contract/{ADDR721}/smart/{b64query}'#; print(url)
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

        url = f'{CRAFTD_REST}/cosmwasm/wasm/v1/contract/{ADDR721}/smart/{b64query}'#; print(url)
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

    @staticmethod
    def queryOfferingsWithData(): # cache this data
        # Queries all marketplace offerings, and queries their parent contract for base64 data for each tokenid
        offerings = Contract_Query.queryOfferings(debugPrint=False)
        newOutput = {}
        for token in offerings:
            # print(type(token),token);  exit()
            tokenID = token['token_id'] # token based on the parent contract
            base64Value = Contract_Query.queryToken(tokenID, decodeBase64=True)
            newOutput[tokenID] = base64Value
        return newOutput

# --- Other ------------------------------------------------------------------------------------------------------------


def step1_prepareRealEstateDocuments():
    print("Step 1: Preparing real estate documents from MongoDB. Put into 'mintCommands' variable.")
    # Get all properties from MongoDB & save to the mintCommands dict (key=id, value = dict or data)
    global mintCommands
    for idx, doc in enumerate(reProperties.find(), START_IDX):    
        for k in removeKeys:
            del doc[k]
        doc['cityName'] = Utils.getCityFromID(doc['cityId'])
        doc['buildingName'] = Utils.getBuildingFromID(doc['buildingId'])
        doc['_nft_type'] = "real_estate"
        # print(f"Getting data for {idx}, {doc}")
        mintCommands[idx] = doc
        # input(f"{doc=}")

# in the future we need to craftd tx sign as a big array of messages, but for now this works
def step2_encodeRealEstateDocumentAndSaveMintToFile():
    fileName = "tx_mint_realestate_commands.txt"
    print(f"Step 2: Encoding Real Estate Documents from Step1 -> {fileName}")
    for idx, data in mintCommands.items():
        b64Data = b64encode(json.dumps(data).encode('utf-8')).decode('utf-8')
        # print(b64Data)
        # we use the base64 data as the token URI rather than requiring even more queries to other locations for the data (ipfs)
        mintJSON = '''{"mint":{"token_id":"{IDX}","owner":"{ADMIN_WALLET}","token_uri":"{B64DATA}"}}''' \
            .replace("{IDX}", str(idx)) \
            .replace("{ADMIN_WALLET}", admin_wallet) \
            .replace("{B64DATA}", b64Data)

        mintCmd = f"""craftd tx wasm execute {ADDR721} '{mintJSON}' --from $KEY --output json -y"""
        with open(os.path.join(current_dir, f"{fileName}"), 'a') as mintF:
            mintF.write(mintCmd + "\n")
    print(f"Commands to mint saved to file {fileName}. Please run these before continuing to Step3.")


def step3_generateRESendCommandsToMarketplaceContract():
    nft_token_list = list(Contract_Query.getUserOwnedNFTsALL(f"{admin_wallet}", decodeBase64=False).keys())
    print(nft_token_list)

    cTx = Contract_Tx(admin_wallet)
    for tokenId in nft_token_list:
        metadata = dict(json.loads(Contract_Query.queryToken(tokenId, decodeBase64=True)))
        floorArea = metadata['floorArea']
        
        listingCraftPrice = Utils._calcListingPrice(metadata.get("type"), floorArea)
        print(f"{listingCraftPrice}")
        if listingCraftPrice <= 0:
            print(f"[!] Property {tokenId} is a government property & will not be listed (listingCraftPrice <= 0).")
            continue

        # v = input("\n>>>")
        # ADDR721, id, forSalePrice, fileName
        cTx.transferNFTToMarketplace(ADDR721, int(tokenId), listingCraftPrice, "RE_txSendToMarketplace.txt")

if __name__ == '__main__':
    step1_prepareRealEstateDocuments()
    step2_encodeRealEstateDocumentAndSaveMintToFile()
    # input("Did you already run commands from step2?"); step3_generateRESendCommandsToMarketplaceContract()

    # moved to rest API
    # q = Contract_Query.getNFTContractInfo()
    # q = Contract_Query.getNFTInfo(1)

    # exit()


    # query_data = Contract_Query.queryToken(3, decodeBase64=True)
    # print(query_data)

    # Contract_Query.queryOfferings()

    # owned_nfts = Contract_Query.getUsersOwnedNFTs("craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl")
    # print(owned_nfts)
    # all_tokens = Contract_Query.getUserOwnedNFTsALL("craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl", decodeBase64=True)
    # print(all_tokens)

    

    

    pass