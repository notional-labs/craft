
import sys
sys.dont_write_bytecode = True
import os
import json
# from dotenv import load_dotenv
# load_dotenv()

import requests
START_IDX = 30 # put at 1 for mainnet mint
addresses = requests.get("https://api.crafteconomy.io/v1/nfts/get_contract_addresses").json()
ADDR721 = addresses['ADDR721_REALESTATE']
ADDR721IMAGES = addresses['ADDR721_IMAGES']
ADDRM = addresses['MARKETPLACE']
DAO_MULTISIG = "craft1n3a53mz55yfsa2t4wvdx3jycjkarpgkf07zwk7" # dao account for now. They should be the one who inited the 721 contract (DAO)
CRAFTD_REST = "https://craft-rest.crafteconomy.io"


COST_PER_IMAGE = 5 # 5craft
links = [
    # random link(s)
    "https://ipfs.io/ipfs/QmNLoezbXkk37m1DX5iYADRwpqvZ3yfu5UjMG6sndu1AaQ",
    "https://ipfs.io/ipfs/QmNLjZSFV3GUMcusj8keEqVtToEE3ceTSguNom7e4S6pbJ",
    "https://ipfs.io/ipfs/QmNLijobERK4VhSDZdKjt5SrezdRM6k813qcSHd68f3Mqg",    
    "https://i.imgur.com/sqmreSn.png",
]

# A normal contract for images
# craftd tx wasm execute craft1zjd5lwhch4ndnmayqxurja4x5y5mavy9ktrk6fzsyzan4wcgawnq7d4srp '{"mint":{"token_id":"1","owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","token_uri":"https://www.instagram.com/static/images/homepage/screenshots/screenshot1.png/fdfe239b7c9f.png"}}' --from $KEY --output json -y

# Send to marketplace
# craftd tx wasm execute craft1zjd5lwhch4ndnmayqxurja4x5y5mavy9ktrk6fzsyzan4wcgawnq7d4srp '{"send_nft":{"contract":"craft13h9k5rsrgveg6sdtzg34qg499ns0e5kku74kapnskegtwyfspf6qhxcdfh","token_id":"1","msg":"eyJsaXN0X3ByaWNlIjp7ImFkZHJlc3MiOiJjcmFmdDFoajVmdmVlcjVjanRuNHdkNndzdHp1Z2pmZHh6bDB4cDg2cDlmbCIsImFtb3VudCI6IjUwMCIsImRlbm9tIjoidWNyYWZ0In19"}}' --gas-prices="0.025ucraft" --gas="auto" --gas-adjustment="1.2" -y --from $KEY

def main():
    part1_mintToAdminAccount()
    part2_sendToMarketplace()


def part1_mintToAdminAccount():    
    msgFmt = { "body": { "messages": [], "memo": "minting real estate", "timeout_height": "0", "extension_options": [], "non_critical_extension_options": []}, "auth_info": {"signer_infos": [],"fee": {"amount": [],"gas_limit": "10000000","payer": "","granter": ""},"tip": None},"signatures": []}
    for idx, link in enumerate(links, START_IDX):
        msgFmt['body']['messages'].append({
            "@type": "/cosmwasm.wasm.v1.MsgExecuteContract",
            "sender": f"{DAO_MULTISIG}", # wallet who can mint (DAO multisig)
            "contract": f"{ADDR721IMAGES}",
            "msg": {
                "mint": {
                    "token_id": f"{idx}",
                    "owner": f"{DAO_MULTISIG}", # dao owns all images it mints
                    "token_uri": f"{link}"
                }
            },
            "funds": []
        })

    # save output to file
    os.makedirs("images", exist_ok=True)    
    # save msgFmt to images 
    with open("images/mint_images.json", "w") as f:
        json.dump(msgFmt, f, indent=4)

def part2_sendToMarketplace():
    # move to marketplace contract
    from base64 import b64encode
    listPriceBase64 = b64encode(json.dumps({"list_price": f"{COST_PER_IMAGE*1_000_000}"}).encode('utf-8')).decode('utf-8')

    msgFmt = { "body": { "messages": [], "memo": "minting real estate", "timeout_height": "0", "extension_options": [], "non_critical_extension_options": []}, "auth_info": {"signer_infos": [],"fee": {"amount": [],"gas_limit": "10000000","payer": "","granter": ""},"tip": None},"signatures": []}
    for idx, link in enumerate(links, START_IDX):
        msgFmt['body']['messages'].append({
            "@type": "/cosmwasm.wasm.v1.MsgExecuteContract",
            "sender": f"{DAO_MULTISIG}", # wallet who can mint, always the DAO (contract init)
            "contract": f"{ADDR721IMAGES}",
            "msg": {
                "send_nft": {
                    "contract": f"{ADDRM}",
                    "token_id": f"{idx}",
                    "msg": f"{listPriceBase64}"
                }
            },
            "funds": []
        })

    # save output to file
    os.makedirs("images", exist_ok=True)
    with open("images/images_to_marketplace.json", "w") as f:
        json.dump(msgFmt, f, indent=4)

if __name__ == "__main__":
    main()