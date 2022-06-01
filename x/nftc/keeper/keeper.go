package keeper

import (
	"github.com/cosmos/cosmos-sdk/codec"
	storetypes "github.com/cosmos/cosmos-sdk/store/types"
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	"github.com/notional-labs/craft/x/nftc/types"
)

// Keeper of the nft store.
type Keeper struct {
	cdc      codec.BinaryCodec
	storeKey storetypes.StoreKey
	bk       types.BankKeeper
	ek       types.ExpKeeper
}

// NewKeeper creates a new nft Keeper instance.
func NewKeeper(key storetypes.StoreKey,
	cdc codec.BinaryCodec, ak types.AccountKeeper, bk types.BankKeeper,
) Keeper {
	// ensure nft module account is set
	if addr := ak.GetModuleAddress(types.ModuleName); addr == nil {
		panic("the nft module account has not been set")
	}

	return Keeper{
		cdc:      cdc,
		storeKey: key,
		bk:       bk,
	}
}

// Authorize checks if the sender is the owner of the given NFT
// Return the NFT if true, an error otherwise
func (k Keeper) Authorize(ctx sdk.Context, classID, nftID string, owner sdk.AccAddress) (types.NFT, error) {
	nft, has := k.GetNFT(ctx, classID, nftID)
	if has == false {
		return types.NFT{}, types.ErrInvalidID
	}

	if !owner.Equals(k.GetOwner(ctx, classID, nftID)) {
		return types.NFT{}, sdkerrors.Wrap(types.ErrUnauthorized, owner.String())
	}

	return nft, nil
}

// AuthorizeDAO checks sender is DAO in exp module
func (k Keeper) AuthorizeDAO(ctx sdk.Context, sender string) error {
	daoAccount := k.ek.GetDAOAccount(ctx)

	if daoAccount != sender {
		return sdkerrors.Wrapf(types.ErrUnauthorized, "DAO address must be %s not %s", daoAccount, sender)
	}
	return nil
}
