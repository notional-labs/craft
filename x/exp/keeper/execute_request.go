package keeper

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/types"
)

func (k ExpKeeper) GetMintRequest(ctx sdk.Context, accAddress sdk.AccAddress) (types.MintRequest, error) {
	return k.GetMintRequestByKey(ctx, types.GetMintRequestAddressBytes(accAddress))
}

func (k ExpKeeper) ExecuteBurnExp(ctx sdk.Context, burnRequest types.BurnRequest) error {
	burnAccount, err := sdk.AccAddressFromBech32(burnRequest.Account)
	if err != nil {
		return err
	}

	if burnRequest.BurnTokenLeft.Amount == sdk.NewInt(0) {
		k.removeBurnRequest(ctx, burnRequest)
		burnRequest.Status = types.StatusCompleteRequest
		k.SetBurnRequest(ctx, burnRequest)
	}

	tokenReturn, _ := k.calculateStableTokenReturn(ctx, *burnRequest.BurnTokenLeft)

	coinWilReceive := sdk.NewCoin(k.GetIbcDenom(ctx), tokenReturn.TruncateInt())
	coinModule := k.bankKeeper.GetBalance(ctx, k.accountKeeper.GetModuleAccount(ctx, types.ModuleName).GetAddress(), k.GetIbcDenom(ctx))
	// if coin module don't have money return err .
	if coinModule.Amount == sdk.NewInt(0) {
		return nil
	}

	// logic when amount in exp module < amount need pay to member
	if coinWilReceive.IsGTE(coinModule) {
		err = k.bankKeeper.SendCoinsFromModuleToAccount(ctx, types.ModuleName, burnAccount, sdk.NewCoins(coinModule))
		if err != nil {
			return err
		}

		burnRequest.BurnTokenLeft.Amount = coinWilReceive.Amount.Sub(coinModule.Amount)
		return k.BurnExpFromAccount(ctx, sdk.NewCoins(coinModule), burnAccount)
	}

	err = k.bankKeeper.SendCoinsFromModuleToAccount(ctx, types.ModuleName, burnAccount, sdk.NewCoins(coinWilReceive))
	if err != nil {
		return err
	}

	err = k.BurnExpFromAccount(ctx, sdk.NewCoins(*burnRequest.BurnTokenLeft), burnAccount)
	if err != nil {
		return err
	}

	// set burn request state
	burnRequest.BurnTokenLeft = nil
	burnRequest.Status = types.StatusCompleteRequest

	k.completeBurnRequest(ctx, burnRequest)
	return nil
}

func (k ExpKeeper) ExecuteMintExp(ctx sdk.Context, mintRequest types.MintRequest) error {
	if mintRequest.DaoTokenMinted == sdk.NewDec(0) {
		mintRequest.Status = types.StatusNoFundRequest
		k.completeMintRequest(ctx, mintRequest)
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

	if mintRequest.DaoTokenLeft == sdk.NewDec(0) {
		mintRequest.Status = types.StatusCompleteRequest
	} else {
		mintRequest.Status = types.StatusExpiredRequest
	}

	k.completeMintRequest(ctx, mintRequest)
	return nil
}

// should modify .
func (k ExpKeeper) calculateStableTokenReturn(ctx sdk.Context, expCoin sdk.Coin) (sdk.Dec, error) {
	if expCoin.Denom != k.GetDenom(ctx) {
		return sdk.NewDec(0), types.ErrInputOutputMismatch
	}
	daoTokenPrice := k.GetDaoTokenPrice(ctx)
	return daoTokenPrice.MulInt(expCoin.Amount), nil
}

func (k ExpKeeper) ValidateBurnRequestByTime(ctx sdk.Context, burnRequest types.BurnRequest) bool {
	burnPeriod := k.GetBurnExpPeriod(ctx)
	return burnRequest.RequestTime.Add(burnPeriod).Before(ctx.BlockTime())
}

func (k ExpKeeper) ValidateMintRequestByTime(ctx sdk.Context, mintRequest types.MintRequest) bool {
	mintPeriod := k.GetBurnExpPeriod(ctx)
	return mintRequest.RequestTime.Add(mintPeriod).Before(ctx.BlockTime())
}
