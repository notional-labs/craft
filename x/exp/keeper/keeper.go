package keeper

import (
	"errors"
	"time"

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

	storeKey      storetypes.StoreKey
	paramSpace    paramtypes.Subspace
	accountKeeper types.AccountKeeper
	bankKeeper    types.BankKeeper
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
		cdc:           cdc,
		storeKey:      key,
		paramSpace:    paramSpace,
		accountKeeper: ak,
		bankKeeper:    bk,
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

func (k ExpKeeper) BurnExpFromAccount(ctx sdk.Context, newCoins sdk.Coins, dstAccount sdk.AccAddress) error {
	if newCoins.Empty() {
		// skip as no coins need to be minted
		return nil
	}
	// only mint one denom
	if newCoins.Len() != 1 && newCoins[0].Denom != k.GetDenom(ctx) {
		return errors.New("Exp module only burn exp")
	}

	// send coin from account to module
	err := k.bankKeeper.SendCoinsFromAccountToModule(ctx, dstAccount, types.ModuleName, newCoins)
	if err != nil {
		return err
	}

	// mint coin for exp module
	err = k.bankKeeper.BurnCoins(ctx, types.ModuleName, newCoins)
	if err != nil {
		return nil
	}

	return nil
}

func (k ExpKeeper) burnCoinAndExitDao(ctx sdk.Context, memberAccount sdk.AccAddress) error {
	whiteList := k.GetWhiteList(ctx)

	for _, ar := range whiteList {
		if ar.Account == memberAccount.String() {
			timeCheck := ar.GetJoinDaoTime().Add(k.GetClosePoolPeriod(ctx)).Add(time.Hour * 24)
			if ctx.BlockTime().Before(timeCheck) {
				return sdkerrors.Wrap(types.ErrTimeOut, "exp in vesting time, cannot burn")
			}
			k.RemoveRecord(ctx, memberAccount)
			return nil
		}
	}

	return types.ErrAddressdNotFound
}

// verify Dao member: balances, whitelist .
func (k ExpKeeper) verifyAccountForMint(ctx sdk.Context, daoAddress sdk.AccAddress, dstAddress sdk.AccAddress) error {
	params := k.GetParams(ctx)

	if params.DaoAccount != daoAddress.String() {
		return sdkerrors.Wrapf(types.ErrDaoAccount, "DAO address must be %s not %s", params.DaoAccount, daoAddress.String())
	}

	whiteList := k.GetWhiteList(ctx)

	// check if dstAddress in whitelist .
	for _, accountRecord := range whiteList {
		if dstAddress.String() == accountRecord.Account {
			dstAddressBalances := k.bankKeeper.GetBalance(ctx, dstAddress, params.Denom)
			// amount check
			if dstAddressBalances.Amount.GT(accountRecord.MaxToken.Amount) {
				return types.ErrInputOutputMismatch
			}
			// vesting time check, give one day for DAO sign
			timeCheck := accountRecord.GetJoinDaoTime().Add(k.GetClosePoolPeriod(ctx)).Add(time.Hour * 24)
			if ctx.BlockTime().After(timeCheck) {
				return types.ErrTimeOut
			}
			return nil
		}
	}
	return types.ErrAddressdNotFound
}

func (k ExpKeeper) verifyAccount(ctx sdk.Context, memberAddress sdk.AccAddress) error {
	// check if dstAddress in whitelist .
	whiteList := k.GetWhiteList(ctx)

	for _, accountRecord := range whiteList {
		if memberAddress.String() == accountRecord.Account {
			return nil
		}
	}
	return types.ErrAddressdNotFound
}

func (k ExpKeeper) stakingCheck(ctx sdk.Context, memberAccount sdk.AccAddress, ar types.AccountRecord) error {
	balance := k.bankKeeper.GetBalance(ctx, memberAccount, k.GetDenom(ctx))
	if ar.MaxToken.Amount.Equal(balance.Amount) {
		return types.ErrStaking
	}
	return nil
}

func (k ExpKeeper) addAddressToWhiteList(ctx sdk.Context, memberAccount sdk.AccAddress, maxToken sdk.Coin) error {
	whiteList := k.GetWhiteList(ctx)

	for _, ar := range whiteList {
		if ar.Account == memberAccount.String() {
			return sdkerrors.Wrap(types.ErrDuplicate, "address already in whitelist")
		}
	}

	accountRecord := types.AccountRecord{
		Account:     memberAccount.String(),
		MaxToken:    &maxToken,
		JoinDaoTime: time.Now(),
	}

	k.SetAccountRecord(ctx, memberAccount, accountRecord)

	return nil
}

// FundExpPool allows an account to directly fund the exp fund pool.
// The amount is first added to the distribution module account and then directly
// added to the pool. An error is returned if the amount cannot be sent to the
// module account.
func (k ExpKeeper) FundExpPool(ctx sdk.Context, amount sdk.Coins, sender sdk.AccAddress) error {
	if err := k.bankKeeper.SendCoinsFromAccountToModule(ctx, sender, types.ModuleName, amount); err != nil {
		return err
	}

	return nil
}

func (k ExpKeeper) requestBurnCoinFromAddress(ctx sdk.Context, memberAccount sdk.AccAddress) error {
	whiteList := k.GetWhiteList(ctx)

	for _, ar := range whiteList {
		if ar.Account == memberAccount.String() {
			err := k.stakingCheck(ctx, memberAccount, ar)
			if err != nil {
				return err
			}

			timeCheck := ar.GetJoinDaoTime().Add(k.GetClosePoolPeriod(ctx)).Add(time.Hour * 24)
			if ctx.BlockTime().Before(timeCheck) {
				return sdkerrors.Wrap(types.ErrTimeOut, "exp in vesting time, cannot burn")
			}

			k.RemoveRecord(ctx, memberAccount)
			err = k.addAddressToBurnRequestList(ctx, ar.GetAccount(), ar.MaxToken)

			if err != nil {
				return err
			}
			return nil
		}
	}
	return types.ErrAddressdNotFound
}

func (k ExpKeeper) executeMintRequest(ctx sdk.Context, fromAdress sdk.AccAddress, coin sdk.Coin) error {
	mintList, err := k.GetMintRequestList(ctx)
	if err != nil {
		return err
	}

	mintRequestList := mintList.MintRequestList

	for index, mintRequest := range mintRequestList {
		if mintRequest.Status == types.StatusOnGoingRequest && mintRequest.Account == fromAdress.String() {
			expWillGet := k.calculateDaoTokenValue(ctx, coin.Amount)
			if expWillGet.GTE(mintRequest.DaoTokenLeft) {
				coinSpend := sdk.NewCoin(k.GetIbcDenom(ctx), mintRequest.DaoTokenLeft.TruncateInt())

				err := k.FundExpPool(ctx, sdk.NewCoins(coinSpend), fromAdress)
				if err != nil {
					return err
				}

				mintRequest.DaoTokenLeft = sdk.NewDec(0)
				mintRequest.DaoTokenMinted = mintRequest.DaoTokenLeft.Add(mintRequest.DaoTokenMinted)
				mintRequest.Status = types.StatusCompleteRequest
			}
			err := k.FundExpPool(ctx, sdk.NewCoins(coin), fromAdress)
			if err != nil {
				return err
			}

			mintRequest.DaoTokenLeft = mintRequest.DaoTokenLeft.Add(expWillGet.Neg())
			mintRequest.DaoTokenMinted = mintRequest.DaoTokenLeft.Add(expWillGet)
		}
		mintRequestList[index] = mintRequest
		mintList.MintRequestList = mintRequestList
		k.SetMintRequestList(ctx, mintList)
	}

	return types.ErrWrongFundDenom
}