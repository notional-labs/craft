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
	return &msgServer{
		ExpKeeper: keeper,
	}
}

func (k ExpKeeper) MintAndAllocateExp(goCtx context.Context, msg *types.MsgMintAndAllocateExp) (*types.MsgMintAndAllocateExpResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	fromAddress, err := sdk.AccAddressFromBech32(msg.FromAddress)
	if err != nil {
		return nil, err
	}

	memberAddress, err := sdk.AccAddressFromBech32(msg.Member)
	if err != nil {
		return nil, err
	}

	if err := k.verifyDao(ctx, fromAddress, memberAddress); err != nil {
		return nil, err
	}

	err = k.MintExpForAccount(ctx, msg.Amount, memberAddress)
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

func (k ExpKeeper) BurnAndRemoveMember(goCtx context.Context, msg *types.MsgBurnAndRemoveMember) (*types.MsgBurnAndRemoveMemberResponse, error) {
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

func (k ExpKeeper) JoinDao(goCtx context.Context, msg *types.MsgJoinDao) (*types.MsgJoinDaoResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	joinAddress, err := sdk.AccAddressFromBech32(msg.JoinAddress)
	if err != nil {
		return nil, err
	}
	MaxCoinMint := sdk.Coin{
		Amount: sdk.NewInt(msg.MaxToken),
		Denom:  k.GetDenom(ctx),
	}
	err = k.AddAddressToWhiteList(ctx, joinAddress, MaxCoinMint)
	if err != nil {
		return nil, err
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.AttributeKeyBurnExp),
		),
	)

	return &types.MsgJoinDaoResponse{}, nil
}
