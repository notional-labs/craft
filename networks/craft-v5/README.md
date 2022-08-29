# Craft Economy

>This is the Craft Economy Testnet-1 (craft-testnet-1)

>[GENESIS](https://github.com/notional-labs/craft/raw/master/networks/craft-v5/genesis.json) PUBLISHED

>[PEERS PUBLISHED](https://hackmd.io/YsZv1UXeRHOsJUH-Mkrfvw)

## Hardware Requirements
**Minimal**
* 4 GB RAM
* 100 GB SSD
* 3.2 x4 GHz CPU

**Recommended**
* 8 GB RAM
* 100 GB NVME SSD
* 4.2 GHz x6 CPU 

**Operating System**
* Linux (x86_64) or Linux (amd64) Recommended Arch Linux

### Dependencies
>Prerequisite: go1.18+, git, gcc, make, jq

**Arch Linux:** 
```
pacman -S go git gcc make
```

**Ubuntu Linux:** 
```
sudo snap install go --classic
sudo apt-get install git gcc make jq
```

---

## Craftd Installation Steps

```bash
# Clone git repository
git clone https://github.com/notional-labs/craft.git
cd craft
git checkout v0.5.3
make install
# go install ./...
craftd config chain-id craft-v5
```
> to add ledger support `go install -tags ledger ./...`

### Generate keys
* `craftd keys add [key_name]`
* `craftd keys add [key_name] --recover` to regenerate keys with your BIP39 mnemonic
to add ledger key
* `craftd keys add [key_name] --ledger` to add a ledger key 

# Validator setup instructions
## Genesis Tx:
```bash
# Validator variables
KEYNAME='validator' # your keyname
MONIKER='pbcups'
SECURITY_CONTACT="reece@crafteconomy.io"
WEBSITE="https://reece.sh"
MAX_RATE='0.20'        # 20%
COMMISSION_RATE='0.05' # 5%
MAX_CHANGE='0.05'      # 5%
CHAIN_ID='craft-v5'
PROJECT_HOME="${HOME}/.craftd"
KEYNAME_ADDR=$(craftd keys show $KEYNAME -a)
# /Validator variables

# echo -e "$KEYNAME\n$MONIKER\n$DETAILS\n$SECURITY_CONTACT\n$WEBSITE\n$MAX_RATE\n$COMMISSION_RATE\n$MAX_CHANGE\n$CHAIN_ID\n$HOME_DIR\n$KEYNAME_ADDR"

# Remove old files if they exist
craftd tendermint unsafe-reset-all
rm $HOME/.craftd/config/genesis.json
rm $HOME/.craftd/config/gentx/*.json

# Give yourself 1exp for the genesis Tx signed
craftd init "$MONIKER" --chain-id $CHAIN_ID --staking-bond-denom uexp
craftd add-genesis-account $KEYNAME_ADDR 1000000uexp

# genesis transaction using all above variables
craftd gentx $KEYNAME 1000000uexp \
    --home=$PROJECT_HOME \
    --chain-id=$CHAIN_ID \
    --moniker="$MONIKER" \
     --commission-max-change-rate=$MAX_CHANGE \
    --commission-max-rate=$MAX_RATE \
    --commission-rate=$COMMISSION_RATE \
    --security-contact=$SECURITY_CONTACT \
    --website=$WEBSITE \
    --details=""

# Get that gentx data easily -> your home directory
cat ${PROJECT_HOME}/config/gentx/gentx-*.json

# Download the file from $HOME/MONIKER.json & upload to the discord channel
echo -e "\n\n\nPlease paste the contents above into the discord channel (( ${PROJECT_HOME}/config/gentx/gentx-*.json ))"
echo -e "     (also remember to backup ~/.craftd/node_key.json && ~/.craftd/priv_validator_key.json)\n"
echo -e "     Your peer: `echo $(craftd tendermint show-node-id)@$(curl -s ifconfig.me):26656`"
# DO NOT SUBMIT A PR FOR THIS, POST IN THE DISCORD `gentx-submit` CHANNEL THANK YOU!
```

## Peers, Seeds, Genesis & Service File (Post GenTX)
```bash
curl https://raw.githubusercontent.com/notional-labs/craft/master/networks/craft-v5/genesis.json > ${HOME}/.craftd/config/genesis.json
```

### Find Peers & Seeds [here](https://hackmd.io/YsZv1UXeRHOsJUH-Mkrfvw)

<br/>

<!-- * Copy below value as minimum-gas-prices in `${HOME}/.craftd/config/app.toml -->
> Update minimum gas price for craft
```bash
# nano ${HOME}/.craftd/config/app.toml # minimum-gas-prices -> "0.025ucraft"
sed -i 's/minimum-gas-prices = "0ucraft"/minimum-gas-prices = "0.025ucraft"/g' ${HOME}/.craftd/config/app.toml
```

> systemd service file, best to NOT run your node in a screen.
```bash
# nano /etc/systemd/system/craft.service
[Unit]
Description=Craft Economy Node
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/root/
ExecStart=/root/go/bin/craftd start
Restart=on-failure
StartLimitInterval=0
RestartSec=3
LimitNOFILE=65535
LimitMEMLOCK=209715200

[Install]
WantedBy=multi-user.target
```

>Reload the service files & start running it
```bash
sudo systemctl daemon-reload
sudo systemctl start craft.service
sudo systemctl enable craft.service
# journalctl -u craft.service -n 250 -f --output cat
```

# Post-Genesis
### Become a validator
* [Install craftd binary](https://github.com/notional-labs/craft/tree/master/networks/craft-v5#craftd-installation-steps)

### Initialize node
`craftd init <moniker> --chain-id  --staking-bond-denom exp`
### Peers, Seeds, Genesis & Service File
* Replace the initialized genesis `${HOME}/.craftd/config/genesis.json` with [testnet-v4 genesis](https://github.com/notional-labs/craft/raw/master/networks/craft-v5/genesis.json) 

* Find Peers & Seeds [here](https://hackmd.io/YsZv1UXeRHOsJUH-Mkrfvw)

* Copy below value as minimum-gas-prices in `${HOME}/.craftd/config/app.toml
0.02ucraft`

* Start craftd by creating a systemd service to run the node in the background
```bash
nano /etc/systemd/system/craft.service
Copy and paste the following file into your service file. Be sure to edit as you see fit.
[Unit]
Description=Craft Node
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/root/
ExecStart=/root/go/bin/craftd start
Restart=on-failure
StartLimitInterval=0
RestartSec=3
LimitNOFILE=65535
LimitMEMLOCK=209715200

[Install]
WantedBy=multi-user.target
```

### Start the node
* Reload the service file `sudo systemctl daemon-reload` Create the symlinlk `sudo systemctl enable craft.service`
* Start the node `systemctl start craft` optional command for logging `journalctl -u craft -f`
