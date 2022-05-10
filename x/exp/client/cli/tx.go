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
	txCmd.AddCommand(NewSpendIbcAssetForExpCmd())
	txCmd.AddCommand(NewFundToExpModule())
	txCmd.AddCommand(NewAdjustDaoTokenPrice())
	return txCmd
}

func NewMintExpCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "mintexp [dao_member_address] [amount]",
		Short: `Mint exp for a dao member, this only execute by DAO account`,
		Long: `Mint exp for a dao member, this only execute by DAO account. [dao_member_address] should be on whitelist first.
You can check DAO account address by following command:  craftd q params subspace exp daoAccount
Also you can check whitelist by following command: 		craftd q exp whitelist`,
		Args: cobra.ExactArgs(2),
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
		Short: `Burn exp and exit dao.`,
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

func NewSpendIbcAssetForExpCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "spend [coins]",
		Short: `Spend ibc asset for receive exp and join DAO.`,
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientTxContext(cmd)
			if err != nil {
				return err
			}

			coins, err := sdk.ParseCoinsNormalized(args[0])
			if err != nil {
				return err
			}

			msg := types.NewMsgSpendIbcAssetToExp(clientCtx.GetFromAddress().String(), coins)

			return tx.GenerateOrBroadcastTxCLI(clientCtx, cmd.Flags(), msg)
		},
	}

	flags.AddTxFlagsToCmd(cmd)

	return cmd
}

func NewFundToExpModule() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "fund [coins]",
		Short: `Send [coins] to exp module.`,
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientTxContext(cmd)
			if err != nil {
				return err
			}

			coins, err := sdk.ParseCoinsNormalized(args[0])
			if err != nil {
				return err
			}

			msg := types.NewMsgFundExpPool(clientCtx.GetFromAddress().String(), coins)

			return tx.GenerateOrBroadcastTxCLI(clientCtx, cmd.Flags(), msg)
		},
	}

	flags.AddTxFlagsToCmd(cmd)

	return cmd
}

func NewAdjustDaoTokenPrice() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "adjust [price]",
		Short: `adjust dao token price to [price] .`,
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientTxContext(cmd)
			if err != nil {
				return err
			}

			price, err := sdk.NewDecFromStr(args[0])
			if err != nil {
				return err
			}

			msg := types.NewMsgAdjustDaoTokenPrice(clientCtx.GetFromAddress().String(), price)

			return tx.GenerateOrBroadcastTxCLI(clientCtx, cmd.Flags(), msg)
		},
	}

	flags.AddTxFlagsToCmd(cmd)

	return cmd
}

func NewSendCoinsByDAO() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "adjust [price]",
		Short: `adjust dao token price to [price] .`,
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			clientCtx, err := client.GetClientTxContext(cmd)
			if err != nil {
				return err
			}

			price, err := sdk.NewDecFromStr(args[0])
			if err != nil {
				return err
			}

			msg := types.NewMsgAdjustDaoTokenPrice(clientCtx.GetFromAddress().String(), price)

			return tx.GenerateOrBroadcastTxCLI(clientCtx, cmd.Flags(), msg)
		},
	}

	flags.AddTxFlagsToCmd(cmd)

	return cmd
}

// This func only for testing
// func NewJoinDaoCmd() *cobra.Command {
// 	cmd := &cobra.Command{
// 		Use:   "join [dao_member_address]",
// 		Short: `Burn exp and exit dao. Note, the'--from' flag is ignored as it is implied from [from_key_or_address].`,
// 		Args:  cobra.ExactArgs(1),
// 		RunE: func(cmd *cobra.Command, args []string) error {
// 			clientCtx, err := client.GetClientTxContext(cmd)
// 			if err != nil {
// 				return err
// 			}

// 			msg := types.NewMsgJoinDaoByNonIbcAsset(clientCtx.GetFromAddress(), 100000, "craft10d07y265gmmuvt4z0w9aw880jnsr700jm5qjn0")

// 			return tx.GenerateOrBroadcastTxCLI(clientCtx, cmd.Flags(), msg)
// 		},
// 	}

// 	flags.AddTxFlagsToCmd(cmd)

// 	return cmd
// }
