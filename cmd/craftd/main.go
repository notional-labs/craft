package main

import (
	"os"

	"github.com/cosmos/cosmos-sdk/server"
	svrcmd "github.com/cosmos/cosmos-sdk/server/cmd"
	"github.com/notional-labs/craft/app"
	"github.com/notional-labs/craft/app/params"
	"github.com/notional-labs/craft/cmd/craftd/cmd"
)

//nolint:errorlint
func main() {
	params.SetAddressPrefixes()
	rootCmd, _ := cmd.NewRootCmd()

	if err := svrcmd.Execute(rootCmd, "CRAFT", app.DefaultNodeHome); err != nil {
		switch e := err.(type) {
		case server.ErrorCode:
			os.Exit(e.Code)

		default:
			os.Exit(1)
		}
	}
}
