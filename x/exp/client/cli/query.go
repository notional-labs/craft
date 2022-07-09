package cli

import (
	"strings"

	"github.com/cosmos/cosmos-sdk/client"
	"github.com/cosmos/cosmos-sdk/client/flags"
	"github.com/notional-labs/craft/x/exp/types"
	"github.com/spf13/cobra"
)

func GetQueryCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:                        types.ModuleName,
		Short:                      "Querying commands for the exp module",
		DisableFlagParsing:         true,
		SuggestionsMinimumDistance: 2,
		RunE:                       client.ValidateCmd,
	}

	cmd.AddCommand(
		GetWhiteList(),
		GetDaoAsset(),
		GetMintRequestList(),
		GetBurnRequestList(),
	)

	return cmd
}

func GetWhiteList() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "white_list",
		Short: "Query whitelist",
		Long:  strings.TrimSpace("Query whitelist. etc"),
		Args:  cobra.ExactArgs(0),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientQueryContext(cmd)
			if err != nil {
				return err
			}

			queryClient := types.NewQueryClient(clientCtx)
			ctx := cmd.Context()

			res, err := queryClient.WhiteList(ctx, &types.QueryWhiteListRequest{})
			if err != nil {
				return err
			}

			return clientCtx.PrintObjectLegacy(res)
		},
	}
	flags.AddQueryFlagsToCmd(cmd)
	flags.AddPaginationFlagsToCmd(cmd, "white list")

	return cmd
}

func GetDaoAsset() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "dao_asset",
		Short: "Query DAO asset",
		Long:  strings.TrimSpace("Query dao asset. etc"),
		Args:  cobra.ExactArgs(0),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientQueryContext(cmd)
			if err != nil {
				return err
			}

			queryClient := types.NewQueryClient(clientCtx)
			ctx := cmd.Context()

			res, err := queryClient.DaoAsset(ctx, &types.QueryDaoAssetRequest{})
			if err != nil {
				return err
			}

			return clientCtx.PrintObjectLegacy(res)
		},
	}
	flags.AddQueryFlagsToCmd(cmd)

	return cmd
}

func GetMintRequestList() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "mint_request",
		Short: "Query mint request list ",
		Long:  strings.TrimSpace("Query mint request list. etc"),
		Args:  cobra.ExactArgs(0),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientQueryContext(cmd)
			if err != nil {
				return err
			}

			queryClient := types.NewQueryClient(clientCtx)
			ctx := cmd.Context()

			res, err := queryClient.MintRequestList(ctx, &types.QueryMintRequestListRequest{})
			if err != nil {
				return err
			}

			return clientCtx.PrintObjectLegacy(res)
		},
	}
	flags.AddQueryFlagsToCmd(cmd)

	return cmd
}

func GetBurnRequestList() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "burn_request",
		Short: "Query burn request list ",
		Long:  strings.TrimSpace("Query burn request list. etc"),
		Args:  cobra.ExactArgs(0),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientQueryContext(cmd)
			if err != nil {
				return err
			}

			queryClient := types.NewQueryClient(clientCtx)
			ctx := cmd.Context()

			res, err := queryClient.BurnRequestList(ctx, &types.QueryBurnRequestListRequest{})
			if err != nil {
				return err
			}

			return clientCtx.PrintObjectLegacy(res)
		},
	}
	flags.AddQueryFlagsToCmd(cmd)

	return cmd
}
