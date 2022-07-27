use cosmwasm_std::BankMsg;
use crate::coin_helpers::assert_sent_exact_coin;
use cw721::{Cw721ExecuteMsg, Cw721ReceiveMsg};

// use crate::package::{ContractInfoResponse};
use crate::state::{increment_offerings, Offering, CONTRACT_INFO, OFFERINGS};
use cosmwasm_std::{
    from_binary, to_binary, CosmosMsg, DepsMut, MessageInfo, Response, SubMsg, WasmMsg, Coin
};

use crate::error::ContractError;
use crate::msg::{SellNft};

// receive funds & buy NFT if funds are enough
pub fn buy_nft( deps: DepsMut, info: MessageInfo, offering_id: String) -> Result<Response, ContractError> {

    // load offering from storage if a given offering_id exist, if not, return NoMarketplaceOfferingWithGivenID
    let off = OFFERINGS.may_load(deps.storage, &offering_id)?;
    if off.is_none() {
        return Err(ContractError::NoMarketplaceOfferingWithGivenID { id: offering_id });
    } 

    let off = off.unwrap();
    if off.seller == info.sender {
        return Err(ContractError::UnableToPurchaseMarketplaceItemYouSold {});
    }

    let denom = CONTRACT_INFO.load(deps.storage)?.denom;

    // check for enough coins (>= the listing price with the same denom)
    assert_sent_exact_coin(&info.funds, Some(Coin::new(off.list_price.clone().u128(), &denom)))?;
    
    // DAO TAX AND PAYMENTS
    let tax_rate = CONTRACT_INFO.load(deps.storage)?.platform_fee; // 5 = 5%
    let dao_addr = CONTRACT_INFO.load(deps.storage)?.fee_receive_address;

    // 1_000_000ucraft * 0.05 = 50000ucraft -> DAO [5 = 5/100 = 5%]
    let dao_tax_payment = off.list_price.clone().u128() / 100 * tax_rate;
    // // println!("dao_tax_payment: {}", &dao_tax_payment);
    // 1_000_000ucraft - 50000 = 950_000ucraft -> seller
    let seller_payment: u128 = off.list_price.clone().u128() - &dao_tax_payment;

    // if the user sends more funds then the list price, return those to them on success (if any)

    // PAYMENT COINS
    // convert off.list_price to a vector of coins
    let sellers_token_payment = vec![Coin::new(seller_payment.clone(), &denom)];
    let daos_token_tax = vec![Coin::new(dao_tax_payment as u128, &denom)];

    // == TRANSFERS ==
    // send the ucraft -> the off.seller using BankMsg & the DAOs contract address
    let transfer_seller_tokens = BankMsg::Send {
        to_address: off.seller.to_string(),
        amount: sellers_token_payment,
    };
    let transfer_daos_tokens = BankMsg::Send {
        to_address: dao_addr.to_string(),
        amount: daos_token_tax,
    };

    // create transfer cw721 msg
    let transfer_cw721_msg = Cw721ExecuteMsg::TransferNft {
        recipient: info.sender.to_string(),
        token_id: off.token_id.clone(),
    };
    let exec_cw721_transfer = WasmMsg::Execute {
        contract_addr: off.contract_addr.clone().into_string(),
        msg: to_binary(&transfer_cw721_msg)?,
        funds: vec![],
    };

    // == SUBMIT TRANSFERS == 
    // if everything is fine transfer ucraft to seller
    let transfer_cosmos_msg_seller: CosmosMsg = transfer_seller_tokens.into();
    let transfer_cosmos_msg_dao: CosmosMsg = transfer_daos_tokens.into();
    // transfer nft to buyer
    let cw721_transfer_cosmos_msg: CosmosMsg = exec_cw721_transfer.into();

    let cosmos_msgs = vec![
        SubMsg::new(transfer_cosmos_msg_seller), 
        SubMsg::new(transfer_cosmos_msg_dao), 
        SubMsg::new(cw721_transfer_cosmos_msg)
    ];

    //delete offering
    OFFERINGS.remove(deps.storage, &offering_id);

    let price_string = format!("{} {}", off.list_price.clone().u128(), info.sender);

    return Ok(Response::new()
        .add_attribute("action", "buy_nft")
        .add_attribute("buyer", info.sender.to_string())
        .add_attribute("seller", off.seller)
        .add_attribute("total_paid_price", price_string)
        .add_attribute("tax_paid", dao_tax_payment.to_string())
        .add_attribute("seller_receive", seller_payment.to_string())
        .add_attribute("token_id", off.token_id)
        .add_attribute("contract_addr", off.contract_addr)
        .add_submessages(cosmos_msgs))
}

