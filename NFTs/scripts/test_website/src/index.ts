/**
 * The point of this program is to show how to interact with the marketplace via TS with CosmJS.
 * Marketplace has its own CraftAPI to make it simple to read the data from the marketplace.
 * - Craft 721's use base64 encoded JSON data to store more efficiently.
 * - This makes it useful to use our REST api vs just strait up querying all of it.
 * - Our API also uses a redis cache to ensure data is very quick back to the user on load.
 * - Extra data is also added in such as USD cost, and _nft_type within the token_data object
 * - This marketplace is compatible with: Real Estate, Skins, and standard 721 contracts :) and the format is all the same for any
 */


import axios from 'axios';
import { stringToPath } from "@cosmjs/crypto";

import { StdFee, assertIsDeliverTxSuccess, calculateFee, GasPrice, 
    SigningStargateClient, StargateClient, QueryClient, coin, coins  } from "@cosmjs/stargate";


import { CosmWasmClient, SigningCosmWasmClient, Secp256k1HdWallet, SigningCosmWasmClientOptions, Secp256k1HdWalletOptions } from "cosmwasm"; // https://github.com/CosmWasm/CosmWasmJS


const offering_api = 'http://api.crafteconomy.io/v1/marketplace/offerings';
// http://api.crafteconomy.io/v1/nfts/get_contract_addresses
const marketplace_721 = "craft1nwp0ynjv84wxysf2f5ctvysl6dpm8ngm70hss6jeqt8q7e7u345sgynrhu"

const rpcEndpoint = "http://65.108.125.182:26657/";


async function main() {
  // console.log(client);
  const client = CosmWasmClient.connect(rpcEndpoint);
  getAvailableOfferings(client);

  buyNFT("2", 20856);

}

main();

// export function getAllNFTs(who: string = offering_api) {
export async function getAvailableOfferings(client) {
  // While you could get it this way, you don't get all the data that the API provides extra.
  // const offerings = await client.queryContractSmart(marketplace_721, { get_offerings: {} });
  // console.log(offerings);


  // make an axios requests
  const response = await axios.get(offering_api);
  const offerings = response.data;
  console.log(offerings);

}


export async function buyNFT(MarketplaceID: string, ucraft_amt: number) {
  // buy an NFT from the marketplace with CosmWasmJS
  // teskey_test 2 mnumonic from test script
  const gasPrice = GasPrice.fromString("0.025ucraft");
  const fee = calculateFee(200_000, gasPrice);

    // create an object based on Secp256k1HdWalletOptions
    // const options = new Secp256k1HdWalletOptions () 


  const mnemonic = "flag meat remind stamp unveil junior goose first hold atom deny ramp raven party lens jazz tape dad produce wrap citizen common vital hungry";
  // readonly bip39Password: string;
  // /** The BIP-32/SLIP-10 derivation paths. Defaults to the Cosmos Hub/ATOM path `m/44'/118'/0'/0/0`. */
  // readonly hdPaths: readonly HdPath[];
  // /** The bech32 address prefix (human readable part). Defaults to "cosmos". */
  // readonly prefix: string;
  const options2: Secp256k1HdWalletOptions = {
    bip39Password: "",
    hdPaths: [stringToPath("m/44'/118'/0'/0/0")],
    prefix: "craft",
  }
  const wallet = await Secp256k1HdWallet.fromMnemonic(mnemonic, options2);

  // const options = { ...defaultSigningClientOptions,  };

  const options: SigningCosmWasmClientOptions = {
    broadcastPollIntervalMs: 300,
    broadcastTimeoutMs: 8_000,
    gasPrice: gasPrice,
    prefix: "craft"
  };

  const client = await SigningCosmWasmClient.connectWithSigner(
    rpcEndpoint,
    wallet,
    options,
  );
  const account = await wallet.getAccounts();
  console.log(account);


  // client.execute(senderAddress: string, contractAddress: string, msg: Record<string, unknown>, fee: StdFee | "auto" | number, memo?: string, funds?: readonly Coin[]): Promise<ExecuteResult>;
  // {"buy_nft":{"offering_id":"2"}}
  // Execute contract message WITH funds to actually buy the NFT. ucraft_amt = the offering price exactly, tho it overpay if they wanted too.
  const resp = await client.execute(
      "craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r", 
      marketplace_721, 
      { buy_nft: { offering_id: MarketplaceID } }, 
      fee,     
      "here is my memo", 
      coins(ucraft_amt, "ucraft")
  );  
  console.log(resp);
}


export async function queryTokensUserOwns(craft_address: string) {
  // craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl","start_after":"0","limit":50}}' // DAO

  // craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r","start_after":"0","limit":50}}'

  // http://api.crafteconomy.io/v1/nfts/owned/craft1udfs22xpxle475m2nz7u47jfa3vngncdegmczwwdx00cmetypa3s5mr4eq/craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r
}