package keeper_test

import (
	"encoding/base64"
	"encoding/binary"
	"strings"

	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/cosmos/cosmos-sdk/types/bech32"
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

	addr := genTestBech32List(1)
	addressRequest, _ := sdk.AccAddressFromBech32(addr[0])
	strExpPrice := sdk.NewDec(1).String()
	oracleID := suite.querier.GetNextOracleID(suite.Ctx)

	for _, tc := range []struct {
		desc string
		fn   func()
		err  error
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
				suite.querier.SetMintRequest(suite.Ctx, mintRequest)

				// Create oracle request
				clientID := oracleID
				coin := sdk.NewCoin("token", sdk.NewInt(1000000))
				oracleRequest := types.OracleRequest{
					OracleId:        clientID,
					Type:            "mint",
					AddressRequest:  addressRequest.String(),
					AmountInRequest: coin,
				}
				suite.querier.SetNextOracleRequest(suite.Ctx, oracleRequest)

			},
			err: nil,
		},
	} {
		tc := tc
		suite.Run(tc.desc, func() {
			suite.SetupTest()
			suite.FundAcc(addressRequest, defaultAcctFunds)
			tc.fn()

			err := suite.querier.ProccessRecvPacketMintRequest(suite.Ctx, addr[0], strExpPrice, oracleID)
			suite.Require().NoError(err)
		})

	}
}
