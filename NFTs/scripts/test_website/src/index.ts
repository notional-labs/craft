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
import { calculateFee, GasPrice, coins } from "@cosmjs/stargate";
import { CosmWasmClient, SigningCosmWasmClient, Secp256k1HdWallet, SigningCosmWasmClientOptions, Secp256k1HdWalletOptions } from "cosmwasm"; // https://github.com/CosmWasm/CosmWasmJS

const rpcEndpoint = "http://65.108.125.182:26657/";
const offering_api = 'http://api.crafteconomy.io/v1/marketplace/offerings';
// http://api.crafteconomy.io/v1/nfts/get_contract_addresses
const MARKETPLACE = "craft1nwp0ynjv84wxysf2f5ctvysl6dpm8ngm70hss6jeqt8q7e7u345sgynrhu"
const ADDR721_SKINS = "craft1qjxu65ucccpg8c5kac8ng6yxfqq85fluwd0p9nt74g2304qw8eyqz8azvt"
const ADDR721_REALESTATE = "craft1udfs22xpxle475m2nz7u47jfa3vngncdegmczwwdx00cmetypa3s5mr4eq"




async function main() {
	// console.log(client);
	const client = CosmWasmClient.connect(rpcEndpoint);

	// returns a list of {} objects which are currently for sale. To purchase, buyNFT with the offering_id & amount in ucraft sent
	// getAvailableOfferings(client);
	// buys an NFT buy sending a msg -> the contract to buy the offering ID, also passes through the ucraft amount to buy it
	// buyNFT("2", 5_000_000);


	// queryTokensUserOwns("craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r")

	rendering();

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
	// buy an NFT from the marketplace with CosmWasmJS. This would be via keplr wallet in the future.
	// teskey_test 2 mnumonic from test script
	const gasPrice = GasPrice.fromString("0.025ucraft");
	const fee = calculateFee(200_000, gasPrice);


	const mnemonic = "flag meat remind stamp unveil junior goose first hold atom deny ramp raven party lens jazz tape dad produce wrap citizen common vital hungry";
	const hd_options: Secp256k1HdWalletOptions = {
		bip39Password: "",
		hdPaths: [stringToPath("m/44'/118'/0'/0/0")],
		prefix: "craft",
	}
	const wallet = await Secp256k1HdWallet.fromMnemonic(mnemonic, hd_options);

	const client_options: SigningCosmWasmClientOptions = {
		broadcastPollIntervalMs: 300,
		broadcastTimeoutMs: 8_000,
		gasPrice: gasPrice,
		prefix: "craft"
	};
	const client = await SigningCosmWasmClient.connectWithSigner(
		rpcEndpoint,
		wallet,
		client_options,
	);
	const account = await wallet.getAccounts();
	console.log(account);



	// {"buy_nft":{"offering_id":"2"}}
	// Execute contract message WITH funds to actually buy the NFT. ucraft_amt = the offering price exactly, tho it overpay if they wanted too.
	const resp = await client.execute(
		"craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r",
		MARKETPLACE,
		{ buy_nft: { offering_id: MarketplaceID } },
		fee,
		"here is my memo",
		coins(ucraft_amt, "ucraft")
	);
	console.log(resp);
}


export async function queryTokensUserOwns(craft_address: string) {
	// craftd query wasm contract-state smart $ADDR721 '{"tokens":{"owner":"craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r","start_after":"0","limit":50}}'

	// real estate holdings
	const response = await axios.get(`http://api.crafteconomy.io/v1/nfts/owned/${ADDR721_REALESTATE}/${craft_address}`);
	console.log(response.data);


	const skins = await axios.get(`http://api.crafteconomy.io/v1/nfts/owned/${ADDR721_SKINS}/${craft_address}`);
	console.log(skins.data);
}

export async function rendering() {

	// Option 1:
	// So idk how to do JS/TS things, but here is a skin rendering package
	// https://www.npmjs.com/package/minerender

	// Example: https://minerender.org/embed/skin/?skin=reecepbcups&shadow=true
	// Marketplace IdeaL https://cdn.discordapp.com/attachments/999037174165028874/999037365773422682/unknown.png

	// here is a skin object in the marketplace / 721 token:
	// value: 'ewogICJ0aW1lc3RhbXAiIDogMTY1ODI0OTYzODE5NSwKICAicHJvZmlsZUlkIiA6ICJiMDBmZWRjOTM0YmU0NWIxOGI3M2MyOTgzNjFjZTg3MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbGRlcmJyYXVlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgxNGMzYzM5ODI0ZmJkMmE3YjRlYzllOWQ5MjdiZjNlNWJjNWU2MmQ4MDg2YTZhN2QxZmZiYTY4MzEzODBmOTgiCiAgICB9CiAgfQp9',
    // signature: 'r7ZJUSxRzEPeroG4mM8MT1gmelvT7skzTUq+1oRUjAH0tLngy8YVRFtztFm46VxoVa6nXjXfaihSqLjg4E+3uC4dIDabtxKXhUL6WUFMJU6QFZYLJPBw9k776dodtb3y4/rQwqzppvtvkWA6247nso1+UcKwwCOn/Ha7H9Fmr3dQVL3dNcpusXLHIerBCAbRZ3NG2QUHb7+9/nHbG9d8z/9lE7zbevH+QkYklUcLNBJWhOXIqXGXpW0iQny73bmsJAD9XhQH6JutiTjoardLC8d1cWSKdKPtjZ/qn8J9zOk9ckf3aG/vtA8uy6VwE+ExAepNUtAhf4VHSVbKJk7NFRbgjpmMjLbeAg79+lWYmLA9NZj2CAM5DaMyE+w2ypqwBf1UTR4jJ4pfDIfWh+ZtT4PBnK5DUSvgfvSMpBEBrqQBzbZUURcDbdaFT6kjq5bqUVCBPwlU2G+JDSDN4f0Qj9TkJsVedy02X6csRQsqyOemvn660mAAuTZ1YoAJX6gwh5r+IhXNwL8YPQWa1Xa2qQWAIMhKULprjlEJRmdWj6I9/8uMzwWvA+zNTX8od+KOSIjNmIBdwxmaPH38j2PzrXyJVIEMVhaZb8WoNYI1sZZQKi0mEQUhR+5GI7H2xhDmrvEA8ZagbbkKDTswUORvlDMpfvQDc/qB1lwnamvtHvc=',
    // url: 'http://textures.minecraft.net/texture/814c3c39824fbd2a7b4ec9e9d927bf3e5bc5e62d8086a6a7d1ffba6831380f98',
    // tokenId: '10'

	// url is the link to the texture itself. This can be put on a skin model & rendered.
	// https://github.com/bs-community/skinview3d
	// This library does it.

	// HOWTO:
	// 1) Visit https://bs-community.github.io/skinview3d/
	// 2) Download http://textures.minecraft.net/texture/814c3c39824fbd2a7b4ec9e9d927bf3e5bc5e62d8086a6a7d1ffba6831380f98
	// 3) In the "Skin:" section, browse & select that downloaded image
	// It now renders in browser (remove Panorama)


	

}