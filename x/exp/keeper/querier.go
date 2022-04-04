package keeper

import (
	abci "github.com/tendermint/tendermint/abci/types"

	"github.com/cosmos/cosmos-sdk/codec"
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	"github.com/notional-labs/craft/x/exp/types"
)

// NewQuerier returns a exp Querier handler.
func NewQuerier(k ExpKeeper, legacyQuerierCdc *codec.LegacyAmino) sdk.Querier {
	return func(ctx sdk.Context, path []string, _ abci.RequestQuery) ([]byte, error) {
		switch path[0] {
		case types.QueryParameters:
			return queryParams(ctx, k, legacyQuerierCdc)
		case types.QueryWhiteList:
			return queryWhiteList(ctx, k, legacyQuerierCdc)
		case types.QueryDaoTokenPrice:
			return queryDaoTokenPrice(ctx, k, legacyQuerierCdc)
		default:
			return nil, sdkerrors.Wrapf(sdkerrors.ErrUnknownRequest, "unknown query path: %s", path[0])
		}
	}
}

func queryParams(ctx sdk.Context, k ExpKeeper, legacyQuerierCdc *codec.LegacyAmino) ([]byte, error) {
	params := k.GetParams(ctx)

	res, err := codec.MarshalJSONIndent(legacyQuerierCdc, params)
	if err != nil {
		return nil, sdkerrors.Wrap(sdkerrors.ErrJSONMarshal, err.Error())
	}

	return res, nil
}

func queryWhiteList(ctx sdk.Context, k ExpKeeper, legacyQuerierCdc *codec.LegacyAmino) ([]byte, error) {
	daoInfo, err := k.GetDaoInfo(ctx)
	if err != nil {
		return nil, err
	}
	res, err := codec.MarshalJSONIndent(legacyQuerierCdc, daoInfo)
	if err != nil {
		return nil, sdkerrors.Wrap(sdkerrors.ErrJSONMarshal, err.Error())
	}

	return res, nil
}
