package keeper

import (
	"context"

	sdk "github.com/cosmos/cosmos-sdk/types"
	govtypes "github.com/cosmos/cosmos-sdk/x/gov/types"
	"github.com/notional-labs/craft/x/exp/types"
)

type msgServer struct {
	ExpKeeper
}

var _ types.MsgServer = msgServer{}

// NewMsgServerImpl returns an implementation of the exp MsgServer interface
// for the provided Keeper.
func NewMsgServerImpl(keeper ExpKeeper) types.MsgServer {
	return &msgServer{
		ExpKeeper: keeper,
	}
}

func (k msgServer) MintAndAllocateExp(goCtx context.Context, msg *types.MsgMintAndAllocateExp) (*types.MsgMintAndAllocateExpResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	fromAddress, err := sdk.AccAddressFromBech32(msg.FromAddress)
	if err != nil {
		return nil, err
	}

	memberAddress, err := sdk.AccAddressFromBech32(msg.Member)
	if err != nil {
		return nil, err
	}

	if err := k.verifyAccountForMint(ctx, fromAddress, memberAddress); err != nil {
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

func (k msgServer) RequestBurnCoinAndExitDao(goCtx context.Context, msg *types.MsgBurnAndRemoveMember) (*types.MsgBurnAndRemoveMemberResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	from, err := sdk.AccAddressFromBech32(msg.FromAddress)
	if err != nil {
		return nil, err
	}

	err = k.requestBurnCoinFromAddress(ctx, from)
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

func (k msgServer) JoinDaoByNonIbcAsset(goCtx context.Context, msg *types.MsgJoinDaoByNonIbcAsset) (*types.MsgJoinDaoByNonIbcAssetResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	joinAddress, err := sdk.AccAddressFromBech32(msg.JoinAddress)
	if err != nil {
		return nil, err
	}
	if k.accountKeeper.GetModuleAccount(ctx, govtypes.ModuleName).GetAddress().String() != msg.GovAddress {
		return nil, types.ErrGov
	}

	MaxCoinMint := sdk.Coin{
		Amount: sdk.NewInt(msg.MaxToken),
		Denom:  k.GetDenom(ctx),
	}
	err = k.addAddressToWhiteList(ctx, joinAddress, MaxCoinMint)
	if err != nil {
		return nil, err
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.AttributeKeyBurnExp),
		),
	)

	return &types.MsgJoinDaoByNonIbcAssetResponse{}, nil
}

func (k msgServer) JoinDaoByIbcAsset(goCtx context.Context, msg *types.MsgJoinDaoByIbcAsset) (*types.MsgJoinDaoByIbcAssetResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	joinAddress, err := sdk.AccAddressFromBech32(msg.JoinAddress)
	if err != nil {
		return nil, err
	}

	err = k.addAddressToMintRequestList(ctx, joinAddress, msg.Amount)
	if err != nil {
		return nil, err
	}
	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.AttributeKeyMintExp),
			sdk.NewAttribute(sdk.AttributeKeySender, joinAddress.String()),
		),
	)
	return &types.MsgJoinDaoByIbcAssetResponse{}, nil
}

func (k msgServer) FundExpPool(goCtx context.Context, msg *types.MsgFundExpPool) (*types.MsgFundExpPoolResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	fromAddress, err := sdk.AccAddressFromBech32(msg.FromAddress)
	if err != nil {
		return nil, err
	}

	err = k.ExpKeeper.FundExpPool(ctx, msg.Amount, fromAddress)
	if err != nil {
		return nil, err
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.AttributeKeyMintExp),
			sdk.NewAttribute(sdk.AttributeKeySender, msg.FromAddress),
		),
	)
	return &types.MsgFundExpPoolResponse{}, nil
}

func (k msgServer) SpendIbcAssetToExp(goCtx context.Context, msg *types.MsgSpendIbcAssetToExp) (*types.MsgSpendIbcAssetToExpResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	fromAddress, err := sdk.AccAddressFromBech32(msg.FromAddress)
	if err != nil {
		return nil, err
	}
	if len(msg.Amount) != 1 {
		return nil, types.ErrDenomNotMatch
	}

	err = k.ExpKeeper.executeMintRequest(ctx, fromAddress, msg.Amount[0])
	if err != nil {
		return nil, err
	}

	return &types.MsgSpendIbcAssetToExpResponse{}, nil
}
