'''
Reece Williams | June 27, 2022.
Use this NFT script to generate CRAFT real estate NFTs for the 721/marketplace.
Commands are from commands.md. & list prices are based on the floor volume & type of property.

[!] This script REQUIRES that you already have the 20, 721, and marketplace NFTs instantiated on chain
[!] This requires that you set the values of chain, keys, node, etc via commands.md top export commands
'''

# ---- Configuration --------------------------------------------------------------------------------------------------
START_IDX = 20 # put at 1 for mainnet mint

MINT_PRICES = { # price per sqBlock (floor volume)
    # src/main/java/com/crafteconomy/realestate/property/PropertyType.java
    "GOVERNMENT": -1, # not for sale
    "RESIDENTIAL": 1,
    "BUSINESS": 3, # (500 floorArea * 3 = 1500craft list price on marketplace)
}

mintCommands = {}
removeKeys = ["state", "restrictions", "restrictionTemplate", "rentingPlayer", "lastPayment", "lastFailedPayment", "price", "ownerId", "region", "rentalPeriod"]

DENOM = "token"

# ---- Imports ---------------------------------------------------------------------------------------------------------
import os
import json
from base64 import b64decode, b64encode
from dotenv import load_dotenv
from pymongo import MongoClient

# --- Initialization ---------------------------------------------------------------------------------------------------
load_dotenv()
uri = os.getenv("CRAFT_MONGO_DB")
admin_wallet = os.getenv("CRAFT_ADMIN_WALLET")
current_dir = os.path.dirname(os.path.abspath(__file__))

# --- Database ---------------------------------------------------------------------------------------------------------
client = MongoClient(uri)
db = client['crafteconomy']
reProperties = db['reProperties']
reCities = db['reCities']
reBuildings = db['reBuildings']


# --- Other ------------------------------------------------------------------------------------------------------------
def getCityFromID(region_id):
    doc = reCities.find_one(filter={'_id': region_id})
    if doc == None:
        return ""    
    return doc.get('name', "")

def getBuildingFromID(region_id):
    doc = reBuildings.find_one(filter={'_id': region_id})
    if doc == None:
        return ""    
    return doc.get('name', "")

def prepareRealEstateDocuments():
    # Get all properties from MongoDB & save to the mintCommands dict (key=id, value = dict or data)
    global mintCommands
    for idx, doc in enumerate(reProperties.find(), START_IDX):    
        for k in removeKeys:
            del doc[k]
        doc['cityName'] = getCityFromID(doc['cityId'])
        doc['buildingName'] = getBuildingFromID(doc['buildingId'])
        # print(f"Getting data for {idx}, {doc}")
        mintCommands[idx] = doc
        # break

# in the future we need to craftd tx sign as a big array of messages, but for now this works
def encodeRealEstateDocumentAndSaveMintToFile():
    for idx, data in mintCommands.items():
        b64Data = b64encode(json.dumps(data).encode('utf-8')).decode('utf-8')
        # print(b64Data)
        # we use the base64 data as the token URI rather than requiring even more queries to other locations for the data (ipfs)
        mintJSON = '''{"mint":{"token_id":"{IDX}","owner":"{ADMIN_WALLET}","token_uri":"{B64DATA}"}}''' \
            .replace("{IDX}", str(idx)) \
            .replace("{ADMIN_WALLET}", admin_wallet) \
            .replace("{B64DATA}", b64Data)

        mintCmd = f"""craftd tx wasm execute $ADDR721 '{mintJSON}' --from $KEY --yes --output json"""
        with open(os.path.join(current_dir, "mintCommands.txt"), 'a') as mintF:
            mintF.write(mintCmd + "\n")

# query contracts to ensure they work properly
def queryToken(id):
    cmd = '''craftd q wasm contract-state smart $ADDR721 '{"all_nft_info":{"token_id":"{ID}"}}' --output json'''.replace("{ID}", str(id))
    output = os.popen(cmd).read()
    print(output)
    pass

def queryOfferings():
    # craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}'
    cmd = """craftd query wasm contract-state smart $ADDRM '{"get_offerings": {}}' --output json"""
    output = os.popen(cmd).read()
    print(output)
    pass

def _calcListingPrice(data: dict) -> int:
    return MINT_PRICES.get(data['type'], -1) * data['floorArea']

def transferNFTToMarketplace(id):
    '''
    export NFT_LISTING_BASE64=`printf  $ADDR20 | base64 -w 0` && echo $NFT_LISTING_BASE64
    # send_nft from 721 -> marketplace contract =  $ADDRM
    export SEND_NFT_JSON=`printf '{"send_nft":{"contract":"%s","token_id":"2","msg":"%s"}}' $ADDRM $NFT_LISTING_BASE64`
    craftd tx wasm execute $ADDR721 $SEND_NFT_JSON --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY
    '''
    listingCraftPrice = _calcListingPrice(mintCommands[id])
    if listingCraftPrice < 0:
        print(f"Error: Property {id} is a govnerment property & will not be listed.")
        return
    # print(f"{listingCraftPrice=}")
    listPrice = '{"list_price":{"address":"{ADMIN_WALLET}","amount":"{AMT}","denom":"{TOKEN}"}}'\
        .replace("{ADMIN_WALLET}", admin_wallet).replace("{AMT}", str(listingCraftPrice)).replace("{TOKEN}", DENOM)

    SEND_NFT_JSON = '''{"send_nft":{"contract":"{ADDRM}","token_id":"{ID}","msg":"{LIST_PRICE}"}}''' \
        .replace("{ADDRM}", os.getenv("ADDRM")) \
        .replace("{ID}", str(id)) \
        .replace("{LIST_PRICE}", b64encode(listPrice.encode('utf-8')).decode('utf-8'))
    # print(SEND_NFT_JSON)
    cmd = f"""craftd tx wasm execute $ADDR721 '{SEND_NFT_JSON}' --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY"""
    print(cmd)
    # output = os.popen(cmd).read()
    # print(output)


if __name__ == '__main__':
    # prepareRealEstateDocuments()
    # transferNFTToMarketplace(25)
    queryToken(25)
    queryOfferings()

    pass