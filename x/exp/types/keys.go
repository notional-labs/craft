package types

import (
	"github.com/cosmos/cosmos-sdk/types/address"

	sdk "github.com/cosmos/cosmos-sdk/types"
)

const (
	// Module name .
	ModuleName = "exp"

	// StoreKey is the default store key for exp .
	StoreKey = ModuleName

	// Query endpoints supported by the exp querier .
	QueryParameters = "parameters"
	QueryWhiteList  = "whiteList"
	QueryDaoAsset   = "daoasset"
)

// MintRequest key in kvstore is : KeyMintRequest + AddressByte
// BurnRequest key in kvstore is : KeyBurnRequest + AddressByte

var (
	// ExpKey is the key to use for the keeper store .
	ExpKey = []byte{0x00}
	// KeyDaoInfo defines key to store the DaoInfo .
	KeyDaoInfo = []byte{0x01}
	// KeyDaoAssetInfo defines key to store the DaoAssetInfo .
	KeyDaoAssetInfo = []byte{0x02}
	// KeyMintRequestList defines key to store the MintRequestList .
	KeyMintRequestList = []byte{0x03}
	// KeyBurnRequestList defines key to store the BurnRequestList .
	KeyBurnRequestList = []byte{0x04}
	// KeyCompletedMintRequest defines key to store the CompletedMintRequest .
	KeyCompletedMintRequest = []byte{0x05}
	// KeyCompletedBurnRequest defines key to store the CompletedBurnRequest .
	KeyCompletedBurnRequest = []byte{0x06}
	// KeyWhiteList defines key to store the WhiteList .
	KeyWhiteList = []byte{0x07}
	// KeyOracleID key to store the OracleID .
	KeyOracleID = []byte{0x08}
	// KeyOracleRequest to store the Oracle Request .
	KeyOracleRequest = []byte{0x09}
)

func GetMintRequestAddressBytes(addressRequest sdk.AccAddress) (mintRequestsBytes []byte) {
	return append(KeyMintRequestList, address.MustLengthPrefix(addressRequest.Bytes())...)
}

func GetEndedMintRequestKey(addressRequest sdk.AccAddress) []byte {
	return append(KeyCompletedMintRequest, address.MustLengthPrefix(addressRequest.Bytes())...)
}

func GetBurnRequestAddressBytes(addressRequest sdk.AccAddress) (mintRequestsBytes []byte) {
	return append(KeyBurnRequestList, address.MustLengthPrefix(addressRequest.Bytes())...)
}

func GetEndedBurnRequestKey(addressRequest sdk.AccAddress) []byte {
	return append(KeyCompletedBurnRequest, address.MustLengthPrefix(addressRequest.Bytes())...)
}

func GetAccountRecordKey(addressRequest sdk.AccAddress) []byte {
	return append(KeyWhiteList, address.MustLengthPrefix(addressRequest.Bytes())...)
}

const (
	// IBCPortID is the default port id that profiles module binds to.
	IBCPortID = "ibc-exp"
)

// IBCPortKey defines the key to store the port ID in store.
var IBCPortKey = []byte{0x01}
