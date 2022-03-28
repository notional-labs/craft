package types

import (
	fmt "fmt"

	sdk "github.com/cosmos/cosmos-sdk/types"
	paramtypes "github.com/cosmos/cosmos-sdk/x/params/types"
)

var (
	ParamStoreKeyMaxCoinMint = []byte("max_coin_mint")
	ParamStoreKeyDaoAccount  = []byte("dao_account")
	ParamStoreKeyDenom       = []byte("denom")
)

// ParamTable for exp module.
func ParamKeyTable() paramtypes.KeyTable {
	return paramtypes.NewKeyTable().RegisterParamSet(&Params{})
}

func NewParams(MaxCoinMint uint64, DaoAccount string, denom string) Params {
	return Params{
		MaxCoinMint: MaxCoinMint,
		DaoAccount:  DaoAccount,
		Denom:       denom,
	}
}

func DefaultParams() Params {
	return Params{
		MaxCoinMint: uint64(100000),
		DaoAccount:  "craft16pctk89ystuwg4gv2dgj5lwtsavy9pkfdxlc5u",
		Denom:       "exp2",
	}
}

func (p Params) ParamSetPairs() paramtypes.ParamSetPairs {
	return paramtypes.ParamSetPairs{
		paramtypes.NewParamSetPair(ParamStoreKeyMaxCoinMint, &p.MaxCoinMint, validateMaxCoinMint),
		paramtypes.NewParamSetPair(ParamStoreKeyDaoAccount, &p.DaoAccount, validateDaoAccount),
		paramtypes.NewParamSetPair(ParamStoreKeyDenom, &p.Denom, validateDenom),
	}
}

func validateMaxCoinMint(i interface{}) error {
	_, ok := i.(uint64)
	if !ok {
		return fmt.Errorf("invalid parameter type: %s", i)
	}
	return nil
}

func validateDaoAccount(i interface{}) error {
	daoAccount, ok := i.(string)
	if !ok {
		return fmt.Errorf("invalid parameter DaoAccount type: %T", i)
	}

	if _, err := sdk.AccAddressFromBech32(daoAccount); err != nil {
		return err
	}
	return nil
}

func validateDenom(i interface{}) error {
	denom, ok := i.(string)
	if !ok {
		return fmt.Errorf("invalid parameter denom type: %T", i)
	}

	return sdk.ValidateDenom(denom)
}

func (p Params) Validate() error {
	if err := validateDaoAccount(p.DaoAccount); err != nil {
		return err
	}
	if err := validateMaxCoinMint(p.MaxCoinMint); err != nil {
		return err
	}
	return nil
}
