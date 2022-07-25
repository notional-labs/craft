import * as mongo from 'mongodb';
import * as redis from 'redis';

// List of loaded mongo collections
export const collections: { statistics?: mongo.Collection; accounts?: mongo.Collection; connections?: mongo.Collection, reProperties?: mongo.Collection, webappSyncCodes?: mongo.Collection, nfts?: mongo.Collection } = {};
// Connected redis client
export let redisClient: redis.RedisClientType<any, any>;

/**
 * Connect to MongoDB
 *
 * @param connectionString Connection string
 * @param dbName Name of database
 */
export const connectToMongo = async (connectionString, dbName) => {
    // Connect to DB
    const client: mongo.MongoClient = new mongo.MongoClient(connectionString);
    await client.connect();

    const db: mongo.Db = client.db(dbName);

    // Connect required collections
    collections.statistics = db.collection('playerStatistics');
    collections.accounts = db.collection('accounts');
    collections.connections = db.collection('connections');
    collections.reProperties = db.collection('reProperties');
    collections.webappSyncCodes = db.collection('webappSyncCodes'); // used for syncing in game -> the webapp
    collections.nfts = db.collection('nftImageLinks'); // TODO: used to sync a players image NFTs from chain -> in game database.  [!] Need to see how Joel wants these stored

    console.log(`Successfully connected to Mongo Database ${db.databaseName}`);
};

/**
 * Connect to Redis
 * 
 * @param connectionString Connection string
 */
export const connectToRedis = async (connectionString) => {
    redisClient = redis.createClient({
        url: connectionString
    });

    redisClient.on('error', (err) => console.log('Redis Client Error', err));
    redisClient.on('ready', () => console.log('Successfully connected to Redis Server'));

    await redisClient.connect();

    // Auth - No longer required when done in URI
    // redisClient.sendCommand(['AUTH', auth])
}