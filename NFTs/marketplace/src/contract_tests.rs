#[cfg(test)]

// Example I like & am using:
// https://github.com/osmosis-labs/cw-usdc/blob/main/contracts/cw-usdc/src/contract_tests.rs

// use cosmwasm_std::{DepsMut, Uint128};

// use super::*;
use cosmwasm_std::testing::{mock_dependencies, mock_env, mock_info};
use cosmwasm_std::{coins, from_binary, Uint128, MessageInfo, Deps};

// use crate::package::{ContractInfoResponse, OfferingsResponse, QueryOfferingsResult};
// use crate::state::{increment_offerings, Offering, CONTRACT_INFO, OFFERINGS};
use cosmwasm_std::{to_binary};

use cw721::{Cw721ReceiveMsg};
use crate::error::ContractError;
use crate::msg::{HandleMsg, InitMsg, QueryMsg, SellNft, ContractInfoResponse};

use cosmwasm_std::{DepsMut};
use crate::contract::{instantiate, execute, query};
use crate::contract;
use crate::msg::{OfferingsResponse};

// test helper
fn initialize_contract(deps: DepsMut) -> (String, String, u128) {
    let denom = String::from("ucraft");
    let msg = InitMsg {
        name: String::from("test market"),
        denom: denom.clone(),
        fee_receive_address: String::from("craftdaoaddr"),
        platform_fee: 5,
    };

    let info = mock_info("creator", &coins(1000000, denom.clone()));
    contract::instantiate(deps, mock_env(), info.clone(), msg.clone()).unwrap();

    (msg.denom, msg.fee_receive_address, msg.platform_fee)
}

#[test]
fn proper_initialization() {
    let mut deps = mock_dependencies();
    let (denom, dao_address, platform_fee) = initialize_contract(deps.as_mut());

    let res: ContractInfoResponse = from_binary(&contract::query(deps.as_ref(), mock_env(), QueryMsg::GetContractInfo {}).unwrap()).unwrap();
    assert_eq!(res.name, "test market");
    assert_eq!(res.denom, denom);
    assert_eq!(res.fee_receive_address, dao_address);
    assert_eq!(res.platform_fee, platform_fee);
}

#[test]
fn test_sell_offering() {
    let mut deps = mock_dependencies();

    let (denom, _dao_address, _tax_rate) = initialize_contract(deps.as_mut());

    let amount: u128 = 1_000_000; // list price & buy price
    let info = mock_info("anyone", &coins(0, &denom));
    let sell_msg = SellNft {
        list_price: Uint128::new(amount.clone()), // so DAO should get 50k @ 5%
    };
    let msg = HandleMsg::ReceiveNft(Cw721ReceiveMsg {
        sender: String::from("seller_contract"),
        token_id: String::from("token_id"),
        msg: to_binary(&sell_msg).unwrap(),
    });

    let res = contract::execute(deps.as_mut(), mock_env(), info, msg);
    match res {
        Ok(_) => {},
        Err(e) => {
            panic!("{:?}", e);
        }
    }

    // Offering should be listed = length of 1
    let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
    let value: OfferingsResponse = from_binary(&res).unwrap();
    assert_eq!(1, value.offerings.len());
}

