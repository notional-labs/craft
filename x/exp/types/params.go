package types

import (
	fmt "fmt"
	"time"

	sdk "github.com/cosmos/cosmos-sdk/types"
	paramtypes "github.com/cosmos/cosmos-sdk/x/params/types"
)

// Exp params default values .
const (
	// MUST MODIFY IN GENESIS WHEN MAINNET
	// After pass, ISO 8601 format for when they can no longer mint EXP from this proposal
	// TODO: Justify our choice of default here.
	DefaultClosePoolPeriod time.Duration = time.Minute * 100

	// After pass, ISO 8601 format for when they can no longer burn EXP
	// TODO: Justify our choice of default here.
	DefaultVestingPeriodEnd time.Duration = time.Minute * 1

	// Burning time .
	DefaultBurnPeriod time.Duration = time.Minute * 1
)

var (
	ParamStoreKeyMaxCoinMint      = []byte("maxCoinMint")
	ParamStoreKeyDaoAccount       = []byte("daoAccount")
	ParamStoreKeyDenom            = []byte("denom")
	ParamStoreKeyClosePoolPeriod  = []byte("closepool")
	ParamStoreKeyVestingPeriodEnd = []byte("vestingperiodvestend")
	ParamStoreKeyBurnPeriod       = []byte("vestingperiodburnend")
	ParamStoreIbcDenom            = []byte("ibcassetdenom")
	ParamStoreKeyScriptId         = []byte("scriptId")
	ParamStoreKeyAskCount         = []byte("askCount")
	ParamStoreKeyMinCount         = []byte("minCount")
	ParamStoreKeyFeeAmount        = []byte("feeAmount")
	ParamStoreKeyPrepareGas       = []byte("prepareGas")
	ParamStoreKeyExecuteGas       = []byte("executeGas")
)

// ParamKeyTable for exp module.
func ParamKeyTable() paramtypes.KeyTable {
	return paramtypes.NewKeyTable().RegisterParamSet(&Params{})
}

// NewParams create a new param for exp module .
func NewParams(maxCoinMint uint64, daoAccount string, denom string) Params {
	return Params{
		MaxCoinMint: maxCoinMint,
		DaoAccount:  daoAccount,
		Denom:       denom,
	}
}

// DefaultParams of ExpModule .
func DefaultParams() Params {
	return Params{
		MaxCoinMint:      uint64(10000000000),
		DaoAccount:       "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl",
		Denom:            "uexp",
		ClosePoolPeriod:  DefaultClosePoolPeriod,
		VestingPeriodEnd: DefaultVestingPeriodEnd,
		IbcAssetDenom:    "token",
		BurnExpPeriod:    DefaultBurnPeriod,
		ScriptId:         209,
		AskCount:         1,
		MinCount:         1,
		FeeAmount:        sdk.NewCoin("uband", sdk.NewInt(100000)),
		PrepareGas:       300000,
		ExecuteGas:       300000,
	}
}

func (p *Params) ParamSetPairs() paramtypes.ParamSetPairs {
	return paramtypes.ParamSetPairs{
		paramtypes.NewParamSetPair(ParamStoreKeyMaxCoinMint, &p.MaxCoinMint, validateMaxCoinMint),
		paramtypes.NewParamSetPair(ParamStoreKeyDaoAccount, &p.DaoAccount, validateDaoAccount),
		paramtypes.NewParamSetPair(ParamStoreKeyDenom, &p.Denom, validateDenom),
		paramtypes.NewParamSetPair(ParamStoreKeyClosePoolPeriod, &p.ClosePoolPeriod, validatePeriod),
		paramtypes.NewParamSetPair(ParamStoreKeyVestingPeriodEnd, &p.VestingPeriodEnd, validatePeriod),
		paramtypes.NewParamSetPair(ParamStoreKeyBurnPeriod, &p.BurnExpPeriod, validatePeriod),
		paramtypes.NewParamSetPair(ParamStoreIbcDenom, &p.IbcAssetDenom, validateDenom),
		paramtypes.NewParamSetPair(ParamStoreKeyScriptId, &p.ScriptId, validateScriptId),
		paramtypes.NewParamSetPair(ParamStoreKeyAskCount, &p.AskCount, validateAskCount),
		paramtypes.NewParamSetPair(ParamStoreKeyMinCount, &p.MinCount, validateMinCount),
		paramtypes.NewParamSetPair(ParamStoreKeyFeeAmount, &p.FeeAmount, validateFeeAmount),
		paramtypes.NewParamSetPair(ParamStoreKeyPrepareGas, &p.PrepareGas, validatePrepareGas),
		paramtypes.NewParamSetPair(ParamStoreKeyExecuteGas, &p.ExecuteGas, validateExecuteGas),
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

func validatePeriod(i interface{}) error {
	v, ok := i.(time.Duration)
	if !ok {
		return fmt.Errorf("invalid parameter type: %T", i)
	}

	if v <= 0 {
		return fmt.Errorf("time must be positive: %d", v)
	}

	return nil
}

func validateScriptId(i interface{}) error {
	_, ok := i.(uint64)
	if !ok {
		return fmt.Errorf("invalid parameter type: %s", i)
	}
	return nil
}

func validateAskCount(i interface{}) error {
	_, ok := i.(uint64)
	if !ok {
		return fmt.Errorf("invalid parameter type: %s", i)
	}
	return nil
}

func validateMinCount(i interface{}) error {
	_, ok := i.(uint64)
	if !ok {
		return fmt.Errorf("invalid parameter type: %s", i)
	}
	return nil
}

func validatePrepareGas(i interface{}) error {
	_, ok := i.(uint64)
	if !ok {
		return fmt.Errorf("invalid parameter type: %s", i)
	}
	return nil
}

func validateExecuteGas(i interface{}) error {
	_, ok := i.(uint64)
	if !ok {
		return fmt.Errorf("invalid parameter type: %s", i)
	}
	return nil
}

func validateFeeAmount(i interface{}) error {
	v, ok := i.(sdk.Coin)
	if !ok {
		return fmt.Errorf("invalid parameter type: %T", i)
	}

	if v.Validate() != nil {
		return fmt.Errorf("invalid fee amount: %+v", i)
	}

	return nil
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
