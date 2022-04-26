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
func (k ExpKeeper) calculateDaoTokenPrice(ctx sdk.Context) (sdk.Dec, error) {
	asset, err := k.GetDaoAssetInfo(ctx)
	if err != nil {
		return sdk.NewDec(-1), err
	}

	return asset.DaoTokenPrice, nil
}

// calculate exp value by ibc asset .
func (k ExpKeeper) calculateDaoTokenValue(ctx sdk.Context, amount sdk.Int) sdk.Dec {
	daoTokenPrice, _ := k.calculateDaoTokenPrice(ctx)

	return daoTokenPrice.MulInt(amount)
}

func (k ExpKeeper) SetBurnRequest(ctx sdk.Context, burnRequest types.BurnRequest) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&burnRequest)

	accAddress, err := sdk.AccAddressFromBech32(burnRequest.Account)
	if err != nil {
		panic(err)
	}
	store.Set(types.GetBurnRequestAddressBytes(int(burnRequest.Status), accAddress), bz)
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
func (k ExpKeeper) RemoveBurnRequest(ctx sdk.Context, accAddress sdk.AccAddress) {
	store := ctx.KVStore(k.storeKey)

	if store.Has(types.GetBurnRequestAddressBytes(int(types.StatusCompleteRequest), accAddress)) {
		store.Delete(types.GetBurnRequestAddressBytes(int(types.StatusCompleteRequest), accAddress))
	}

	if store.Has(types.GetBurnRequestAddressBytes(int(types.StatusNoFundRequest), accAddress)) {
		store.Delete(types.GetBurnRequestAddressBytes(int(types.StatusNoFundRequest), accAddress))
	}
	if store.Has(types.GetBurnRequestAddressBytes(int(types.StatusOnGoingRequest), accAddress)) {
		store.Delete(types.GetBurnRequestAddressBytes(int(types.StatusOnGoingRequest), accAddress))
	}

	if store.Has(types.GetBurnRequestAddressBytes(int(types.StatusExpiredRequest), accAddress)) {
		store.Delete(types.GetBurnRequestAddressBytes(int(types.StatusExpiredRequest), accAddress))
	}
}

// not good logic need modify
func (k ExpKeeper) GetBurnRequest(ctx sdk.Context, accAddress sdk.AccAddress) (types.BurnRequest, error) {
	store := ctx.KVStore(k.storeKey)

	if store.Has(types.GetBurnRequestAddressBytes(int(types.StatusCompleteRequest), accAddress)) {
		return k.GetBurnRequestByKey(ctx, types.GetBurnRequestAddressBytes(int(types.StatusCompleteRequest), accAddress))
	}

	if store.Has(types.GetBurnRequestAddressBytes(int(types.StatusNoFundRequest), accAddress)) {
		return k.GetBurnRequestByKey(ctx, types.GetBurnRequestAddressBytes(int(types.StatusNoFundRequest), accAddress))
	}
	if store.Has(types.GetBurnRequestAddressBytes(int(types.StatusOnGoingRequest), accAddress)) {
		return k.GetBurnRequestByKey(ctx, types.GetBurnRequestAddressBytes(int(types.StatusOnGoingRequest), accAddress))
	}

	if store.Has(types.GetBurnRequestAddressBytes(int(types.StatusExpiredRequest), accAddress)) {
		return k.GetBurnRequestByKey(ctx, types.GetBurnRequestAddressBytes(int(types.StatusExpiredRequest), accAddress))
	}

	return types.BurnRequest{}, sdkerrors.Wrapf(types.ErrInvalidKey, "burnRequest")
}

