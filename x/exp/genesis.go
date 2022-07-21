package exp

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"
)

// InitGenesis new exp genesis .
func InitGenesis(ctx sdk.Context, keeper keeper.ExpKeeper, ak types.AccountKeeper, data *types.GenesisState) {
	keeper.SetParams(ctx, data.Params)
	// set daoInfo
	for _, record := range data.WhiteList {
		accAddress, _ := sdk.AccAddressFromBech32(record.GetAccount())
		keeper.SetAccountRecord(ctx, accAddress, record)
	}

	// for IBC
	keeper.SetPort(ctx, data.PortId)
	// Only try to bind to port if it is not already bound, since we may already own
	// port capability from capability InitGenesis
	if !keeper.IsBound(ctx, data.PortId) {
		// module binds to the port on InitChain
		// and claims the returned capability
		err := keeper.BindPort(ctx, data.PortId)
		if err != nil {
			panic("could not claim port capability: " + err.Error())
		}
	}
	keeper.SetDaoAssetInfo(ctx, *data.DaoAsset)
	keeper.SetOracleID(ctx, 0)
	ak.GetModuleAccount(ctx, types.ModuleName)
}

// ExportGenesis returns a GenesisState for a given context and keeper.
func ExportGenesis(ctx sdk.Context, keeper keeper.ExpKeeper) *types.GenesisState {
	params := keeper.GetParams(ctx)
	whiteList := keeper.GetWhiteList(ctx)
	daoAssetInfo, _ := keeper.GetDaoAssetInfo(ctx)
	return types.NewGenesisState(whiteList, params, daoAssetInfo)
}
