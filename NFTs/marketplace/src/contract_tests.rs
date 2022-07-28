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

    let info = mock_info("creator", &coins(1000000, &denom));
    contract::instantiate(deps, mock_env(), info, msg.clone()).unwrap();

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
fn test_update_fee_receiver_address() {
    let mut deps = mock_dependencies();
    let (_, fee_receiver, _) = initialize_contract(deps.as_mut());
    // println!("Initial fee_receiver: {:?}", fee_receiver);

    // contract::update_fee_receiver_address(deps.as_mut(), mock_info("anyone", &coins(1, "token")), "new_dao_address".to_string()).unwrap();
    let msg = HandleMsg::UpdateFeeReceiverAddress {
        new_address: "new_dao_address".to_string()
    };

    // ensure the new address is not the initial one
    assert_ne!(fee_receiver, "new_dao_address");

    let useless_coins = coins(1, "ucraft");

    // try changing the current address as a non DAO user (should fail)
    let non_dao_user_info = mock_info("not_the_fee_receiver", &useless_coins);
    let err = contract::execute(deps.as_mut(), mock_env(), non_dao_user_info, msg.clone()).unwrap_err();
    match err {
        ContractError::Unauthorized { msg: _ } => {}
        _ => panic!("Unexpected error: {:?}", err),
    }

    // try to change it successfully as the DAO (fee receiver)
    let info = mock_info(&fee_receiver, &useless_coins);
    contract::execute(deps.as_mut(), mock_env(), info, msg).unwrap();

    let res: ContractInfoResponse = from_binary(&contract::query(deps.as_ref(), mock_env(), QueryMsg::GetContractInfo {}).unwrap()).unwrap();
    // println!("New receiver: {:?}", res);
    assert_eq!(res.fee_receive_address, "new_dao_address");

}

#[test]
fn test_update_platform_fee() {
    let mut deps = mock_dependencies();
    let (_, fee_receiver, _platform_fee) = initialize_contract(deps.as_mut());
    // println!("Initial fee_receiver: {}, platform fee: {}", fee_receiver, platform_fee);

    // contract::update_fee_receiver_address(deps.as_mut(), mock_info("anyone", &coins(1, "token")), "new_dao_address".to_string()).unwrap();
    let msg = HandleMsg::UpdatePlatformFee { new_fee: 7 };
    let high_msg = HandleMsg::UpdatePlatformFee { new_fee: 101 };

    let useless_coins = coins(1, "ucraft");

    // try changing the current address as a non DAO user (should fail)
    let non_dao_user_info = mock_info("not_the_fee_receiver", &useless_coins);
    let err = contract::execute(deps.as_mut(), mock_env(), non_dao_user_info, msg.clone()).unwrap_err();
    match err {
        ContractError::Unauthorized { msg: _ } => {}
        _ => panic!("Unexpected error: {:?}", err),
    }

    // change as DAO, but >100 (should fail)
    let info = mock_info(&fee_receiver, &useless_coins);
    let err = contract::execute(deps.as_mut(), mock_env(), info, high_msg).unwrap_err();
    match err {
        ContractError::PlatformFeeToHigh {} => {}
        _ => panic!("Unexpected error: {:?}", err),
    }
    
    // try to change it successfully as the DAO (fee receiver)
    let info = mock_info(&fee_receiver, &useless_coins);
    contract::execute(deps.as_mut(), mock_env(), info, msg).unwrap();

    let res: ContractInfoResponse = from_binary(&contract::query(deps.as_ref(), mock_env(), QueryMsg::GetContractInfo {}).unwrap()).unwrap();
    // println!("New platform fee: {:?}", res);
    assert_eq!(res.platform_fee, 7);
}

#[test]
fn test_force_withdraw_all_from_marketplace() {
    let mut deps = mock_dependencies();
    let (_, fee_receiver, _) = initialize_contract(deps.as_mut());
    println!("Initial fee_receiver: {}", fee_receiver);

    // contract::update_fee_receiver_address(deps.as_mut(), mock_info("anyone", &coins(1, "token")), "new_dao_address".to_string()).unwrap();
    let msg = HandleMsg::ForceWithdrawAll {  };
    let useless_coins = coins(1, "ucraft");

    // try changing the current address as a non DAO user (should fail)
    // let non_dao_user_info = mock_info("not_the_fee_receiver", &useless_coins);
    // let err = contract::execute(deps.as_mut(), mock_env(), non_dao_user_info, msg.clone()).unwrap_err();
    // match err {
    //     ContractError::Unauthorized { msg: _ } => {}
    //     _ => panic!("Unexpected error: {:?}", err),
    // }

    // try to change it, but you are not the dao so you can't
    let info = mock_info("not_the_fee_receiver", &useless_coins);
    let err = contract::execute(deps.as_mut(), mock_env(), info, msg.clone()).unwrap_err();
    match err {
        ContractError::Unauthorized { msg: _ } => {}
        _ => panic!("Unexpected error: {:?}", err),
    }

    // Takes all tokens for sale and sends them back to the rightful owners IF they are the fee_receiver.
    // Ex: 2 NFTs on marketplace, sends all to users (& removes from marketplace offerings), until there are 0 left.
    let info = mock_info(&fee_receiver, &useless_coins);

    sell_nft(deps.as_mut(), info.clone(), String::from("token1"), 12);
    sell_nft(deps.as_mut(), info.clone(), String::from("token2"), 13);

    let res: OfferingsResponse = get_offerings(deps.as_ref());
    assert_eq!(res.offerings.len(), 2);
    // send them to users
    contract::execute(deps.as_mut(), mock_env(), info, msg).unwrap();

    let res: OfferingsResponse = get_offerings(deps.as_ref());
    assert_eq!(res.offerings.len(), 0);
}

