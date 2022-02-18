// Mongo
import * as mongo from 'mongodb';

// Collections for statistics, internal to service
const collections: { statistics?: mongo.Collection; accounts?: mongo.Collection } = {};

/**
 * Connect to MongoDB
 *
 * @param connectionString Connection string
 * @param dbName Name of database
 */
export const connectToDatabaseStats = async (connectionString, dbName) => {
    // Connect to DB
    const client: mongo.MongoClient = new mongo.MongoClient(connectionString);
    await client.connect();

    // Set collections
    const db: mongo.Db = client.db(dbName);
    const playerStatistics: mongo.Collection = db.collection('playerStatistics');
    const accounts: mongo.Collection = db.collection('accounts');
    collections.statistics = playerStatistics;
    collections.accounts = accounts;

    console.log(`Successfully connected to database (player statistics service) ${db.databaseName}`);
};

/**
 * @returns Total registered users that have joined the server
 */
export const getTotalUsers = async (at: Date) => {
        return collections?.statistics
            ?.aggregate([
                {
                    $match: {
                        firstJoined: { $lt: at }
                    }
                },
                {
                    $group: {
                        _id: '$_id'
                    }
                }
            ]).toArray();
};

/**
 * @returns Total playtime across all players from date range
 */
export const getTotalPlaytime = async (from: Date, to: Date) => {
    return collections?.statistics
        ?.aggregate([
            {
                // Take from range
                $match: {
                    timestamp: { $gte: from, $lt: to }
                }
            },
            {
                $sort: { timestamp: 1 }
            },
            {
                $group: {
                    _id: '$_id',
                    startPlaytime: { $last: '$playtime' },
                    endPlaytime: { $first: '$playtime' }
                }
            },
            {
                $group: {
                    _id: 'total',
                    totalPlaytime: {
                        $sum: {
                            $subtract: ['$startPlaytime', '$endPlaytime']
                        }
                    }
                }
            }
        ]).toArray();
};

/**
 * Get total new users that joined the server from a date
 *
 * @param from Time from
 * @param to Time to
 * @returns New users and their stats
 */
export const getNewPlayers = async (from: Date, to: Date) => {
    return collections?.statistics
        ?.aggregate([
            {
                $match: {
                    firstJoined: { $gte: from, $lt: to }
                }
            },
            {
                $group: {
                    _id: '$_id'
                }
            }
        ]).toArray();
};

/**
 * Get latest statistics for a user
 *
 * @param uuid The UUID of the user
 */
export const getLatestStatistics = async (uuid: string) => {
    return collections?.statistics?.find({ _id: uuid }).sort({ timestamp: -1 }).tryNext();
};

/**
 * Get all statistics from a period of time and sort latest entries to top
 *
 * @param from Time from
 * @param to Time till
 * @returns All stats in period of time
 */
export const getLatestStatisticsFrom = async (from: Date, to: Date) => {
    return collections?.statistics
        ?.find({ timestamp: { $gte: from, $lt: to } })
        .sort({ timestamp: -1 })
        .toArray();
};

export default {
    connectToDatabaseStats,
    getLatestStatistics
};
