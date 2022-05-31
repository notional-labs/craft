package keeper

import (
	"context"

	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"

	"github.com/notional-labs/craft/x/nftc/types"
)

type msgServer struct {
	Keeper
}

var _ types.MsgServer = msgServer{}

// NewMsgServerImpl returns an implementation of the exp MsgServer interface
// for the provided Keeper.
func NewMsgServerImpl(keeper Keeper) types.MsgServer {
	return &msgServer{
		Keeper: keeper,
	}
}

// Send implement Send method of the types.MsgServer.
func (k Keeper) SendNFT(goCtx context.Context, msg *types.MsgSend) (*types.MsgSendResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)
	sender, err := sdk.AccAddressFromBech32(msg.Sender)
	if err != nil {
		return nil, err
	}

	owner := k.GetOwner(ctx, msg.ClassId, msg.Id)
	if !owner.Equals(sender) {
		return nil, sdkerrors.Wrapf(sdkerrors.ErrUnauthorized, "%s is not the owner of nft %s", sender, msg.Id)
	}

	receiver, err := sdk.AccAddressFromBech32(msg.Receiver)
	if err != nil {
		return nil, err
	}

	if err := k.Transfer(ctx, msg.ClassId, msg.Id, receiver); err != nil {
		return nil, err
	}

	err = ctx.EventManager().EmitTypedEvent(&types.EventSend{
		ClassId:  msg.ClassId,
		Id:       msg.Id,
		Sender:   msg.Sender,
		Receiver: msg.Receiver,
	})

	if err != nil {
		return nil, err
	}

	return &types.MsgSendResponse{}, nil
}

// Send implement Send method of the types.MsgServer.
func (k Keeper) MintNFT(goCtx context.Context, msg *types.MsgMint) (*types.MsgMintResponse, error) {
	return &types.MsgMintResponse{}, nil
}

// Send implement Send method of the types.MsgServer.
func (k Keeper) BurnNFT(goCtx context.Context, msg *types.MsgBurn) (*types.MsgBurnResponse, error) {
	return &types.MsgBurnResponse{}, nil
}

// Send implement Send method of the types.MsgServer.
func (k Keeper) CreateClass(goCtx context.Context, msg *types.MsgCreateClass) (*types.MsgCreateClassResponse, error) {
	return &types.MsgCreateClassResponse{}, nil
}
