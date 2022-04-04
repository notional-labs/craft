package types

const (
	// Module name .
	ModuleName = "exp"

	// StoreKey is the default store key for exp .
	StoreKey = ModuleName

	// Query endpoints supported by the exp querier .
	QueryParameters    = "parameters"
	QueryWhiteList     = "whiteList"
	QueryDaoTokenPrice = "daotokenprice"
)

var (
	// ExpKey is the key to use for the keeper store .
	ExpKey = []byte{0x00}

	// KeyDaoInfo defines key to store the DaoInfo .
	KeyDaoInfo = []byte{0x01}
)
