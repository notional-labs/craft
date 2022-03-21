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

## Dependencies
>Prerequisite: go1.18+ required
* **Arch Linux:** `pacman -S go`
* **Ubuntu sudo:** `snap install go --classic`

>Prerequisite: git
* **Arch Linux:** `pacman -S git`
* **Ubuntu sudo:** `apt-get install git`

>Optional requirement: GNU make
* **Arch Linux:** `pacman -S make`
* **Ubuntu sudo:** `apt-get install make`

# Craftd Installation Steps

> ### TIP: Perform as `root` user

## Create service user

```bash
adduser craft
```

## Install Prerequeisties

```bash
apt-get update && apt-get -y upgrade
apt-get install -y build-essential
## OPTIONAL ADDITIONS to install with build-essential, net-tools htop jq ntp fail2ban
apt-get install -y net-tools htop jq ntp fail2ban
```
## Install latest version of Golang

```bash
GOVER=$(curl https://go.dev/VERSION?m=text)
wget https://golang.org/dl/${GOVER}.linux-amd64.tar.gz
sudo rm -rf /usr/local/go && sudo tar -C /usr/local -xzf ${GOVER}.linux-amd64.tar.gz
```

## Firewall configuration

```bash
ufw limit ssh/tcp comment 'Rate limit for openssh server'
ufw default deny incoming
ufw default allow outgoing
ufw allow 26656/tcp comment 'Cosmos SDK/Tendermint P2P'
ufw enable
```

## Create a service file

```bash
nano /etc/systemd/system/craft.service
```

```bash
[Unit]
Description=Craft Node
After=network.target

[Service]
Type=simple
User=craft
Group=craft
WorkingDirectory=/home/craft
ExecStart=/home/craft/go/bin/craftd start
Restart=on-failure
StartLimitInterval=0
RestartSec=3
LimitNOFILE=10000
LimitMEMLOCK=209715200

[Install]
WantedBy=multi-user.target
```

### Reload the changes and enable the daemon to start

```bash
systemctl daemon-reload && systemctl enable craft.service
```

# Build and Initiate Craft Validator

> ### TIP: Perform as `craft` user

Add the following to the bottom of your profile

```bash
# add environmental variables for Go
if [ -f "/usr/local/go/bin/go" ] ; then
    export GOROOT=/usr/local/go
    export GOPATH=${HOME}/go
    export GOBIN=$GOPATH/bin
    export PATH=${PATH}:${GOROOT}/bin:${GOBIN}
fi
```

And then create a place to store your bin and reload your profile

```bash
mkdir -p ${HOME}/.local/bin
. ~/.profile
```

## Clone GitHub Repo

```bash
Clone git repository
git clone https://github.com/notional-labs/craft.git
cd craft
git checkout v0.1.1
go install ./...
```
> to add ledger support `go install -tags ledger ./...`

## Validator setup instructions
## GenTx : [Skip to Post Genesis](https://github.com/chalabi2/craft/blob/master/networks/craft-testnet-1/README.md#become-a-validator-post-genesis)

- Install craftd binary
- Initialize node

Initialize your node **FIRST**

```bash
craftd init <moniker> --chain-id craft-testnet-1 --staking-bond-denom exp
```

## Generate key and set keyring passphrase
### Generate keys and back up recovery

* `craftd keys add [key_name]`
* `craftd keys add [key_name] --recover` to regenerate keys with your BIP39 mnemonic
to add ledger key
* `craftd keys add [key_name] --ledger` to add a ledger key 

## Add Genesis Account

```bash
craftd add-genesis-account <key_name> 1000000uexp
```

### Create & Submit GenTX
```bash
craftd gentx <key_name> 1000000uexp --keyring-backend=os --chain-id=craft-testnet-1 --moniker="<your_moniker>" --commission-max-change-rate=0.01 --commission-max-rate=0.5 --commission-rate=0.05 --details="<details here>" --security-contact="<email>" --website="<website>"
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

>Reload the service files `sudo systemctl daemon-reload Create the symlinlk sudo systemctl enable craft.service 

Start the node sudo systemctl start craft && journalctl -u craft -f`

## Become a validator (Post Genesis)
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
>Reload the service files `sudo systemctl daemon-reload Create the symlinlk sudo systemctl enable craft.service Start the node sudo systemctl start craft && journalctl -u craft -f`

### Start the node
* `systemctl start craft` optional command for logging `journalctl -u craft -f`