#[test]
fn test_sell_offering() {
    let mut deps = mock_dependencies();

    let (denom, _dao_address, _tax_rate) = initialize_contract(deps.as_mut());

    let amount: u128 = 1_000_000; // list price & buy price
    let info = mock_info("anyone", &coins(0, &denom));
    let sell_msg = SellNft {
        list_price: Uint128::new(amount), // so DAO should get 50k @ 5%
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
    let info_seller = mock_info("seller", &coins(0, &denom));
    sell_nft(deps.as_mut(), info_seller.clone(), String::from("token1"), amount);
    

    // Offering should be listed = length of 1
    let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
    let value: OfferingsResponse = from_binary(&res).unwrap();
    assert_eq!(1, value.offerings.len());

    // == new logic ==
    // Purchase the NFT from the store for 1mil ucraft
    let info = mock_info("buyer", &coins(amount, &denom));
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
    sell_nft(deps.as_mut(), info_seller.clone(), String::from("token1"), for_sale_amount);
    

    // get offering_id in the offering list
    let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
    let value: OfferingsResponse = from_binary(&res).unwrap();
    let offering_id = value.offerings[0].id.clone();
    assert_eq!("2", offering_id);

    // try to have the seller buy their own offering. Should error out
    match buy_nft(deps.as_mut(), info_seller, offering_id.clone()) { // 
        Ok(_) => panic!("should have failed, InsufficientFundsSend"),
        Err(e) => {
            match e {
                ContractError::UnableToPurchaseMarketplaceItemYouSold {  } => {}, 
                _ => panic!("should have failed with UnableToPurchaseMarketplaceItemYouSold"),
            }
        }
    }

    // buyer tries to buy it with an overpayment
    let buyer_info = mock_info("addr1", &coins(overpay_amount, &denom));
    match buy_nft(deps.as_mut(), buyer_info, offering_id) { // 
        Ok(_) => panic!("should have failed, InsufficientFundsSend"),
        Err(e) => {
            match e {
                ContractError::InsufficientFundsSend { needed: _, received: _ } => {},
                // ContractError::UnableToPurchaseMarketplaceItemYouSold {  } => {}, // not checked this case yet
                _ => panic!("should have failed with OverpayingForOffering"),
            }
        }
    }
}

#[test]
fn test_withdraw_offering() {
    // TODO: Cleanup
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
    receive_nft(deps.as_mut(), info, 5, String::from("token_id")).unwrap();


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

#[test]
fn test_update_offering_price() {
    let mut deps = mock_dependencies();

    let (denom, _, _) = initialize_contract(deps.as_mut());

    let amount: u128 = 1_000_000; // list price & buy price
    let info = mock_info("anyone", &coins(0, &denom));
    sell_nft(deps.as_mut(), info.clone(), String::from("token1"), amount);

    // Offering should be listed = length of 1
    let value: OfferingsResponse = get_offerings(deps.as_ref());
    assert_eq!(1, value.offerings.len());

    // get the first offering
    let offering_id = value.offerings[0].id.clone();

    // update the price of the offering
    let new_amount: Uint128 = Uint128::from(9_999_999_u128);
    let update_msg = HandleMsg::UpdateListingPrice {
        offering_id,
        new_price: new_amount,
    };


    let res = contract::execute(deps.as_mut(), mock_env(), info, update_msg.clone());
    match res {
        Ok(_) => {},
        Err(e) => panic!("should have succeeded: {:?}", e),
    }

    // confirm the attribute key of old_price is updated to new_price
    let value: OfferingsResponse = get_offerings(deps.as_ref());
    assert_eq!(new_amount, value.offerings[0].list_price);


    // try to update the price of the offering, but with the incorrect sender
    let info = mock_info("wrong_person", &coins(0, &denom));
    let res = contract::execute(deps.as_mut(), mock_env(), info, update_msg);
    match res {
        Ok(_) => panic!("should have failed"),
        Err(e) => {
            match e {
                ContractError::Unauthorized { msg: _ } => {}
                _ => panic!("should have failed with InvalidSender"),
            }
        }
    }


}

fn buy_nft(deps: DepsMut, info: MessageInfo, offering_id: String) -> Result<(), ContractError> {
    let res = execute(deps, mock_env(), info, HandleMsg::BuyNft { offering_id });
    match res {
        Ok(_) => Ok(()),
        Err(e) => {
            println!("{:?}", e);
            Err(e)
        },
    }
}

fn sell_nft(deps: DepsMut, info: MessageInfo, token_id: String, amount: u128) {
// now test if you overpay (expect error) [can this be done in helper function?]
    let sell_msg = SellNft {
        list_price: Uint128::new(amount), // so DAO should get 50k @ 5%
    };
    let msg = HandleMsg::ReceiveNft(Cw721ReceiveMsg {
        sender: String::from(info.sender.clone()), // was "seller_contract"
        token_id,
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
        token_id,
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
