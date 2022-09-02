use crate::msg::{OfferingsResponse, QueryOfferingsResult, CollectionDataResponse}; // TODO: move these to msg
                                                           // use crate::msg::{PlatformFeeResponse, DenomResponse, DaoAddressResponse};
use crate::msg::{CollectionVolumeResponse, ContractInfoResponse};
use cosmwasm_std::{Deps, Order, StdResult, Uint128};

use crate::state::{Offering, COLLECTION_VOLUME, CONTRACT_INFO, OFFERINGS, Volume};

// gets all offerings
// ============================== Query Handlers ==============================
pub fn query_offerings(deps: Deps, filter_seller: Option<String>) -> StdResult<OfferingsResponse> {
    let res: StdResult<Vec<QueryOfferingsResult>> = OFFERINGS
        .range(deps.storage, None, None, Order::Ascending)
        // .map(|kv_item| parse_offering(kv_item))
        .map(parse_offering)

        // get just offerings from the seller we requested.
        .filter(|item| {
            match item {
                Ok(item) => {
                    if let Some(filter_seller) = &filter_seller {
                        item.seller.to_string() == filter_seller.to_string()
                    } else {
                        true
                    }
                }
                Err(_) => false,
        }
        })
        .collect();
    Ok(OfferingsResponse {
        offerings: res?, // Placeholder
    })
}

fn parse_offering(item: StdResult<(String, Offering)>) -> StdResult<QueryOfferingsResult> {
    item.map(|(k, offering)| QueryOfferingsResult {
        offering_id: k,
        token_id: offering.token_id,
        list_denom: offering.list_denom,
        list_price: offering.list_price,
        token_uri: offering.token_uri,
        contract_addr: offering.contract_addr,
        seller: offering.seller,
    })
}

pub fn query_contract_info(deps: Deps) -> StdResult<ContractInfoResponse> {
    let config = CONTRACT_INFO.load(deps.storage)?;
    Ok(ContractInfoResponse {
        name: config.name,
        denom: config.denom,
        fee_receive_address: config.fee_receive_address,
        platform_fee: config.platform_fee,
        version: config.version,
        contact: "reece@crafteconomy.io".to_string(),
    })
}

pub fn query_collection_volume(
    deps: Deps,
    contract_address: &str,
) -> StdResult<CollectionVolumeResponse> {
    let volume = COLLECTION_VOLUME.may_load(deps.storage, contract_address)?;
    let denom = CONTRACT_INFO.load(deps.storage)?.denom; 

    let v_default = Volume {
        collection_volume: Uint128::zero(),
        num_traded: Uint128::zero(),
    };

    Ok(CollectionVolumeResponse {
        total_volume: volume.clone().unwrap_or_else(|| v_default.clone()).collection_volume,
        num_traded: volume.unwrap_or_else(|| v_default).num_traded,
        denom,
    })
}

pub fn query_collection_data(
    deps: Deps,
    contract_address: &str,
) -> StdResult<CollectionDataResponse> {

    // query offerings
    let offerings = OFFERINGS
        .range(deps.storage, None, None, Order::Ascending)
        .map(parse_offering)
        .filter(|item| {
            match item {
                Ok(item) => {
                    item.contract_addr.to_string() == contract_address.to_string()
                }
                Err(_) => false,
            }
        })
        .collect::<StdResult<Vec<QueryOfferingsResult>>>()?;        

    // loop through offerings's & get the min and max list_price
    let mut floor_price = Uint128::zero();
    let mut ceiling_price = Uint128::zero();
    for offering in offerings.clone().iter() {
        if floor_price.is_zero() && ceiling_price.is_zero() {
            floor_price = offering.list_price;
            ceiling_price = offering.list_price;
        }
        // gets max listing price if its the highest
        if offering.list_price > ceiling_price {
            ceiling_price = offering.list_price;
        }
        // ... if its it the lowest
        if offering.list_price < floor_price {
            floor_price = offering.list_price;
        }
    }

    let volume_data = query_collection_volume(deps, contract_address)?;    
    Ok(CollectionDataResponse {
        floor_price: floor_price,
        total_offerings: offerings.len() as u128,
        ceiling_price: ceiling_price,
        volume: volume_data,     
    })
}