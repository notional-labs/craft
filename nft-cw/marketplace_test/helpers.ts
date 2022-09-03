import { Coin, coin, SigningStargateClient } from "@cosmjs/stargate";
import { Secp256k1HdWallet } from "cosmwasm";
import { rpcEndpoint } from "./0_test";

export const getBalance = async (wallet_addr: any) => {
    // get craft escrow account
    let balance = coin("0", "ucraft");
    try {
        const client = await SigningStargateClient.connectWithSigner(rpcEndpoint, wallet_addr);        
        balance = await client.getBalance(wallet_addr, "ucraft")        
    } catch (error) {
        console.log("getCraftBalance", error);
    }
    return balance;
}


export const getAccountFromMnemonic = async (mnemonic: any, prefix: string = "cosmos") => {
    let wallet = await Secp256k1HdWallet.fromMnemonic(mnemonic, { prefix: prefix });
    const [account] = await wallet.getAccounts();
    return {
        wallet: wallet,
        account: account,
    }
}

export const getRandomAccount = async (prefix: string = "cosmos") => {
    let wallet = await Secp256k1HdWallet.generate(12, { prefix: prefix });
    const [account] = await wallet.getAccounts();
    return {
        wallet: wallet,
        account: account
    }
};


export const sendTokensToAccount = async (client: any, data: any, to_address: string, rpc: string, coins: [Coin], fee: any) => {
    const result = await client.sendTokens(data.account.address, to_address, coins, fee);
    return result;
}