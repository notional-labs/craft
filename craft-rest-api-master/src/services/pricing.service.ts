
import { redisClient } from './database.service';

// CoinGecko API
import { CoinGeckoAPI } from "@coingecko/cg-api-ts";

export const getCraftUSDPrice = async () => {
    return await getPrice("terra-luna-2").catch(err => {
        console.log(err);
        return -1;
    });
}

export const getPrice = async (coin: string) => {
    // Get cached
    const REDIS_KEY = `cache:coingecko_coin_prices`;
    const TTL = 60; // 30 seconds / 2 minutes
    const REDIS_HSET_KEY = `${coin}` // for marketplace expansion
    let cached_usd_price = await redisClient?.hGet(REDIS_KEY, REDIS_HSET_KEY);
    if (cached_usd_price) {
        // console.log(`Price: ${coin} = ${cached_usd_price} found in redis cache -> ${REDIS_KEY}`);
        return JSON.parse(cached_usd_price);
    }

    const cg = new CoinGeckoAPI(fetch);
    // create a String array with just the coin in it
    const coinArray = [coin];
    const priceArray = ["usd"];

    const usd_price = await cg.getSimplePrice(coinArray, priceArray).then((res) => {
        return res.data[coin].usd
    }).catch((err) => {
        console.log(err);
        return -1; // service is down
    });

    await redisClient?.hSet(REDIS_KEY, REDIS_HSET_KEY, JSON.stringify(usd_price));
    await redisClient?.expire(REDIS_KEY, TTL);
    return usd_price;
}