package types

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	types "github.com/cosmos/cosmos-sdk/types"
	"github.com/tendermint/tendermint/libs/time"
)

// NewGenesisState creates a new GenesisState object .
func NewGenesisState(whiteList AccountRecords, params Params, daoAssetInfo DaoAssetInfo) *GenesisState {
	return &GenesisState{
		WhiteList: whiteList,
		DaoAsset:  &daoAssetInfo,
		Params:    params,
	}
}

// DefaultGenesisState creates a default GenesisState object .
func DefaultGenesisState() *GenesisState {
	coin := types.NewCoin("uexp", types.NewInt(100000))
	data := AccountRecord{
		Account:     "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl",
		MaxToken:    &coin,
		JoinDaoTime: time.Now(),
	}

	return &GenesisState{
		WhiteList: []AccountRecord{
			data,
		},
		DaoAsset: &DaoAssetInfo{
			DaoTokenPrice: sdk.NewDec(1),
			AssetDao:      nil,
		},
		Params: DefaultParams(),
	}
}

// ValidateGenesis validates the provided genesis state to ensure the
// expected invariants holds.
func ValidateGenesis(data GenesisState) error {
	if err := data.Params.Validate(); err != nil {
		return err
	}
	return ValidateWhiteList(data.WhiteList)
}

func ValidateWhiteList(whiteList []AccountRecord) error {
	for _, accRecord := range whiteList {
		err := ValidateAccoutRecord(accRecord)
		if err != nil {
			return err
		}
	}
	return nil
}

func ValidateAccoutRecord(accRecord AccountRecord) error {
	return nil
}
