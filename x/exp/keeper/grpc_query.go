package keeper

import (
	"context"

	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/types"
)

var _ types.QueryServer = ExpKeeper{}

// Params returns params of the exp module.
func (k ExpKeeper) WhiteList(c context.Context, _ *types.QueryWhiteListRequest) (*types.QueryWhiteListResponse, error) {
	ctx := sdk.UnwrapSDKContext(c)
	whiteList := k.GetWhiteList(ctx)

	return &types.QueryWhiteListResponse{AccountRecord: whiteList}, nil
}

func (k ExpKeeper) DaoAsset(c context.Context, _ *types.QueryDaoAssetRequest) (*types.QueryDaoAssetResponse, error) {
	ctx := sdk.UnwrapSDKContext(c)
	daoAssetInfo, err := k.GetDaoAssetInfo(ctx)
	if err != nil {
		return nil, err
	}
	return &types.QueryDaoAssetResponse{DaoAsset: &daoAssetInfo}, nil
}

func (k ExpKeeper) MintRequestList(c context.Context, _ *types.QueryMintRequestListRequest) (*types.QueryMintRequestListRequestResponse, error) {
	ctx := sdk.UnwrapSDKContext(c)
	mintRequestList := k.GetAllMintRequest(ctx)
	return &types.QueryMintRequestListRequestResponse{MintRequests: mintRequestList}, nil
}

func (k ExpKeeper) BurnRequestList(c context.Context, _ *types.QueryBurnRequestListRequest) (*types.QueryBurnRequestListRequestResponse, error) {
	ctx := sdk.UnwrapSDKContext(c)
	burnRequestList := k.GetAllBurnRequests(ctx)
	return &types.QueryBurnRequestListRequestResponse{BurnRequests: burnRequestList}, nil
}
