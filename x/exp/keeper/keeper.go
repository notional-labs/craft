package keeper

import (
	"errors"
	"fmt"
	"strings"

	"github.com/cosmos/cosmos-sdk/codec"
	storetypes "github.com/cosmos/cosmos-sdk/store/types"
	sdk "github.com/cosmos/cosmos-sdk/types"
	sdkerrors "github.com/cosmos/cosmos-sdk/types/errors"
	capabilitykeeper "github.com/cosmos/cosmos-sdk/x/capability/keeper"
	paramtypes "github.com/cosmos/cosmos-sdk/x/params/types"
	clienttypes "github.com/cosmos/ibc-go/v4/modules/core/02-client/types"
	channeltypes "github.com/cosmos/ibc-go/v4/modules/core/04-channel/types"
	host "github.com/cosmos/ibc-go/v4/modules/core/24-host"
	"github.com/notional-labs/craft/utils/obi"
	"github.com/notional-labs/craft/x/exp/types"
	oracletypes "github.com/notional-labs/craft/x/oracle"
	"github.com/tendermint/tendermint/libs/log"
)

// Keeper for expModule.
type ExpKeeper struct {
	cdc codec.BinaryCodec

	storeKey      storetypes.StoreKey
	paramSpace    paramtypes.Subspace
	accountKeeper types.AccountKeeper
	bankKeeper    types.BankKeeper

	channelKeeper types.ChannelKeeper
	portKeeper    types.PortKeeper
	scopedKeeper  types.ScopedKeeper
}

// oracleScriptCallData represents the data that should be OBI-encoded and sent to perform an oracle request.
type oracleScriptCallData struct {
	AddressRequest string `obi:"address_request"`
	RequestType    string `obi:"request_type"`
	Status         string `obi:"status"`
}

