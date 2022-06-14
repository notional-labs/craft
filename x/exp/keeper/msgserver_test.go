package keeper_test

import (
	"fmt"

	sdk "github.com/cosmos/cosmos-sdk/types"

	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"

	govtypes "github.com/cosmos/cosmos-sdk/x/gov/types"
)

var (
	defaultAcctFunds  sdk.Coins = sdk.NewCoins(
		sdk.NewCoin("uexp", sdk.NewInt(10000000000)),
		sdk.NewCoin("foo", sdk.NewInt(10000000)),
		sdk.NewCoin("bar", sdk.NewInt(10000000)),
		sdk.NewCoin("baz", sdk.NewInt(10000000)),
	)
	daoAddress = "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
)

func (suite *KeeperTestSuite) TestJoinDaoByNonIbcAsset() {
	tests := []struct {
		fn func()
	}{
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					MaxToken: 1000000,
				}
				resp, err := msgServer.JoinDaoByNonIbcAsset(c, &req)
				suite.Require().NoError(err)
				fmt.Println(resp)
			},
		},
	}

	for _, test := range tests {
		suite.SetupTest()
		// Mint some assets to the accounts.
		for _, acc := range suite.TestAccs {
			suite.FundAcc(acc, defaultAcctFunds)
		}
		

		test.fn()
	}
}
