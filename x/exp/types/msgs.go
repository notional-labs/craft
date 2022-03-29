package types

import (
	"github.com/cosmos/cosmos-sdk/codec/legacy"
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
)

var _ sdk.Msg = &MsgMintAndAllocateExp{}

// Route Implements Msg
func (m MsgMintAndAllocateExp) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg.
func (m MsgMintAndAllocateExp) Type() string { return sdk.MsgTypeURL(&m) }

// GetSigners returns the expected signers for a MsgMintAndAllocateExp .
func (m MsgMintAndAllocateExp) GetSigners() []sdk.AccAddress {
	daoAccount, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		panic(err)
	}
	return []sdk.AccAddress{daoAccount}
}

// GetSignBytes Implements Msg.
func (m MsgMintAndAllocateExp) GetSignBytes() []byte {
	return sdk.MustSortJSON(legacy.Cdc.MustMarshalJSON(&m))
}

// ValidateBasic does a sanity check on the provided data
func (m MsgMintAndAllocateExp) ValidateBasic() error {
	_, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "daoAdmin")
	}
	return nil
}

func NewMsgMintAndAllocateExp(fromAddr sdk.AccAddress, member sdk.AccAddress, amount sdk.Coins) *MsgMintAndAllocateExp {
	return &MsgMintAndAllocateExp{
		FromAddress: fromAddr.String(),
		Member:      member.String(),
		Amount:      amount,
	}
}

var _ sdk.Msg = &MsgBurnAndRemoveMember{}

// Route Implements Msg
func (m MsgBurnAndRemoveMember) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg.
func (m MsgBurnAndRemoveMember) Type() string { return sdk.MsgTypeURL(&m) }

// GetSigners returns the expected signers for a MsgBurnAndRemoveMember .
func (m MsgBurnAndRemoveMember) GetSigners() []sdk.AccAddress {
	daoAccount, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		panic(err)
	}
	return []sdk.AccAddress{daoAccount}
}

// GetSignBytes Implements Msg.
func (m MsgBurnAndRemoveMember) GetSignBytes() []byte {
	return sdk.MustSortJSON(legacy.Cdc.MustMarshalJSON(&m))
}

// ValidateBasic does a sanity check on the provided data
func (m MsgBurnAndRemoveMember) ValidateBasic() error {
	_, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "daoAdmin")
	}
	return nil
}

func NewMsgBurnAndRemoveMember(fromAddr sdk.AccAddress, metadata string) *MsgBurnAndRemoveMember {
	return &MsgBurnAndRemoveMember{
		FromAddress: fromAddr.String(),
		Metadata:    metadata,
	}
}
