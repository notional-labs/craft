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
const ADDR721_REALESTATE = "craft1udfs22xpxle475m2nz7u47jfa3vngncdegmczwwdx00cmetypa3s5mr4eq"




async function main() {
	// console.log(client);
	const client = CosmWasmClient.connect(rpcEndpoint);

	// returns a list of {} objects which are currently for sale. To purchase, buyNFT with the offering_id & amount in ucraft sent
	// getAvailableOfferings(client);

	// buys an NFT buy sending a msg -> the contract to buy the offering ID, also passes through the ucraft amount to buy it
	// buyNFT("2", 5_000_000);

	// Gets CRAFT tokens the user owns (real estate, skins, [images in the future])
	// queryTokensUserOwns("craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r")	


	// puts up an NFT for sale. Takes in the 
	// listNFT("craft1qjxu65ucccpg8c5kac8ng6yxfqq85fluwd0p9nt74g2304qw8eyqz8azvt", "10", 127);

	// const values = await getUsersMarketplaceListings("craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r");
	const values = await getUsersMarketplaceListings("craft1hj5fveer5cjtn4wd6wstzugjfdxzl0xp86p9fl");
	console.log(values);

	// withdrawNFTBack("24") // even tho the token is 10, 24 is the marketplace id listing.

}

main();

// export function getAllNFTs(who: string = offering_api) {
export async function getAvailableOfferings() {
	// While you could get it this way, you don't get all the data that the API provides extra.
	// const offerings = await client.queryContractSmart(marketplace_721, { get_offerings: {} });
	// console.log(offerings);

	// make an axios requests
	const response = await axios.get(offering_api);
	const offerings = response.data;
	// console.log(offerings);
	return offerings;
}


export async function listNFT(contract_address: string, token_id: string, ucraft_amt: number) {
	// sell an NFT to the marketplace with CosmWasm
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

	const encode = (str: string):string => Buffer.from(str, 'binary').toString('base64');
	
	const resp = await client.execute(
		"craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r",
		contract_address,
		{ send_nft: { contract: MARKETPLACE, token_id: token_id, msg: encode(JSON.stringify({"list_price":ucraft_amt.toString()}))		} },
		fee,
		`Listing ${token_id} ${contract_address} for sale`,		
	);
	console.log(resp);
}
export async function withdrawNFTBack(marketplace_offering_id: string) {
	// sell an NFT to the marketplace with CosmWasm
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
	
	const resp = await client.execute(
		"craft1wc5njh20antht9hd60wpup7j2sk6ajmhjwsy2r",
		MARKETPLACE,
		{ withdraw_nft: { offering_id: marketplace_offering_id } },
		fee,
		`Withdrawing ${marketplace_offering_id} from marketplace.`,		
	);
	console.log(resp);
}

export async function getUsersMarketplaceListings(craft_address: string) {
	// gets all offerings & then sorts them if they are the wanted addresse's listing.
	let ourOfferings: any = [];

	// let ourOfferings: any = await getAvailableOfferings();
	const offerings = await getAvailableOfferings();
	for(let offering of offerings) {
		if(offering.seller === craft_address) {			
			ourOfferings.push(offering);
		}
	}
	return ourOfferings;
	
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

	// TODO: Future owned will return all real estate AND normal images, just have to add mroe queries on the backend
}