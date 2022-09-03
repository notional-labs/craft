import { CosmWasmClient, SigningCosmWasmClient, Secp256k1HdWallet, GasPrice, coin, calculateFee } from "cosmwasm";

import * as fs from 'fs';
// import axios from 'axios';
import { getAccountFromMnemonic, getBalance, getRandomAccount } from "./helpers";
import assert from "assert";

// ! npm test

const rpcEndpoint = "https://craft-rpc.crafteconomy.io:443";
const config = {
    chainId: "craft-v5",
    rpcEndpoint: rpcEndpoint,
    prefix: "craft",
    gasPrice: GasPrice.fromString("0.025ucraft"),
};

const fee = calculateFee(200_000, GasPrice.fromString("0.025ucraft"));

export { rpcEndpoint };


const cw721 = fs.readFileSync("../already_compiled/cw721_base.wasm");
const marketplace = fs.readFileSync("../already_compiled/craft_marketplace.wasm");

// THIS IS A TEST MNUMONIC ONLY FOR TESTING PURPOSES WITH CRAFT, DO NOT USE WITH ACTUAL FUNDS. USED IN test_script.sh 
// EVER. FOR ANY REASON. DO ANYTHING FOR ANYONE, FOR ANY REASON, EVER, NO MATTER WHAT. NO MATTER WHERE.
// OR WHO, OR WHO YOU ARE WITH, OR WHERE YOU ARE GOING, OR WHERE YOU'VE BEEN, EVER, FOR ANY REASON WHATSOEVER.
const mnemonic = "decorate bright ozone fork gallery riot bus exhaust worth way bone indoor calm squirrel merry zero scheme cotton until shop any excess stage laundry";
const mn_addr = "craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl"

describe("Testing Craft Wallets & Contracts", () => {
    xit("Ensure wallet has balance", async () => {
        let data = await getAccountFromMnemonic(mnemonic, "craft");
        assert.equal(data.account.address, mn_addr);

        const balance = await getBalance(data.account.address);                
        assert.ok(BigInt(balance.amount) > 0);
    });

    xit("Ensure sending tokens works", async () => {
        let data = await getAccountFromMnemonic(mnemonic, "craft");        
        const randomAccount = await getRandomAccount("craft");

        // send 1 token from data -> randomAccount.address
        const client = await SigningCosmWasmClient.connectWithSigner(rpcEndpoint, data.wallet, config);
        const result = await client.sendTokens(data.account.address, randomAccount.account.address, [coin(1, "ucraft")], fee);
        // console.log(result);
        assert.ok(result.code === 0);
        return new Promise((resolve) => resolve());
    }).timeout(50000);

    // TODO:
    xit("Instantiate code on to craft", async () => {
        let data = await getAccountFromMnemonic(mnemonic, "craft");
        // console.log(account.account.address);
        const client = await SigningCosmWasmClient.connectWithSigner(rpcEndpoint, data.wallet, config);

        console.log("uploading marketplace");
        // const m_res = await client.upload(data.account.address, marketplace, "auto");
        const cw721_res = await client.upload(data.account.address, cw721, "auto");

        console.log(cw721_res);        
        
    }).timeout(50000);

    it("Instantiate code on testnet", async() => {        
        let data = await getAccountFromMnemonic(mnemonic, "craft");
        // console.log(account.account.address);
        const client = await SigningCosmWasmClient.connectWithSigner(rpcEndpoint, data.wallet, config);
        
        // get the current epoch time
        const epoch = Math.floor(Date.now() / 1000);

        let res = await client.instantiate(data.account.address, 18, {name:`marketplace-testing-${epoch}`, denom:"ucraft", fee_receive_address:`${data.account.address}`,platform_fee:"5"}, "marketplace-testing", "auto", {admin: data.account.address});
        const ADDRM = res.contractAddress;
        // console.log(ADDRM);
        ///client.execute("juno10c3slrqx3369mfsr9670au22zvq082jaej8ve4", "", )

        let v = await client.queryContractSmart(ADDRM, {get_contract_info: {}});
        console.log(v);

        // {
        //     name: 'marketplace-testing-1662219984',
        //     denom: 'ucraft',
        //     fee_receive_address: 'craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl',
        //     platform_fee: '5',
        //     version: '0.5.1',
        //     contact: 'reece@crafteconomy.io',
        //     is_selling_allowed: true
        //   }

    }).timeout(20000);


});