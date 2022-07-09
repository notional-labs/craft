package keeper_test

import (
	"fmt"
	"time"

	abci "github.com/tendermint/tendermint/abci/types"
	sdk "github.com/cosmos/cosmos-sdk/types"

	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"

	govtypes "github.com/cosmos/cosmos-sdk/x/gov/types"
)

var (
	defaultAcctFunds  sdk.Coins = sdk.NewCoins(
		sdk.NewCoin("token", sdk.NewInt(10000000)),
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
				
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					MaxToken: 1000000,
				}
				_, err := msgServer.JoinDaoByNonIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
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
				
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					MaxToken: 1000000,
				}
				_, err := msgServer.JoinDaoByNonIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().NoError(err)
				suite.Ctx = suite.Ctx.WithBlockTime(suite.Ctx.BlockTime().Add(time.Second))
				_, err = msgServer.JoinDaoByNonIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Invalid join address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: "",
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					MaxToken: 1000000,
				}
				_, err := msgServer.JoinDaoByNonIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Invalid gov address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgJoinDaoByNonIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.TestAccs[1].String(),
					MaxToken: 1000000,
				}
				_, err := msgServer.JoinDaoByNonIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
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
				
				req := types.MsgJoinDaoByIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					Amount: sdk.NewDec(1000000),
				}
				_, err := msgServer.JoinDaoByIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
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
				
				req := types.MsgJoinDaoByIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					Amount: sdk.NewDec(1000000),
				}
				_, err := msgServer.JoinDaoByIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().NoError(err)
				suite.Ctx = suite.Ctx.WithBlockTime(suite.Ctx.BlockTime().Add(time.Second))
				_, err = msgServer.JoinDaoByIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err) // should be err here
			},
		},

		// Invalid join address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgJoinDaoByIbcAsset{
					JoinAddress: "",
					GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
					Amount: sdk.NewDec(1000000),
				}
				_, err := msgServer.JoinDaoByIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Invalid gov address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgJoinDaoByIbcAsset{
					JoinAddress: suite.TestAccs[0].String(),
					GovAddress: suite.TestAccs[1].String(),
					Amount: sdk.NewDec(1000000),
				}
				_, err := msgServer.JoinDaoByIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
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
				
				balances := suite.App.BankKeeper.GetAccountsBalances(suite.Ctx)
				fmt.Println(balances)
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &req)
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
				
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[1].String(),
				}
				_, err := msgServer.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Only Dao address can excute mint msg
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: suite.TestAccs[1].String(),
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Amount must be < MaxAmount
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(10000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Only mint exp
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				

				// try to mint one token but not exp
				req := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uosmo", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)

				// try to mint more than one token but contain exp
				req = types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uosmo", sdk.NewInt(1000000)), sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err = msgServer.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &req)
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
		
		req := types.MsgJoinDaoByNonIbcAsset{
			JoinAddress: suite.TestAccs[0].String(),
			GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
			MaxToken: 1000000,
		}
		_, err := msgServer.JoinDaoByNonIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
		suite.Require().NoError(err)
		
		test.fn()
	}
}

func (suite *KeeperTestSuite) TestRequestBurnCoinAndExitDao() {
	tests := []struct {
		fn func()
	}{
		// expected case
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				// Mint before burn
				mintReq := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &mintReq)
				suite.Require().NoError(err)

				// Change block time after close period end
				accountRecord := suite.App.ExpKeeper.GetAccountRecord(suite.Ctx, suite.TestAccs[0])
				closePeriod := suite.App.ExpKeeper.GetClosePoolPeriod(suite.Ctx)
				requestTime := accountRecord.GetJoinDaoTime().Add(closePeriod).Add(time.Second)
				suite.Ctx = suite.Ctx.WithBlockTime(requestTime)

				req := types.MsgBurnAndRemoveMember{
					FromAddress: suite.TestAccs[0].String(),
					Metadata: "",
				}
				_, err = msgServer.RequestBurnCoinAndExitDao(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().NoError(err)
			},
		},

		// Must be member in whitelist
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgBurnAndRemoveMember{
					FromAddress: suite.TestAccs[1].String(),
					Metadata: "",
				}
				_, err := msgServer.RequestBurnCoinAndExitDao(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Only burn when exp token reaches max. In this case, we dont mint to member address => so err
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgBurnAndRemoveMember{
					FromAddress: suite.TestAccs[0].String(),
					Metadata: "",
				}
				_, err := msgServer.RequestBurnCoinAndExitDao(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Can not burn in vesting period
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				// Mint before burn
				mintReq := types.MsgMintAndAllocateExp{
					Amount: sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
					FromAddress: daoAddress,
					Member: suite.TestAccs[0].String(),
				}
				_, err := msgServer.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &mintReq)
				suite.Require().NoError(err)

				// Change block time
				accountRecord := suite.App.ExpKeeper.GetAccountRecord(suite.Ctx, suite.TestAccs[0])
				closePeriod := suite.App.ExpKeeper.GetClosePoolPeriod(suite.Ctx)
				timeCheck := accountRecord.GetJoinDaoTime().Add(closePeriod)
				suite.Require().Less(suite.Ctx.BlockTime(), timeCheck)

				req := types.MsgBurnAndRemoveMember{
					FromAddress: suite.TestAccs[0].String(),
					Metadata: "",
				}
				_, err = msgServer.RequestBurnCoinAndExitDao(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},
	}

	for _, test := range tests {
		suite.SetupTest()
		msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
		
		req := types.MsgJoinDaoByNonIbcAsset{
			JoinAddress: suite.TestAccs[0].String(),
			GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
			MaxToken: 1000000,
		}
		_, err := msgServer.JoinDaoByNonIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
		suite.Require().NoError(err)
		
		test.fn()
	}
}

