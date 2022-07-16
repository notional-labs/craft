package keeper

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	oracletypes "github.com/notional-labs/craft/x/oracle"
)

// OnOracleRequestTimeoutPacket handles the OracleRequestPacketData instance that is sent when a request times out
func (k ExpKeeper) OnOracleRequestTimeoutPacket(
	ctx sdk.Context,
	data oracletypes.OracleRequestPacketData,
) error {
	// TODO: need implement
	return nil
}

func (k ExpKeeper) ProccessRecvPacketMintRequest(ctx sdk.Context, addressRequest string, expPrice string, oracleID uint64) error {
	accAddress, err := sdk.AccAddressFromBech32(addressRequest)
	if err != nil {
		return err
	}
	mintRequest, err := k.GetMintRequest(ctx, accAddress)
	oracleRequest := k.GetOracleRequest(ctx, oracleID)
	if err != nil {
		return err
	}
	err = k.ExecuteMintExpByIbcToken(ctx, mintRequest, oracleRequest.AmountInRequest)
	if err != nil {
		return err
	}
	return nil
}

func (k ExpKeeper) ProccessRecvPacketBurnRequest(ctx sdk.Context, addressRequest string, expPrice string, oracleID uint64) error {
	return nil
}
