// Express
import { Request, Response } from 'express';
import { createLink, doesLinkExist, getMCUUID } from '../services/connections.service';

/**
 * Handles the creation of an account link
 */
export const createConnectionLink = async (req: Request, res: Response) => {
    const { discordId, keplrId, minecraftId } = req.body;

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


/**
 * Handles getting a minecraft account UUID from a randomly generated code (craft-webapp-sync plugin)
 * 
 * http://127.0.0.1:4000/v1/connections/code/SOMERANDOMCODEHERE
 * 
 * @param req 
 * @param res 
 * @returns 
 */
export const getMinecraftIDFromCode = async (req: Request, res: Response) => {
    const { minecraftCode } = req.params;

    const document = await getMCUUID(minecraftCode);

    if (document) return res.status(200).json(document);
    return res.status(404).json('No account found for this code.');
};

export default {
    createConnectionLink,
    getMinecraftIDFromCode
};
