package keeper_test

import (
	"encoding/base64"
	"encoding/binary"
	"strings"

	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/cosmos/cosmos-sdk/types/bech32"
	govtypes "github.com/cosmos/cosmos-sdk/x/gov/types"
	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"
	oracletypes "github.com/notional-labs/craft/x/oracle"
)

func genTestBech32List(numAccount int) []string {
	bech32PrefixAccAddr := sdk.GetConfig().GetBech32AccountAddrPrefix()

	accounts := make([]string, numAccount)
	// valid AccAddress is 20 bytes - an uint64 is 8 bytes. We pad with 12 fixed characters
	prefix := "testAddress-"
	for i := 0; i < numAccount; i++ {
		val := make([]byte, 8)
		binary.LittleEndian.PutUint64(val, uint64(i))
		val = append([]byte(prefix), val...)
		addr, _ := bech32.ConvertAndEncode(bech32PrefixAccAddr, val)
		accounts[i] = addr
	}

	return accounts
}

func createResponsePacketData(
	clientID string,
	requestID uint64,
	status oracletypes.ResolveStatus,
	result string,
) oracletypes.OracleResponsePacketData {
	var resultBz []byte

	if strings.TrimSpace(result) != "" {
		bz, err := base64.StdEncoding.DecodeString(result)
		if err != nil {
			panic(err)
		}
		resultBz = bz
	}

	return oracletypes.OracleResponsePacketData{
		ClientID:      clientID,
		RequestID:     oracletypes.RequestID(requestID),
		AnsCount:      1,
		RequestTime:   1,
		ResolveTime:   1,
		ResolveStatus: status,
		Result:        resultBz,
	}
}

func (suite *KeeperTestSuite) TestOnOracleRequestTimeoutPacket() {
	for _, tc := range []struct {
		desc string
		req  oracletypes.OracleRequestPacketData
		err  error
	}{
		{
			desc: "nil",
			req:  oracletypes.OracleRequestPacketData{},
			err:  nil,
		},
	} {
		tc := tc
		suite.Run(tc.desc, func() {
			err := suite.querier.OnOracleRequestTimeoutPacket(suite.Ctx, tc.req)
			if tc.err != nil {
				suite.Require().ErrorIs(err, tc.err)
			} else {
				suite.Require().Nil(err)
			}
		})
	}
}

func (suite *KeeperTestSuite) TestProccessRecvPacketMintRequest() {

	// type resultData struct {
	// 	ExpPrice       string `obi:"exp_price"`
	// 	AddressRequest string `obi:"address_request"`
	// 	RequestType    string `obi:"request_type"`
	// 	Status         string `obi:"status"`
	// }

	addr := genTestBech32List(2)
	addressRequest, _ := sdk.AccAddressFromBech32(addr[0])
	strExpPrice := sdk.NewDec(1).String()
	oracleID := suite.App.ExpKeeper.GetNextOracleID(suite.Ctx)

	for _, tc := range []struct {
		desc      string
		fn        func()
		shouldErr bool
	}{
		{
			desc: "Success",
			fn: func() {
				// Create mint request
				mintRequest := types.MintRequest{
					Account:        addr[0],
					DaoTokenLeft:   sdk.NewDec(1000000),
					DaoTokenMinted: sdk.NewDec(0),
					Status:         types.StatusOnGoingRequest,
					RequestTime:    suite.Ctx.BlockHeader().Time,
				}
				suite.App.ExpKeeper.SetMintRequest(suite.Ctx, mintRequest)

				// Create oracle request
				clientID := oracleID
				coin := sdk.NewCoin("token", sdk.NewInt(1000000))
				oracleRequest := types.OracleRequest{
					OracleId:        clientID,
					Type:            "mint",
					AddressRequest:  addressRequest.String(),
					AmountInRequest: coin,
				}
				suite.App.ExpKeeper.SetNextOracleRequest(suite.Ctx, oracleRequest)

			},
			shouldErr: false,
		},
		{
			desc: "Invalid Address",
			fn: func() {
				// Create mint request
				mintRequest := types.MintRequest{
					Account:        addr[1],
					DaoTokenLeft:   sdk.NewDec(1000000),
					DaoTokenMinted: sdk.NewDec(0),
					Status:         types.StatusOnGoingRequest,
					RequestTime:    suite.Ctx.BlockHeader().Time,
				}
				suite.App.ExpKeeper.SetMintRequest(suite.Ctx, mintRequest)

				// Create oracle request
				clientID := oracleID
				coin := sdk.NewCoin("token", sdk.NewInt(1000000))
				oracleRequest := types.OracleRequest{
					OracleId:        clientID,
					Type:            "mint",
					AddressRequest:  addressRequest.String(),
					AmountInRequest: coin,
				}
				suite.App.ExpKeeper.SetNextOracleRequest(suite.Ctx, oracleRequest)
			},
			shouldErr: true,
		},
		{
			desc: "Token Amount Not Enough",
			fn: func() {
				// Create mint request
				mintRequest := types.MintRequest{
					Account:        addr[1],
					DaoTokenLeft:   sdk.NewDec(100000000),
					DaoTokenMinted: sdk.NewDec(0),
					Status:         types.StatusOnGoingRequest,
					RequestTime:    suite.Ctx.BlockHeader().Time,
				}
				suite.App.ExpKeeper.SetMintRequest(suite.Ctx, mintRequest)

				// Create oracle request
				clientID := oracleID
				coin := sdk.NewCoin("token", sdk.NewInt(1000000))
				oracleRequest := types.OracleRequest{
					OracleId:        clientID,
					Type:            "mint",
					AddressRequest:  addressRequest.String(),
					AmountInRequest: coin,
				}
				suite.App.ExpKeeper.SetNextOracleRequest(suite.Ctx, oracleRequest)
			},
			shouldErr: true,
		},
		{
			desc: "Ibc Assets Denom",
			fn: func() {
				// Create mint request
				mintRequest := types.MintRequest{
					Account:        addr[1],
					DaoTokenLeft:   sdk.NewDec(1000000),
					DaoTokenMinted: sdk.NewDec(0),
					Status:         types.StatusOnGoingRequest,
					RequestTime:    suite.Ctx.BlockHeader().Time,
				}
				suite.App.ExpKeeper.SetMintRequest(suite.Ctx, mintRequest)

				// Create oracle request
				clientID := oracleID
				coin := sdk.NewCoin("ibc", sdk.NewInt(1000000))
				oracleRequest := types.OracleRequest{
					OracleId:        clientID,
					Type:            "mint",
					AddressRequest:  addressRequest.String(),
					AmountInRequest: coin,
				}
				suite.App.ExpKeeper.SetNextOracleRequest(suite.Ctx, oracleRequest)
			},
			shouldErr: true,
		},
	} {
		tc := tc
		suite.Run(tc.desc, func() {
			suite.SetupTest()
			suite.FundAcc(addressRequest, defaultAcctFunds)
			tc.fn()
			if tc.shouldErr {
				err := suite.App.ExpKeeper.ProccessRecvPacketMintRequest(suite.Ctx, addr[0], strExpPrice, oracleID)
				suite.Require().Error(err)
			} else {
				err := suite.App.ExpKeeper.ProccessRecvPacketMintRequest(suite.Ctx, addr[0], strExpPrice, oracleID)
				suite.Require().NoError(err)
			}
		})

	}
}