func NewKeeper(
	key storetypes.StoreKey,
	cdc codec.BinaryCodec,
	paramSpace paramtypes.Subspace,
	ak types.AccountKeeper,
	bk types.BankKeeper,
	pk types.PortKeeper,
	sk capabilitykeeper.ScopedKeeper,
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
		portKeeper:    pk,
		scopedKeeper:  sk,
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
	if newCoins.Len() != 1 || newCoins[0].Denom != k.GetDenom(ctx) {
		return errors.New("exp module only mint exp")
	}

	// mint coin for exp module
	err := k.bankKeeper.MintCoins(ctx, types.ModuleName, newCoins)
	if err != nil {
		return err
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
	if newCoins.Len() != 1 || newCoins[0].Denom != k.GetDenom(ctx) {
		return errors.New("exp module only burn exp")
	}

	// send coin from account to module
	err := k.bankKeeper.SendCoinsFromAccountToModule(ctx, dstAccount, types.ModuleName, newCoins)
	if err != nil {
		return err
	}

	// mint coin for exp module
	err = k.bankKeeper.BurnCoins(ctx, types.ModuleName, newCoins)
	if err != nil {
		return err
	}

	return nil
}

// verify Dao member: balances, whitelist .
func (k ExpKeeper) verifyAccountForMint(ctx sdk.Context, daoAddress sdk.AccAddress, dstAddress sdk.AccAddress, amountMint sdk.Coins) error {
	params := k.GetParams(ctx)

	if params.DaoAccount != daoAddress.String() {
		return sdkerrors.Wrapf(types.ErrDaoAccount, "DAO address must be %s not %s", params.DaoAccount, daoAddress.String())
	}

	whiteList := k.GetWhiteList(ctx)

	// check if dstAddress in whitelist .
	for _, accountRecord := range whiteList {
		if dstAddress.String() == accountRecord.Account {
			// amount check
			if amountMint[0].Amount.GT(accountRecord.MaxToken.Amount) {
				return types.ErrInputOutputMismatch
			}
			timeCheck := accountRecord.GetJoinDaoTime().Add(k.GetClosePoolPeriod(ctx))
			if ctx.BlockTime().After(timeCheck) {
				return types.ErrTimeOut
			}
			return nil
		}
	}
	return types.ErrAddressdNotFound
}

func (k ExpKeeper) verifyAccountToWhiteList(ctx sdk.Context, memberAddress sdk.AccAddress) error {
	// check if dstAddress already in whitelist .
	whiteList := k.GetWhiteList(ctx)

	for _, accountRecord := range whiteList {
		if memberAddress.String() == accountRecord.Account {
			return types.ErrAddressdNotFound
		}
	}
	return nil
}

func (k ExpKeeper) stakingCheck(ctx sdk.Context, memberAccount sdk.AccAddress, ar types.AccountRecord) error {
	balance := k.bankKeeper.GetBalance(ctx, memberAccount, k.GetDenom(ctx))
	if !ar.MaxToken.Amount.Equal(balance.Amount) {
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
		JoinDaoTime: ctx.BlockHeader().Time,
	}

	k.SetAccountRecord(ctx, memberAccount, accountRecord)

	return nil
}

// FundPoolForExp allows an account to directly fund the exp fund pool.
// The amount is first added to the distribution module account and then directly
// added to the pool. An error is returned if the amount cannot be sent to the
// module account.
func (k ExpKeeper) FundPoolForExp(ctx sdk.Context, amount sdk.Coins, sender sdk.AccAddress) error {
	if err := k.bankKeeper.SendCoinsFromAccountToModule(ctx, sender, types.ModuleName, amount); err != nil {
		return err
	}

	return nil
}

func (k ExpKeeper) requestBurnCoinFromAddress(ctx sdk.Context, memberAccount sdk.AccAddress) error {
	ar := k.GetAccountRecord(ctx, memberAccount)

	if (ar == types.AccountRecord{}) {
		return types.ErrAddressdNotFound
	}

	err := k.stakingCheck(ctx, memberAccount, ar)
	if err != nil {
		return err
	}
	timeCheck := ar.GetJoinDaoTime().Add(k.GetClosePoolPeriod(ctx))

	if ctx.BlockTime().Before(timeCheck) {
		return sdkerrors.Wrap(types.ErrTimeOut, fmt.Sprintf("exp in vesting time, cannot burn, UNLOCK TIME %s", timeCheck))
	}
	k.RemoveRecord(ctx, memberAccount)
	err = k.addAddressToBurnRequestList(ctx, ar.GetAccount(), ar.MaxToken)
	if err != nil {
		return err
	}
	return nil
}

// SendIbcOracle send a package to query exp price over ibc.
func (k ExpKeeper) SendIbcOracle(ctx sdk.Context, fromAddress string, coin sdk.Coin, status string, timeoutHeight clienttypes.Height, timeoutTimestamp uint64,
) error {
	requestType := "exp_price"
	// get IBC params
	sourcePort := "ibc-exp"
	sourceChannel := "channel-1"

	sourceChannelEnd, found := k.channelKeeper.GetChannel(ctx, sourcePort, sourceChannel)
	if !found {
		return sdkerrors.Wrapf(channeltypes.ErrChannelNotFound, "port ID (%s) channel ID (%s)", sourcePort, sourceChannel)
	}

	destinationPort := sourceChannelEnd.GetCounterparty().GetPortID()
	destinationChannel := sourceChannelEnd.GetCounterparty().GetChannelID()

	// Get the next sequence
	sequence, found := k.channelKeeper.GetNextSequenceSend(ctx, sourcePort, sourceChannel)
	if !found {
		return sdkerrors.Wrapf(
			channeltypes.ErrSequenceSendNotFound,
			"source port: %s, source channel: %s", sourcePort, sourceChannel,
		)
	}

	// set OracleID
	clientID := k.GetNextOracleID(ctx)

	oracleRequest := types.OracleRequest{
		OracleId:        clientID,
		Type:            status,
		AddressRequest:  fromAddress,
		AmountInRequest: coin,
	}
	k.SetNextOracleRequest(ctx, oracleRequest)

	// Begin createOutgoingPacket logic
	channelCap, ok := k.scopedKeeper.GetCapability(ctx, host.ChannelCapabilityPath(sourcePort, sourceChannel))
	if !ok {
		return sdkerrors.Wrap(channeltypes.ErrChannelCapabilityNotFound, "module does not own channel capability")
	}
	// Create the call data to be used
	data := oracleScriptCallData{
		AddressRequest: strings.ToLower(fromAddress),
		RequestType:    requestType,
		Status:         status,
	}

	// Serialize the call data using the OBI encoding
	callDataBz, err := obi.Encode(data)
	if err != nil {
		return err
	}

	feeAmount := sdk.NewCoin("uband", sdk.NewInt(100000)) // 0.1band to fee, need change by gov
	packetData := oracletypes.NewOracleRequestPacketData(
		fmt.Sprint(clientID),
		209, // oracletypes.OracleScriptID(oraclePrams.ScriptID),
		callDataBz,
		1,                       // oraclePrams.AskCount, need change to use gov param
		1,                       // oraclePrams.MinCount,need change to use gov param
		sdk.NewCoins(feeAmount), // oraclePrams.FeeAmount,need change to use gov param
		300000,                  // oraclePrams.PrepareGas,need change to use gov param
		300000,                  // oraclePrams.ExecuteGas,need change to use gov param
	)

	// Create the IBC packet
	packet := channeltypes.NewPacket(
		packetData.GetBytes(),
		sequence,
		sourcePort,
		sourceChannel,
		destinationPort,
		destinationChannel,
		timeoutHeight,
		timeoutTimestamp,
	)
	// Send the IBC packet
	err = k.channelKeeper.SendPacket(ctx, channelCap, packet)
	if err != nil {
		return err
	}

	return nil
}
