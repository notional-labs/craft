import { collections } from './database.service';

import axios from 'axios';

/**
 * Get data about a property (name, desc, worldName, imageLink, etc.) - from reProperties
 * http://127.0.0.1:4000/v1/realestate/dbcd78cb-326e-4842-982b-9252f9ca25a7
 * 
 * @param uuid The UUID of the property
 */
export const getPropertyInformation = async (uuid: string) => {
    let doc = await collections?.reProperties?.find({ _id: uuid }).tryNext();

    // Set username
    if (doc) {
        return doc;
    } 
    
    return undefined;    
};

/**
 * Gets a properties status/state (FOR_RENT, FOR_SALE, RENTED, OWNED)
 * http://127.0.0.1:4000/v1/realestate/state/FOR_RENT
 * 
 * @param state The state to query of the property (FOR_RENT, FOR_SALE, RENTED, OWNED)
 * @returns The properties with the given state as an array of Strings
 */
export const getPropertiesState = async (state: string) => {
    // FOR_RENT, FOR_SALE, RENTED, OWNED
    let properties: string[] = [];

    await collections?.reProperties?.find({ state: state }).forEach(doc => {
        const reID = doc?._id.toString();
        properties.push(reID);
    });
    
    return properties;    
};

/**
 * Get a users CRAFT owned NFTs (for now this is the omniflix for onft testing)
 * (Acts more so like middle ware)
 * http://127.0.0.1:4000/v1/realestate/owned/omniflix13na285c3llhnfenq6hl3fh255scljcue4td9nh
 * 
 * @param wallet The CRAFT address of the user
 */
export const getNFTs = async (wallet: string) => {
    let usersNFTs = {} // uuid as key, description, name, type as values (useful for webapp & in game minecraft syncing)

    let api = `https://rest.flixnet-4.omniflix.network/omniflix/onft/v1beta1/onfts/onftdenome307136eed384681ab981e6aedbcad5c/${wallet}`
    // console.log(api)

    // make a get request to that API with Axios
    let response = await axios.get(api).catch(err => {
        console.log(err);
        return undefined;
    })
    // console.log(response)

    // get collections section from the JSON response of the blockchain rest API
    // https://rest.flixnet-4.omniflix.network/omniflix/onft/v1beta1/onfts/onftdenome307136eed384681ab981e6aedbcad5c/omniflix13na285c3llhnfenq6hl3fh255scljcue4td9nh
    let onfts = response?.data?.collections?.[0]?.onfts;
    // console.log(onfts)

    if(!onfts) { 
        console.log(`onfts is undefined or there are none for request: ${api}`)
        return {};
    }

    // loop through onfts objects
    for (let onft of onfts) {
        let name = onft?.metadata?.name
        let dataObj = JSON.parse(onft?.data) // convert that json string into an object

        // console.log(name, dataObj?.description, dataObj?.uuid, dataObj?.type)

        usersNFTs[dataObj?.uuid] = {
            "name": name,
            "description": dataObj?.description,
            "type": dataObj?.type
        }
    }

    // return the response data as JSON
    return usersNFTs;
       
};
