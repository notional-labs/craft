# CRAFT Integration

## [Developer Java API](docs/integration-api.md)
---

## Setup
```
Load plugin(s)

Set MongoDB server, Redis-Server, and other information in craft-integration/config.yml

Install keplr, visit: http://65.108.71.66/ [or any testnet with :1317 and :26657]

Copy address for CRAFT wallet

Then in game do “/wallet set <craft-address>”

/wallet faucet ADDRESS 500 [will be disabled]
```

## Commands
```
/wallet help
/wallet balance <player/address>
/wallet set <address>
/wallet pay <player/wallet> <amount>
/wallet supply (shows stake and craft)
/wallet pending

ADMIN:
/wallet faucet <name/wallet> <amount> [Will be firewalled in the future]
/wallet faketx <license/purchase> [item]
/wallet fakesign <generated_TxID>
/wallet allpending
```

## Testing Commands
```
  /test-balanceapi (Show balance of current wallet)
  /test-walletapi (Get current wallet as a WalletClickable)
  /test-exampleapi (Generates a Tx which prints out a user bought something. Requires /wallet fakesign <id>)
  /test-tokensapi (Show total CRAFT and STAKE tokens in supply)
  /test-trade (Req. 2 players. Hold items, confirm trade amount, items are taken. On Blockchain sign trade items as agreed upon)
  /test-keplr (Send user link to KEPLR wallet documentation)
  /test-escrowspend (pays 1 craft for some dirt to ensure it works correctly)
```