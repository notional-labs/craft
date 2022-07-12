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
	//TODO: need implement
	return nil
}
