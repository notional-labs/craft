package exp

import (
	"time"

	"github.com/cosmos/cosmos-sdk/telemetry"
	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"
)

// EndBlocker called every block, process inflation, update validator set.
func EndBlocker(ctx sdk.Context, keeper keeper.ExpKeeper) {
	defer telemetry.ModuleMeasureSince(types.ModuleName, time.Now(), telemetry.MetricKeyEndBlocker)

	err := BurnRequestListEndBlocker(ctx, keeper)
	if err != nil {
		panic(err)
	}

	err = MintRequestListEndBlocker(ctx, keeper)
	if err != nil {
		panic(err)
	}
}

func BurnRequestListEndBlocker(ctx sdk.Context, keeper keeper.ExpKeeper) error {
	burnList := keeper.GetBurnRequestsByStatus(ctx, int(types.StatusOnGoingRequest))
	if len(burnList) == 0 {
		return nil
	}

	for _, burnRequest := range burnList {
		if !keeper.ValidateBurnRequestByTime(ctx, burnRequest) {
			continue
		}

		err := keeper.ExecuteBurnExp(ctx, burnRequest)
		if err != nil {
			return err
		}
	}

	return nil
}

func MintRequestListEndBlocker(ctx sdk.Context, keeper keeper.ExpKeeper) error {
	mintListOnGoing := keeper.GetMintRequestsByStatus(ctx, int(types.StatusOnGoingRequest))
	if len(mintListOnGoing) == 0 {
		return nil
	}

	for _, mintRequst := range mintListOnGoing {
		if !keeper.ValidateMintRequestByTime(ctx, mintRequst) {
			continue
		}

		err := keeper.ExecuteMintExp(ctx, mintRequst)
		if err != nil {
			return err
		}
	}
	return nil
}
