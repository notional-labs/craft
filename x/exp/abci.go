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
	burnListRequest, err := keeper.GetBurnRequestList(ctx)
	if err != nil {
		return err
	}

	burnList := burnListRequest.BurnRequestList
	if len(burnList) == 0 {
		return nil
	}

	for i, burnRequest := range burnList {
		if !keeper.ValidateBurnRequestByTime(ctx, *burnRequest) {
			continue
		}

		b, _ := keeper.ExecuteBurnExp(ctx, *burnRequest)
		burnList[i] = &b
	}
	burnListRequest.BurnRequestList = burnList
	keeper.SetBurnRequestList(ctx, burnListRequest)

	return nil
}

func MintRequestListEndBlocker(ctx sdk.Context, keeper keeper.ExpKeeper) error {
	mintRequestList, err := keeper.GetMintRequestList(ctx)
	if err != nil {
		return nil
	}
	mintList := mintRequestList.MintRequestList
	if len(mintList) == 0 {
		return nil
	}

	for i, mintRequst := range mintList {
		if !keeper.ValidateMintRequestByTime(ctx, *mintRequst) {
			continue
		}

		m, _ := keeper.ExecuteMintExp(ctx, *mintRequst)
		mintList[i] = &m
	}
	mintRequestList.MintRequestList = mintList
	keeper.SetMintRequestList(ctx, mintRequestList)
	return nil
}
