import { redisClient } from './database.service';
import { sendDiscordWebhook } from './discord.service';
import { getPrice, getCraftUSDPrice } from './pricing.service';

import axios from 'axios';

// https://cosmos.github.io/cosmjs/
import { StdFee, assertIsDeliverTxSuccess, calculateFee, GasPrice, SigningStargateClient, StargateClient, QueryClient } from "@cosmjs/stargate";
import { DirectSecp256k1HdWallet } from "@cosmjs/proto-signing";
import { coin, coins, Coin } from "@cosmjs/amino";
import { fromBech32, toBech32, toHex } from "@cosmjs/encoding";

// Env
import { config } from 'dotenv';
config();

const prefixes = {
    "cosmos": {
        rpc: "https://rpc-cosmoshub.ecostake.com",
        denom: "uatom",
        coingecko: "cosmos",
    },
    "osmo": {
        rpc: "https://rpc-osmosis.whispernode.com",
        denom: "uosmo",
        coingecko: "osmosis",
    },
    "juno": {
        rpc: "https://rpc-juno.whispernode.com",
        denom: "ujuno",
        coingecko: "juno-network",
    },
    "akash": {
        rpc: "https://rpc.akash.forbole.com:443",
        denom: "uakt",
        coingecko: "akash-network",
    },
    "dig": {
        rpc: "https://rpc-1-dig.notional.ventures",
        denom: "udig",
        coingecko: "dig-chain",
    },
};

// https://github.com/cosmos/cosmjs/tree/main/packages/cli/examples

/**
 * http://127.0.0.1:4000/v1/dao/get_wallet
 */
export const getAllEndpoints = async () => {

    // def need to save to cache

    // console.log("Signer address:", account.address);

    // make axios request
    const exp_resp = await getTotalSupply("uexp");
    if (exp_resp === -1) {
        return undefined;
    }

    const exp_total_supply = Number(exp_resp);
    console.log("exp_total_supply", exp_total_supply);

    // console.log(balance);

    // gets all DAO wallets
    const addresses: string[] = await getWallets();

    // loops through all DAO wallets, gets an RPC of that wallet from cache if found, connect & get balance
    let ubalances, TOTAL_ASSETS = await getAssets();
    // console.log(TOTAL_ASSETS);

    // .total includes staked holdings
    let TOTAL_USD_VALUE_OF_ASSETS = await getTotalUSDValue(TOTAL_ASSETS).then((total) => {
        return total;
    }).catch((err) => {
        console.log(err);
        return -1
    });
    console.log("TOTAL_USD_VALUE_OF_ASSETS", TOTAL_USD_VALUE_OF_ASSETS)

    // http://65.108.125.182:1317/cosmos/bank/v1beta1/supply/by_denom?denom=uexp

    const craft_price = await getCraftUSDPrice();

    const returnValue = {
        ESCROW_ACCOUNT: await getServersEscrowAccountInfo(),
        ADDRESSES: addresses,
        UBALANCES: ubalances,
        TOTAL_ASSETS: TOTAL_ASSETS,
        TOTAL_DAO_USD_VALUE: TOTAL_USD_VALUE_OF_ASSETS,
        UEXP_TOTAL_SUPPLY: exp_total_supply,
        EXP_TOTAL_SUPPLY: exp_total_supply / 1_000_000,
        PRICE_PER_EXP: TOTAL_USD_VALUE_OF_ASSETS / exp_total_supply,
        PRICE_PER_CRAFT: craft_price,
    }
    // client.disconnect();
    return returnValue;
};


export const getServersEscrowAccountInfo = async () => {
    const walletMnumonic = `${process.env.CRAFT_DAO_ESCROW_WALLET_MNUMONIC}`
    if (walletMnumonic.split(" ").length < 12) {
        return {
            address: "",
            denom: "",
            balance: -1,
            error: "CRAFT_DAO_ESCROW_WALLET_MNUMONIC variable was not set correctly."
        }
    }

    const wallet = await DirectSecp256k1HdWallet.fromMnemonic(walletMnumonic, { prefix: "craft" });
    const [account] = await wallet.getAccounts();
    const balance = await getCraftBalance(account.address);
    return {
        address: `${account.address}`,
        denom: balance.denom,
        balance: balance.amount,
    };
}

/**
 * @param coin String
 * @returns 
 */
export const getTotalSupply = async (coin: string) => {    
    const REDIS_KEY = `cache:token_total_supply`;
    const TTL = 30*5; // 5 min
    const REDIS_HSET_KEY = `${coin}` // ucraft, uexp
    let cached_total_supply = await redisClient?.hGet(REDIS_KEY, REDIS_HSET_KEY);
    if (cached_total_supply) {
        console.log(`TotalSupply token: ${coin} found in redis -> ${REDIS_KEY}`);
        return JSON.parse(cached_total_supply);
    }

    const value = await axios.get(`${process.env.CRAFTD_REST}/cosmos/bank/v1beta1/supply/by_denom?denom=${coin}`, {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        }
    }).then(res => {
        // console.log("amount", res.data.amount.amount);
        return res.data.amount.amount; // { denom: 'uexp', amount: '1000000000000000010000000' }
    }).catch(err => {
        console.log(err);
        return -1;
    });
    await redisClient?.hSet(REDIS_KEY, REDIS_HSET_KEY, JSON.stringify(value));
    await redisClient?.expire(REDIS_KEY, TTL);

    return value;
}

