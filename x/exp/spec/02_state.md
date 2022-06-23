<!--
order: 2
-->

# State

The `crafts` module tracks the state of these primary objects: 

- AccountRecord
- AssetDao
- DaoAssetInfo

## AccountRecord

AccountRecord objects represent members in Dao.

```protobuf
message AccountRecord {
  string account                      = 1;
  cosmos.base.v1beta1.Coin max_token  = 2;
  google.protobuf.Timestamp join_dao_time = 3
}
```

## AssetDao

AssetDao is governace token of system
We use "uexp" as AssetDao 

```protobuf
message AssetDao {
  string denom = 1;
  uint64 amount = 2;
}
```

## AssetDaoInfo

Save information of DaoAsset, price in ibc token

```protobuf
message DaoAssetInfo{
  string dao_token_price = 2 [
    (gogoproto.customtype) = "github.com/cosmos/cosmos-sdk/types.Dec",
    (gogoproto.nullable)   = false
  ];  
  repeated AssetDao asset_dao = 3;
}
```