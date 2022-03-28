package keeper

import (
	"context"

	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/types"
)

type msgServer struct {
	ExpKeeper
}

// NewMsgServerImpl returns an implementation of the bank MsgServer interface
// for the provided Keeper.
func NewMsgServerImpl(keeper ExpKeeper) types.MsgServer {
	return &msgServer{ExpKeeper: keeper}
}

var _ types.MsgServer = msgServer{}

func (k msgServer) MintAndAllocateExp(goCtx context.Context, msg *types.MsgMintAndAllocateExp) (*types.MsgMintAndAllocateExpResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	from_address, err := sdk.AccAddressFromBech32(msg.FromAddress)
	if err != nil {
		return nil, err
	}

	member_address, err := sdk.AccAddressFromBech32(msg.Member)
	if err != nil {
		return nil, err
	}

	if err := k.verifyDao(ctx, from_address, member_address); err != nil {
		return nil, err
	}

	err = k.MintExpForAccount(ctx, sdk.NewCoins(*msg.Amount), member_address)
	if err != nil {
		return nil, err
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.AttributeKeyMintExp),
		),
	)

	return &types.MsgMintAndAllocateExpResponse{}, nil
}

func (k msgServer) BurnAndRemoveMember(goCtx context.Context, msg *types.MsgBurnAndRemoveMember) (*types.MsgBurnAndRemoveMemberResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	from, err := sdk.AccAddressFromBech32(msg.FromAddress)
	if err != nil {
		return nil, err
	}

	err = k.BurnCoinAndExitDao(ctx, from)
	if err != nil {
		return nil, err
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.AttributeKeyBurnExp),
		),
	)

	return &types.MsgBurnAndRemoveMemberResponse{}, nil
}
