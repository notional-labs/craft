package exp

import (
	"github.com/cosmos/cosmos-sdk/codec"
	porttypes "github.com/cosmos/ibc-go/v3/modules/core/05-port/types"
	"github.com/notional-labs/craft/x/exp/keeper"
)

var (
	_ porttypes.IBCModule = IBCModule{}
)

// IBCModule implements the ICS26 interface for transfer given the transfer keeper.
type IBCModule struct {
	cdc    codec.Codec
	keeper keeper.ExpKeeper
}
