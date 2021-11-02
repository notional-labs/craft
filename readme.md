# Craft Economy
> This is the Craft Economy Mainnet (craftnet-1)

> GENESIS PUBLISHED `https://ipfs.io/ipfs/QmcuMhkRVyhTdRmsx5Mh8tBVA7145KQwvfrNiTE8FzMGFM`

> PEERS PUBLISHED `0b9b1eedc4cd011bc03320a4fa4876b863ec263c@143.198.94.140:1337`

## Hardware Requirements
* **Minimal**
    * 4 GB RAM
    * 100 GB SSD
    * 3.2 x4 GHz CPU
* **Recommended**
    * 8 GB RAM
    * 100 GB NVME SSD
    * 4.2 GHz x6 CPU
* **Raspberry Pi**
    * Pi 4
    * 128 GB SD Card

## Operating System
* Linux/Windows/MacOS(x86)
* **Recommended**
    * Linux(x86_64)
    * Craft Pi Image

## Installation Steps
>Prerequisite: go1.17+ required. [ref](https://golang.org/doc/install)
  * Arch Linux `pacman -S go`
  * Ubuntu `sudo snap install go --classic`

>Prerequisite: git. [ref](https://github.com/git/git)
  * Arch Linux `pacman -S git`
  * Ubuntu `sudo apt-get install git`

>Optional requirement: GNU make. [ref](https://www.gnu.org/software/make/manual/html_node/index.html)
  * Arch Linux `pacman -S make`
  * Ubuntu `sudo apt-get install make`

* Clone git repository
```shell
git clone https://github.com/notional-labs/craft
```
* Checkout latest tag
```shell
cd craft
git fetch --tags
```
* Install
```shell
go mod tidy
cd cmd/craftd
go install ./...
```

### Generate keys

`craftd keys add [key_name]`

or

`craftd keys add [key_name] --recover` to regenerate keys with your [BIP39](https://github.com/bitcoin/bips/tree/master/bip-0039) mnemonic

to add ledger key

`craftd keys add [key_name] --ledger`


## Validator setup instructions

### GenTx : (Skip to [Validator](#become-a-validator) post genesis)

* [Install](#installation-steps) craftd binary
* Initialize node
```shell
craftd init {{NODE_NAME}} --chain-id craftnet-1
craftd add-genesis-account {{KEY_NAME}} 1000000000ucraft
craftd gentx {{KEY_NAME}} 10000000ucraft \
--chain-id craftnet-1 \
--moniker="{{VALIDATOR_NAME}}" \
--commission-max-change-rate=0.01 \
--commission-max-rate=1.0 \
--commission-rate=0.05 \
--details="<description>" \
--security-contact="<contact_information>" \
--website="<your_website>"
```
* Copy the contents of `${HOME}/.craftd/config/gentx/gentx-XXXXXXXX.json`.
* Fork the [repository](https://github.com/comdex-official/networks/)
* Create a file `gentx-{{VALIDATOR_NAME}}.json` under the mainnet/craftnet-1/gentxs folder in the forked repo, paste the copied text into the file. Find reference file gentx-examplexxxxxxxx.json in the same folder.
* Run `craftd tendermint show-node-id` and copy your nodeID.
* Run `ifconfig` or `curl ipinfo.io/ip` and copy your publicly reachable IP address.
* Create a file `peers-{{VALIDATOR_NAME}}.json` under the mainnet/craftnet-1/peers folder in the forked repo, paste the copied text from the last two steps into the file. Find reference file sample-peers.json in the same folder.
* Create a Pull Request to the `main` branch of the [repository](https://github.com/comdex-official/networks)
>**NOTE:** The Pull Request will be merged by the maintainers to confirm the inclusion of the validator at the genesis. The final genesis file will be published under the file mainnet/craftnet-1/genesis_final.json.
* Replace the contents of your `${HOME}/.craftd/config/genesis.json` with that of mainnet/craftnet-1/genesis_final.json.
* Copy below node as `persistent_peers` or `seeds` in `${HOME}/.craftd/config/config.toml`
 
```shell
0b9b1eedc4cd011bc03320a4fa4876b863ec263c@143.198.94.140:1337
```
* Copy below value as minimum-gas-prices in `${HOME}/.craftd/config/app.toml`
```shell
0.2ucraft
```

* Start craftd by creating a `systemd` service to run the node in the background
```shell
nano /etc/systemd/system/craft.service
```
Copy and paste the following file into your service file. Be sure to edit as you see fit.

```shell
[Unit]
Description=Althea Node
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
Reload the service files
`sudo systemctl daemon-reload`
Create the symlinlk
`sudo systemctl enable craft.service`
Start the node
`sudo systemctl start craft && journalctl -u craft -f`

### Become a validator

* [Install](#installation-steps) comdex core application
* Initialize node
```shell
comdex init {{NODE_NAME}}
```
* Replace the contents of your `${HOME}/.comdex/config/genesis.json` with that of testnet/comets-test/genesis_final.json from the `main` branch of [repository](https://github.com/comdex-official/networks).
* Copy below node as `persistent_peers` or `seeds` in `${HOME}/.comdex/config/config.toml`
```shell
3659590cd1466671a49421089e55f1392e1cad0e@15.207.189.210:26656,8b1ccf5cf3a3ba65ee074f46ea8c6c164d867104@52.201.166.91:26656,5307ce50bd8a6f7bb5a922e3f7109b5f3241c425@13.51.118.56:26656,9c25a7ab94a315f683c3693e17aec6b2c91c851c@52.77.115.73:26656
```

* Copy below value as minimum-gas-prices in ${HOME}/.comdex/config/app.toml
```shell
0.2ucmdx
```

* Start comdex by running below command or create a `systemd` service to run comdex in background.
```shell
comdex start
```
* Acquire $ucmdx by sending a message to the validators channel in [Discord](https://discord.gg/gH6RTrnexk).
* Run `comdex tendermint show-validator` and copy your consensus public key.
* Send a create-validator transaction
```
comdex tx staking create-validator \
--from {{KEY_NAME}} \
--amount XXXXXXXXucmdx \
--pubkey $(comdex tendermint show-validator) \
--chain-id comets-test \
--moniker="{{VALIDATOR_NAME}}" \
--commission-max-change-rate=0.01 \
--commission-max-rate=1.0 \
--commission-rate=0.07 \
--min-self-delegation="1" \
--details="XXXXXXXX" \
--security-contact="XXXXXXXX" \
--website="XXXXXXXX"
```

## Version
This chain is currently running on Comdex [v0.0.2](https://github.com/comdex-official/comdex/releases/tag/v0.0.2)
Commit Hash: 36e59abc8aff22a8eea2e851ee19e497c7f754ea
>Note: If your node is running on an older version of the application, please update it to this version at the earliest to avoid being exposed to security vulnerabilities /defects.

## Binary
The binary can be downloaded from [here](https://github.com/comdex-official/comdex/releases/tag/v0.0.2).

## Explorer
The explorer for this chain is hosted [TestNet Explorer](https://comets-test.comdex.one/)

## Genesis Time
The genesis transactions sent before 1200HRS UTC 15th October 2021 will be used to publish the genesis_final.json at 1400HRS UTC 15th October 2021 and then start the chain at 14.30UTC once the quorum is reached.
