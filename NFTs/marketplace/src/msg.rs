// use cw20::{Cw20Coin, Cw20ReceiveMsg};
// use cosmwasm_std::{Coin};
use cosmwasm_std::{Addr, Uint128};
use cw721::Cw721ReceiveMsg;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
pub struct InitMsg {
    pub name: String,
    pub denom: String, // ucraft
    pub fee_receive_address: String, // where we pay the 'tax' (platform fee) too, a craft multisig addr
    pub platform_fee: u128 // 5 = 5%
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub enum HandleMsg {
    WithdrawNft { offering_id: String },
    BuyNft { offering_id: String },
    ReceiveNft(Cw721ReceiveMsg),
    // UpdateDaoAddress { new_dao_address: String } // only contract admin can execute this, or maybe only the DAO themselfs?

    UpdateFeeReceiverAddress { new_address: String }
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub struct SellNft {
    pub list_price: Uint128,
}


// ======= RESPONSES =======
// should these be in their own file??
#[derive(Serialize, Deserialize, Clone, PartialEq, JsonSchema, Debug)]
pub struct ContractInfoResponse {
    pub name: String,
    pub denom: String,
    pub fee_receive_address: String, 
    pub platform_fee: u128 // 5 = 5%.
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



#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub enum QueryMsg {
    // GetOfferings returns a list of all offerings
    GetOfferings {},
    // Returns info about the contract such as name, denom, dao_address, and the tax_rate (platform fee)
    GetContractInfo {},
}