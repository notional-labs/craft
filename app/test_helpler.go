package app

import (
	"encoding/json"
	"os"
	"testing"

	"github.com/notional-labs/craft/x/exp/types"
	abci "github.com/tendermint/tendermint/abci/types"
	"github.com/tendermint/tendermint/libs/log"

	dbm "github.com/tendermint/tm-db"

	"github.com/cosmos/cosmos-sdk/simapp"
)

// Setup initializes a new CraftApp.
func Setup(isCheckTx bool) *CraftApp {
	db := dbm.NewMemDB()
	encCdc := simapp.MakeTestEncodingConfig()
	app := NewCraftApp(log.NewNopLogger(), db, nil, true, map[int64]bool{}, DefaultNodeHome, 5, MakeEncodingConfig(), GetEnabledProposals(), simapp.EmptyAppOptions{}, nil)
	if !isCheckTx {
		sapp := simapp.NewSimApp(log.NewNopLogger(), db, nil, true, map[int64]bool{}, simapp.DefaultNodeHome, 5, encCdc, simapp.EmptyAppOptions{})
		genesisState := simapp.GenesisStateWithSingleValidator(&testing.T{}, sapp)
		defaultGenesis := NewDefaultGenesisState(encCdc.Codec)
		genesisState[types.ModuleName] = defaultGenesis[types.ModuleName]
		stateBytes, err := json.MarshalIndent(genesisState, "", " ")
		if err != nil {
			panic(err)
		}

		app.InitChain(
			abci.RequestInitChain{
				Validators:      []abci.ValidatorUpdate{},
				ConsensusParams: simapp.DefaultConsensusParams,
				AppStateBytes:   stateBytes,
			},
		)
	}

	return app
}

// SetupTestingAppWithLevelDB initializes a new CraftApp intended for testing,
// with LevelDB as a db.
func SetupTestingAppWithLevelDB(isCheckTx bool) (app *CraftApp, cleanupFn func()) {
	dir := "craft_testing"
	encCdc := simapp.MakeTestEncodingConfig()

	db, err := dbm.NewDB("leveldb_testing", "goleveldb", dir)
	if err != nil {
		panic(err)
	}
	app = NewCraftApp(log.NewNopLogger(), db, nil, true, map[int64]bool{}, DefaultNodeHome, 5, MakeEncodingConfig(), GetEnabledProposals(), nil, nil)
	if !isCheckTx {
		genesisState := NewDefaultGenesisState(encCdc.Codec)
		stateBytes, err := json.MarshalIndent(genesisState, "", " ")
		if err != nil {
			panic(err)
		}
		app.InitChain(
			abci.RequestInitChain{
				Validators:      []abci.ValidatorUpdate{{}},
				ConsensusParams: simapp.DefaultConsensusParams,
				AppStateBytes:   stateBytes,
			},
		)
	}

	cleanupFn = func() {
		err = db.Close()
		if err != nil {
			panic(err)
		}
		err = os.RemoveAll(dir)
		if err != nil {
			panic(err)
		}
	}

	return app, cleanupFn
}
