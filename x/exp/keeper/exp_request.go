package keeper

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	"github.com/notional-labs/craft/x/exp/types"
)

func (k ExpKeeper) addAddressToBurnRequestList(ctx sdk.Context, memberAccount string, tokenLeft *sdk.Coin) error {
	burnRequestList, err := k.GetBurnRequestList(ctx)
	if err != nil {
		return err
	}

	burnRequestList.BurnRequestList = append(
		burnRequestList.BurnRequestList,
		&types.BurnRequest{
			Account:       memberAccount,
			BurnTokenLeft: tokenLeft,
			Status:        types.StatusOnGoingRequest,
		},
	)

	k.SetBurnRequestList(ctx, types.BurnRequestList{BurnRequestList: burnRequestList.BurnRequestList})

	return nil
}

func (k ExpKeeper) addAddressToMintRequestList(ctx sdk.Context, memberAccount sdk.AccAddress, tokenLeft sdk.Dec) error {
	mintRequestList, err := k.GetMintRequestList(ctx)
	if err != nil {
		return err
	}
	expPrice, err := k.calculateDaoTokenPrice(ctx)
	if err != nil {
		return err
	}

	stableTokenLeft := tokenLeft.Mul(expPrice)

	mintRequestList.MintRequestList = append(
		mintRequestList.MintRequestList,
		&types.MintRequest{
			Account:        memberAccount.String(),
			DaoTokenLeft:   tokenLeft,
			DaoTokenMinted: stableTokenLeft,
			Status:         types.StatusOnGoingRequest,
		},
	)

	k.SetMintRequestList(ctx, types.MintRequestList{MintRequestList: mintRequestList.MintRequestList})

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

func (k ExpKeeper) ExecuteBurnExp(ctx sdk.Context, burnRequest types.BurnRequest) (types.BurnRequest, error) {
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
		return burnRequest, nil
	}

	err = k.bankKeeper.SendCoinsFromModuleToAccount(ctx, types.ModuleName, burnAccount, sdk.NewCoins(coin))
	if err != nil {
		return burnRequest, nil
	}

	burnRequest.BurnTokenLeft = nil

	return burnRequest, nil
}

func (k ExpKeeper) ExecuteMintExp(ctx sdk.Context, mintRequest types.MintRequest) (types.MintRequest, error) {
	if mintRequest.DaoTokenMinted == sdk.NewDec(0) {
		mintRequest.Status = types.StatusNoFundRequest
		return mintRequest, nil
	}

	memberAccount, _ := sdk.AccAddressFromBech32(mintRequest.Account)
	maxToken := sdk.NewCoin(k.GetDenom(ctx), mintRequest.DaoTokenMinted.TruncateInt())

	err := k.addAddressToWhiteList(ctx, memberAccount, maxToken)
	if err != nil {
		return types.MintRequest{}, err
	}

	if mintRequest.Status == types.StatusOnGoingRequest {
		mintRequest.Status = types.StatusExpiredRequest
	}

	return mintRequest, nil
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
