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
* Linux (x86_64) or Linux (amd64) Reccomended Arch Linux

### Dependencies
>Prerequisite: go1.18+ required
* **Arch Linux:** `pacman -S go`
* **Ubuntu sudo:** `snap install go --classic`

>Prerequisite: git
* **Arch Linux:** `pacman -S git`
* **Ubuntu sudo:** `apt-get install git`

>Optional requirement: GNU make
* **Arch Linux:** `pacman -S make`
* **Ubuntu sudo:** `apt-get install make`

## Craftd Installation Steps

```bash
Clone git repository
git clone https://github.com/notional-labs/craft.git
cd craft
git checkout v0.1.1
go install ./...
```
> to add ledger support `go install -tags ledger ./...`

# Generate keys
* `craftd keys add [key_name]`
* `craftd keys add [key_name] --recover` to regenerate keys with your BIP39 mnemonic
to add ledger key
* `craftd keys add [key_name] --ledger` to add a ledger key 

# Validator setup instructions
## GenTx : [Skip to Post Genesis](https://github.com/chalabi2/craft/blob/master/networks/craft-testnet-1/README.md#become-a-validator-post-genesis)

```bash
Install craftd binary
Initialize node
craftd init <moniker> --chain-id craft-testnet-1 --staking-bond-denom exp
```
### Create & Submit GenTX
```bash
craftd gentx <key_name> 1000000uexp --home=~/.craftd/ --keyring-backend=os --chain-id=craft-testnet-1 --moniker=<your_moniker> --commission-max-change-rate=0.01 --commission-max-rate=0.5 --commission-rate=0.05 --details="<details here>" --security-contact="<email>" --website="<website>"
```
### Fork the repository 

**Copy the contents of** `${HOME}/.craftd/config/gentx/gentx-XXXXXXXX.json to craft/networks/craft-testnet-1/gentx/<yourvalidatorname>.json`

**Create a Pull Request to the main branch of the repository** 

>NOTE: The Pull Request will be merged by the maintainers to confirm the inclusion of the validator at the genesis.

### Peers, Seeds, Genesis & Service File (Post GenTX)
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
>Reload the service files `sudo systemctl daemon-reload Create the symlinlk sudo systemctl enable craft.service Start the node sudo systemctl start craft && journalctl -u craft -f`

## Become a validator (Post Genesis)
* Install craftd binary
### Initialize node
`craftd init <moniker> --chain-id craft-testnet-1 --staking-bond-denom exp`
### Peers, Seeds, Genesis & Service File
Replace the contents of your `${HOME}/.craftd/config/genesis.json` with that of `https://github.com/notional-labs/craft/raw/master/networks/craft-testnet-1/genesis.json`
Copy below node as persistent_peers or seeds in ${HOME}/.craftd/config/config.toml
0b9b1eedc4cd011bc03320a4fa4876b863ec263c@143.198.94.140:1337
Copy below value as minimum-gas-prices in ${HOME}/.craftd/config/app.toml
0.02ucraft
Start craftd by creating a systemd service to run the node in the background
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
Reload the service files
sudo systemctl daemon-reload Create the symlinlk sudo systemctl enable craft.service Start the node sudo systemctl start craft && journalctl -u craft -f
Acquire $ucraft by sending a message to the validators channel in Discord.
Create Validator TX
craftd tx staking create-validator \
--from {{KEY_NAME}} \
--chain-id craft-testnet-1 \
--moniker="<VALIDATOR_NAME>" \
--commission-max-change-rate=0.01 \
--commission-max-rate=1.0 \
--commission-rate=0.05 \
--details="<description>" \
--security-contact="<contact_information>" \
--website="<your_website>" \
--pubkey $(craftd tendermint show-validator) \
--min-self-delegation="1" \
--amount <token delegation>ucraft \
