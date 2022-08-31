'''
Reece Williams | June 27, 2022.
Use this NFT script to generate CRAFT real estate NFTs for the 721/marketplace.
Commands are from commands.md. & list prices are based on the floor volume & type of property.

[!] This script REQUIRES that you already have the 721 and marketplace NFTs instantiated on chain
[!] This requires that you set the values of chain, keys, node, etc via commands.md top export commands
'''

# ---- Configuration --------------------------------------------------------------------------------------------------
# ensure it matches for real estate & images
from pprint import pprint
import requests
START_IDX = 1 # put at 1 for mainnet mint
addresses = requests.get("https://api.crafteconomy.io/v1/nfts/get_contract_addresses").json()
ADDR721 = addresses['ADDR721_REALESTATE']
ADDR721IMAGES = addresses['ADDR721_IMAGES']
ADDRM = addresses['MARKETPLACE']
DAO_MULTISIG = "craft1n3a53mz55yfsa2t4wvdx3jycjkarpgkf07zwk7" # dao account for now. They should be the one who inited the 721 contract (DAO)
CRAFTD_REST = "https://craft-rest.crafteconomy.io"


# Specific to real estate
MINT_PRICES = { # price per sqBlock (floor volume) IN UCRAFT (1mill ucraft = 1 craft.)
    # src/main/java/com/crafteconomy/realestate/property/PropertyType.java
    "GOVERNMENT": -1, # not for sale
    "RESIDENTIAL": 2_000_000,
    "BUSINESS": 5_000_000, # (500 floorArea * 5 = 2500craft list price on marketplace)
}

mintCommands = {}
OTHER_OWNERS = {"78e7445f-e079-421e-a9b4-b1019ac329cb": "craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh"} # if a DAO member pre owns a property, set their wallet as the owner. In this case reece validator as example
removeKeys = ["state", "restrictions", "restrictionTemplate", "rentingPlayer", "lastPayment", "lastFailedPayment", "price", "ownerId", "region", "rentalPeriod"]
NON_GOV_PROPERTY_NAMES_CHECK = ["apartment", "home", "house"] # if these are type GOVERNMENT, then we need to error out so that can be regenerated.

DENOM = "ucraft"

# ---- Imports ---------------------------------------------------------------------------------------------------------
import sys
sys.dont_write_bytecode = True
import os
import json
import requests
from base64 import b64decode, b64encode
from dotenv import load_dotenv
from pymongo import MongoClient

# from Util import Contract_Tx


# --- Initialization ---------------------------------------------------------------------------------------------------
load_dotenv()

current_dir = os.path.dirname(os.path.abspath(__file__))
current_dir = f"{current_dir}/real_estate"
testing = f"{current_dir}/re_testing"

os.makedirs(current_dir, exist_ok=True)
os.makedirs(testing, exist_ok=True)


params_base64 = {'encoding': 'base64'}

# --- Database ---------------------------------------------------------------------------------------------------------
uri = os.getenv("CRAFT_MONGO_DB")
client = MongoClient(uri)
db = client['crafteconomy']
reProperties = db['reProperties']
reCities = db['reCities']
reBuildings = db['reBuildings']


def main():
    encodeRealEstateDocumentAndSaveMintToFile()    
    step3_generateRESendCommandsToMarketplaceContract()

    print("\nHow to Sign & Broadcast:")
    print("(Ensure you are in the real_estate folder)")
    print(f"1. Run ' craftd tx sign real_estate_mint.json --from dao  &> signed_estate_mint.json '") # --multisig=<multisig_key> how ??    
    print(f"1. Run ' craftd tx sign real_estate_to_marketplace.json --from dao  &> signed_estate_marketplace.json '") # --multisig=<multisig_key> how ??    
    print()
    print(f"3. Run 'craftd tx broadcast signed_estate_mint.json'")
    print(f"4. Run 'craftd tx broadcast signed_estate_marketplace.json'")

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
        # print(f"{mintMultiplier=}, {floorArea=}, {product=}")
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

# We will add messages for minting from here, just copy first
# FORMAT = { "body": { "messages": [], "memo": "", "timeout_height": "0", "extension_options": [], "non_critical_extension_options": []}, "auth_info": {"signer_infos": [],"fee": {"amount": [],"gas_limit": "10000000","payer": "","granter": ""},"tip": None},"signatures": []}


