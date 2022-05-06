package keeper

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	"github.com/notional-labs/craft/x/exp/types"
)

func (k ExpKeeper) addAddressToBurnRequestList(ctx sdk.Context, memberAccount string, tokenLeft *sdk.Coin) error {
	burnReques := types.BurnRequest{
		Account:       memberAccount,
		BurnTokenLeft: tokenLeft,
		RequestTime:   ctx.BlockTime(),
		Status:        types.StatusOnGoingRequest,
	}
	k.SetBurnRequest(ctx, burnReques)

	return nil
}

func (k ExpKeeper) addAddressToMintRequestList(ctx sdk.Context, memberAccount sdk.AccAddress, tokenLeft sdk.Dec) error {
	mintRequest := types.MintRequest{
		Account:        memberAccount.String(),
		DaoTokenLeft:   tokenLeft,
		DaoTokenMinted: sdk.NewDec(0),
		Status:         types.StatusOnGoingRequest,
		RequestTime:    ctx.BlockHeader().Time,
	}

	k.SetMintRequest(ctx, mintRequest)

	return nil
}

// need modify for better performance .
func (k ExpKeeper) GetDaoTokenPrice(ctx sdk.Context) sdk.Dec {
	asset, _ := k.GetDaoAssetInfo(ctx)

	return asset.DaoTokenPrice
}

// calculate exp value by ibc asset .
func (k ExpKeeper) calculateDaoTokenValue(ctx sdk.Context, amount sdk.Int) sdk.Dec {
	daoTokenPrice := k.GetDaoTokenPrice(ctx)

	return daoTokenPrice.MulInt(amount)
}

func (k ExpKeeper) SetBurnRequest(ctx sdk.Context, burnRequest types.BurnRequest) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&burnRequest)

	accAddress, err := sdk.AccAddressFromBech32(burnRequest.Account)
	if err != nil {
		panic(err)
	}
	store.Set(types.GetBurnRequestAddressBytes(accAddress), bz)
}

func (k ExpKeeper) GetBurnRequestByKey(ctx sdk.Context, key []byte) (types.BurnRequest, error) {
	var burnRequest types.BurnRequest

	store := ctx.KVStore(k.storeKey)
	if !store.Has(key) {
		return types.BurnRequest{}, sdkerrors.Wrapf(types.ErrInvalidKey, "burnRequest")
	}

	bz := store.Get(key)
	err := k.cdc.Unmarshal(bz, &burnRequest)
	if err != nil {
		return types.BurnRequest{}, err
	}

	return burnRequest, nil
}

// GetAllBurnRequest returns all the burn request from store .
func (keeper ExpKeeper) GetAllBurnRequests(ctx sdk.Context) (burnRequests types.BurnRequests) {
	keeper.IterateBurnRequests(ctx, func(burnRequest types.BurnRequest) bool {
		burnRequests = append(burnRequests, burnRequest)
		return false
	})
	return
}

// IterateBurnRequest iterates over the all the BurnRequest and performs a callback function .
func (k ExpKeeper) IterateBurnRequests(ctx sdk.Context, cb func(burnRequest types.BurnRequest) (stop bool)) {
	store := ctx.KVStore(k.storeKey)

	iterator := sdk.KVStorePrefixIterator(store, types.KeyBurnRequestList)
	defer iterator.Close()
	for ; iterator.Valid(); iterator.Next() {
		var burnRequest types.BurnRequest
		err := k.cdc.Unmarshal(iterator.Value(), &burnRequest)
		if err != nil {
			panic(err)
		}

		if cb(burnRequest) {
			break
		}
	}
}

// not good logic need modify
func (k ExpKeeper) RemoveBurnRequest(ctx sdk.Context, burnRequest types.BurnRequest) {
	store := ctx.KVStore(k.storeKey)
	accAddress, _ := sdk.AccAddressFromBech32(burnRequest.Account)
	if store.Has(types.GetBurnRequestAddressBytes(accAddress)) {
		store.Delete(types.GetBurnRequestAddressBytes(accAddress))
	}
}

func (k ExpKeeper) GetBurnRequestsByStatus(ctx sdk.Context, status int) (burnRequests types.BurnRequests) {
	k.IterateStatusBurnRequests(ctx, status, func(burnRequest types.BurnRequest) bool {
		burnRequests = append(burnRequests, burnRequest)
		return false
	})
	return
}

