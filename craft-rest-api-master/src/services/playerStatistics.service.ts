import { collections } from './database.service';

/**
 * @returns Total registered users that have joined the server
 */
export const getTotalUsers = async (from: Date, at: Date) => {
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
        ])
        .toArray();
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
        ])
        .toArray();
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
        ])
        .toArray();
};

/**
 * Get the total amount of active players (defaults to last 24 hours).
 * This pretty much tracks how many unique players have joined since the
 * from data.
 */
export const getActivePlayers = async (from: Date, to: Date) => {
    return collections?.statistics
        ?.aggregate([
            {
                $match: {
                    lastLogin: {  $gte: from, $lt: to }
                }
            },
            {
                $group: {
                    _id: '$_id'
                }
            }
        ])
        .toArray();
}

/**
 * Get latest statistics for a user
 *
 * @param uuid The UUID of the user
 */
export const getLatestStatistics = async (uuid: string) => {
    let doc = await collections?.statistics?.find({ _id: uuid }).sort({ timestamp: -1 }).tryNext();
    // Set username
    if (doc) {
        let username = await collections?.accounts?.find({ _id: uuid }).next();

        if (username)
            doc["username"] = username.name;
    }
    return doc;
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
