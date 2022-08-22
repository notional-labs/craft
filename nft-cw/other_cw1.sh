# This is a cw1-whitelist -> CW721 NFT. Done for WBA, but it is useful to know how to init via CLI w/ a COsmMSg via CLI

# upload & init CW1 w/ whitelist addresses (craft1unyuj8qnmygvzuex3dwmg9yzt9alhvyeat0uu0jedg2wj33efl5qhl3ex3)
# upload and init CW721 (craft1cnuw3f076wgdyahssdkd0g3nr96ckq8cwa2mh029fn5mgf2fmcmsjl6xjf), with minter = CW1 contract address 
# get mint cmd & base64 encode it

# {"mint":{"token_id":"1","owner":"craft13vhr3gkme8hqvfyxd4zkmf5gaus840j5hwuqkh","token_uri":"https://reece.sh"}} -> base64

# take this and pass through CW1 contract to mint via the 721
craftd tx wasm execute craft1unyuj8qnmygvzuex3dwmg9yzt9alhvyeat0uu0jedg2wj33efl5qhl3ex3 '{"execute": {"msgs": [{"wasm": {"execute": {"contract_addr": "craft1cnuw3f076wgdyahssdkd0g3nr96ckq8cwa2mh029fn5mgf2fmcmsjl6xjf", "funds": [], "msg": "eyJtaW50Ijp7InRva2VuX2lkIjoiMSIsIm93bmVyIjoiY3JhZnQxM3ZocjNna21lOGhxdmZ5eGQ0emttZjVnYXVzODQwajVod3Vxa2giLCJ0b2tlbl91cmkiOiJodHRwczovL3JlZWNlLnNoIn19"}}}]}}' --from $KEY