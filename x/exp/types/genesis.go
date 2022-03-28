package types

// NewGenesisState creates a new GenesisState object
func NewGenesisState(whiteList []*AccountRecord, params Params) *GenesisState {
	return &GenesisState{
		WhiteList: whiteList,
		Params:    params,
	}
}

// DefaultGenesisState creates a default GenesisState object
func DefaultGenesisState() *GenesisState {
	return &GenesisState{
		WhiteList: []*AccountRecord{},
		Params:    DefaultParams(),
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

func ValidateWhiteList(whiteList []*AccountRecord) error {
	for _, accRecord := range whiteList {
		err := ValidateAccoutRecord(*accRecord)
		if err != nil {
			return err
		}
	}
	return nil
}

func ValidateAccoutRecord(accRecord AccountRecord) error {
	return nil
}
