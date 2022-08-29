// Express
import { Request, Response } from 'express';
import { createLink, doesLinkExist, getMCUUID } from '../services/connections.service';

/**
 * Handles the creation of an account link
 * 
 * curl --data '{"discordId":"288822117993283604", "keplrId":"craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0", "minecraftCode":"GONMNM9YX9JFSWXBTJKI8JAR4QNFFP"}' -X POST -H "Content-Type: application/json"  http://localhost:4000/v1/connections/link
 */
export const createConnectionLink = async (req: Request, res: Response) => {
    // we not longer have to submit their minecraftId bc the minecraftCode -> the UUID, then we just insert that.
    const { discordId, keplrId, minecraftCode } = req.body;
    // console.log('MinecraftCode: ', minecraftCode);

    const myUUID = await getMCUUID(minecraftCode);
    if (!myUUID) return res.status(200).json('This secret code for syncing is not found for any minecraft account!');

    // get _id from the myUUID object
    const { _id } = myUUID;
    const minecraftId = _id.toString();
    // console.log('Minecraft ID: ', minecraftId);        

    const options = {
        discordId: discordId ?? undefined,
        keplrId: keplrId ?? undefined,
        minecraftId: minecraftId ?? undefined
    };

    // if link already exist, we update it to new options. or insert a new one.
    const document = await createLink(options);
    if (document) return res.status(200).json(document);
    return res.status(404).json({ message: 'Connection not found' });
};

export default {
    createConnectionLink,    
};
