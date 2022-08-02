package exp

import (
	"fmt"
	"strconv"

	"github.com/cosmos/cosmos-sdk/codec"
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	capabilitytypes "github.com/cosmos/cosmos-sdk/x/capability/types"
	channeltypes "github.com/cosmos/ibc-go/v4/modules/core/04-channel/types"
	porttypes "github.com/cosmos/ibc-go/v4/modules/core/05-port/types"
	host "github.com/cosmos/ibc-go/v4/modules/core/24-host"
	ibcexported "github.com/cosmos/ibc-go/v4/modules/core/exported"
	"github.com/notional-labs/craft/utils/obi"
	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"
	oracletypes "github.com/notional-labs/craft/x/oracle"
)

var _ porttypes.IBCModule = IBCModule{}

// resultData represents the data that is returned by the oracle script.
type resultData struct {
	ExpPrice       string `obi:"exp_price"`
	AddressRequest string `obi:"address_request"`
	RequestType    string `obi:"request_type"`
	Status         string `obi:"status"`
}

// IBCModule implements the ICS26 interface for transfer given the transfer keeper.
type IBCModule struct {
	cdc    codec.Codec
	keeper keeper.ExpKeeper
}

// NewIBCModule creates a new IBCModule given the keeper.
func NewIBCModule(cdc codec.Codec, k keeper.ExpKeeper) IBCModule {
	return IBCModule{
		cdc:    cdc,
		keeper: k,
	}
}

// ValidateChannelParams does validation of a newly created profiles channel. A profiles
// channel must be UNORDERED, use the correct port (by default 'profiles'), and use the current
// supported version.
func ValidateChannelParams(
	ctx sdk.Context,
	keeper keeper.ExpKeeper,
	order channeltypes.Order,
	portID string,
	channelID string,
) error {
	if order != channeltypes.UNORDERED {
		return sdkerrors.Wrapf(channeltypes.ErrInvalidChannelOrdering, "expected %s channel, got %s ", channeltypes.UNORDERED, order)
	}

	// Require portID is the portID profiles module is bound to
	boundPort := keeper.GetPort(ctx)
	if boundPort != portID {
		return sdkerrors.Wrapf(porttypes.ErrInvalidPort, "invalid port: %s, expected %s", portID, boundPort)
	}

	return nil
}

// -------------------------------------------------------------------------------------------------------------------

// OnChanOpenInit implements the IBCModule interface.
func (am IBCModule) OnChanOpenInit(
	ctx sdk.Context,
	order channeltypes.Order,
	connectionHops []string,
	portID string,
	channelID string,
	channelCap *capabilitytypes.Capability,
	counterparty channeltypes.Counterparty,
	version string,
) (string, error) {
	if err := ValidateChannelParams(ctx, am.keeper, order, portID, channelID); err != nil {
		return version, err
	}

	// Claim channel capability passed back by IBC module
	if err := am.keeper.ClaimCapability(ctx, channelCap, host.ChannelCapabilityPath(portID, channelID)); err != nil {
		return version, err
	}

	return version, nil
}

// OnChanOpenTry implements the IBCModule interface.
func (am IBCModule) OnChanOpenTry(
	ctx sdk.Context,
	order channeltypes.Order,
	connectionHops []string,
	portID,
	channelID string,
	channelCap *capabilitytypes.Capability,
	counterparty channeltypes.Counterparty,
	counterpartyVersion string,
) (string, error) {
	if err := ValidateChannelParams(ctx, am.keeper, order, portID, channelID); err != nil {
		return "", err
	}

	// Module may have already claimed capability in OnChanOpenInit in the case of crossing hellos
	// (ie chainA and chainB both call ChanOpenInit before one of them calls ChanOpenTry)
	// If module can already authenticate the capability then module already owns it so we don't need to claim
	// Otherwise, module does not have channel capability and we must claim it from IBC
	if !am.keeper.AuthenticateCapability(ctx, channelCap, host.ChannelCapabilityPath(portID, channelID)) {
		// Only claim channel capability passed back by IBC module if we do not already own it
		err := am.keeper.ClaimCapability(ctx, channelCap, host.ChannelCapabilityPath(portID, channelID))
		if err != nil {
			return "", err
		}
	}

	return counterpartyVersion, nil
}

// OnChanOpenAck implements the IBCModule interface.
func (am IBCModule) OnChanOpenAck(
	ctx sdk.Context,
	portID,
	channelID string,
	counterpartyChannelID string,
	counterpartyVersion string,
) error {
	return nil
}

// OnChanOpenConfirm implements the IBCModule interface.
func (am IBCModule) OnChanOpenConfirm(
	ctx sdk.Context,
	portID,
	channelID string,
) error {
	return nil
}

// OnChanCloseInit implements the IBCModule interface.
func (am IBCModule) OnChanCloseInit(
	ctx sdk.Context,
	portID,
	channelID string,
) error {
	// Disallow user-initiated channel closing for channels
	return sdkerrors.Wrap(sdkerrors.ErrInvalidRequest, "user cannot close channel")
}

// OnChanCloseConfirm implements the IBCModule interface.
func (am IBCModule) OnChanCloseConfirm(
	ctx sdk.Context,
	portID,
	channelID string,
) error {
	return nil
}

