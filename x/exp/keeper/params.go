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

// GetParams gets the exp module's parameters.
func (k ExpKeeper) GetParams(ctx sdk.Context) (params types.Params) {
	k.paramSpace.GetParamSet(ctx, &params)
	return types.DefaultParams()
}

//SetParams sets the exp module's parameters.
func (k ExpKeeper) SetParams(ctx sdk.Context, params types.Params) {
	k.paramSpace.SetParamSet(ctx, &params)
}
