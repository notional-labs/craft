import { CosmWasmClient, SigningCosmWasmClient, Secp256k1HdWallet, GasPrice, coin, calculateFee } from "cosmwasm";

import * as fs from 'fs';
// import axios from 'axios';
import { getAccountFromMnemonic, getBalance, getRandomAccount, sendTokensToAccount } from "./helpers";
import assert from "assert";

// ! npm test

const rpcEndpoint = "https://craft-rpc.crafteconomy.io:443";
const config = {
    chainId: "craft-v5",
    rpcEndpoint: rpcEndpoint,
    prefix: "craft",
    gasPrice: GasPrice.fromString("0.03ucraft"),
};

const fee = calculateFee(200_000, GasPrice.fromString("0.03ucraft"));
const CW721_CODE_ID = 1;



const cw721 = fs.readFileSync("../already_compiled/cw721_base.wasm"); // code id: 1
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
        let result = await sendTokensToAccount(client, data, randomAccount.account.address, config.rpcEndpoint, [coin(1, "ucraft")], fee);        
        assert.ok(result.code === 0);

        return new Promise((resolve) => resolve());
    }).timeout(50000);

    // TODO:
    // it("Instantiate code on to craft", async () => {
    //     let data = await getAccountFromMnemonic(mnemonic, "craft");
    //     // console.log(account.account.address);
    //     const client = await SigningCosmWasmClient.connectWithSigner(rpcEndpoint, data.wallet, config);

    //     let contracts = await uploadContracts(client, data);
    //     console.log(contracts.marketplace);         
        
    // }).timeout(50000);

    it("List CW721 on Marketplace", async() => {        
        let data = await getAccountFromMnemonic(mnemonic, "craft");   
        const randomAccount = await getRandomAccount("craft");

        const client = await SigningCosmWasmClient.connectWithSigner(rpcEndpoint, data.wallet, config);
        const randomAccountClient = await SigningCosmWasmClient.connectWithSigner(rpcEndpoint, randomAccount.wallet, config);
                 
        // init marketplace contract
        const m_name = `marketplace-testing-${Math.floor(Date.now() / 1000)}`; 
        const ADDRM = await initMarketplace(client, data, m_name); 
        
        // Ensure marketplace is initialized correctly
        let v = await client.queryContractSmart(ADDRM, {get_contract_info: {}});        
        assert.ok(v.name === m_name);
        assert.ok(v.denom === "ucraft");
        assert.ok(v.fee_receive_address === data.account.address);
        assert.ok(v.platform_fee === "5");
        assert.ok(v.is_selling_allowed === true);

        // init 721
        const cw721_name = `cw721-testing-${Math.floor(Date.now() / 1000)}`;
        const ADDR721 = await initCW721(client, data, cw721_name);

        console.log(ADDR721, ADDRM);

        // mint CW721 for the other user, transfer to marketplace. then main account buy
        // before we can mint we have to send them 1 token
        let result = await sendTokensToAccount(client, data, randomAccount.account.address, config.rpcEndpoint, [coin(1, "ucraft")], fee);       

        // mint a CW721 folr randomAccount.account.addres
        let res = await client.execute(data.account.address, ADDR721, {mint: {token_id: "1", owner: randomAccount.account.address, token_uri: "https://crafteconomy.io"}}, fee);
        console.log("res", res.transactionHash);

        // send that token to the marketplace from the randomAccount.account.address account
        let price = {list_price:"1000000"};
        let res2 = await randomAccountClient.execute(randomAccount.account.address, ADDR721, {send_nft: {contract: ADDRM, token_id: "1", msg: Buffer.from(JSON.stringify(price)).toString('base64')}}, fee);
        console.log("res2", res2.transactionHash);
        

        // query marketplace offerings
        let offerings = (await client.queryContractSmart(ADDRM, {get_offerings: {}})).offerings;   
        // console.log(offerings, offerings[0]);
        assert.ok(offerings[0].offering_id === "1");
        assert.ok(offerings[0].token_id === "1");
        assert.ok(offerings[0].contract_addr === ADDR721);
        assert.ok(offerings[0].seller === randomAccount.account.address);
        // TODO: ensure data matches


        // buy the token with our main account for 1 craft
        let res3 = await client.execute(data.account.address, ADDRM, {buy_nft: {offering_id: offerings[0].offering_id}}, fee, "buying NFT", [coin(1_000_000, "ucraft")]);
        console.log(res3.transactionHash);

        // query marketplace offerings (should be 0 now that it is bought)
        let offerings2 = (await client.queryContractSmart(ADDRM, {get_offerings: {}})).offerings;   
        assert.ok(offerings2.length === 0);

    }).timeout(100000);
});


const uploadContracts = async (client: any, data: any)  => {
    const m_res = await client.upload(data.account.address, marketplace, "auto");
    // const cw721_res = await client.upload(data.account.address, cw721, "auto"); // code id 1
    return {
        marketplace: m_res,
        // cw721: cw721_res,        
    };
}

const initMarketplace = async (client: any, data: any, m_name: string) => {    
    let res = await client.instantiate(data.account.address, 18, {name:m_name, denom:"ucraft", fee_receive_address:`${data.account.address}`,platform_fee:"5"}, "marketplace-testing", "auto", {admin: data.account.address});
    const ADDRM = res.contractAddress;
    return ADDRM;
};

const initCW721 = async (client: any, data: any, cw_name: string) => {    
    let res = await client.instantiate(data.account.address, CW721_CODE_ID, {name: cw_name,"symbol": "test","minter": `${data.account.address}`}, "cw721-testing", "auto", {admin: data.account.address});
    const ADDR721 = res.contractAddress;
    return ADDR721;
};


export { rpcEndpoint };