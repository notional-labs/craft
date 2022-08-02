
import sys
sys.dont_write_bytecode = True
import os
import json
from dotenv import load_dotenv
load_dotenv()

START_INDEX = 1
CRAFT_ADMIN_WALLET = os.getenv("CRAFT_ADMIN_WALLET") 
ADDR_TEST721 = os.getenv("ADDR_TEST721")
ADDRM = os.getenv('ADDRM')

links = [
    # random link(s)
    "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ",
    "https://ipfs.io/ipfs/QmNLjZSFV3GUMcusj8keEqVtToEE3ceTSguNom7e4S6pbJ",
    "https://ipfs.io/ipfs/QmNLijobERK4VhSDZdKjt5SrezdRM6k813qcSHd68f3Mqg"
]

# A normal contract for images
# craftd tx wasm execute craft1zjd5lwhch4ndnmayqxurja4x5y5mavy9ktrk6fzsyzan4wcgawnq7d4srp '{"mint":{"token_id":"1","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"https://www.instagram.com/static/images/homepage/screenshots/screenshot1.png/fdfe239b7c9f.png"}}' --from $KEY --output json -y

# Send to marketplace
# craftd tx wasm execute craft1zjd5lwhch4ndnmayqxurja4x5y5mavy9ktrk6fzsyzan4wcgawnq7d4srp '{"send_nft":{"contract":"craft13h9k5rsrgveg6sdtzg34qg499ns0e5kku74kapnskegtwyfspf6qhxcdfh","token_id":"1","msg":"eyJsaXN0X3ByaWNlIjp7ImFkZHJlc3MiOiJjcmFmdDFoajVmdmVlcjVjanRuNHdkNndzdHp1Z2pmZHh6bDB4cDg2cDlmbCIsImFtb3VudCI6IjUwMCIsImRlbm9tIjoidWNyYWZ0In19"}}' --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY

def main():
    part1_mintToAdminAccount()
    part2_sendToMarketplace()


def part1_mintToAdminAccount():
    for idx, link in enumerate(links, START_INDEX):
        # we do not base64 encode this since we want it to be a standard base CW721 contract
        # we convert it to our offering in the API so as to not break CW721 standard
        mintJSON = '''{"mint":{"token_id":"{IDX}","owner":"{ADMIN_WALLET}","token_uri":"{LINK}"}}''' \
            .replace("{IDX}", str(idx)) \
            .replace("{ADMIN_WALLET}", CRAFT_ADMIN_WALLET) \
            .replace("{LINK}", link)
        mintCmd = f'''craftd tx wasm execute {ADDR_TEST721} '{mintJSON}' --from $KEY --output json -y'''
        print(mintCmd)

def part2_sendToMarketplace():
    # move to marketplace contract
    from base64 import b64encode
    listPrice = '{"list_price":"{AMT}"}'.replace("{AMT}", str(69))

    for idx, link in enumerate(links, START_INDEX):
        SEND_NFT_JSON = '''{"send_nft":{"contract":"{ADDRM}","token_id":"{ID}","msg":"{LIST_PRICE}"}}''' \
            .replace("{ADDRM}", ADDRM) \
            .replace("{ID}", str(idx)) \
            .replace("{LIST_PRICE}", b64encode(listPrice.encode('utf-8')).decode('utf-8'))
        # print(SEND_NFT_JSON)

        cmd = f"""craftd tx wasm execute {ADDR_TEST721} '{SEND_NFT_JSON}' --gas-prices="0.025ucraft" -y --from $KEY"""
        print(cmd)

if __name__ == "__main__":
    main()