// Express
import { Request, Response } from 'express';
import { getLink, createLink, doesLinkExist } from '../services/connections.service';

/**
 * Handles the return of a link between accounts
 */
export const getConnectionLink = async (req: Request, res: Response) => {
    console.log(req.params);
    const {
        discordId,
        keplrId,
        minecraftId
    } = req.params;

    const document = await getLink({
        discordId: discordId,
        keplrId: keplrId,
        minecraftId: minecraftId
    });

    if (document) return res.status(200).json(document);
    return res.status(404).json({ message: 'Connection not found' });
};

/**
 * Handles the creation of an account link
 */
export const createConnectionLink = async (req: Request, res: Response) => {
    const {
        discordId,
        keplrId,
        minecraftId
    } = req.body;

    const options = {
        discordId: discordId ?? undefined,
        keplrId: keplrId ?? undefined,
        minecraftId: minecraftId ?? undefined
    };

    if (await doesLinkExist(options)) {
        return res.status(400).json({ message: 'Connection already exists between 1 or more of your accounts' });
    }

    const document = await createLink(options);

    if (document) return res.status(200).json(document);
    return res.status(404).json({ message: 'Connection not found' });
};

export default {
    getConnectionLink,
    createConnectionLink
};
