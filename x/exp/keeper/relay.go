package keeper

import (
	"strconv"

	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	clienttypes "github.com/cosmos/ibc-go/v4/modules/core/02-client/types"
	"github.com/notional-labs/craft/x/exp/types"
	oracletypes "github.com/notional-labs/craft/x/oracle"
)

// OnOracleRequestTimeoutPacket handles the OracleRequestPacketData instance that is sent when a request times out.
func (k ExpKeeper) OnOracleRequestTimeoutPacket(
	ctx sdk.Context,
	data oracletypes.OracleRequestPacketData,
) error {
	// TODO: need implement
	return nil
}

func (k ExpKeeper) ProccessRecvPacketMintRequest(ctx sdk.Context, addressRequest string, strExpPrice string, oracleID uint64) error {

	accAddress, err := sdk.AccAddressFromBech32(addressRequest)
	if err != nil {
		return err
	}

	// set price to state
	price, err := strconv.ParseInt(strExpPrice, 10, 64)
	if err != nil {
		return err
	}

	k.setDaoTokenPrice(ctx, sdk.NewDec(price))

	mintRequest, found := k.GetMintRequest(ctx, accAddress)

	if !found {
		return types.ErrAddressdNotFound
	}
	// verify time
	if !k.ValidateMintRequestByTime(ctx, mintRequest) {
		return types.ErrTimeOut
	}

	oracleRequest := k.GetOracleRequest(ctx, oracleID)
	err = k.ExecuteMintExpByIbcToken(ctx, mintRequest, oracleRequest.AmountInRequest)
	if err != nil {
		return err
	}
	return nil
}

func (k ExpKeeper) ProccessRecvPacketBurnRequest(ctx sdk.Context, addressRequest string, strExpPrice string, oracleID uint64) error {
	accAddress, err := sdk.AccAddressFromBech32(addressRequest)
	if err != nil {
		return err
	}

	// set price to state
	price, err := strconv.ParseInt(strExpPrice, 10, 64)
	if err != nil {
		return err
	}

	k.setDaoTokenPrice(ctx, sdk.NewDec(price))

	burnRequest, err := k.GetBurnRequest(ctx, accAddress)
	if err != nil {
		return err
	}

	err = k.ExecuteBurnExp(ctx, burnRequest)
	if err != nil {
		return err
	}
	k.RemoveBurnRequestOracle(ctx, addressRequest)
	return nil
}

// ExecuteMintExpByIbcToken only run in OnPacketRecv
func (k ExpKeeper) ExecuteMintExpByIbcToken(ctx sdk.Context, mintRequest types.MintRequest, coin sdk.Coin) error {
	expWillGet := k.calculateDaoTokenValue(ctx, coin.Amount)

	if expWillGet.GTE(mintRequest.DaoTokenLeft) {
		coinSpend := sdk.NewCoin(k.GetIbcDenom(ctx), mintRequest.DaoTokenLeft.TruncateInt())

		err := k.FundPoolForExp(ctx, sdk.NewCoins(coinSpend), sdk.AccAddress(mintRequest.Account))
		if err != nil {
			return err
		}

		mintRequest.DaoTokenLeft = sdk.NewDec(0)
		mintRequest.DaoTokenMinted = mintRequest.DaoTokenLeft.Add(mintRequest.DaoTokenMinted)

		k.SetMintRequest(ctx, mintRequest)
	}
	err := k.FundPoolForExp(ctx, sdk.NewCoins(coin), sdk.AccAddress(mintRequest.Account))
	if err != nil {
		return sdkerrors.Wrap(err, "fund error")
	}
	k.removeMintRequest(ctx, mintRequest)
	decCoin := sdk.NewDecFromInt(coin.Amount)

	mintRequest.DaoTokenMinted = mintRequest.DaoTokenMinted.Add(decCoin)
	mintRequest.DaoTokenLeft = mintRequest.DaoTokenLeft.Sub(decCoin)

	k.SetMintRequest(ctx, mintRequest)

	return nil
}

func (k ExpKeeper) SendBurnOracleRequest(ctx sdk.Context, burnRequest types.BurnRequest) error {
	_, found := k.GetBurnRequestOracle(ctx, burnRequest.Account)
	if found == true {
		return nil
	}

	oracleRequest := types.OracleRequest{
		OracleId:        k.GetNextOracleID(ctx),
		Type:            "burn",
		AddressRequest:  burnRequest.Account,
		AmountInRequest: *burnRequest.BurnTokenLeft,
	}
	k.SetBurnRequestOracle(ctx, oracleRequest)
	k.SetNextOracleRequest(ctx, oracleRequest)

	timeoutHeight := clienttypes.Height{
		RevisionNumber: 1,
		RevisionHeight: uint64(ctx.BlockHeight() + 100),
	}
	k.SendIbcOracle(ctx, burnRequest.Account, *burnRequest.BurnTokenLeft, "burn", timeoutHeight, 0)
	return nil
}
