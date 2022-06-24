<!--
order: 5
-->

# Parameters

The pylons module contains the following parameters:

| Key                                   | Type          | Example                                     	   						  |
| ------------------------------------- | ------------- | ----------------------------------------------------------------------- |
| MaxCoinMint                           | uint64  		| 10000000000                                      						  | 
| DaoAccount                    		| string        | craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl     						  |
| Denom             					| string        | uexp                                             						  |
| ClosePoolPeriod                   	| Duration      | time.Minute                 					   						  |
| VestingPeriodEnd                   	| Duration      | time.Minute                					   						  |
| IbcAssetDenom                         | string        | ibc/27394FB092D2ECCD56123C74F36E4C1F926001CEADA9CA97EA622B25F41E5EB2    |
| BurnExpPeriod                         | Duration      | time.Day * 28                                      					  |


## MaxCoinMint

Max gov tokens can be minted to user

## DaoAccount

Dao address

## Denom

denom of gov token

## ClosePoolPeriod

The duration a user can spend ibc or non ibc token to execute mint request. If out of this duration => error

## VestingPeriodEnd

Time period for gov token to be minted continuously until `AccountRecord.MaxToken` for user

## IbcAssetDenom

Ibc token's denom is used for payment

## BurnExpPeriod

Time from request burn token to receive locked token back

## PaymentProcessors

Structure to represent a payment processor (such as Stripe) on-chain.  

Example:

```go
	DefaultProcessorPercentage  = sdk.ZeroDec()
    DefaultValidatorsPercentage = sdk.MustNewDecFromStr("0.003")
    DefaultPylonsIncPubKey      = "EVK1dqjD6K8hGylacMpWAa/ru/OnWUDtCZ+lPkv2TTA=" // this is a testing key, do not use in production!
    DefaultPaymentProcessors    = []PaymentProcessor{
        {
            CoinDenom:            StripeCoinDenom,
            PubKey:               DefaultPylonsIncPubKey,
            ProcessorPercentage:  DefaultProcessorPercentage,
            ValidatorsPercentage: DefaultValidatorsPercentage,
            Name:                 "Pylons_Inc",
        },
    }
    DefaultPaymentProcessorsTokensBankParams = []types.SendEnabled{
        {Denom: StripeCoinDenom, Enabled: false},
    }
```

## DistrEpochIdentifier

String identifier to choose an epoch length from the `x/epochs` module.

## EngineVersion

Application version.  Planned for use in the future to deprecate recipes.




