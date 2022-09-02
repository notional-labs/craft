import { CosmWasmClient } from "cosmwasm";


/**
 * Connect to the RPC node (26657) as the cosmwasm client
 * 
 * @returns CosmWasmClient | void
 */
 export const getCosmWasmClient = async (): Promise<CosmWasmClient | void> => {
    let rpc_url = `${process.env.CRAFTD_NODE}`;
    if(!rpc_url.endsWith("/")) {
        rpc_url += "/";
    }

    const client = await CosmWasmClient.connect(rpc_url).then((client) => {
        // console.log(`Successfully connected to CosmWasm node ${rpc_url}`);
        return client;
    }).catch((err) => {
        console.log(`Error: connectToNode: connecting to CosmWasm node: ${rpc_url}`);
        console.log(err);
        return undefined;
    });

    return client;
}