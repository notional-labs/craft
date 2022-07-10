package keeper

import (
	"context"

	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
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

	if err := k.verifyAccountForMint(ctx, fromAddress, memberAddress, msg.Amount); err != nil {
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

	err = k.verifyAccountToWhiteList(ctx, joinAddress)
	if err != nil {
		return &types.MsgJoinDaoByNonIbcAssetResponse{}, err
	}

	err = k.addAddressToWhiteList(ctx, joinAddress, MaxCoinMint)
	if err != nil {
		return nil, err
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyAction, types.AttributeKeyJoinDao),
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

	if k.accountKeeper.GetModuleAccount(ctx, govtypes.ModuleName).GetAddress().String() != msg.GovAddress {
		return nil, types.ErrGov
	}

	err = k.verifyAccountToWhiteList(ctx, joinAddress)

	if err != nil {
		return &types.MsgJoinDaoByIbcAssetResponse{}, err
	}

	k.addAddressToMintRequestList(ctx, joinAddress, msg.Amount)

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyAction, types.AttributeKeyMintExp),
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

	err = k.ExpKeeper.FundPoolForExp(ctx, msg.Amount, fromAddress)
	if err != nil {
		return nil, err
	}

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
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

	if len(msg.Amount) != 1 || msg.Amount[0].Denom != k.GetIbcDenom(ctx) {
		return nil, types.ErrDenomNotMatch
	}

	// oracle for exp price

	err = k.ExpKeeper.executeMintExpByIbcToken(ctx, fromAddress, msg.Amount[0])
	if err != nil {
		return nil, err
	}

	return &types.MsgSpendIbcAssetToExpResponse{}, nil
}

func (k msgServer) AdjustDaoPrice(goCtx context.Context, msg *types.MsgAdjustDaoTokenPrice) (*types.MsgAdjustDaoTokenPriceResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	params := k.GetParams(ctx)

	if params.DaoAccount != msg.FromAddress {
		return nil, sdkerrors.Wrapf(types.ErrDaoAccount, "DAO address must be %s not %s", params.DaoAccount, msg.FromAddress)
	}

	daoAssetInfo, err := k.GetDaoAssetInfo(ctx)
	if err != nil {
		return nil, err
	}

	daoAssetInfo.DaoTokenPrice = msg.DaoTokenPrice
	k.SetDaoAssetInfo(ctx, daoAssetInfo)

	ctx.EventManager().EmitEvent(
		sdk.NewEvent(
			sdk.EventTypeMessage,
			sdk.NewAttribute(sdk.AttributeKeyModule, types.AttributeAdjustDaoTokenPrice),
			sdk.NewAttribute("new_price", daoAssetInfo.DaoTokenPrice.String()),
		),
	)

	return &types.MsgAdjustDaoTokenPriceResponse{}, nil
}

func (k msgServer) SendCoinsByDAO(goCtx context.Context, msg *types.MsgSendCoinsFromModuleToDAO) (*types.MsgSendCoinsFromModuleToDAOResponse, error) {
	ctx := sdk.UnwrapSDKContext(goCtx)

	params := k.GetParams(ctx)

	if params.DaoAccount != msg.ToAddress {
		return nil, sdkerrors.Wrapf(types.ErrDaoAccount, "DAO address must be %s not %s", params.DaoAccount, msg.ToAddress)
	}
	acc, err := sdk.AccAddressFromBech32(msg.ToAddress)
	if err != nil {
		return nil, sdkerrors.ErrInvalidAddress
	}

	err = k.bankKeeper.SendCoinsFromModuleToAccount(ctx, types.ModuleName, acc, msg.Amount)
	if err != nil {
		return nil, types.ErrInputOutputMismatch
	}

	return &types.MsgSendCoinsFromModuleToDAOResponse{}, nil
}
