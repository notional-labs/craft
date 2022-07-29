
use crate::msg::{OfferingsResponse, QueryOfferingsResult}; // TODO: move these to msg
// use crate::msg::{PlatformFeeResponse, DenomResponse, DaoAddressResponse};
use crate::msg::{ContractInfoResponse};
use cosmwasm_std::{Deps, Order, StdResult};

use crate::state::{CONTRACT_INFO, OFFERINGS, Offering};

// gets all offerings
// ============================== Query Handlers ==============================
pub fn query_offerings(deps: Deps) -> StdResult<OfferingsResponse> {
    let res: StdResult<Vec<QueryOfferingsResult>> = OFFERINGS
        .range(deps.storage, None, None, Order::Ascending)
        // .map(|kv_item| parse_offering(kv_item))
        .map(parse_offering)
        .collect();
    Ok(OfferingsResponse {
        offerings: res?, // Placeholder
    })
}

fn parse_offering(item: StdResult<(String, Offering)>) -> StdResult<QueryOfferingsResult> {
    item.map(|(k, offering)| {
        QueryOfferingsResult {
            id: k,
            token_id: offering.token_id,
            list_denom: offering.list_denom,
            list_price: offering.list_price,
            contract_addr: offering.contract_addr,
            seller: offering.seller,
        }
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