// OnRecvPacket implements the IBCModule interface.
func (am IBCModule) OnRecvPacket(
	ctx sdk.Context,
	packet channeltypes.Packet,
	relayer sdk.AccAddress,
) ibcexported.Acknowledgement {
	var data oracletypes.OracleResponsePacketData
	fmt.Println("==============================")
	fmt.Println("========v1======================")
	fmt.Println(data)
	fmt.Println("==============================")
	fmt.Println("==============================")

	if err := types.ModuleCdc.UnmarshalJSON(packet.GetData(), &data); err != nil {
		return channeltypes.Acknowledgement{}
	}

	acknowledgement := channeltypes.NewResultAcknowledgement([]byte{byte(1)})

	// handler IBC packet
	// err = am.keeper.OnRecvApplicationLinkPacketData(ctx, data)
	// if err != nil {
	// 	acknowledgement = channeltypes.NewErrorAcknowledgement(err.Error())
	// }
	oracleID, err := strconv.ParseUint(data.ClientID, 10, 64)
	if err != nil {
		return channeltypes.Acknowledgement{}
	}
	fmt.Println("=========v2=====================")
	fmt.Println("==============================")
	fmt.Println(data)
	fmt.Println("==============================")
	fmt.Println("==============================")

	if data.ResolveStatus == oracletypes.RESOLVE_STATUS_SUCCESS {
		var result resultData
		err := obi.Decode(data.Result, &result)
		if err != nil {
			return channeltypes.Acknowledgement{}
		}
		switch result.Status {
		case "mint":
			err = am.keeper.ProccessRecvPacketMintRequest(ctx, result.AddressRequest, result.ExpPrice, oracleID)
			if err != nil {
				return channeltypes.Acknowledgement{}
			}
		case "burn":
			err = am.keeper.ProccessRecvPacketBurnRequest(ctx, result.AddressRequest, result.ExpPrice, oracleID)
			if err != nil {
				return channeltypes.Acknowledgement{}
			}
		}
	} else {
		oracleRequest := am.keeper.GetOracleRequest(ctx, oracleID)
		if oracleRequest.Type == "burn" {
			// remove burnRequest to resend burn oracle request in next block.
			am.keeper.RemoveBurnRequestOracle(ctx, oracleRequest.AddressRequest)
		}
		// else {
		// Change state for user know failed mint request .
		// }
	}
	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			types.EventTypePacket,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.ModuleName),
			sdk.NewAttribute(types.AttributeKeyClientID, data.ClientID),
			sdk.NewAttribute(types.AttributeKeyRequestID, fmt.Sprintf("%d", data.RequestID)),
			sdk.NewAttribute(types.AttributeKeyResolveStatus, data.ResolveStatus.String()),
			sdk.NewAttribute(types.AttributeKeyAckSuccess, fmt.Sprintf("%t", true)),
		),
	)

	// NOTE: acknowledgement will be written synchronously during IBC handler execution.
	return acknowledgement
}

// OnAcknowledgementPacket implements the IBCModule interface.
func (am IBCModule) OnAcknowledgementPacket(
	ctx sdk.Context,
	packet channeltypes.Packet,
	acknowledgement []byte,
	relayer sdk.AccAddress,
) error {
	var ack channeltypes.Acknowledgement
	err := types.ModuleCdc.UnmarshalJSON(acknowledgement, &ack)
	if err != nil {
		return sdkerrors.Wrapf(sdkerrors.ErrUnknownRequest,
			"cannot unmarshal oracle packet acknowledgement: %v", err)
	}

	var data oracletypes.OracleRequestPacketData
	err = types.ModuleCdc.UnmarshalJSON(packet.GetData(), &data)
	if err != nil {
		return sdkerrors.Wrapf(sdkerrors.ErrUnknownRequest,
			"cannot unmarshal oracle request packet data: %s", err.Error())
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			types.EventTypePacket,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.ModuleName),
			sdk.NewAttribute(types.AttributeKeyClientID, data.ClientID),
			sdk.NewAttribute(types.AttributeKeyAck, fmt.Sprintf("%v", ack)),
		),
	)

	switch resp := ack.Response.(type) {
	case *channeltypes.Acknowledgement_Result:
		ctx.EventManager().EmitEvent(
			sdk.NewEvent(
				types.EventTypePacket,
				sdk.NewAttribute(types.AttributeKeyAckSuccess, string(resp.Result)),
			),
		)
	case *channeltypes.Acknowledgement_Error:
		ctx.EventManager().EmitEvent(
			sdk.NewEvent(
				types.EventTypePacket,
				sdk.NewAttribute(types.AttributeKeyAckError, resp.Error),
			),
		)
	}

	return nil
}

// -------------------------------------------------------------------------------------------------------------------

// OnTimeoutPacket implements the IBCModule interface.
func (am IBCModule) OnTimeoutPacket(
	ctx sdk.Context,
	packet channeltypes.Packet,
	relayer sdk.AccAddress,
) error {
	var data oracletypes.OracleRequestPacketData
	err := types.ModuleCdc.UnmarshalJSON(packet.GetData(), &data)
	if err != nil {
		return sdkerrors.Wrapf(sdkerrors.ErrUnknownRequest,
			"cannot unmarshal oracle request packet data: %s", err.Error())
	}

	err = am.keeper.OnOracleRequestTimeoutPacket(ctx, data)
	if err != nil {
		return err
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			types.EventTypeTimeout,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.ModuleName),
			sdk.NewAttribute(types.AttributeKeyOracleID, fmt.Sprintf("%d", data.OracleScriptID)),
			sdk.NewAttribute(types.AttributeKeyClientID, data.ClientID),
		),
	)

	return nil
}
