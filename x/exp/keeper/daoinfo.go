package keeper

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/types"
)

func (k ExpKeeper) GetDaoInfo(ctx sdk.Context) (types.DaoInfo, error) {
	var daoInfo types.DaoInfo

	store := ctx.KVStore(k.storeKey)
	if !store.Has(types.KeyDaoInfo) {
		return types.DaoInfo{}, types.ErrInvalidKey
	}

	bz := store.Get(types.KeyDaoInfo)
	err := k.cdc.Unmarshal(bz, &daoInfo)
	if err != nil {
		return types.DaoInfo{}, err
	}

	return daoInfo, nil
}

func (k ExpKeeper) SetDaoInfo(ctx sdk.Context, daoInfo types.DaoInfo) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&daoInfo)
	store.Set(types.KeyDaoInfo, bz)
}

func (k ExpKeeper) GetDaoAssetInfo(ctx sdk.Context) (types.DaoAssetInfo, error) {
	var daoAssetInfo types.DaoAssetInfo

	store := ctx.KVStore(k.storeKey)
	if !store.Has(types.KeyDaoAssetInfo) {
		return types.DaoAssetInfo{}, types.ErrInvalidKey
	}

	bz := store.Get(types.KeyDaoInfo)
	err := k.cdc.Unmarshal(bz, &daoAssetInfo)
	if err != nil {
		return types.DaoAssetInfo{}, err
	}

	return daoAssetInfo, nil
}

func (k ExpKeeper) SetDaoAssetInfo(ctx sdk.Context, daoInfo types.DaoAssetInfo) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&daoInfo)
	store.Set(types.KeyDaoInfo, bz)
}
