package keeper

import (
	"errors"

	"github.com/cosmos/cosmos-sdk/codec"
	storetypes "github.com/cosmos/cosmos-sdk/store/types"
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	paramtypes "github.com/cosmos/cosmos-sdk/x/params/types"
	"github.com/notional-labs/craft/x/exp/types"
	"github.com/tendermint/tendermint/libs/log"
)

// Keeper for expModule.
type ExpKeeper struct {
	cdc codec.BinaryCodec

	storeKey   storetypes.StoreKey
	paramSpace paramtypes.Subspace
	bankKeeper types.BankKeeper
}

func NewKeeper(
	key storetypes.StoreKey,
	cdc codec.BinaryCodec,
	paramSpace paramtypes.Subspace,
	ak types.AccountKeeper,
	bk types.BankKeeper,
) ExpKeeper {
	// ensure module account is set
	if addr := ak.GetModuleAddress(types.ModuleName); addr == nil {
		panic("the exp module account has not been set")
	}

	// set KeyTable if it has not already been set
	if !paramSpace.HasKeyTable() {
		paramSpace = paramSpace.WithKeyTable(types.ParamKeyTable())
	}

	return ExpKeeper{
		cdc:        cdc,
		storeKey:   key,
		paramSpace: paramSpace,
		bankKeeper: bk,
	}
}

// Logger returns a module-specific logger.
func (k ExpKeeper) Logger(ctx sdk.Context) log.Logger {
	return ctx.Logger().With("module", "x/"+types.ModuleName)
}

func (k ExpKeeper) MintExpForAccount(ctx sdk.Context, newCoins sdk.Coins, dstAccount sdk.AccAddress) error {
	if newCoins.Empty() {
		// skip as no coins need to be minted
		return nil
	}
	// only mint one denom
	if newCoins.Len() != 1 && newCoins[0].Denom != k.GetDenom(ctx) {
		return errors.New("Exp module only mint exp")
	}

	// mint coin for exp module
	err := k.bankKeeper.MintCoins(ctx, types.ModuleName, newCoins)
	if err != nil {
		return nil
	}

	// send coin to account
	err = k.bankKeeper.SendCoinsFromModuleToAccount(ctx, types.ModuleName, dstAccount, newCoins)
	if err != nil {
		return err
	}

	return nil
}

func (k ExpKeeper) BurnCoinAndExitDao(ctx sdk.Context, memberAccount sdk.AccAddress) error {
	var newDaoInfo types.DaoInfo

	daoInfo, err := k.GetDaoInfo(ctx)
	if err != nil {
		return err
	}

	whiteList := daoInfo.GetWhitelist()

	for index, ar := range whiteList {
		if ar.Account == memberAccount.String() {
			newDaoInfo = types.DaoInfo{
				Whitelist: append(whiteList[:index], whiteList[index+1:]...),
			}
			break
		}
	}

	k.SetDaoInfo(ctx, newDaoInfo)
	return nil
}

// verify Dao member: balances, whitelist .
func (k ExpKeeper) verifyDao(ctx sdk.Context, daoAddress sdk.AccAddress, dstAddress sdk.AccAddress) error {
	params := k.GetParams(ctx)

	if params.DaoAccount != daoAddress.String() {
		return sdkerrors.Wrapf(types.ErrDaoAccount, "must be %s addrees not %s", params.DaoAccount, daoAddress.String())
	}

	daoInfo, err := k.GetDaoInfo(ctx)
	if err != nil {
		return err
	}
	// check if dstAddress in whitelist .
	for _, accountRecord := range daoInfo.Whitelist {
		if dstAddress.String() == accountRecord.Account {
			dstAddressBalances := k.bankKeeper.GetBalance(ctx, dstAddress, params.Denom)

			if dstAddressBalances.Amount.GT(accountRecord.MaxToken.Amount) {
				return types.ErrInputOutputMismatch
			}
			return nil
		}
	}
	return types.ErrAddressdNotFound
}

func (k ExpKeeper) verifyAccount(ctx sdk.Context, memberAddress sdk.AccAddress) error {
	// check if dstAddress in whitelist .
	daoInfo, err := k.GetDaoInfo(ctx)
	if err != nil {
		return err
	}

	for _, accountRecord := range daoInfo.Whitelist {
		if memberAddress.String() == accountRecord.Account {
			return nil
		}
	}
	return types.ErrAddressdNotFound
}

func (k ExpKeeper) AddAddressToWhiteList(ctx sdk.Context, memberAccount sdk.AccAddress, maxToken sdk.Coin) error {
	var newDaoInfo types.DaoInfo

	daoInfo, err := k.GetDaoInfo(ctx)
	if err != nil {
		return err
	}
	whiteList := daoInfo.GetWhitelist()

	for _, ar := range whiteList {
		if ar.Account == memberAccount.String() {
			return sdkerrors.Wrap(types.ErrDuplicate, "address already in whitelist")
		}
	}

	accountRecord := &types.AccountRecord{
		Account: memberAccount.String(), MaxToken: &maxToken,
	}

	newDaoInfo = types.DaoInfo{
		Whitelist: append(whiteList, accountRecord),
	}

	k.SetDaoInfo(ctx, newDaoInfo)
	return nil
}