func (suite *KeeperTestSuite) TestAdjustDaoPrice() {
	tests := []struct {
		fn func()
	}{
		// expected case
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgAdjustDaoTokenPrice{
					FromAddress: daoAddress,
					DaoTokenPrice: sdk.NewDec(2),
				}
				_, err := msgServer.AdjustDaoPrice(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().NoError(err)

				daoAssetPrice := suite.App.ExpKeeper.GetDaoTokenPrice(suite.Ctx)
				suite.Require().Equal(daoAssetPrice, sdk.NewDec(2))
			},
		},

		// Only execute by DAO address
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgAdjustDaoTokenPrice{
					FromAddress: suite.TestAccs[1].String(),
					DaoTokenPrice: sdk.NewDec(10),
				}
				_, err := msgServer.AdjustDaoPrice(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},

		// Should not set price to 0
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgAdjustDaoTokenPrice{
					FromAddress: daoAddress,
					DaoTokenPrice: sdk.NewDec(0),
				}
				_, err := msgServer.AdjustDaoPrice(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err) // should return err
			},
		},
	}

	for _, test := range tests {
		suite.SetupTest()		
		test.fn()
	}
}

func (suite *KeeperTestSuite) TestSpendIbcAssetToExp() {
	tests := []struct {
		fn func()
	}{
		// expected case
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
				req := types.MsgSpendIbcAssetToExp{
					FromAddress: suite.TestAccs[0].String(),
					Amount: sdk.NewCoins(sdk.NewCoin("token", sdk.NewInt(1000000))),
				}
				_, err := msgServer.SpendIbcAssetToExp(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().NoError(err)

				// check balance
				suite.App.EndBlock(abci.RequestEndBlock{Height: suite.Ctx.BlockHeight()})
				ibcBalance := suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "token")
				daoBalance := suite.App.BankKeeper.GetBalance(suite.Ctx, suite.TestAccs[0], "uexp")

				// init 10000000, spent 1000000
				suite.Require().Equal(ibcBalance.Amount, sdk.NewInt(9000000))
				
				daoBalanceExpected := suite.App.ExpKeeper.GetDaoTokenPrice(suite.Ctx).Mul(sdk.NewDec(1000000))
				suite.Require().Equal(daoBalanceExpected, daoBalance.Amount)
			},
		},

		// Only accept ibc denom
		{
			fn: func() {
				msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				req := types.MsgSpendIbcAssetToExp{
					FromAddress: suite.TestAccs[0].String(),
					Amount: sdk.NewCoins(sdk.NewCoin("ibc", sdk.NewInt(1000000))),
				}
				_, err := msgServer.SpendIbcAssetToExp(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)

				req = types.MsgSpendIbcAssetToExp{
					FromAddress: suite.TestAccs[0].String(),
					Amount: sdk.NewCoins(sdk.NewCoin("ibc", sdk.NewInt(1000000)), sdk.NewCoin("token", sdk.NewInt(1000000))),
				}
				_, err = msgServer.SpendIbcAssetToExp(sdk.WrapSDKContext(suite.Ctx), &req)
				suite.Require().Error(err)
			},
		},
	}

	for _, test := range tests {
		suite.SetupTest()

		for _, acc := range suite.TestAccs {
			suite.FundAcc(acc, defaultAcctFunds)
		}

		msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
				
		req := types.MsgJoinDaoByIbcAsset{
			JoinAddress: suite.TestAccs[0].String(),
			GovAddress: suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
			Amount: sdk.NewDec(1000000),
		}
		_, err := msgServer.JoinDaoByIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
		suite.Require().NoError(err)		
		test.fn()
	}
}

