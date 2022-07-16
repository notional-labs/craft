package exp_test

import (
	"testing"

	sdk "github.com/cosmos/cosmos-sdk/types"

	govtypes "github.com/cosmos/cosmos-sdk/x/gov/types"

	"github.com/notional-labs/craft/app/apptesting"
	"github.com/notional-labs/craft/x/exp"
	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"
	"github.com/stretchr/testify/suite"
)

var (
	defaultAcctFunds sdk.Coins = sdk.NewCoins(
		sdk.NewCoin("token", sdk.NewInt(10000000)),
	)
	daoAddress = "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
)

type AbciTestSuite struct {
	apptesting.KeeperTestHelper
}

func (suite *AbciTestSuite) SetupTest() {
	suite.Setup()
}

func TestEndblockTestSuite(t *testing.T) {
	suite.Run(t, new(AbciTestSuite))
}

func (suite *AbciTestSuite) TestBurnRequestListEndBlocker() {
	testCases := []struct {
		name string
		fn   func()
	}{
		{
			name: "expected request (Module Balance > Burn request)",
			fn: func() {
				// check balances
				balance := suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(0))
				// Mint token exp
				err := suite.App.ExpKeeper.MintExpForAccount(suite.Ctx, sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(100000))), suite.TestAccs[0])
				suite.Require().NoError(err)
				balance = suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(100000))
				// Check balance after mint
				balance = suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(100000))
				// Send burn request
				account := suite.TestAccs[0].String()
				tokenLeft := sdk.NewCoin("uexp", sdk.NewInt(100000))
				burnRequest := types.BurnRequest{
					Account:       account,
					BurnTokenLeft: &tokenLeft,
					RequestTime:   suite.Ctx.BlockTime().Add(-suite.App.ExpKeeper.GetBurnExpPeriod(suite.Ctx)),
					Status:        types.StatusOnGoingRequest,
				}

				suite.App.ExpKeeper.SetBurnRequest(suite.Ctx, burnRequest)
				err = exp.BurnRequestListEndBlocker(suite.Ctx, suite.App.ExpKeeper)
				suite.Require().NoError(err)
				// check balances
				balance = suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(0))
			},
		},
		{
			name: "expected request (Module Balance < Burn request)",
			fn: func() {
				// check balances
				balance := suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(0))
				// Mint token exp
				err := suite.App.ExpKeeper.MintExpForAccount(suite.Ctx, sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(10000000))), suite.TestAccs[0])
				suite.Require().NoError(err)
				balance = suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(10000000))
				// Check balance after mint
				// Send burn request
				account := suite.TestAccs[0].String()
				tokenLeft := sdk.NewCoin("uexp", sdk.NewInt(10000000))
				burnRequest := types.BurnRequest{
					Account:       account,
					BurnTokenLeft: &tokenLeft,
					RequestTime:   suite.Ctx.BlockTime().Add(-suite.App.ExpKeeper.GetBurnExpPeriod(suite.Ctx)),
					Status:        types.StatusOnGoingRequest,
				}

				suite.App.ExpKeeper.SetBurnRequest(suite.Ctx, burnRequest)
				err = exp.BurnRequestListEndBlocker(suite.Ctx, suite.App.ExpKeeper)
				suite.Require().NoError(err)
				// check balances
				balance = suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(5000000))
			},
		},
	}

	for _, tc := range testCases {
		suite.SetupTest()
		// Mint some assets to account
		for _, acc := range suite.TestAccs {
			suite.FundAcc(acc, defaultAcctFunds)
		}

		msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)

		req := types.MsgJoinDaoByIbcAsset{
			JoinAddress: suite.TestAccs[0].String(),
			GovAddress:  suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
			Amount:      sdk.NewDec(100000),
		}
		_, err := msgServer.JoinDaoByIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
		suite.Require().NoError(err)

		reqFund := types.MsgFundExpPool{
			FromAddress: suite.TestAccs[0].String(),
			Amount:      sdk.NewCoins(sdk.NewCoin("token", sdk.NewInt(5000000))),
		}
		_, err = msgServer.FundExpPool(sdk.WrapSDKContext(suite.Ctx), &reqFund)
		suite.Require().NoError(err)
		tc.fn()
	}
}
