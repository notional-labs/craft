package cli

import (
	"fmt"
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
	)

	return cmd
}

func GetWhiteList() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "whitelist",
		Short: "Query whitelist",
		Long:  strings.TrimSpace(fmt.Sprintf(`Query whitelist. etc`)),
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

			return clientCtx.PrintObjectLegacy(res.AccountRecord)
		},
	}
	flags.AddQueryFlagsToCmd(cmd)
	flags.AddPaginationFlagsToCmd(cmd, "white list")

	return cmd
}