#[test]
fn test_buying_offering() {
    // test_sell_offering
    let mut deps = mock_dependencies();

    let (denom, _dao_address, _tax_rate) = initialize_contract(deps.as_mut());

    let amount: u128 = 1_000_000; // list price & buy price

    let info = mock_info("anyone", &coins(0, &denom));
    sell_nft(deps.as_mut(), info.clone(), amount.clone());
    

    // Offering should be listed = length of 1
    let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
    let value: OfferingsResponse = from_binary(&res).unwrap();
    assert_eq!(1, value.offerings.len());

    // == new logic ==
    // Purchase the NFT from the store for 1mil ucraft
    let info = mock_info("buyer", &coins(amount.clone(), &denom));
    let res = contract::execute(deps.as_mut(), mock_env(), info, HandleMsg::BuyNft { offering_id: value.offerings[0].id.clone() });
    match res {
        Ok(_) => {},
        Err(_) => panic!("should have succeeded"),
    }

    // TODO: check the balance of seller == 950_000 after the Tx, and the DAO has 50k ucraft
    // panic!("{}", _res.unwrap_err()); // useful for debugging

    // check offerings again. Should be 0 since the NFT is bought
    let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
    let value: OfferingsResponse = from_binary(&res).unwrap();
    assert_eq!(0, value.offerings.len());  

    // == OVERPAYING FOR AN OFFERING ==
    let for_sale_amount = 1_000_000;
    let overpay_amount = 3_100_000;
    let buyer_info = mock_info("addr1", &coins(overpay_amount, &denom));
    sell_nft(deps.as_mut(), buyer_info.clone(), for_sale_amount);
    

    // get offering_id in the offering list
    let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
    let value: OfferingsResponse = from_binary(&res).unwrap();
    let offering_id = value.offerings[0].id.clone();
    assert_eq!("2", offering_id);

    match buy_nft(deps.as_mut(), buyer_info, offering_id) { // 
        Ok(_) => panic!("should have failed, InsufficientFundsSend"),
        Err(e) => {
            match e {
                ContractError::InsufficientFundsSend { needed: _, received: _ } => {},
                _ => panic!("should have failed with OverpayingForOffering"),
            }
        }
    }
}

fn buy_nft(deps: DepsMut, info: MessageInfo, offering_id: String) -> Result<(), ContractError> {
    let res = execute(deps, mock_env(), info, HandleMsg::BuyNft { offering_id: offering_id });
    match res {
        Ok(_) => Ok(()),
        Err(e) => Err(e),
    }
}

fn sell_nft(deps: DepsMut, info: MessageInfo, amount: u128) {
// now test if you overpay (expect error) [can this be done in helper function?]
    let sell_msg = SellNft {
        list_price: Uint128::new(amount.clone()), // so DAO should get 50k @ 5%
    };
    let msg = HandleMsg::ReceiveNft(Cw721ReceiveMsg {
        sender: String::from("seller_contract"),
        token_id: String::from("token_id"),
        msg: to_binary(&sell_msg).unwrap(),
    });
    contract::execute(deps, mock_env(), info, msg).unwrap();
}

fn receive_nft(deps: DepsMut, info: MessageInfo, list_price: u128, token_id: String) -> Result<(), ContractError> {
    let sell_msg = SellNft {
        list_price: Uint128::new(list_price),
    };

    let msg = HandleMsg::ReceiveNft(Cw721ReceiveMsg {
        sender: String::from("seller"),
        token_id: String::from(token_id),
        msg: to_binary(&sell_msg).unwrap(),
    });

    match execute(deps, mock_env(), info, msg) {
        Ok(_) => Ok(()),
        Err(e) => Err(e),
    }
}

fn get_offerings(deps: Deps) -> OfferingsResponse {
    let res = query(deps, mock_env(), QueryMsg::GetOfferings {}).unwrap();
    let value: OfferingsResponse = from_binary(&res).unwrap();
    value
}

#[test]
fn test_withdraw_offering() {
    let mut deps = mock_dependencies();

    let msg = InitMsg {
        name: String::from("test market"),
        denom: String::from("ucraft"),
        fee_receive_address: String::from("craftdaoaddr"),
        platform_fee: 5
    };
    let denom = msg.denom.clone();

    let info = mock_info("creator", &coins(2, &denom));
    let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

    // beneficiary can release it
    let info = mock_info("anyone", &coins(2, &denom));
    receive_nft(deps.as_mut(), info.clone(), 5, String::from("token_id")).unwrap();


    // Offering should be listed
    let value = get_offerings(deps.as_ref());
    assert_eq!(1, value.offerings.len());

    // withdraw offering
    let withdraw_info = mock_info("seller", &coins(2, &denom));
    let withdraw_msg = HandleMsg::WithdrawNft {
        offering_id: value.offerings[0].id.clone(),
    };
    let _res = execute(deps.as_mut(), mock_env(), withdraw_info, withdraw_msg).unwrap();

    // Offering should be removed
    let value = get_offerings(deps.as_ref());
    assert_eq!(0, value.offerings.len());
}