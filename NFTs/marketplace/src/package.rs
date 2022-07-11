use cosmwasm_std::{Addr, Uint128};
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Clone, PartialEq, JsonSchema, Debug)]
pub struct ContractInfoResponse {
    pub name: String,
    pub denom: String
}

#[derive(Serialize, Deserialize, Clone, PartialEq, JsonSchema, Debug)]
pub struct QueryOfferingsResult {
    pub id: String,
    pub token_id: String,
    pub list_denom: String,
    pub list_price: Uint128,
    pub contract_addr: Addr,
    pub seller: Addr,
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
pub struct OfferingsResponse {
    pub offerings: Vec<QueryOfferingsResult>,
}

// THIS FILE SHOULD BE EXTRACTED TO ITS OWN PACKAGE PROJECT LIKE CW20 OR CW721