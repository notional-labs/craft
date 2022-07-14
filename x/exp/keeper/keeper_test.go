package keeper_test

import (
	"testing"

	"github.com/stretchr/testify/suite"

	sdk "github.com/cosmos/cosmos-sdk/types"

	"github.com/notional-labs/craft/app/apptesting"
	"github.com/notional-labs/craft/x/exp/keeper"
	"github.com/notional-labs/craft/x/exp/types"

	stakingtypes "github.com/cosmos/cosmos-sdk/x/staking/types"
)

type KeeperTestSuite struct {
	apptesting.KeeperTestHelper

	queryClient types.QueryClient
	querier     keeper.ExpKeeper
}

func (suite *KeeperTestSuite) SetupTest() {
	suite.Setup()
	suite.queryClient = types.NewQueryClient(suite.QueryHelper)
	suite.querier = keeper.ExpKeeper{}
}

func (suite *KeeperTestSuite) SetupValidators(bondStatuses []stakingtypes.BondStatus) []sdk.ValAddress {
	valAddrs := []sdk.ValAddress{}
	for _, status := range bondStatuses {
		valAddr := suite.SetupValidator(status)
		valAddrs = append(valAddrs, valAddr)
	}
	return valAddrs
}

func TestKeeperTestSuite(t *testing.T) {
	suite.Run(t, new(KeeperTestSuite))
}
