<!--
order: 4
-->

# Events

The `craft`  module emits Cosmos SDK `TypedEvent`s in the form of proto messages to provide state updates for applications like block explorers.

The `craft` module emits the following events keys:

EventSpendIbcAsset            = "spend_ibc_asset"
AttributeKeyMintExp           = "mintexp"
AttributeKeyBurnExp           = "burnexp"
AttributeKeyJoinDao           = "joindao"
AttributeAdjustDaoTokenPrice  = "adjust_price"

## EventSpendIbcAsset

Emitted when user spen ibc tokens to mint gov tokens

## AttributeKeyMintExp

Emitted when a Dao address mint gov token to user 

## AttributeKeyBurnExp

Emitted when burning gov token and returning the locked token to the user

## AttributeKeyJoinDao

Emitted when user join DAO by non-ibc or ibc tokens

## AttributeAdjustDaoTokenPrice

Emitted when updating gov token price

