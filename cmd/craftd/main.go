package main

import (
	"os"

	"github.com/cosmos/cosmos-sdk/server"
	srvcmd "github.com/cosmos/cosmos-sdk/server/cmd"
	"github.com/notional-labs/craft/app"
	"github.com/notional-labs/craft/cmd"
)

func main() {
	rootCmd, _ := cmd.NewRootCmd()

	if err := srvcmd.Execute(rootCmd, app.DefaultNodeHome); err != nil {
		switch e := err.(type) {
		case server.ErrorCode:
			os.Exit(e.Code)

		default:
			os.Exit(1)
		}
	}
}