// not good logic need modify
func (k ExpKeeper) GetBurnRequest(ctx sdk.Context, accAddress sdk.AccAddress) (types.BurnRequest, error) {

	return k.GetBurnRequestByKey(ctx, types.GetBurnRequestAddressBytes(accAddress))
}

// IterateBurnRequest iterates over the all the BurnRequest and performs a callback function .
func (k ExpKeeper) IterateStatusBurnRequests(ctx sdk.Context, status int, cb func(burnRequest types.BurnRequest) (stop bool)) {
	store := ctx.KVStore(k.storeKey)

	iterator := sdk.KVStorePrefixIterator(store, types.KeyBurnRequestList)
	defer iterator.Close()
	for ; iterator.Valid(); iterator.Next() {
		var burnRequest types.BurnRequest
		err := k.cdc.Unmarshal(iterator.Value(), &burnRequest)
		if err != nil {
			panic(err)
		}

		if cb(burnRequest) {
			break
		}
	}
}

// not good logic need modify
func (k ExpKeeper) RemoveMintRequest(ctx sdk.Context, mintRequest types.MintRequest) {
	store := ctx.KVStore(k.storeKey)
	accAddress, _ := sdk.AccAddressFromBech32(mintRequest.Account)
	if store.Has(types.GetMintRequestAddressBytes(accAddress)) {
		store.Delete(types.GetMintRequestAddressBytes(accAddress))
	}
}

func (k ExpKeeper) SetMintRequest(ctx sdk.Context, mintRequest types.MintRequest) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&mintRequest)

	accAddress, err := sdk.AccAddressFromBech32(mintRequest.Account)
	if err != nil {
		panic(err)
	}
	store.Set(types.GetMintRequestAddressBytes(accAddress), bz)
}

func (k ExpKeeper) GetMintRequestsByStatus(ctx sdk.Context, status int) (mintRequests types.MintRequests) {
	k.IterateStatusMintRequests(ctx, status, func(mintRequest types.MintRequest) bool {
		mintRequests = append(mintRequests, mintRequest)
		return false
	})
	return
}

// GetAllMintRequest returns all the MintRequest from store .
func (keeper ExpKeeper) GetAllMintRequest(ctx sdk.Context) (mintRequests types.MintRequests) {
	keeper.IterateMintRequest(ctx, func(mintRequest types.MintRequest) bool {
		mintRequests = append(mintRequests, mintRequest)
		return false
	})
	return
}

func (k ExpKeeper) GetMintRequestByKey(ctx sdk.Context, key []byte) (types.MintRequest, error) {
	var mintRequest types.MintRequest

	store := ctx.KVStore(k.storeKey)
	if !store.Has(key) {
		return types.MintRequest{}, sdkerrors.Wrapf(types.ErrInvalidKey, "mintRequest")
	}

	bz := store.Get(key)
	err := k.cdc.Unmarshal(bz, &mintRequest)
	if err != nil {
		return types.MintRequest{}, err
	}

	return mintRequest, nil
}

// IterateMintRequest iterates over the all the MintRequest and performs a callback function .
func (k ExpKeeper) IterateMintRequest(ctx sdk.Context, cb func(mintRequest types.MintRequest) (stop bool)) {
	store := ctx.KVStore(k.storeKey)

	iterator := sdk.KVStorePrefixIterator(store, types.KeyMintRequestList)
	defer iterator.Close()
	for ; iterator.Valid(); iterator.Next() {
		var mintRequest types.MintRequest
		err := k.cdc.Unmarshal(iterator.Value(), &mintRequest)
		if err != nil {
			panic(err)
		}

		if cb(mintRequest) {
			break
		}
	}
}

// IterateStatusMintRequests iterates over the all the BurnRequest and performs a callback function .
func (k ExpKeeper) IterateStatusMintRequests(ctx sdk.Context, status int, cb func(mintRequest types.MintRequest) (stop bool)) {
	store := ctx.KVStore(k.storeKey)

	iterator := sdk.KVStorePrefixIterator(store, types.KeyMintRequestList)
	defer iterator.Close()
	for ; iterator.Valid(); iterator.Next() {
		var mintRequest types.MintRequest
		err := k.cdc.Unmarshal(iterator.Value(), &mintRequest)
		if err != nil {
			panic(err)
		}

		if cb(mintRequest) {
			break
		}
	}
}
