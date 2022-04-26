package types

import (
	"encoding/binary"

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

// MintRequest key in kvstore is : KeyMintRequest + Status + AddressByte

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
	// KeyWhiteList defines key to store the WhiteList .
	KeyWhiteList = []byte{0x05}
)

func GetMintRequestsStatusBytes(requestStatus int) (mintRequestsBytes []byte) {

	mintRequestsBytes = make([]byte, 8)
	binary.BigEndian.PutUint64(mintRequestsBytes, uint64(requestStatus))

	return mintRequestsBytes
}

func GetMintRequestAddressBytes(requestStatus int, addressRequest sdk.AccAddress) []byte {
	var mintRequestsBytes = make([]byte, 8)
	binary.BigEndian.PutUint64(mintRequestsBytes, uint64(requestStatus))

	return append(mintRequestsBytes, address.MustLengthPrefix(addressRequest.Bytes())...)
}

func GetBurnRequestsStatusBytes(requestStatus int) (burnRequestsBytes []byte) {

	burnRequestsBytes = make([]byte, 8)
	binary.BigEndian.PutUint64(burnRequestsBytes, uint64(requestStatus))

	return burnRequestsBytes
}

func GetBurnRequestAddressBytes(requestStatus int, addressRequest sdk.AccAddress) []byte {
	var burnRequestsBytes = make([]byte, 8)
	binary.BigEndian.PutUint64(KeyBurnRequestList, uint64(requestStatus))

	return append(burnRequestsBytes, address.MustLengthPrefix(addressRequest.Bytes())...)
}

func GetWhiteListByAddressBytes(addressRequest sdk.AccAddress) []byte {
	return append(KeyWhiteList, address.MustLengthPrefix(addressRequest.Bytes())...)
}
