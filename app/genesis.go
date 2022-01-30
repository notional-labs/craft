package app

import (
	"encoding/json"

	"github.com/cosmos/cosmos-sdk/codec"
	"github.com/cosmos/cosmos-sdk/std"
	"github.com/notional-labs/craft/app/params"
)

// The genesis state of the blockchain is represented here as a map of raw json
// messages key'd by a identifier string.
// The identifier is used to determine which module genesis information belongs
// to so it may be appropriately routed during init chain.
// Within this application default genesis information is retrieved from
// the ModuleBasicManager which populates json from each BasicModule
// object provided to it during init.
type GenesisState map[string]json.RawMessage

// NewDefaultGenesisState generates the default state for the application.
func NewDefaultGenesisState(cdc codec.Codec) GenesisState {
	return ModuleBasics.DefaultGenesis(cdc)
}

// MakeEncodingConfig creates an EncodingConfig for testing
func MakeEncodingConfig() params.EncodingConfig {
	encodingConfig := MakeEncodingConfig()
	std.RegisterLegacyAminoCodec(encodingConfig.Amino)
	std.RegisterInterfaces(encodingConfig.InterfaceRegistry)
	ModuleBasics.RegisterLegacyAminoCodec(encodingConfig.Amino)
	ModuleBasics.RegisterInterfaces(encodingConfig.InterfaceRegistry)
	return encodingConfig
}
