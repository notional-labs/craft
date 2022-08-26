How To:
```bash
python3 Mint_RealEstatemongoDB.py
python3 Mint_Images.py

craftd tx sign real_estate/real_estate_mint.json --from dao # (would use mutlisig in future)
# copy output -> its own file in signed
craftd tx broadcast signed_re.json
```