// IterateBurnRequest iterates over the all the BurnRequest and performs a callback function .
func (k ExpKeeper) IterateStatusBurnRequests(ctx sdk.Context, status int, cb func(burnRequest types.BurnRequest) (stop bool)) {
	store := ctx.KVStore(k.storeKey)

	iterator := sdk.KVStorePrefixIterator(store, types.GetBurnRequestsStatusBytes(status))
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
func (k ExpKeeper) RemoveMintRequest(ctx sdk.Context, accAddress sdk.AccAddress) {
	store := ctx.KVStore(k.storeKey)

	if store.Has(types.GetMintRequestAddressBytes(int(types.StatusCompleteRequest), accAddress)) {
		store.Delete(types.GetMintRequestAddressBytes(int(types.StatusCompleteRequest), accAddress))
	}

	if store.Has(types.GetMintRequestAddressBytes(int(types.StatusNoFundRequest), accAddress)) {
		store.Delete(types.GetMintRequestAddressBytes(int(types.StatusNoFundRequest), accAddress))
	}

	if store.Has(types.GetMintRequestAddressBytes(int(types.StatusOnGoingRequest), accAddress)) {
		store.Delete(types.GetMintRequestAddressBytes(int(types.StatusOnGoingRequest), accAddress))
	}

	if store.Has(types.GetMintRequestAddressBytes(int(types.StatusExpiredRequest), accAddress)) {
		store.Delete(types.GetMintRequestAddressBytes(int(types.StatusExpiredRequest), accAddress))
	}
}

func (k ExpKeeper) SetMintRequest(ctx sdk.Context, mintRequest types.MintRequest) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&mintRequest)

	accAddress, err := sdk.AccAddressFromBech32(mintRequest.Account)
	if err != nil {
		panic(err)
	}
	store.Set(types.GetMintRequestAddressBytes(int(mintRequest.Status), accAddress), bz)
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

	iterator := sdk.KVStorePrefixIterator(store, types.GetMintRequestsStatusBytes(status))
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

// not good logic need modify
func (k ExpKeeper) GetMintRequest(ctx sdk.Context, accAddress sdk.AccAddress) (types.MintRequest, error) {
	store := ctx.KVStore(k.storeKey)

	if store.Has(types.GetMintRequestAddressBytes(int(types.StatusCompleteRequest), accAddress)) {
		return k.GetMintRequestByKey(ctx, types.GetMintRequestAddressBytes(int(types.StatusCompleteRequest), accAddress))
	}

	if store.Has(types.GetMintRequestAddressBytes(int(types.StatusNoFundRequest), accAddress)) {
		return k.GetMintRequestByKey(ctx, types.GetMintRequestAddressBytes(int(types.StatusNoFundRequest), accAddress))
	}

	if store.Has(types.GetMintRequestAddressBytes(int(types.StatusOnGoingRequest), accAddress)) {
		return k.GetMintRequestByKey(ctx, types.GetMintRequestAddressBytes(int(types.StatusOnGoingRequest), accAddress))
	}

	if store.Has(types.GetMintRequestAddressBytes(int(types.StatusExpiredRequest), accAddress)) {
		return k.GetMintRequestByKey(ctx, types.GetMintRequestAddressBytes(int(types.StatusExpiredRequest), accAddress))
	}

	return types.MintRequest{}, sdkerrors.Wrapf(types.ErrInvalidKey, "mintRequest")
}

func (k ExpKeeper) GetMintRequestList(ctx sdk.Context) (types.MintRequestList, error) {
	var mintRequestList types.MintRequestList

	store := ctx.KVStore(k.storeKey)
	if !store.Has(types.KeyMintRequestList) {
		return types.MintRequestList{}, sdkerrors.Wrapf(types.ErrInvalidKey, "mintRequest")
	}

	bz := store.Get(types.KeyMintRequestList)
	err := k.cdc.Unmarshal(bz, &mintRequestList)
	if err != nil {
		return types.MintRequestList{}, err
	}

	return mintRequestList, nil
}

func (k ExpKeeper) SetMintRequestList(ctx sdk.Context, mintRequestList types.MintRequestList) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&mintRequestList)
	store.Set(types.KeyMintRequestList, bz)
}

func (k ExpKeeper) GetBurnRequestList(ctx sdk.Context) (types.BurnRequestList, error) {
	var burnRequestList types.BurnRequestList

	store := ctx.KVStore(k.storeKey)
	if !store.Has(types.KeyBurnRequestList) {
		return types.BurnRequestList{}, sdkerrors.Wrapf(types.ErrInvalidKey, "burnRequest")
	}

	bz := store.Get(types.KeyMintRequestList)
	err := k.cdc.Unmarshal(bz, &burnRequestList)
	if err != nil {
		return types.BurnRequestList{}, err
	}

	return burnRequestList, nil
}

