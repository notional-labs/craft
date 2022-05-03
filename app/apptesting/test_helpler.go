package app

import (
	"encoding/json"
	"os"

	craftapp "github.com/notional-labs/craft/app"
	abci "github.com/tendermint/tendermint/abci/types"
	"github.com/tendermint/tendermint/libs/log"
	dbm "github.com/tendermint/tm-db"

	"github.com/cosmos/cosmos-sdk/simapp"
)

// Setup initializes a new CraftApp.
func Setup(isCheckTx bool) *craftapp.CraftApp {
	db := dbm.NewMemDB()
	encCdc := simapp.MakeTestEncodingConfig()

	app := craftapp.NewCraftApp(log.NewNopLogger(), db, nil, true, map[int64]bool{}, craftapp.DefaultNodeHome, 5, craftapp.MakeEncodingConfig(), craftapp.GetEnabledProposals(), nil, nil)
	if !isCheckTx {
		genesisState := craftapp.NewDefaultGenesisState(encCdc.Codec)
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
func SetupTestingAppWithLevelDB(isCheckTx bool) (app *craftapp.CraftApp, cleanupFn func()) {
	dir := "craft_testing"
	encCdc := simapp.MakeTestEncodingConfig()

	db, err := dbm.NewDB("leveldb_testing", "goleveldb", dir)
	if err != nil {
		panic(err)
	}
	app = craftapp.NewCraftApp(log.NewNopLogger(), db, nil, true, map[int64]bool{}, craftapp.DefaultNodeHome, 5, craftapp.MakeEncodingConfig(), craftapp.GetEnabledProposals(), nil, nil)
	if !isCheckTx {
		genesisState := craftapp.NewDefaultGenesisState(encCdc.Codec)
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