func (suite *KeeperTestSuite) TestProccessRecvPacketBurnRequest() {

	addr := genTestBech32List(2)
	addressRequest, _ := sdk.AccAddressFromBech32(addr[0])
	strExpPrice := sdk.NewDec(1).String()
	oracleID := suite.App.ExpKeeper.GetNextOracleID(suite.Ctx)

	for _, tc := range []struct {
		desc      string
		fn        func()
		shouldErr bool
	}{
		{
			desc: "Success",
			fn: func() {
				oracleRequest := types.OracleRequest{
					OracleId:        oracleID,
					Type:            "burn",
					AddressRequest:  addressRequest.String(),
					AmountInRequest: sdk.NewCoin("uexp", sdk.NewInt(1000000)),
				}
				suite.App.ExpKeeper.SetBurnRequestOracle(suite.Ctx, oracleRequest)
				suite.App.ExpKeeper.SetNextOracleRequest(suite.Ctx, oracleRequest)

			},
			shouldErr: false,
		},
	} {
		tc := tc
		suite.Run(tc.desc, func() {
			suite.SetupTest()
			tc.fn()

			msgServer := keeper.NewMsgServerImpl(suite.App.ExpKeeper)

			req := types.MsgJoinDaoByNonIbcAsset{
				JoinAddress: addressRequest.String(),
				GovAddress:  suite.App.AccountKeeper.GetModuleAccount(suite.Ctx, govtypes.ModuleName).GetAddress().String(),
				MaxToken:    1000000,
			}
			_, err := msgServer.JoinDaoByNonIbcAsset(sdk.WrapSDKContext(suite.Ctx), &req)
			suite.Require().NoError(err)

			// Mint before burn
			msgServerMint := keeper.NewMsgServerImpl(suite.App.ExpKeeper)
			mintReq := types.MsgMintAndAllocateExp{
				Amount:      sdk.NewCoins(sdk.NewCoin("uexp", sdk.NewInt(1000000))),
				FromAddress: daoAddress,
				Member:      addressRequest.String(),
			}
			_, err = msgServerMint.MintAndAllocateExp(sdk.WrapSDKContext(suite.Ctx), &mintReq)
			suite.Require().NoError(err)

			if tc.shouldErr {
				suite.Require().True(true)
			} else {
				err := suite.App.ExpKeeper.ProccessRecvPacketBurnRequest(suite.Ctx, addr[0], strExpPrice, oracleID)
				suite.Require().NoError(err)
			}
		})

	}
}