export const getTotalUSDValue = async (TOTAL_ASSETS?) => {
    const REDIS_KEY = `cache:total_dao_usd_value`;
    const TTL = 60 * 5;
    let get_total_value = await redisClient?.get(REDIS_KEY);
    if (get_total_value) {
        console.log(`Total Value: $ ${get_total_value} found in redis cache -> ${REDIS_KEY}`);
        return JSON.parse(get_total_value);
    }

    if (!TOTAL_ASSETS) {
        TOTAL_ASSETS = await getAssets();
    }

    let TOTAL_USD_VALUE_OF_ASSETS = 0;
    for (const asset in TOTAL_ASSETS.total) {
        const asset_amount = TOTAL_ASSETS.total[asset];
        const coingecko_id = prefixes[asset].coingecko;

        console.log("Coingecko (asset, amount, cId): ", asset, asset_amount, coingecko_id);

        const usd_price = await getPrice(coingecko_id);
        const holdings_value = Number(usd_price * asset_amount);
        TOTAL_USD_VALUE_OF_ASSETS += holdings_value;

        console.log("getTotalUSDValue: (price, total)", usd_price, holdings_value)
    }

    // Save to cache
    await redisClient?.set(REDIS_KEY, JSON.stringify(TOTAL_USD_VALUE_OF_ASSETS));
    await redisClient?.expire(REDIS_KEY, TTL);

    // round total usd value to 2 decimal places
    TOTAL_USD_VALUE_OF_ASSETS = Math.round(TOTAL_USD_VALUE_OF_ASSETS * 100) / 100;

    return TOTAL_USD_VALUE_OF_ASSETS;
}

// escrow account
export const getCraftBalance = async (wallet_addr) => {
    // get craft escrow account
    const client = await SigningStargateClient.connectWithSigner(`${process.env.CRAFTD_NODE}`, wallet_addr);
    // const balance = await client.getAllBalances(account.address)
    const balance = await client.getBalance(wallet_addr, "ucraft")
    return balance;
}


export const getAssetHoldingAmount = async (address, prefix, rpc_url, denom) => {
    console.log("getting assets for addr:", address, " via rpc:", rpc_url);

    const REDIS_KEY = `cache:dao_wallet_holding_amt-${address}`;
    let get_wallet_value = await redisClient?.get(REDIS_KEY);
    if (get_wallet_value) {
        // console.log(`Asset: ${denom} holdings ${get_wallet_value} found in redis cache -> ${REDIS_KEY}`);
        return JSON.parse(get_wallet_value);
    }

    let ASSETS = { ubalance: 0, amount: 0 };

    // non cache, get balances & staked amount * price stuff
    const client = await StargateClient.connect(`${rpc_url}`).catch(err => {
        sendDiscordWebhook(`DAO ERROR: ${rpc_url} down`, "The RPC is down for getting asset prices, you should really fix that", {}, '#cf1b1b');
        console.log(err);
        return undefined;
    });

    if (!client) { // RPC is bad or it just did not connect properly
        ASSETS.ubalance = -1;
        ASSETS.amount = -1;
        return ASSETS;
    }

    const bal: Coin = await client.getBalance(address, denom).then(res => {
        console.log("balance:", res);
        return coin(res.amount, res.denom);
    }).catch(err => {
        console.log(err);
        return coin(-1, denom);;
    });

    let staked_amount = await client.getBalanceStaked(address).then((res) => {
        return Number(res?.amount) / 1_000_000;
    }).catch((err) => {
        console.log(err);
        return -1;
    });
    // console.log("staked_amount:", staked_amount);
    if (!staked_amount) { staked_amount = 0; }

    ASSETS.ubalance = Number(bal.amount);
    ASSETS.amount = (ASSETS.ubalance / 1_000_000) + staked_amount; // amount in normal human readable format
    // console.log("ASSETS.amount (should include stake): ", ASSETS.amount);

    // save to redis
    await redisClient?.set(REDIS_KEY, JSON.stringify(ASSETS));
    const TTL = Math.floor(Math.random() * (15 - 10 + 1)) + 10;  // 10 to 15 minutes  
    await redisClient?.expire(REDIS_KEY, TTL * 60);

    return ASSETS
}

export const getAssets = async (addresses?) => {
    // since wallets take so long to query, we save the asset with a random TTL in redis.
    // This way we only have to query 1 or 2 wallets before returning the whole requests, thus increasing speed for little cost.
    let ubalances = {}; // "udenom": amount (held)
    let TOTAL_ASSETS = {}; // "denom": amount

    if (!addresses) {
        addresses = await getWallets();
    }

    for (const addr of addresses) {
        const prefix = getWalletAPrefix(addr);
        const rpc_url = prefixes[prefix].rpc;
        const denom = prefixes[prefix].denom;

        // gets cached amount if it exists
        const t = await getAssetHoldingAmount(addr, prefix, rpc_url, denom);

        if (ubalances[denom] === undefined) {
            ubalances[denom] = 0;
            TOTAL_ASSETS[prefix] = 0;
        }

        ubalances[denom] += t.ubalance;
        TOTAL_ASSETS[prefix] += t.amount; // since we save to prefix for coingecko, we need the whole denom not micro udenom
    }
    // total includes staked amount
    return { balance_only: ubalances, total: TOTAL_ASSETS }
}