def _step1_prepareRealEstateDocuments():
    print("Step 1: Preparing real estate documents from MongoDB. Put into 'mintCommands' variable.")
    # Get all properties from MongoDB & s   ave to the mintCommands dict (key=id, value = dict or data)

    # if property has name which was already used by another UUID, then report it
    # name, uuid
    propertyNames = {}
    noImageLinks = {}
    PROPERTIES = {
        "GOVERNMENT": [],
        "RESIDENTIAL": [],
        "BUSINESS": [],
    }

    global mintCommands
    for idx, doc in enumerate(reProperties.find(), START_IDX): # 1
        for k in removeKeys:
            if k in doc:
                del doc[k]

        name = doc['name']
        uuid = doc['_id']
        prop_type = doc['type']
        imageLink = doc['imageLink']
        doc['cityName'] = Utils.getCityFromID(doc['cityId'])
        doc['buildingName'] = Utils.getBuildingFromID(doc['buildingId'])
        doc['_nft_type'] = "real_estate"

        propertyNames[name] = uuid
        mintCommands[idx] = doc

        if len(imageLink) == 0:                       
            # doc['imageLink'] = "https://i.imgur.com/z7qnMGD.png"
            noImageLinks[name] = uuid
            # continue

        # CHECK IF ANY strings in NON_GOV are in name. This is now done manually
        # name = str(doc['name']).lower()
        # if any(x in name for x in NON_GOV_PROPERTY_NAMES_CHECK) and "firehouse" not in name:
        #     if str(doc['type']).lower().startswith("gov"):                
        #         print(f"[ERROR], {doc['name']} {doc['_id']} is a gov property, but has a non-government name...")                
        #         continue

        PROPERTIES[prop_type].append([name, uuid, imageLink])


    # ! error checking
    # if len(noImageLinks) > 0:
    #     print(f"\n\n[ERROR] There are {len(noImageLinks)} properties with no imageLink. Please fix this before minting.")
    #     pprint(noImageLinks, width=1, compact=True)
    #     exit()

    # pprint(PROPERTIES)

    # save properties to file
    with open(os.path.join(testing, "property_catagory_checker.json"), 'w') as f:
        json.dump(PROPERTIES, f, indent=4, sort_keys=True)

    # save propertyNames to json
    with open(os.path.join(testing, "propertyNames.json"), 'w') as f:
        json.dump(propertyNames, f, indent=4, sort_keys=True)

    # dump properties which do not have an image set (image len of 0)
    with open(os.path.join(testing, "noImageLinks.json"), 'w') as f:
        json.dump(noImageLinks, f, indent=4, sort_keys=True)
    

    print(f"Saved output to {testing} folder, check there for errors.")
    print("=========================================\n\n")

# in the future we need to craftd tx sign as a big array of messages, but for now this works
def encodeRealEstateDocumentAndSaveMintToFile():
    _step1_prepareRealEstateDocuments()
    fileName = "real_estate_mint.json"
    print(f"Step 2: Encoding Real Estate Documents from Step1 -> {fileName}")

    msgFmt = { "body": { "messages": [], "memo": "minting images", "timeout_height": "0", "extension_options": [], "non_critical_extension_options": []}, "auth_info": {"signer_infos": [],"fee": {"amount": [],"gas_limit": "10000000","payer": "","granter": ""},"tip": None},"signatures": []}
    for idx, data in mintCommands.items():
        b64Data = b64encode(json.dumps(data).encode('utf-8')).decode('utf-8')
        # print(b64Data)
        # we use the base64 data as the token URI rather than requiring even more queries to other locations for the data (ipfs)

        # Some properties may be owned by others, so we send to them in mint
        property_owner = str(DAO_MULTISIG)
        if data["_id"] in OTHER_OWNERS.keys():
            property_owner = OTHER_OWNERS[data["_id"]]

        # in the future maybe use a metadata contract for real estate?

        msgFmt['body']['messages'].append({
            "@type": "/cosmwasm.wasm.v1.MsgExecuteContract",
            "sender": f"{DAO_MULTISIG}", # wallet who can mint (DAO multisig)
            "contract": f"{ADDR721}",
            "msg": {
                "mint": {
                    "token_id": f"{idx}",
                    "owner": f"{property_owner}", # DAO by default (admin wallet), unless another member owns it pre launch
                    "token_uri": f"{b64Data}"
                }
            },
            "funds": []
        })

    # save msgFmt to file
    with open(os.path.join(current_dir, f"{fileName}"), 'w') as mintF:
        mintF.write(json.dumps(msgFmt, indent=4))
    print(f"You can now mint the NFts by signing {fileName}.")


# TODO: change this to just mint asbed off of the pregen we made, no need to query
def step3_generateRESendCommandsToMarketplaceContract():
    fileName = "real_estate_to_marketplace.json"

    # since we already needed this in step 2, just reuse
    msgFmt = { "body": { "messages": [], "memo": "images to marketplace", "timeout_height": "0", "extension_options": [], "non_critical_extension_options": []}, "auth_info": {"signer_infos": [],"fee": {"amount": [],"gas_limit": "10000000","payer": "","granter": ""},"tip": None},"signatures": []}
    for tokenId, data in mintCommands.items():
        # print(idx, data) # data = dict (non base64)

        floorArea = data['floorArea']
        listingCraftPrice = Utils._calcListingPrice(data["type"], floorArea)
        if listingCraftPrice <= 0:
            print(f"[!] Property {tokenId} is a government property & will not be listed (listingCraftPrice <= 0).")
            continue

        listPriceBase64 = b64encode(json.dumps({"list_price": f"{listingCraftPrice}"}).encode('utf-8')).decode('utf-8')

        # Some properties may be owned by others, so if they own the property we minted, we cant send it.
        property_owner = str(DAO_MULTISIG)
        if data["_id"] in OTHER_OWNERS.keys():
            print(f"This property is owned by another member. Owned by: {OTHER_OWNERS[data['_id']]}. Skipping...")
            continue

        msgFmt['body']['messages'].append({
            "@type": "/cosmwasm.wasm.v1.MsgExecuteContract",
            "sender": f"{DAO_MULTISIG}", # wallet who can mint, always the DAO (contract init)
            "contract": f"{ADDR721}",
            "msg": {
                "send_nft": {
                    "contract": f"{ADDRM}",
                    "token_id": f"{tokenId}",
                    "msg": f"{listPriceBase64}"
                }
            },
            "funds": []
        })

        # save msgFmt to file
    with open(os.path.join(current_dir, f"{fileName}"), 'w') as mintF:
        mintF.write(json.dumps(msgFmt, indent=4))
    print(f"You can now send non gov NFTs -> marketplace. {fileName}.")




if __name__ == '__main__':
    main()