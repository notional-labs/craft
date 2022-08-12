use crate::msg::{OfferingsResponse, QueryOfferingsResult}; // TODO: move these to msg
                                                           // use crate::msg::{PlatformFeeResponse, DenomResponse, DaoAddressResponse};
use crate::msg::{CollectionVolumeResponse, ContractInfoResponse};
use cosmwasm_std::{Deps, Order, StdResult, Uint128};

use crate::state::{Offering, COLLECTION_VOLUME, CONTRACT_INFO, OFFERINGS};

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
    let total_volumes = COLLECTION_VOLUME.may_load(deps.storage, contract_address)?;
    let denom = CONTRACT_INFO.load(deps.storage)?.denom;

    Ok(CollectionVolumeResponse {
        total_volume: total_volumes.unwrap_or_else(|| Uint128::new(0)),
        denom,
    })
}
