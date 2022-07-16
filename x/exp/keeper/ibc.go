package keeper

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	capabilitytypes "github.com/cosmos/cosmos-sdk/x/capability/types"
	channeltypes "github.com/cosmos/ibc-go/v4/modules/core/04-channel/types"
	host "github.com/cosmos/ibc-go/v4/modules/core/24-host"

	"github.com/notional-labs/craft/x/exp/types"
)

// DONTCOVER
// No need to cover this simple methods

// ChanCloseInit defines a wrapper function for the channel Keeper's function
func (k ExpKeeper) ChanCloseInit(ctx sdk.Context, portID, channelID string) error {
	capName := host.ChannelCapabilityPath(portID, channelID)
	chanCap, ok := k.scopedKeeper.GetCapability(ctx, capName)
	if !ok {
		return sdkerrors.Wrapf(channeltypes.ErrChannelCapabilityNotFound, "could not retrieve channel capability at: %s", capName)
	}
	return k.channelKeeper.ChanCloseInit(ctx, portID, channelID, chanCap)
}

// IsBound checks if the module is already bound to the desired port
func (k ExpKeeper) IsBound(ctx sdk.Context, portID string) bool {
	_, ok := k.scopedKeeper.GetCapability(ctx, host.PortPath(portID))
	return ok
}

// BindPort defines a wrapper function for the port Keeper's function in
// order to expose it to module's InitGenesis function
func (k ExpKeeper) BindPort(ctx sdk.Context, portID string) error {
	capability := k.portKeeper.BindPort(ctx, portID)
	return k.ClaimCapability(ctx, capability, host.PortPath(portID))
}

// GetPort returns the portID for the module. Used in ExportGenesis
func (k ExpKeeper) GetPort(ctx sdk.Context) string {
	store := ctx.KVStore(k.storeKey)
	return string(store.Get(types.IBCPortKey))
}

// SetPort sets the portID for the module. Used in InitGenesis
func (k ExpKeeper) SetPort(ctx sdk.Context, portID string) {
	store := ctx.KVStore(k.storeKey)
	store.Set(types.IBCPortKey, []byte(portID))
}

// AuthenticateCapability wraps the scopedKeeper's AuthenticateCapability function
func (k ExpKeeper) AuthenticateCapability(ctx sdk.Context, cap *capabilitytypes.Capability, name string) bool {
	return k.scopedKeeper.AuthenticateCapability(ctx, cap, name)
}

// ClaimCapability wraps the scopedKeeper's ClaimCapability method
func (k ExpKeeper) ClaimCapability(ctx sdk.Context, cap *capabilitytypes.Capability, name string) error {
	return k.scopedKeeper.ClaimCapability(ctx, cap, name)
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
