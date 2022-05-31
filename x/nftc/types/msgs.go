package types

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
)

const (
	// TypeMsgSend nft message types .
	TypeMsgSend = "send"
)

var _ sdk.Msg = &MsgSend{}

// Route Implements Msg.
func (m MsgSend) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg.
func (m MsgSend) Type() string { return sdk.MsgTypeURL(&m) }

// GetSignBytes Implements Msg.
func (m MsgSend) GetSignBytes() []byte {
	return sdk.MustSortJSON(ModuleCdc.MustMarshalJSON(&m))
}

// ValidateBasic implements the Msg.ValidateBasic method.
func (m MsgSend) ValidateBasic() error {
	if err := ValidateClassID(m.ClassId); err != nil {
		return sdkerrors.Wrapf(ErrInvalidID, "Invalid class id (%s)", m.ClassId)
	}

	if err := ValidateNFTID(m.Id); err != nil {
		return sdkerrors.Wrapf(ErrInvalidID, "Invalid nft id (%s)", m.Id)
	}

	_, err := sdk.AccAddressFromBech32(m.Sender)
	if err != nil {
		return sdkerrors.Wrapf(sdkerrors.ErrInvalidAddress, "Invalid sender address (%s)", m.Sender)
	}

	_, err = sdk.AccAddressFromBech32(m.Receiver)
	if err != nil {
		return sdkerrors.Wrapf(sdkerrors.ErrInvalidAddress, "Invalid receiver address (%s)", m.Receiver)
	}
	return nil
}

// GetSigners implements Msg .
func (m MsgSend) GetSigners() []sdk.AccAddress {
	signer, _ := sdk.AccAddressFromBech32(m.Sender)
	return []sdk.AccAddress{signer}
}

// GetSigners implements Msg .
func (m MsgMint) GetSigners() []sdk.AccAddress {
	signer, _ := sdk.AccAddressFromBech32(m.Sender)
	return []sdk.AccAddress{signer}
}

// Route Implements Msg .
func (m MsgMint) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg .
func (m MsgMint) Type() string { return sdk.MsgTypeURL(&m) }

// GetSignBytes Implements Msg.
func (m MsgMint) GetSignBytes() []byte {
	return sdk.MustSortJSON(ModuleCdc.MustMarshalJSON(&m))
}

// ValidateBasic implements the Msg.ValidateBasic method.
func (m MsgMint) ValidateBasic() error {
	if err := ValidateClassID(m.ClassId); err != nil {
		return sdkerrors.Wrapf(ErrInvalidID, "Invalid class id (%s)", m.ClassId)
	}

	if err := ValidateNFTID(m.Id); err != nil {
		return sdkerrors.Wrapf(ErrInvalidID, "Invalid nft id (%s)", m.Id)
	}

	_, err := sdk.AccAddressFromBech32(m.Sender)
	if err != nil {
		return sdkerrors.Wrapf(sdkerrors.ErrInvalidAddress, "Invalid sender address (%s)", m.Sender)
	}

	_, err = sdk.AccAddressFromBech32(m.Receiver)
	if err != nil {
		return sdkerrors.Wrapf(sdkerrors.ErrInvalidAddress, "Invalid receiver address (%s)", m.Receiver)
	}
	return nil
}

// Route Implements Msg.
func (m MsgCreateClass) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg.
func (m MsgCreateClass) Type() string { return sdk.MsgTypeURL(&m) }

// GetSignBytes Implements Msg.
func (m MsgCreateClass) GetSignBytes() []byte {
	return sdk.MustSortJSON(ModuleCdc.MustMarshalJSON(&m))
}

// ValidateBasic implements the Msg.ValidateBasic method.
func (m MsgCreateClass) ValidateBasic() error {
	if err := ValidateClassID(m.Id); err != nil {
		return sdkerrors.Wrapf(ErrInvalidID, "Invalid class id (%s)", m.Id)
	}

	if err := ValidateNFTID(m.Id); err != nil {
		return sdkerrors.Wrapf(ErrInvalidID, "Invalid nft id (%s)", m.Id)
	}

	_, err := sdk.AccAddressFromBech32(m.Sender)
	if err != nil {
		return sdkerrors.Wrapf(sdkerrors.ErrInvalidAddress, "Invalid sender address (%s)", m.Sender)
	}

	return nil
}

// GetSigners implements Msg.
func (m MsgCreateClass) GetSigners() []sdk.AccAddress {
	signer, _ := sdk.AccAddressFromBech32(m.Sender)
	return []sdk.AccAddress{signer}
}
