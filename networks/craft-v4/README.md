# Craft Economy

>This is the Craft Economy Testnet-1 (craft-testnet-1)

>[GENESIS](https://raw.githubusercontent.com/notional-labs/craft/master/networks/craft-testnet-1/genesis.json) PUBLISHED

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
git checkout v0.5.2
make install
# go install ./...
craftd config chain-id craft-v4
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
KEYNAME='reece'
MONIKER='pbcups'
SECURITY_CONTACT="reece@crafteconomy.io"
WEBSITE="https://reece.sh"
MAX_RATE='0.20'        # 20%
COMMISSION_RATE='0.05' # 5%
MAX_CHANGE='0.05'      # 5%
CHAIN_ID='craft-v4'
PROJECT_HOME="${HOME}/.craftd/"
KEYNAME_ADDR=$(craftd keys show $KEYNAME -a)
echo -e "$KEYNAME\n$MONIKER\n$DETAILS\n$SECURITY_CONTACT\n$WEBSITE\n$MAX_RATE\n$COMMISSION_RATE\n$MAX_CHANGE\n$CHAIN_ID\n$HOME_DIR\n$KEYNAME_ADDR"
# /Validator variables

# Remove old files if they exist
rm $HOME/.craftd/config/genesis.json
rm $HOME/.craftd/config/gentx/*.json

# Give yourself 1exp for the genesis Tx signed
craftd init $MONIKER --chain-id $CHAIN_ID --staking-bond-denom uexp
craftd add-genesis-account $KEYNAME_ADDR 1000000uexp

# genesis transaction using all above variables
craftd gentx $KEYNAME 1000000uexp \
    --home=$PROJECT_HOME \
    --chain-id=$CHAIN_ID \
    --moniker=$MONIKER --commission-max-change-rate=$MAX_CHANGE \
    --commission-max-rate=$MAX_RATE \
    --commission-rate=$COMMISSION_RATE \
    --security-contact=$SECURITY_CONTACT \
    --website=$WEBSITE \
    --details=""

# Get that gentx data easily -> your home directory
DATA=`cat ${PROJECT_HOME}/config/gentx/gentx-*.json`
FILE_LOC=$HOME/`echo $DATA | jq -r '.body.messages[0].description.moniker'`.json
echo $DATA > $FILE_LOC

# Download the file from $HOME/MONIKER.json & upload to the discord channel
echo -e "\n\n\nPlease download '$FILE_LOC' and upload to discord. (or 'cat $FILE_LOC', copy paste send -> discord)"
echo -e "     (also remember to backup ~/.craftd/node_key.json && ~/.craftd/priv_validator_key.json)\n"
echo -e "     Your peer: `echo $(craftd tendermint show-node-id)@$(curl -s ifconfig.me):26656`"
```

## Peers, Seeds, Genesis & Service File (Post GenTX)
* Replace the contents of your `${HOME}/.craftd/config/genesis.json` with that of `FUTURE_GENESIS_LINK_HERE`
<!-- `https://github.com/notional-labs/craft/blob/master/networks/craft-testnet-1/genesis.json` -->

* Find Peers & Seeds [here](https://hackmd.io/eMqK5OrDR3uz3WpGvHUjow)

* Copy below value as minimum-gas-prices in `${HOME}/.craftd/config/app.toml
0.02ucraft`

* Start craftd by creating a systemd service to run the node in the background
Copy and paste the following file into your service file. Be sure to edit as you see fit.
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
>Reload the service files 
```
sudo systemctl daemon-reload
sudo systemctl enable craft.service
```

# Post-Genesis
### Become a validator
* [Install craftd binary](https://github.com/chalabi2/craft/blob/master/networks/craft-testnet-1/README.md#craftd-installation-steps)

### Initialize node
`craftd init <moniker> --chain-id  --staking-bond-denom exp`
### Peers, Seeds, Genesis & Service File
* Replace the contents of your `${HOME}/.craftd/config/genesis.json` with that of `https://github.com/notional-labs/craft/blob/master/networks/craft-testnet-1/genesis.json`

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
