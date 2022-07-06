package cli_test

import (
	"context"
	"fmt"
	"testing"

	"github.com/cosmos/cosmos-sdk/client"
	"github.com/cosmos/cosmos-sdk/server"
	"github.com/cosmos/cosmos-sdk/types/module"
	"github.com/cosmos/cosmos-sdk/x/genutil"
	genutiltest "github.com/cosmos/cosmos-sdk/x/genutil/client/testutil"
	"github.com/tendermint/tendermint/libs/log"

	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/app"
	cli "github.com/notional-labs/craft/x/exp/client/cli"
	"github.com/spf13/viper"
	"github.com/stretchr/testify/require"
)

var testMbm = module.NewBasicManager(genutil.AppModuleBasic{})
var (
	defaultAcctFunds sdk.Coins = sdk.NewCoins(
		sdk.NewCoin("token", sdk.NewInt(10000000)),
	)
	daoAddress = "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"
)

func TestGetWhiteList(t *testing.T) {
	tests := []struct {
		name      string
		expectErr bool
	}{
		{
			name:      "white_list",
			expectErr: false,
		},
		{
			name:      "not white_list",
			expectErr: true,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli := cli.GetWhiteList()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestGetDaoAsset(t *testing.T) {
	tests := []struct {
		name      string
		expectErr bool
	}{
		{
			name:      "dao_asset",
			expectErr: false,
		},
		{
			name:      "not dao_asset",
			expectErr: true,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli := cli.GetDaoAsset()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestGetMintRequestList(t *testing.T) {
	tests := []struct {
		name      string
		expectErr bool
	}{
		{
			name:      "mint_request",
			expectErr: false,
		},
		{
			name:      "not mint_request",
			expectErr: true,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli := cli.GetMintRequestList()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestGetBurnRequestList(t *testing.T) {
	tests := []struct {
		name      string
		expectErr bool
	}{
		{
			name:      "burn_request",
			expectErr: false,
		},
		{
			name:      "not burn_request",
			expectErr: true,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli.GetBurnRequestList()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestNewMintExpCmd(t *testing.T) {
	tests := []struct {
		name               string
		dao_member_address string
		amount             string
		expectErr          bool
	}{
		{
			name:               "mintexp",
			dao_member_address: "",
			amount:             "",
			expectErr:          true,
		},
		{
			name:               "mintexp",
			dao_member_address: daoAddress,
			amount:             "100atom",
			expectErr:          false,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli.NewMintExpCmd()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestNewSpendIbcAssetForExpCmd(t *testing.T) {
	fmt.Println("[LOG]: Get White List")
	tests := []struct {
		name               string
		dao_member_address string
		expectErr          bool
	}{
		{
			name:               "spend",
			dao_member_address: "",
			expectErr:          true,
		},
		{
			name:               "spend",
			dao_member_address: "",
			expectErr:          false,
		},
		{
			name:               "not spend",
			dao_member_address: "100atom",
			expectErr:          true,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli.NewSpendIbcAssetForExpCmd()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestNewBurnExpCmd(t *testing.T) {
	tests := []struct {
		name               string
		dao_member_address string
		expectErr          bool
	}{
		{
			name:               "burnexp",
			dao_member_address: "",
			expectErr:          true,
		},
		{
			name:               "burnexp",
			dao_member_address: daoAddress,
			expectErr:          false,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli.NewBurnExpCmd()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestNewFundToExpModule(t *testing.T) {
	tests := []struct {
		name      string
		coins     string
		expectErr bool
	}{
		{
			name:      "fund",
			coins:     "",
			expectErr: true,
		},
		{
			name:      "fund",
			coins:     "100atom",
			expectErr: false,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli.NewFundToExpModule()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestNewSendCoinsToDAO(t *testing.T) {
	tests := []struct {
		name      string
		amount    string
		expectErr bool
	}{
		{
			name:      "fund",
			amount:    "",
			expectErr: true,
		},
		{
			name:      "fund",
			amount:    "100atom",
			expectErr: false,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli.NewSendCoinsToDAO()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}

func TestNewAdjustDaoTokenPrice(t *testing.T) {
	tests := []struct {
		name      string
		price     string
		expectErr bool
	}{
		{
			name:      "fund",
			price:     "",
			expectErr: true,
		},
		{
			name:      "fund",
			price:     "1$",
			expectErr: false,
		},
	}
	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			cli.GetQueryCmd()
			home := t.TempDir()
			logger := log.NewNopLogger()
			cfg, err := genutiltest.CreateDefaultTendermintConfig(home)
			require.NoError(t, err)

			appCodec := app.MakeEncodingConfig().Codec
			err = genutiltest.ExecInitCmd(testMbm, home, appCodec)
			require.NoError(t, err)

			serverCtx := server.NewContext(viper.New(), cfg, logger)
			clientCtx := client.Context{}.WithCodec(appCodec).WithHomeDir(home)

			ctx := context.Background()
			ctx = context.WithValue(ctx, client.ClientContextKey, &clientCtx)
			ctx = context.WithValue(ctx, server.ServerContextKey, serverCtx)

			cli.NewAdjustDaoTokenPrice()

			if tc.expectErr {
				require.Error(t, cli.ExecuteContext(ctx))
			} else {
				require.NoError(t, cli.ExecuteContext(ctx))
			}
		})
	}
}
