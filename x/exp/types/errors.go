package types

import (
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
)

// x/bank module sentinel errors
var (
	ErrDenomNotMatch       = sdkerrors.Register(ModuleName, 2, "Denom not match")
	ErrDaoAccount          = sdkerrors.Register(ModuleName, 3, "only Dao can  mint exp")
	ErrInputOutputMismatch = sdkerrors.Register(ModuleName, 4, "cannot mint coin with amount > max_mint_amount")
	ErrAddressdNotFound    = sdkerrors.Register(ModuleName, 5, "address not found in whitelist")
	ErrInvalidKey          = sdkerrors.Register(ModuleName, 6, "invalid key")
)
