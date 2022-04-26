package keeper

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/types"
)

func (k ExpKeeper) GetDaoAssetInfo(ctx sdk.Context) (types.DaoAssetInfo, error) {
	var daoAssetInfo types.DaoAssetInfo

	store := ctx.KVStore(k.storeKey)
	if !store.Has(types.KeyDaoAssetInfo) {
		return types.DaoAssetInfo{}, types.ErrInvalidKey
	}

	bz := store.Get(types.KeyDaoAssetInfo)
	err := k.cdc.Unmarshal(bz, &daoAssetInfo)
	if err != nil {
		return types.DaoAssetInfo{}, err
	}

	return daoAssetInfo, nil
}

func (k ExpKeeper) SetDaoAssetInfo(ctx sdk.Context, daoAssetInfo types.DaoAssetInfo) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&daoAssetInfo)
	store.Set(types.KeyDaoAssetInfo, bz)
}

func (k ExpKeeper) IterateWhitelist(ctx sdk.Context, cb func(record types.AccountRecord) (stop bool)) {
	store := ctx.KVStore(k.storeKey)

	iterator := sdk.KVStorePrefixIterator(store, types.KeyWhiteList)

	for ; iterator.Valid(); iterator.Next() {
		var record types.AccountRecord
		err := k.cdc.Unmarshal(iterator.Value(), &record)
		if err != nil {
			panic(err)
		}
		if cb(record) {
			break
		}
	}
}

func (k ExpKeeper) GetWhiteList(ctx sdk.Context) (records types.AccountRecords) {
	k.IterateWhitelist(ctx, func(record types.AccountRecord) bool {
		records = append(records, record)
		return false
	})
	return
}

func (k ExpKeeper) SetAccountRecord(ctx sdk.Context, address sdk.AccAddress, record types.AccountRecord) {
	store := ctx.KVStore(k.storeKey)
	keys := types.GetWhiteListByAddressBytes(address)

	bz := k.cdc.MustMarshal(&record)
	store.Set(keys, bz)
}

func (k ExpKeeper) RemoveRecord(ctx sdk.Context, address sdk.AccAddress) {
	store := ctx.KVStore(k.storeKey)
	keys := types.GetWhiteListByAddressBytes(address)

	store.Delete(keys)
}
