// use cw20::{Cw20Coin, Cw20ReceiveMsg};
// use cosmwasm_std::{Coin};
use cosmwasm_std::{Uint128};
use cw721::Cw721ReceiveMsg;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
pub struct InitMsg {
    pub name: String,
    pub denom: String, // ucraft
    pub dao_address: String, // where we pay the 'tax' too, a craft multisig addr
    pub tax_rate: u128 // 5 = 5%
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub enum HandleMsg {
    WithdrawNft { offering_id: String },
    BuyNft { offering_id: String },
    ReceiveNft(Cw721ReceiveMsg),
    // UpdateDaoAddress { new_dao_address: String } // only contract admin can execute this, or maybe only the DAO themselfs?
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub struct SellNft {
    pub list_price: Uint128,
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub enum QueryMsg {
    // GetOfferings returns a list of all offerings
    GetOfferings {},
    GetConfig {},
}