export const getExpValueCalculation = async () => {
    let dao_usd_value = await getTotalUSDValue();
    let exp_supply = await getTotalSupply("uexp");
    if (exp_supply) {
        // get the number of it and / 1mil
        exp_supply = Number(exp_supply) / 1_000_000;
    }
    return dao_usd_value / exp_supply;
}

export const getWallets = async () => {
    let DAO_ADDRS = `${process.env.DAO_WALLETS}`
    const addresses: string[] = [];
    for (const addr of DAO_ADDRS.split(",")) {
        addresses.push(addr);
    }
    // console.log(addresses);
    return addresses;
}
const getWalletAPrefix = (address: string) => {
    const decoded = fromBech32(address);
    return decoded.prefix;
}


/**
 * https://github.com/cosmos/cosmjs/blob/main/packages/cli/examples/local_faucet.ts
 * 
 * This function will pay a player's account from their esgrow wallet in game.
 * 
 * curl --data '{"secret": "7821719493", "description": "test description", "wallet": "craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0", "ucraft_amount": 500}' -X POST -H "Content-Type: application/json"  http://localhost:4000/v1/dao/make_payment
 */
export const makePayment = async (secret: string, recipient_wallet: string, ucraft_amount: number, description: string) => {
    // confirm request amount not > DAO wallet balance. If so return error & dont process in game
    // TODO: Future: Bulk pay transactions?

    // This should really never happen
    if(ucraft_amount > Number.MAX_SAFE_INTEGER) {
        console.log("ucraftamount was > Number.MAX_SAFE_INTEGER, so set to", Number.MAX_SAFE_INTEGER);
        ucraft_amount = Number.MAX_SAFE_INTEGER;
    }

    // check if secret & if so, check if == process.env.CRAFT_DAO_ESCROW_SECRET
    if (secret !== process.env.CRAFT_DAO_ESCROW_SECRET) {
        console.log("Secret passed through function: " + secret)
        return { "error": "secret is incorrect" };
    }

    let client;
    let account;
    try {
        const server_wallet = await DirectSecp256k1HdWallet.fromMnemonic(`${process.env.CRAFT_DAO_ESCROW_WALLET_MNUMONIC}`, { prefix: "craft" });
        client = await SigningStargateClient.connectWithSigner(`${process.env.CRAFTD_NODE}`, server_wallet);
        account = await server_wallet.getAccounts();   
    } catch (error) {
        console.log(error);
        return;
    }
             
    const time = new Date().toISOString();
    const coins_amt = coins(ucraft_amount, "ucraft");
    const gasPrice = GasPrice.fromString("0.025ucraft");
    const fee = calculateFee(200_000, gasPrice);
    let result;
    try {
        result = await client.sendTokens(
            account.address,
            recipient_wallet,
            coins_amt,
            fee,
            "Payment from SERVER @ " + time + " " + description
        );
        assertIsDeliverTxSuccess(result);
        console.log("Successfully broadcasted:", result.code, result.height, result.transactionHash, result.rawLog);
    } catch (err) {        
        console.log("Error:", err.message);
        // {"error":"{\"code\":-32603,\"message\":\"Internal error\",\"data\":\"tx already exists in cache\"}"}
        // TODO: save to DB to retry later

        let code: string = "unknown"
        let reason: string = err.message;
        if(err.message.includes("Code: ")) {
            code = err.message.split("Code: ");
            code = code[1].split(";")[0];
            reason = err.message.split("message index: ")[1]//.split("\"")[0];
        } else {
            console.log("Error:", err.message);
        }
                
        const hasEnoughFunds = !err.message.includes("insufficient funds");        
        return { "error": { "code": code, "reason": reason, "hasEnoughFunds": hasEnoughFunds } }; 
    }

    let serverBalanceLeft = await getServersEscrowAccountInfo();
    let balanceLeftString = "";
    if(serverBalanceLeft) {
        balanceLeftString = (Number(serverBalanceLeft.balance)/1_000_000).toString() + "craft"
    }
  
    await sendDiscordWebhook('SERVER PAYMENT | ' + time, 
        ucraft_amount.toString() + "ucraft | ("+ (ucraft_amount/1_000_000).toString() + "craft)",
        {
            "Wallet": recipient_wallet,
            "Description": description,
            "Server bal Left: ": serverBalanceLeft        
        },
        '#0099ff'
    );

    return { "success": {"wallet": recipient_wallet, "ucraft_amount": ucraft_amount, "craft_amount": (ucraft_amount/1_000_000), "serverCraftBalLeft": balanceLeftString, "transactionHash": result.transactionHash, "height": result.height} };
};