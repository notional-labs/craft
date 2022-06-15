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
		// sdk.NewCoin("uexp", sdk.NewInt(10000000000)),
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
		// expected case
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					MaxToken: 1000000,
				}
				_, err := msgServer.JoinDaoByNonIbcAsset(c, &req)
				suite.Require().NoError(err)

				// check record
				accountRecord := suite.App.ExpKeeper.GetAccountRecord(suite.Ctx, suite.TestAccs[0])
				suite.Require().Equal(accountRecord.Account, suite.TestAccs[0].String())
				suite.Require().Equal(accountRecord.JoinDaoTime, suite.Ctx.BlockTime())
			},
		},

		// Each address can only be joined once
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					MaxToken: 1000000,
				}
				_, err := msgServer.JoinDaoByNonIbcAsset(c, &req)
				suite.Require().NoError(err)
				suite.Ctx = suite.Ctx.WithBlockTime(suite.Ctx.BlockTime().Add(time.Second))
				_, err = msgServer.JoinDaoByNonIbcAsset(c, &req)
				suite.Require().Error(err)
			},
		},

		// Invalid join address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: "",
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					MaxToken: 1000000,
				}
				_, err := msgServer.JoinDaoByNonIbcAsset(c, &req)
				suite.Require().Error(err)
			},
		},

		// Invalid gov address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.TestAccs[1].String(),
					MaxToken: 1000000,
				}
				_, err := msgServer.JoinDaoByNonIbcAsset(c, &req)
				suite.Require().Error(err)
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

func (suite *KeeperTestSuite) TestJoinDaoByIbcAsset() {
	tests := []struct {
		fn func()
	}{
		// expected case
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					Amount: sdk.NewDec(1000000),
				}
				_, err := msgServer.JoinDaoByIbcAsset(c, &req)
				suite.Require().NoError(err)

				// check record
				mintRequest, err := suite.App.ExpKeeper.GetMintRequest(suite.Ctx, suite.TestAccs[0])
				suite.Require().Equal(mintRequest.Account, suite.TestAccs[0].String())
				suite.Require().Equal(mintRequest.DaoTokenLeft, sdk.NewDec(1000000))
				suite.Require().Equal(mintRequest.DaoTokenMinted, sdk.NewDec(0))
				suite.Require().Equal(mintRequest.RequestTime, suite.Ctx.BlockTime())
				suite.Require().Equal(mintRequest.Status, types.StatusOnGoingRequest)
			},
		},

		// Each address can only be joined once
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					Amount: sdk.NewDec(1000000),
				}
				_, err := msgServer.JoinDaoByIbcAsset(c, &req)
				suite.Require().NoError(err)
				suite.Ctx = suite.Ctx.WithBlockTime(suite.Ctx.BlockTime().Add(time.Second))
				_, err = msgServer.JoinDaoByIbcAsset(c, &req)
				suite.Require().Error(err) // should be err here
			},
		},

		// Invalid join address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByIbcAsset{
					JoinAddress: "",
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					Amount: sdk.NewDec(1000000),
				}
				_, err := msgServer.JoinDaoByIbcAsset(c, &req)
				suite.Require().Error(err)
			},
		},

		// Invalid gov address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgJoinDaoByIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.TestAccs[1].String(),
					Amount: sdk.NewDec(1000000),
				}
				_, err := msgServer.JoinDaoByIbcAsset(c, &req)
				suite.Require().Error(err)
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

func (suite *KeeperTestSuite) TestMintAndAllocateExp() {
	tests := []struct {
		fn func()
	}{
		// expected case
		{
			fn: func() {
				balance := suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(0))

				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				balances := suite.App.BankKeeper.GetAccountsBalances(suite.Ctx)
				fmt.Println(balances)
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(c, &req)
				suite.Require().NoError(err)

				//check balances after mint executed
				balance = suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")
				suite.Require().Equal(balance.Amount, sdk.NewInt(1000000))
			},
		},

		// Only mint to member in whitelist
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[1].String(),
				}
				_, err := msgServer.MintAndAllocateExp(c, &req)
				suite.Require().Error(err)
			},
		},

		// Only Dao address can excute mint msg
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: suite.TestAccs[1].String(),
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(c, &req)
				suite.Require().Error(err)
			},
		},

		// Amount must be < MaxAmount
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(10000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(c, &req)
				suite.Require().Error(err)
			},
		},

		// Only mint exp
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				c := sdk.WrapSDKContext(suite.Ctx)

				// try to mint one token but not exp
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uosmo", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(c, &req)
				suite.Require().Error(err)

				// try to mint more than one token but contain exp
				req = types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uosmo", sdk.NewInt(1000000)), sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err = msgServer.MintAndAllocateExp(c, &req)
				suite.Require().Error(err)
			},
		},
	}

	for _, test := range tests {
		suite.SetupTest()
		// Mint some assets to the accounts.
		for _, acc := range suite.TestAccs {
			suite.FundAcc(acc, defaultAcctFunds)
		}
		msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
		c := sdk.WrapSDKContext(suite.Ctx)
		req := types.MsgJoinDaoByNonIbcAsset{
			JoinAddress: suite.TestAccs[0].String(),
			GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
			MaxToken: 1000000,
		}
		_, err := msgServer.JoinDaoByNonIbcAsset(c, &req)
		suite.Require().NoError(err)
		
		test.fn()
	}
}