// gets NFT from a 721 contract
pub fn receive_nft( deps: DepsMut, info: MessageInfo, rcv_msg: Cw721ReceiveMsg) -> Result<Response, ContractError> {
    let msg: SellNft = from_binary(&rcv_msg.msg)?;

    // check if same token Id form same original contract is already on sale
    // get OFFERING_COUNT
    let id = increment_offerings(deps.storage)?.to_string();

    // save Offering
    let denom = CONTRACT_INFO.load(deps.storage)?.denom;
    let off = Offering {
        contract_addr: info.sender.clone(),
        list_denom: denom,
        token_id: rcv_msg.token_id,
        seller: deps.api.addr_validate(&rcv_msg.sender)?,
        list_price: msg.list_price,
    };

    OFFERINGS.save(deps.storage, &id, &off)?;

    let price_string = format!("{} {}", msg.list_price, "ucraft");

    Ok(Response::new()
        .add_attribute("action", "sell_nft")
        .add_attribute("original_contract", info.sender)
        .add_attribute("seller", off.seller)
        .add_attribute("list_price", price_string)
        .add_attribute("token_id", off.token_id))
}

pub fn withdraw_offering( deps: DepsMut, info: MessageInfo, offering_id: String) -> Result<Response, ContractError> {
    // check if token_id is currently sold by the requesting address
    let off = OFFERINGS.load(deps.storage, &offering_id)?;
    if off.seller == info.sender {
        // transfer token back to original owner
        let transfer_cw721_msg = Cw721ExecuteMsg::TransferNft {
            recipient: off.seller.clone().into_string(),
            token_id: off.token_id.clone(),
        };

        let exec_cw721_transfer = WasmMsg::Execute {
            contract_addr: off.contract_addr.into_string(),
            msg: to_binary(&transfer_cw721_msg)?,
            funds: vec![],
        };

        let cw721_transfer_cosmos_msg: CosmosMsg = exec_cw721_transfer.into();
        let cw721_submsg = SubMsg::new(cw721_transfer_cosmos_msg);

        // remove offering
        OFFERINGS.remove(deps.storage, &offering_id);

        return Ok(Response::new()
            .add_attribute("action", "withdraw_nft")
            .add_attribute("seller", info.sender)
            .add_attribute("offering_id", offering_id)
            .add_submessage(cw721_submsg));
    }
    Err(ContractError::Unauthorized {msg:"You are not the seller of this token, so you can not withdraw it.".to_string()})
}


pub fn update_fee_receiver_address( deps: DepsMut, info: MessageInfo, new_address: String) -> Result<Response, ContractError> {
    // ensure sender is the current fee_collector in CONTRACT_INFO
    let mut contract_info = CONTRACT_INFO.load(deps.storage)?;
    let current_contract_receiver = contract_info.fee_receive_address.clone().to_string();

    if info.sender.to_string() != current_contract_receiver.clone() {
        return Err(ContractError::Unauthorized {msg:"You are not the current fee_receiver".to_string()});
    }

    contract_info.fee_receive_address = new_address.clone();

    // update CONTRACT_INFO
    CONTRACT_INFO.save(deps.storage, &contract_info.clone())?;

    return Ok(Response::new()
        .add_attribute("action", "update_fee_receiver_address")
        .add_attribute("new_address", new_address)
        .add_attribute("old_address", current_contract_receiver));
}

pub fn update_platform_fee( deps: DepsMut, info: MessageInfo, new_fee: u128) -> Result<Response, ContractError> {
    let mut contract_info = CONTRACT_INFO.load(deps.storage)?;

    let current_platform_fee = contract_info.platform_fee.clone();
    let current_fee_receiver = contract_info.fee_receive_address.clone();

    if info.sender.to_string() != current_fee_receiver {
        return Err(ContractError::Unauthorized {msg:"You are not the current fee_receiver".to_string()});
    }

    if new_fee > 100 {
        return Err(ContractError::PlatformFeeToHigh {});
    }

    contract_info.platform_fee = new_fee;
    CONTRACT_INFO.save(deps.storage, &contract_info.clone())?;

    return Ok(Response::new()
        .add_attribute("action", "update_fee_receiver_address")
        .add_attribute("new_fee", new_fee.to_string())
        .add_attribute("old_fee", current_platform_fee.to_string()));
}