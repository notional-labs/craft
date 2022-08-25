package keeper

import (
	"time"

	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/types"
)

// GetDenom get's the denom from the paramSpace .
func (k ExpKeeper) GetDenom(ctx sdk.Context) (denom string) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyDenom, &denom)
	return denom
}

// GetClosePoolPeriod get's the ClosePoolPeriod from the paramSpace .
func (k ExpKeeper) GetClosePoolPeriod(ctx sdk.Context) (duration time.Duration) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyClosePoolPeriod, &duration)
	return duration
}

// GetVestingPeriodEnd get's the VestingPeriodEnd from the paramSpace .
func (k ExpKeeper) GetVestingPeriodEnd(ctx sdk.Context) (duration time.Duration) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyVestingPeriodEnd, &duration)
	return duration
}

// GetBurnExpPeriod get's the BurnExpPeriod from the paramSpace .
func (k ExpKeeper) GetBurnExpPeriod(ctx sdk.Context) (duration time.Duration) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyBurnPeriod, &duration)
	return duration
}

// GetMintExpPeriod get's the BurnExpPeriod from the paramSpace .
func (k ExpKeeper) GetMintExpPeriod(ctx sdk.Context) (duration time.Duration) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyClosePoolPeriod, &duration)
	return duration
}

// GetParams gets the exp module's parameters.
func (k ExpKeeper) GetParams(ctx sdk.Context) (params types.Params) {
	k.paramSpace.GetParamSet(ctx, &params)
	return params
}

// SetParams sets the exp module's parameters.
func (k ExpKeeper) SetParams(ctx sdk.Context, params types.Params) {
	k.paramSpace.SetParamSet(ctx, &params)
}

// GetIbcDenom get ibc denom .
func (k ExpKeeper) GetIbcDenom(ctx sdk.Context) (denom string) {
	k.paramSpace.Get(ctx, types.ParamStoreIbcDenom, &denom)
	return denom
}

func (k ExpKeeper) GetScriptID(ctx sdk.Context) (scriptID uint64) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyScriptID, &scriptID)
	return scriptID
}

func (k ExpKeeper) GetAskCount(ctx sdk.Context) (askCount uint64) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyAskCount, &askCount)
	return askCount
}

func (k ExpKeeper) GetMinCount(ctx sdk.Context) (minCount uint64) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyMinCount, &minCount)
	return minCount
}

func (k ExpKeeper) GetFeeAmount(ctx sdk.Context) (feeAmount sdk.Coin) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyFeeAmount, &feeAmount)
	return feeAmount
}

func (k ExpKeeper) GetPrepareGas(ctx sdk.Context) (prepareGas uint64) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyPrepareGas, &prepareGas)
	return prepareGas
}

func (k ExpKeeper) GetExecuteGas(ctx sdk.Context) (executeGas uint64) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyExecuteGas, &executeGas)
	return executeGas
}
