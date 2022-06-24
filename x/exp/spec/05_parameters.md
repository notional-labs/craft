<!--
order: 5
-->

# Parameters

The exp module contains the following parameters:

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
