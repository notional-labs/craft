import { redisClient, collections } from './database.service';
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

// create boolean to disable caching
const allowCache = false;

const prefixes = {
    "cosmos": {
        rpc: "https://rpc.cosmoshub.strange.love",
        denom: "uatom",
        coingecko: "cosmos",
    },
    "osmo": {
        rpc: "https://rpc-osmosis.whispernode.com",
        denom: "uosmo",
        coingecko: "osmosis",
    },
    "juno": {
        rpc: "https://rpc.juno.chaintools.tech",
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
    const REDIS_KEY = `cache:dao_all_endpoints`;    
    if (allowCache) {
        let all_endpoints_data = await redisClient?.get(REDIS_KEY);
        if (all_endpoints_data) {
            return JSON.parse(all_endpoints_data);
        }
    }

    // console.log("Signer address:", account.address);

    let [exp_resp, craft_price, addresses] = await Promise.all([
        getTotalSupply("uexp"),
        getCraftUSDPrice(),
        getWallets(),
    ]);

    if (exp_resp === -1) {
        return undefined;
    }

    const uexp_total_supply = Number(exp_resp);
    console.log("uexp_total_supply", uexp_total_supply);

    // console.log(balance);

    // gets all DAO wallets
    // const addresses: string[] = await getWallets();

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
    const ucraft_price = craft_price / 1_000_000;

    const uexp_price = TOTAL_USD_VALUE_OF_ASSETS / uexp_total_supply;
    const exp_price = TOTAL_USD_VALUE_OF_ASSETS / (uexp_total_supply/1_000_000);
    

    const returnValue = {
        ESCROW_ACCOUNT: await getServersEscrowAccountInfo(),
        ADDRESSES: addresses,
        UBALANCES: ubalances,
        TOTAL_ASSETS: TOTAL_ASSETS,
        TOTAL_DAO_USD_VALUE: TOTAL_USD_VALUE_OF_ASSETS,
        EXP_TOTAL_SUPPLY: uexp_total_supply / 1_000_000,
        UEXP_TOTAL_SUPPLY: uexp_total_supply,        
        PRICE_PER_EXP: exp_price,
        PRICE_PER_UEXP: uexp_price,
        PRICE_PER_CRAFT: craft_price,
        PRICE_PER_UCRAFT: ucraft_price,
    }    

    if(allowCache) {
        await redisClient.setEx(REDIS_KEY, 30, JSON.stringify(returnValue)); // 30 second cache
    }
    return returnValue;
};

export const getEscrowBalances = async () => {
    const REDIS_KEY = `cache:escrow_balances`;    
    if(allowCache) {
        let escrow_balances = await redisClient?.get(REDIS_KEY);
        if (escrow_balances) {
            // console.log(`BuildingName found in redis cache -> ${cachedBuildingName} from ${buildingId}. Not calling MongoDB`);
            return escrow_balances;
        }
    }

    // print all collections?.escrow
    // const v = await collections?.escrow?.find({}).toArray();
    // console.log(v);

    // sum all the values in the collections?.escrow? collection as ucraft
    const escrow_balances_data = await collections?.escrow?.aggregate([
        {
            $group: {
                _id: null,
                total: { $sum: "$ucraft_amount" },
                unique_records: { $sum: 1 },
            },            
        },
    ]).toArray();


    let data = {
        balances: 0,
        denom: "craft",
        unique_accounts: 0,
    };
    if(escrow_balances_data && escrow_balances_data.length >= 1) {
        let { total, unique_records } = escrow_balances_data[0];
        // console.log(total, unique_records);

        return {
            balances: total,
            denom: "ucraft",
            unique_accounts: unique_records,
        };        
    }

    // save total_escrow_balances to redis
    if(allowCache) {
        await redisClient?.setEx(REDIS_KEY, 60*10, JSON.stringify(data)); // 10 min cache
    }
    return data;
}

export const getServersEscrowAccountInfo = async () => {
    const walletMnumonic = `${process.env.CRAFT_DAO_ESCROW_WALLET_MNUMONIC}`
    let data_format = {
        address: "",
        denom: "",
        balance: -1,
        held_escrows: await getEscrowBalances(),
        // error: "CRAFT_DAO_ESCROW_WALLET_MNUMONIC variable was not set correctly."
    }

    if (walletMnumonic.split(" ").length < 12) {
        return data_format
    }

    const wallet = await DirectSecp256k1HdWallet.fromMnemonic(walletMnumonic, { prefix: "craft" });
    const [account] = await wallet.getAccounts();
    const balance = await getCraftBalance(account.address);

    data_format.address = account.address;
    data_format.denom = balance.denom;
    data_format.balance = Number(balance.amount);    
    // return {
    //     address: `${account.address}`,
    //     denom: balance.denom,
    //     balance: balance.amount,
    //     escrows: escrow_amt,
    // };
    return data_format;
}

/**
 * @param coin String
 * @returns 
 */
export const getTotalSupply = async (coin: string) => {
    const REDIS_KEY = `cache:token_total_supply`;
    const TTL = 30 * 5; // 5 min
    const REDIS_HSET_KEY = `${coin}` // ucraft, uexp
    if (allowCache) {
        let cached_total_supply = await redisClient?.hGet(REDIS_KEY, REDIS_HSET_KEY);
        if (cached_total_supply) {
            console.log(`TotalSupply token: ${coin} found in redis -> ${REDIS_KEY}`);
            return JSON.parse(cached_total_supply);
        }
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

    if(allowCache) {
        await redisClient?.hSet(REDIS_KEY, REDIS_HSET_KEY, JSON.stringify(value));
        await redisClient?.expire(REDIS_KEY, TTL);
    }

    return value;
}

export const getTotalUSDValue = async (TOTAL_ASSETS?) => {
    const REDIS_KEY = `cache:total_dao_usd_value`;
    const TTL = 60 * 5;    
    if (allowCache) {
        let get_total_value = await redisClient?.get(REDIS_KEY);
        if (get_total_value) {
            console.log(`Total Value: $ ${get_total_value} found in redis cache -> ${REDIS_KEY}`);
            return JSON.parse(get_total_value);
        }
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
    if(allowCache) {
        await redisClient?.set(REDIS_KEY, JSON.stringify(TOTAL_USD_VALUE_OF_ASSETS));
        await redisClient?.expire(REDIS_KEY, TTL);
    }

    // round total usd value to 2 decimal places
    TOTAL_USD_VALUE_OF_ASSETS = Math.round(TOTAL_USD_VALUE_OF_ASSETS * 100) / 100;

    return TOTAL_USD_VALUE_OF_ASSETS;
}

// escrow account
export const getCraftBalance = async (wallet_addr) => {
    // get craft escrow account
    let balance = coin("0", "ucraft");
    try {
        const client = await SigningStargateClient.connectWithSigner(`${process.env.CRAFTD_NODE}`, wallet_addr);
        // const balance = await client.getAllBalances(account.address)
        balance = await client.getBalance(wallet_addr, "ucraft")        
    } catch (error) {
        console.log("getCraftBalance", error);
    }

    return balance;
}


export const getAssetHoldingAmount = async (address, prefix, rpc_url, denom) => {
    console.log("getting assets for addr:", address, " via rpc:", rpc_url);

    const REDIS_KEY = `cache:dao_wallet_holding_amt-${address}`;    
    if (allowCache) {
        let get_wallet_value = await redisClient?.get(REDIS_KEY);
        if (get_wallet_value) {
            // console.log(`Asset: ${denom} holdings ${get_wallet_value} found in redis cache -> ${REDIS_KEY}`);
            return JSON.parse(get_wallet_value);
        }
    }

    let ASSETS = { ubalance: "", amount: "" };

    // non cache, get balances & staked amount * price stuff
    const client = await StargateClient.connect(`${rpc_url}`).catch(err => {
        sendDiscordWebhook(`DAO ERROR: ${rpc_url} down`, "The RPC is down for getting asset prices, you should really fix that", {}, '#cf1b1b');
        console.log(err);
        return undefined;
    });

    if (!client) { // RPC is bad or it just did not connect properly
        ASSETS.ubalance = "-1";
        ASSETS.amount = "-1";
        return ASSETS;
    }

    const bal: Coin = await client.getBalance(address, denom).then(res => {
        console.log("balance:", res);
        return coin(`${res.amount}`, res.denom);
    }).catch(err => {
        console.log(err);
        return coin(0, denom);
    });

    let staked_amount = await client.getBalanceStaked(address).then((res) => {
        let amt = res?.amount;
        if(amt){
            return BigInt(amt) / BigInt(1_000_000);
        }
        return -1;        
    }).catch((err) => {
        console.log(err);
        return -1;
    });
    // console.log("staked_amount:", staked_amount);
    if (!staked_amount) { staked_amount = 0; }

    // console.log(ASSETS.amount, ASSETS.ubalance, staked_amount);

    ASSETS.ubalance = bal.amount;
    ASSETS.amount = ((BigInt(bal.amount)/BigInt(1_000_000)) + BigInt(staked_amount)).toString(); // amount in normal human readable format
    // console.log("ASSETS.amount (should include stake): ", ASSETS.amount);

    // console.log(ASSETS.amount, ASSETS.ubalance, staked_amount);

    // save to redis
    if(allowCache) {
        await redisClient?.set(REDIS_KEY, JSON.stringify(ASSETS));
        const TTL = Math.floor(Math.random() * (15 - 10 + 1)) + 10;  // 10 to 15 minutes  
        await redisClient?.expire(REDIS_KEY, TTL * 60);
    }

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

    let promises: any = [];

    for (const addr of addresses) {
        const prefix = getWalletAPrefix(addr);
        const rpc_url = prefixes[prefix].rpc;
        const denom = prefixes[prefix].denom;

        // gets cached amount if it exists
        // const t = await getAssetHoldingAmount(addr, prefix, rpc_url, denom);
        promises.push({
            // addr: addr,
            prefix: prefix,
            // rpc_url: rpc_url,
            denom: denom,
            t: getAssetHoldingAmount(addr, prefix, rpc_url, denom)
        });
    }

    const re = await Promise.all(promises.map(p => p.t.then(t => ({ ...p, t }))));

    for (const r of re) {
        // const addr = r.addr;
        const prefix = r.prefix;
        // const rpc_url = r.rpc_url;
        const denom = r.denom;
        const t = r.t;

        if (ubalances[denom] === undefined) {
            ubalances[denom] = BigInt(0);
            TOTAL_ASSETS[prefix] = BigInt(0);
        }
        ubalances[denom] += BigInt(t.ubalance);
        TOTAL_ASSETS[prefix] += BigInt(t.amount); // since we save to prefix for coingecko, we need the whole denom not micro udenom
    }


    // convert every ubalances & TOTAL_ASSETS TO A STRING
    for (const denom in ubalances) {
        ubalances[denom] = ubalances[denom].toString();
    }
    for (const denom in TOTAL_ASSETS) {
        TOTAL_ASSETS[denom] = TOTAL_ASSETS[denom].toString();
    }    

    // total includes staked amount
    return { balance_only: ubalances, total: TOTAL_ASSETS }
}

export const getExpValueCalculation = async () => {
    let [dao_usd_value, exp_supply] = await Promise.all([
        getTotalUSDValue(),
        getTotalSupply("uexp")
    ]);


    if (exp_supply) {
        // get the number of it and / 1mil
        exp_supply = Number(exp_supply) / 1_000_000;
    }

    const value = dao_usd_value / exp_supply;
    if(value < 0) {
        return -1;
    }
    return value;
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
export const makePayment = async (secret: string, recipient_wallet: string, ucraft_amount: string, description: string) => {
    // confirm request amount not > DAO wallet balance. If so return error & dont process in game
    // TODO: Future: Bulk pay transactions?

    // This should really never happen
    // if (ucraft_amount > Number.MAX_SAFE_INTEGER) {
    //     console.log("ucraftamount was > Number.MAX_SAFE_INTEGER, so set to", Number.MAX_SAFE_INTEGER);
    //     ucraft_amount = Number.MAX_SAFE_INTEGER;
    // }    

    // check if secret & if so, check if == process.env.CRAFT_DAO_ESCROW_SECRET
    if (secret !== process.env.CRAFT_DAO_ESCROW_SECRET) {
        console.log("Secret passed through function: " + secret)
        return { "error": "secret is incorrect" };
    }

    let client;
    let account;
    try {
        // TODO: pre generate these so we can just grab the client & sign? 
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
        console.log("DEBUG: " + account[0].address + " sending " + coins_amt + " to " + recipient_wallet + " with description " + description + " fee: " + fee);
        result = await client.sendTokens(
            account[0].address,
            recipient_wallet,
            coins_amt,
            fee,
            "Payment from SERVER @ " + time + " " + description
        );
        assertIsDeliverTxSuccess(result);
        // console.log("Successfully broadcasted:", result.code, result.height, result.transactionHash, (result.rawLog).toString());
        console.log("Successfully broadcasted:", result.code, result.height, result.transactionHash);
    } catch (err) {
        console.log("Error:", err.message);
        // {"error":"{\"code\":-32603,\"message\":\"Internal error\",\"data\":\"tx already exists in cache\"}"}
        // TODO: save to DB to retry later

        let code: string = "unknown"
        let reason: string = err.message;
        if (err.message.includes("Code: ")) {
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
    if (serverBalanceLeft) {
        balanceLeftString = (Number(serverBalanceLeft.balance) / 1_000_000).toString() + "craft"
    }

    // bigint ucraft_amount
    let asCraft = BigInt(ucraft_amount) / BigInt(1_000_000);

    let desc = ucraft_amount.toString() + "ucraft";
    if(asCraft >= 0.05) {
        desc += " | (" + asCraft.toString() + " craft)";
    }
    await sendDiscordWebhook(
        'SERVER PAYMENT | ' + time,
        desc,
        {
            "Wallet": recipient_wallet,
            "Description": description,
            "Server bal Left: ": balanceLeftString
        },
        '#0099ff'
    );
    // await sendDiscordWebhook(`SERVER PAYMENT | ${time}`, `${ucraft_amount.toString()}ucraft->${recipient_wallet}\n(${asCraft.toString()}craft)\nBalance Left: ${balanceLeftString}`, {}, '#cf1b1b');

    return { "success": { "wallet": recipient_wallet, "ucraft_amount": ucraft_amount, "craft_amount": asCraft.toString(), "serverCraftBalLeft": balanceLeftString, "transactionHash": result.transactionHash, "height": result.height } };
};