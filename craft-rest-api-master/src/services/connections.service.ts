// Mongo
import * as mongo from 'mongodb';

// Collections for statistics, internal to service
const collections: { connections?: mongo.Collection } = {};

// The options associated with connections
type LinkOptions = {
    discordId: string;
    keplrId: string;
    minecraftId: string;
}

/**
 * Connect to MongoDB
 *
 * @param connectionString Connection string
 * @param dbName Name of database
 */
export const connectToDatabaseConnections = async (connectionString, dbName) => {
    // Connect to DB
    const client: mongo.MongoClient = new mongo.MongoClient(connectionString);
    await client.connect();

    // Set collections
    const db: mongo.Db = client.db(dbName);
    collections.connections = db.collection('connections');

    console.log(`Successfully connected to database (connections service) ${db.databaseName}`);
};


/**
 * Get the associated links a user has
 *
 * @param options - the available link params
 */
export const getLink = async (options: LinkOptions) => {
    console.log("Running get link with options: " + JSON.stringify(options));
    let document;

    if (options.discordId) {
        // Get link by Discord
        console.log("Running discord");
        const discord = await collections?.connections?.find({ discordId: options.discordId }).tryNext();
        if (discord) document = discord;
    }

    if (options.keplrId) {
        console.log("Running keplr");
        // Get links by Keplr id
        const keplr = await collections?.connections?.find({ keplrId: options.keplrId }).tryNext();
        if (keplr) document = keplr;
    }

    if (options.minecraftId) {
        console.log("Running mc");
        //  Get links by Minecraft
        const minecraft = await collections?.connections?.find({minecraftId: options.minecraftId  }).tryNext();
        if (minecraft) document = minecraft;
    }

    console.log(document);
    return document;
};

/**
 * Create a link between multiple accounts a user has
 * @param {LinkOptions} options
 * @returns {Promise<InsertOneResult<Document> | undefined>}
 */
export const createLink = async (options: LinkOptions) => {
    return collections?.connections?.insertOne({
        discordId: options.discordId,
        keplrId: options.keplrId,
        minecraftId: options.minecraftId
    });
};

/**
 * Confirm the existence of a link based on parameters
 * @param {LinkOptions} options
 * @returns {Promise<boolean>}
 */
export const doesLinkExist = async (options: LinkOptions) => {
    const document = await getLink(options);
    return document !== undefined;
};

export default {
    connectToDatabaseConnections,
    getLink,
    createLink
};
