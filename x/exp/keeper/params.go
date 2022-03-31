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
func (k ExpKeeper) GetClosePoolPeriod(ctx sdk.Context) (denom string) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyClosePoolPeriod, &denom)
	return denom
}

// GetClosePoolPeriod get's the ClosePoolPeriod from the paramSpace .
func (k ExpKeeper) GetVestingPeriodEnd(ctx sdk.Context) (duration time.Duration) {
	k.paramSpace.Get(ctx, types.ParamStoreKeyVestingPeriodEnd, &duration)
	return denom
}

// GetParams gets the auth module's parameters.
func (k ExpKeeper) GetParams(ctx sdk.Context) (params types.Params) {
	k.paramSpace.GetParamSet(ctx, &params)
	return types.DefaultParams()
}

func (k ExpKeeper) SetParams(ctx sdk.Context, params types.Params) {
	k.paramSpace.SetParamSet(ctx, &params)
}
