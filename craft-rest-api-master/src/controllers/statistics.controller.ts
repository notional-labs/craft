// Express
import { Request, Response } from 'express';
import { getLatestStatistics, getActivePlayers, getTotalUsers, getNewPlayers, getTotalPlaytime } from '../services/playerStatistics.service';

// Total millis in day
const MILLIS_IN_DAY = 86400000;

/**
 * Handles total players and total players within specific range
 */
export const getLatestPlayer = async (req: Request, res: Response) => {
    const { id } = req.params;

    const doc = await getLatestStatistics(id);
    if (doc) return res.status(200).json(doc);
    else return res.status(404).json({ message: 'User not found' });
};

/**
 * Handles total players and total players within specific range
 */
export const getPlayers = async (req: Request, res: Response) => {
    const { at } = req.query;

    // Time to see total users
    var timeFrom: number = at ? Number.parseInt(at.toString()) : new Date().getTime();
    var dateFrom = new Date(timeFrom);

    const doc = await getTotalUsers(new Date(), dateFrom);
    if (doc)
        return res.status(200).json({
            count: doc.length,
            results: doc
        });
    else return res.status(404).json({ message: 'User not found' });
};

/**
 * Handles active players in time frame
 */
export const getPlayersActive = async (req: Request, res: Response) => {
    const { from, to } = req.query;

    // Time to see total users
    // Default to 24 hours ago
    var timeFrom: number = from ? Number.parseInt(from.toString()) : new Date().getTime() - MILLIS_IN_DAY;
    var timeTo: number = to ? Number.parseInt(to.toString()) : new Date().getTime();
    var dateFrom = new Date(timeFrom);
    var dateTo = new Date(timeTo);

    const doc = await getActivePlayers(dateFrom, dateTo);
    if (doc)
        return res.status(200).json({
            count: doc.length,
            results: doc
        });
    else return res.status(404).json({ message: 'User not found' });
};

export const getPlayersNew = async (req: Request, res: Response) => {
    const { from, to } = req.query;

    // Default to 24 hours ago
    var timeFrom: number = from ? Number.parseInt(from.toString()) : new Date().getTime() - MILLIS_IN_DAY;
    var timeTo: number = to ? Number.parseInt(to.toString()) : new Date().getTime();
    var dateFrom = new Date(timeFrom);
    var dateTo = new Date(timeTo);

    const doc = await getNewPlayers(dateFrom, dateTo);
    if (doc)
        return res.status(200).json({
            count: doc.length,
            results: doc
        });
    else return res.status(404).json({ message: 'User not found' });
};

/**
 * Handles total playtime and total playtime within specific range
 */
export const getPlaytime = async (req: Request, res: Response) => {
    const { from, to } = req.query;

    // Get time period if provided
    var timeFrom: number = from ? Number.parseInt(from.toString()) : new Date().getTime() - MILLIS_IN_DAY;
    var timeTo: number = to ? Number.parseInt(to.toString()) : new Date().getTime();
    var dateFrom = new Date(timeFrom);
    var dateTo = new Date(timeTo);

    const doc = await getTotalPlaytime(dateFrom, dateTo);
    if (doc)
        return res.status(200).json({
            count: doc.length,
            results: doc
        });
    else return res.status(404).json({ message: 'User not found' });
};

export default {
    getPlayers,
    getPlayersActive,
    getPlayersNew,
    getPlaytime,
    getLatestPlayer
};
