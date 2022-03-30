package cli

import (
	"github.com/spf13/cobra"

	"github.com/cosmos/cosmos-sdk/client"
	"github.com/cosmos/cosmos-sdk/client/flags"
	"github.com/cosmos/cosmos-sdk/client/tx"
	sdk "github.com/cosmos/cosmos-sdk/types"
	"github.com/notional-labs/craft/x/exp/types"
)

// NewTxCmd returns a root CLI command handler for all x/exp transaction commands.
func NewTxCmd() *cobra.Command {
	txCmd := &cobra.Command{
		Use:                        types.ModuleName,
		Short:                      "Exp transaction subcommands",
		DisableFlagParsing:         true,
		SuggestionsMinimumDistance: 2,
		RunE:                       client.ValidateCmd,
	}

	txCmd.AddCommand(NewMintExpCmd())
	txCmd.AddCommand(NewBurnExpCmd())
	txCmd.AddCommand(NewJoinDaoCmd())
	return txCmd
}

func NewMintExpCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "mintexp [dao_member_address] [amount]",
		Short: `Mint exp for a dao member. Note, the'--from' flag is ignored as it is implied from [from_key_or_address].`,
		Args:  cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientTxContext(cmd)
			if err != nil {
				return err
			}
			toAddr, err := sdk.AccAddressFromBech32(args[0])
			if err != nil {
				return err
			}

			coins, err := sdk.ParseCoinsNormalized(args[1])
			if err != nil {
				return err
			}

			msg := types.NewMsgMintAndAllocateExp(clientCtx.GetFromAddress(), toAddr, coins)

			return tx.GenerateOrBroadcastTxCLI(clientCtx, cmd.Flags(), msg)
		},
	}

	flags.AddTxFlagsToCmd(cmd)

	return cmd
}

func NewBurnExpCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "burnexp [dao_member_address]",
		Short: `Burn exp and exit dao. Note, the'--from' flag is ignored as it is implied from [from_key_or_address].`,
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientTxContext(cmd)
			if err != nil {
				return err
			}

			msg := types.NewMsgBurnAndRemoveMember(clientCtx.GetFromAddress(), args[0])

			return tx.GenerateOrBroadcastTxCLI(clientCtx, cmd.Flags(), msg)
		},
	}

	flags.AddTxFlagsToCmd(cmd)

	return cmd
}

func NewJoinDaoCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "join [dao_member_address]",
		Short: `Burn exp and exit dao. Note, the'--from' flag is ignored as it is implied from [from_key_or_address].`,
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientTxContext(cmd)
			if err != nil {
				return err
			}

			msg := types.NewMsgJoinDao(clientCtx.GetFromAddress(), 100000, "craft10d07y265gmmuvt4z0w9aw880jnsr700jm5qjn0")

			return tx.GenerateOrBroadcastTxCLI(clientCtx, cmd.Flags(), msg)
		},
	}

	flags.AddTxFlagsToCmd(cmd)

	return cmd
}
