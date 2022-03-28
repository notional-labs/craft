package types

// MinterKey is the key to use for the keeper store.
var ExpKey = []byte{0x00}

const (
	// module name
	ModuleName = "exp"

	// StoreKey is the default store key for mint
	StoreKey = ModuleName

	// Query endpoints supported by the exp querier
	QueryParameters = "parameters"
	QueryWhiteList  = "whiteList"
)

var (
	//KeyDaoInfo defines key to store the DaoInfo
	KeyDaoInfo = []byte{0x01}
)
