<!--
order: 3
-->

# Messages

### `MsgJoinDaoByNonIbcAsset`
Add join_address to whitelist, create request for mint govement tokens to joiner. Call by user

```protobuf
message MsgJoinDaoByNonIbcAsset {
  string join_address = 1 ;
  string gov_address = 2;
  int64 max_token = 3;
}
```

The message handling should fail if: 
- join_address already in whitelist
- gov_address different from DAO address


### `MsgJoinDaoByIbcAsset`
Add join_address to mint_list. Call by user

```protobuf
message MsgJoinDaoByIbcAsset{
  string join_address = 1 ;
  string gov_address = 2;
  string amount = 3;
}
```

The message handling should fail if: 
- join_address already in mint_list
- gov_address different from DAO address


### `MsgMintAndAllocateExp`
Mint gov tokens to member address and delete from whitelist. Call by DAO address

```protobuf
message MsgMintAndAllocateExp {
  repeated cosmos.base.v1beta1.Coin amount = 1;
  string from_address = 2;
  string member = 3 ;
}
```

The message handling should fail if: 
- from_address different from DAO address
- member does not exist in whitelist
- amount contains tokens that are not gov tokens
- member out of `ClosePoolPeriod`


### `MsgSpendIbcAssetToExp`
Spend IBC token to execution mint request created before. After mint token to user, delete from mint_request

```protobuf
message MsgSpendIbcAssetToExp{
  string from_address = 1 ;
  repeated cosmos.base.v1beta1.Coin amount = 2 
  [(gogoproto.nullable) = false, (gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];
  
}
```

The message handling should fail if: 
- from_address does not exist in mint_list
- amount contains tokens that are not ibc tokens
- from_address's request out of `ClosePoolPeriod`


### `MsgAdjustDaoTokenPrice`
Update gov token price. Call by DAO address

```protobuf
message MsgAdjustDaoTokenPrice{
  string from_address = 1 ;
  string dao_token_price = 2
  [
    (gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec",
    (gogoproto.nullable)   = false
  ];
}
```

The message handling should fail if: 
- from_address different from DAO address
- dao_token_price = 0


### `MsgFundExpPool`
FundPoolForExp allows an account to directly fund the exp fund pool. Call by user

```protobuf
message MsgFundExpPool {
    // from_address defines the member who want fund.
  string from_address = 1 ;
  repeated cosmos.base.v1beta1.Coin amount = 2 
  [(gogoproto.nullable) = false, (gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];
}
```


### `MsgSendCoinsFromModuleToDAO`
Send coin from module to Dao Address

```protobuf
message MsgSendCoinsFromModuleToDAO{
  string to_address = 1;

  repeated cosmos.base.v1beta1.Coin amount = 2
  [(gogoproto.nullable) = false, (gogoproto.castrepeated) = "github.com/cosmos/cosmos-sdk/types.Coins"];
}
```

The message handling should fail if: 
- to_address different from DAO address


### `MsgBurnAndRemoveMember`
Request burn gov tokens and get back tokens locked before

```protobuf
message MsgBurnAndRemoveMember {
  string from_address = 1 ;
  string metadata = 2;
}
```

The message handling should fail if: 
- in vesting process
- user not owner max token ownerble