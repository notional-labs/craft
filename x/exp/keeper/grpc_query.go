package keeper

import (
	"context"

	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/types"
)

var _ types.QueryServer = ExpKeeper{}

// Params returns params of the mint module.
func (k ExpKeeper) WhiteList(c context.Context, _ *types.QueryWhiteListRequest) (*types.QueryWhiteListResponse, error) {
	ctx := sdk.UnwrapSDKContext(c)
	daoInfo, err := k.GetDaoInfo(ctx)
	if err != nil {
		return nil, err
	}
	return &types.QueryWhiteListResponse{AccountRecord: daoInfo.Whitelist}, nil
}
