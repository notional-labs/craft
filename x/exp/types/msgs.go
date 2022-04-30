package types

import (
	"github.com/cosmos/cosmos-sdk/codec/legacy"
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
)

var _ sdk.Msg = &MsgMintAndAllocateExp{}

// Route Implements Msg.
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

// ValidateBasic does a sanity check on the provided data.
func (m MsgMintAndAllocateExp) ValidateBasic() error {
	_, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "from address must be valid address")
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

// Route Implements Msg.
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

// ValidateBasic does a sanity check on the provided data.
func (m MsgBurnAndRemoveMember) ValidateBasic() error {
	_, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "from address must be valid address")
	}
	return nil
}

func NewMsgBurnAndRemoveMember(fromAddr sdk.AccAddress, metadata string) *MsgBurnAndRemoveMember {
	return &MsgBurnAndRemoveMember{
		FromAddress: fromAddr.String(),
		Metadata:    metadata,
	}
}

var _ sdk.Msg = &MsgJoinDaoByNonIbcAsset{}

// Route Implements Msg.
func (m MsgJoinDaoByNonIbcAsset) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg.
func (m MsgJoinDaoByNonIbcAsset) Type() string { return sdk.MsgTypeURL(&m) }

// GetSigners returns the expected signers for a MsgBurnAndRemoveMember.
func (m MsgJoinDaoByNonIbcAsset) GetSigners() []sdk.AccAddress {
	daoAccount, err := sdk.AccAddressFromBech32(m.GovAddress)
	if err != nil {
		panic(err)
	}
	return []sdk.AccAddress{daoAccount}
}

// GetSignBytes Implements Msg.
func (m MsgJoinDaoByNonIbcAsset) GetSignBytes() []byte {
	return sdk.MustSortJSON(legacy.Cdc.MustMarshalJSON(&m))
}

// ValidateBasic does a sanity check on the provided data.
func (m MsgJoinDaoByNonIbcAsset) ValidateBasic() error {
	_, err := sdk.AccAddressFromBech32(m.JoinAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "join address must be valid address")
	}
	return nil
}

var _ sdk.Msg = &MsgJoinDaoByNonIbcAsset{}

// Route Implements Msg.
func (m MsgJoinDaoByIbcAsset) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg.
func (m MsgJoinDaoByIbcAsset) Type() string { return sdk.MsgTypeURL(&m) }

// GetSigners returns the expected signers for a MsgJoinDaoByIbcAsset.
func (m MsgJoinDaoByIbcAsset) GetSigners() []sdk.AccAddress {
	daoAccount, err := sdk.AccAddressFromBech32(m.GovAddress)
	if err != nil {
		panic(err)
	}

	return []sdk.AccAddress{daoAccount}
}

// GetSignBytes Implements Msg.
func (m MsgJoinDaoByIbcAsset) GetSignBytes() []byte {
	return sdk.MustSortJSON(legacy.Cdc.MustMarshalJSON(&m))
}

// ValidateBasic does a sanity check on the provided data.
func (m MsgJoinDaoByIbcAsset) ValidateBasic() error {
	_, err := sdk.AccAddressFromBech32(m.JoinAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "join address must be valid address")
	}

	_, err = sdk.AccAddressFromBech32(m.GovAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "gov address must be valid address")
	}

	return nil
}

var _ sdk.Msg = &MsgFundExpPool{}

// Route Implements Msg.
func (m MsgFundExpPool) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg.
func (m MsgFundExpPool) Type() string { return sdk.MsgTypeURL(&m) }

// GetSigners returns the expected signers for a MsgFundExpPool.
func (m MsgFundExpPool) GetSigners() []sdk.AccAddress {
	daoAccount, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		panic(err)
	}

	return []sdk.AccAddress{daoAccount}
}

// GetSignBytes Implements Msg.
func (m MsgFundExpPool) GetSignBytes() []byte {
	return sdk.MustSortJSON(legacy.Cdc.MustMarshalJSON(&m))
}

// ValidateBasic does a sanity check on the provided data.
func (m MsgFundExpPool) ValidateBasic() error {
	_, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "join address must be valid address")
	}
	return nil
}

func NewMsgFundExpPool(fromAddress string, amount sdk.Coins) *MsgFundExpPool {
	return &MsgFundExpPool{
		FromAddress: fromAddress,
		Amount:      amount,
	}
}

var _ sdk.Msg = &MsgSpendIbcAssetToExp{}

// Route Implements Msg.
func (m MsgSpendIbcAssetToExp) Route() string { return sdk.MsgTypeURL(&m) }

// Type Implements Msg.
func (m MsgSpendIbcAssetToExp) Type() string { return sdk.MsgTypeURL(&m) }

// GetSigners returns the expected signers for a MsgSpendIbcAssetToExp.
func (m MsgSpendIbcAssetToExp) GetSigners() []sdk.AccAddress {
	fromAddr, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		panic(err)
	}

	return []sdk.AccAddress{fromAddr}
}

// GetSignBytes Implements Msg.
func (m MsgSpendIbcAssetToExp) GetSignBytes() []byte {
	return sdk.MustSortJSON(legacy.Cdc.MustMarshalJSON(&m))
}

// ValidateBasic does a sanity check on the provided data.
func (m MsgSpendIbcAssetToExp) ValidateBasic() error {
	_, err := sdk.AccAddressFromBech32(m.FromAddress)
	if err != nil {
		return sdkerrors.Wrap(err, "join address must be valid address")
	}
	return nil
}

func NewMsgSpendIbcAssetToExp(fromAddress string, amount sdk.Coins) *MsgSpendIbcAssetToExp {
	return &MsgSpendIbcAssetToExp{
		FromAddress: fromAddress,
		Amount:      amount,
	}
}
