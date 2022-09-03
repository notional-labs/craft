// use cw20::{Cw20Coin, Cw20ReceiveMsg};
// use cosmwasm_std::{Coin};
use cosmwasm_std::{Addr, Uint128};
use cw721::Cw721ReceiveMsg;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

use crate::state::Offering;

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
pub struct InitMsg {
    pub name: String,
    pub denom: String,               // ucraft
    pub fee_receive_address: String, // where we pay the 'tax' (platform fee) too, a craft multisig addr
    pub platform_fee: u128,          // 5 = 5%
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub enum ExecuteMsg {
    WithdrawNft {
        offering_id: String,
    },
    BuyNft {
        offering_id: String,
    },
    ReceiveNft(Cw721ReceiveMsg),

    UpdateListingPrice {
        offering_id: String,
        new_price: Uint128,
    },

    // DAO / Fee receiver only functions
    UpdateFeeReceiverAddress {
        new_address: String, // change to a new DAO wallet / admin of the contract
    },
    UpdatePlatformFee {
        new_fee: u128, // 1 = 1%, 2=2% etc...
    },
    ForceWithdrawAll {}, // contract admin sends ALL NFTs back to original owners (good for during upgrades)
    ToggleAbilityToSell{ status: bool }, // contract admin can turn off selling, useful to do before force withdrawing all
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub struct SellNft {
    pub list_price: Uint128,
}

// ======= RESPONSES =======
#[derive(Serialize, Deserialize, Clone, PartialEq, JsonSchema, Debug)]
pub struct ContractInfoResponse {
    pub name: String,
    pub denom: String,
    pub fee_receive_address: String,
    pub platform_fee: u128, // 5 = 5%.
    pub version: String,
    pub contact: String,   
    pub is_selling_allowed: bool, 
}

#[derive(Serialize, Deserialize, Clone, PartialEq, JsonSchema, Debug)]
pub struct QueryOfferingsResult {
    pub offering_id: String,
    pub token_id: String,
    pub token_uri: String,
    pub list_denom: String,
    pub list_price: Uint128,
    pub contract_addr: Addr,
    pub seller: Addr,
}

#[derive(Serialize, Deserialize, Clone, PartialEq, JsonSchema, Debug)]
pub struct CollectionVolumeResponse {
    pub total_volume: Uint128,
    pub num_traded: Uint128,
    pub denom: String,
}

#[derive(Serialize, Deserialize, Clone, PartialEq, JsonSchema, Debug)]
pub struct CollectionDataResponse {
    pub floor_price: Uint128,
    pub ceiling_price: Uint128,
    pub total_offerings: u128,
    pub volume: CollectionVolumeResponse,
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
pub struct OfferingsResponse {
    pub offerings: Vec<QueryOfferingsResult>,
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
pub struct RecentlySoldResponse {
    pub recently_sold: Vec<Offering>,
}

impl Iterator for OfferingsResponse {
    type Item = QueryOfferingsResult;
    fn next(&mut self) -> Option<Self::Item> {
        self.offerings.pop()
    }
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub enum QueryMsg {
    // GetOfferings returns a list of all offerings
    GetOfferings {
        filter_seller: Option<String>
    },
    // Returns info about the contract such as name, denom, dao_address, and the tax_rate (platform fee)
    GetContractInfo {},

    // GetCollectionVolume { address: String },

    GetCollectionData { address: String }, // move collectionvolume to here?

    GetRecentlySold {},
}

#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub struct MigrateMsg {}
