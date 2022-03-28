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
