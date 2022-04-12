package exp

import (
	"fmt"

	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"
)

// InitGenesis new exp genesis .
func InitGenesis(ctx sdk.Context, keeper keeper.ExpKeeper, ak types.AccountKeeper, data *types.GenesisState) {
	keeper.SetParams(ctx, data.Params)
	keeper.SetDaoInfo(ctx, types.DaoInfo{Whitelist: data.WhiteList})
	fmt.Println(data.DaoAsset)
	keeper.SetDaoAssetInfo(ctx, *data.DaoAsset)
	ak.GetModuleAccount(ctx, types.ModuleName)
}

// ExportGenesis returns a GenesisState for a given context and keeper.
func ExportGenesis(ctx sdk.Context, keeper keeper.ExpKeeper) *types.GenesisState {
	params := keeper.GetParams(ctx)
	daoInfo, _ := keeper.GetDaoInfo(ctx)
	daoAssetInfo, _ := keeper.GetDaoAssetInfo(ctx)
	return types.NewGenesisState(daoInfo.Whitelist, params, daoAssetInfo)
}