func (k ExpKeeper) SetBurnRequestList(ctx sdk.Context, burnRequestList types.BurnRequestList) {
	store := ctx.KVStore(k.storeKey)
	bz := k.cdc.MustMarshal(&burnRequestList)
	store.Set(types.KeyBurnRequestList, bz)
}

func (k ExpKeeper) ExecuteBurnExp(ctx sdk.Context, burnRequest *types.BurnRequest) (*types.BurnRequest, error) {
	burnAccount, err := sdk.AccAddressFromBech32(burnRequest.Account)
	if err != nil {
		return burnRequest, err
	}

	tokenReturn, _ := k.calculateStableTokenReturn(ctx, *burnRequest.BurnTokenLeft)

	coin := sdk.NewCoin(k.GetIbcDenom(ctx), tokenReturn.TruncateInt())
	coinModule := k.bankKeeper.GetBalance(ctx, k.accountKeeper.GetModuleAccount(ctx, types.ModuleName).GetAddress(), k.GetDenom(ctx))

	if coin.IsGTE(coinModule) {
		err = k.bankKeeper.SendCoinsFromModuleToAccount(ctx, types.ModuleName, burnAccount, sdk.NewCoins(coinModule))
		if err != nil {
			return burnRequest, err
		}
		burnRequest.BurnTokenLeft.Amount = coin.SubAmount(coin.Amount).Amount
		k.BurnExpFromAccount(ctx, sdk.NewCoins(coin), burnAccount)
		return burnRequest, nil
	}

	err = k.bankKeeper.SendCoinsFromModuleToAccount(ctx, types.ModuleName, burnAccount, sdk.NewCoins(coin))
	if err != nil {
		return burnRequest, nil
	}
	k.BurnExpFromAccount(ctx, sdk.NewCoins(*burnRequest.BurnTokenLeft), burnAccount)
	burnRequest.BurnTokenLeft = nil

	k.RemoveBurnRequest(ctx, burnAccount)
	k.SetBurnRequest(ctx, *burnRequest)
	return burnRequest, nil
}

func (k ExpKeeper) ExecuteMintExp(ctx sdk.Context, mintRequest types.MintRequest) error {
	if mintRequest.DaoTokenMinted == sdk.NewDec(0) {
		mintRequest.Status = types.StatusNoFundRequest
		return nil
	}

	memberAccount, _ := sdk.AccAddressFromBech32(mintRequest.Account)
	maxToken := sdk.NewCoin(k.GetDenom(ctx), mintRequest.DaoTokenMinted.TruncateInt())

	err := k.addAddressToWhiteList(ctx, memberAccount, maxToken)
	if err != nil {
		return err
	}

	err = k.MintExpForAccount(ctx, sdk.NewCoins(maxToken), memberAccount)
	if err != nil {
		return err
	}

	mintRequest.Status = types.StatusExpiredRequest
	k.RemoveMintRequest(ctx, memberAccount)
	k.SetMintRequest(ctx, mintRequest)
	return nil
}

// should modify .
func (k ExpKeeper) calculateStableTokenReturn(ctx sdk.Context, expCoin sdk.Coin) (sdk.Dec, error) {
	if expCoin.Denom != k.GetDenom(ctx) {
		return sdk.NewDec(0), types.ErrInputOutputMismatch
	}
	daoTokenPrice, _ := k.calculateDaoTokenPrice(ctx)
	return daoTokenPrice.AddMut(expCoin.Amount.ToDec()), nil
}

func (k ExpKeeper) ValidateBurnRequestByTime(ctx sdk.Context, burnRequest types.BurnRequest) bool {
	burnPeriod := k.GetBurnExpPeriod(ctx)
	return burnRequest.RequestTime.Add(burnPeriod).Before(ctx.BlockTime())
}

func (k ExpKeeper) ValidateMintRequestByTime(ctx sdk.Context, mintRequest types.MintRequest) bool {
	mintPeriod := k.GetBurnExpPeriod(ctx)
	return mintRequest.RequestTime.Add(mintPeriod).Before(ctx.BlockTime())
}
