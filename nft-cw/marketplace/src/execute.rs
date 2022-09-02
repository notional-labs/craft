use crate::coin_helpers::assert_sent_exact_coin;
use crate::queries;
use cosmwasm_std::{BankMsg, Deps, StdResult, Uint128, WasmQuery};
use cw721::{Cw721ExecuteMsg, Cw721ReceiveMsg, Cw721QueryMsg, NftInfoResponse};

// use crate::package::{ContractInfoResponse};
use crate::state::{increment_offerings, Offering, COLLECTION_VOLUME, CONTRACT_INFO, OFFERINGS, Volume};
use cosmwasm_std::{
    from_binary, to_binary, Coin, CosmosMsg, DepsMut, MessageInfo, Response, SubMsg, WasmMsg,
};

use crate::error::ContractError;
use crate::msg::SellNft;

// receive funds & buy NFT if funds are enough
pub fn buy_nft(
    deps: DepsMut,
    info: MessageInfo,
    offering_id: String,
) -> Result<Response, ContractError> {
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
    assert_sent_exact_coin(
        &info.funds,
        Some(Coin::new(off.list_price.clone().u128(), &denom)),
    )?;

    // DAO TAX AND PAYMENTS
    let tax_rate = CONTRACT_INFO.load(deps.storage)?.platform_fee; // 5 = 5%
    let dao_addr = CONTRACT_INFO.load(deps.storage)?.fee_receive_address;

    // 1_000_000ucraft * 0.05 = 50000ucraft -> DAO [5 = 5/100 = 5%]
    let mut dao_tax_payment = (off.list_price.clone().u128() / 100) * &tax_rate;
    println!("dao_tax_payment: {}", &dao_tax_payment);
    // 1_000_000ucraft - 50000 = 950_000ucraft -> seller
    let mut seller_payment: u128 = off.list_price.clone().u128() - &dao_tax_payment;

    // if the offering price is <100, we dont payt any tax on it bc it is too small.
    // doing so errors out as 0ucraft send to seller, which doesn't work.
    if off.list_price.clone().u128() < 100 {
        dao_tax_payment = 0;
        seller_payment = off.list_price.clone().u128();
    }

    // if the user sends more funds then the list price, return those to them on success (if any)

    // PAYMENT COINS
    // convert off.list_price to a vector of coins
    let sellers_token_payment = vec![Coin::new(seller_payment, &denom)];
    let daos_token_tax = vec![Coin::new(dao_tax_payment as u128, &denom)];

    // == TRANSFERS ==
    // send the ucraft -> the off.seller using BankMsg & the DAOs contract address
    let transfer_seller_tokens = BankMsg::Send {
        to_address: off.seller.to_string(),
        amount: sellers_token_payment.clone(),
    };
    let transfer_daos_tokens = BankMsg::Send {
        to_address: dao_addr.clone(),
        amount: daos_token_tax.clone(),
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
        SubMsg::new(cw721_transfer_cosmos_msg),
    ];

    //delete offering
    OFFERINGS.remove(deps.storage, &offering_id);

    let price_string = format!("{} {}", off.list_price.clone().u128(), info.sender);

    // default value to update the collection volume information
    let v_default: Volume = Volume {
        collection_volume: Uint128::new(0),
        num_traded: Uint128::new(0),
    };

    // update COLLECTION_VOLUME with the new volume
    COLLECTION_VOLUME.update(
        deps.storage,
        &off.contract_addr.to_string(),
        |volume| -> StdResult<Volume> {
            Ok(Volume {
                collection_volume: volume.clone().unwrap_or_else(|| v_default.clone()).collection_volume + off.list_price.clone(),
                num_traded: volume.unwrap_or_else(|| v_default).num_traded + Uint128::new(1),
            })
        },
    )?;

    Ok(Response::new()
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
pub fn receive_nft(
    deps: DepsMut,
    info: MessageInfo,
    rcv_msg: Cw721ReceiveMsg,
) -> Result<Response, ContractError> {
    let msg: SellNft = from_binary(&rcv_msg.msg)?;

    // check if same token Id form same original contract is already on sale
    // get OFFERING_COUNT
    let id = increment_offerings(deps.storage)?.to_string();

    // save Offering
    let denom = CONTRACT_INFO.load(deps.storage)?.denom;

    // done here & in the update_listing_price method. Fixes issue with tax rates if price is too low
    if &msg.list_price < &Uint128::from(1_000_000u128) {
        return Err(ContractError::ListingPriceTooLow {});
    }

    // Queries the CW721 contract & gets the token_uri. If no token_uri, uses "".to_string()
    let cw721_query_msg = WasmQuery::Smart { 
        contract_addr: info.sender.to_string(), 
        msg: to_binary(&Cw721QueryMsg::NftInfo { token_id: rcv_msg.token_id.clone() })?
    };
    let cw721_res: NftInfoResponse<()> = deps.querier.query(&cw721_query_msg.into())?;
    let token_uri = cw721_res.token_uri.or_else(|| Some("".to_string())).unwrap();

    let off = Offering {
        contract_addr: info.sender.clone(),
        list_denom: denom,
        token_id: rcv_msg.token_id,
        token_uri: token_uri,
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

pub fn withdraw_offering(
    deps: DepsMut,
    info: MessageInfo,
    offering_id: String,
) -> Result<Response, ContractError> {
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
    Err(ContractError::Unauthorized {
        msg: "You are not the seller of this token, so you can not withdraw it.".to_string(),
    })
}

pub fn update_listing_price(
    deps: DepsMut,
    info: MessageInfo,
    offering_id: String,
    new_price: Uint128,
) -> Result<Response, ContractError> {
    // check if offering_id exist & they are the seller of it
    let off = OFFERINGS.load(deps.storage, &offering_id)?;
    if off.seller != info.sender {
        // println!("{}, {}", off.seller, info.sender);
        return Err(ContractError::Unauthorized {
            msg: "You are not the seller of this token, so you can not update its price."
                .to_string(),
        });
    }

    let old_price = off.list_price;

    if new_price < Uint128::from(1_000_000u128) {
        return Err(ContractError::ListingPriceTooLow {});
    }

    // update offering
    let updated_offering = Offering {
        contract_addr: off.contract_addr,
        list_denom: off.list_denom,
        token_id: off.token_id,
        token_uri: off.token_uri,
        seller: off.seller,
        list_price: new_price,
    };

    OFFERINGS.save(deps.storage, &offering_id, &updated_offering)?;

    Ok(Response::new()
        .add_attribute("action", "update_listing_price")
        .add_attribute("old_price", old_price.to_string())
        .add_attribute("new_price", new_price.to_string()))
}

pub fn update_fee_receiver_address(
    deps: DepsMut,
    info: MessageInfo,
    new_address: String,
) -> Result<Response, ContractError> {
    check_executer_is_authorized_fee_receiver(deps.as_ref(), info.sender.to_string())?;

    // update the contract fee in memory
    let mut contract_info = CONTRACT_INFO.load(deps.storage)?;
    contract_info.fee_receive_address = new_address.clone();

    // save to state
    CONTRACT_INFO.save(deps.storage, &contract_info)?;

    Ok(Response::new()
        .add_attribute("action", "update_fee_receiver_address")
        .add_attribute("new_address", new_address)
        // since you have to run this as the fee receiver, you can use your own address as the old address, this is the old address
        .add_attribute("old_address", info.sender))
}

pub fn update_platform_fee(
    deps: DepsMut,
    info: MessageInfo,
    new_fee: u128,
) -> Result<Response, ContractError> {
    check_executer_is_authorized_fee_receiver(deps.as_ref(), info.sender.to_string())?;

    let mut contract_info = CONTRACT_INFO.load(deps.storage)?;
    let current_platform_fee = contract_info.platform_fee;

    if new_fee > 100 {
        return Err(ContractError::PlatformFeeToHigh {});
    }

    contract_info.platform_fee = new_fee;
    CONTRACT_INFO.save(deps.storage, &contract_info)?;

    Ok(Response::new()
        .add_attribute("action", "update_fee_receiver_address")
        .add_attribute("new_fee", new_fee.to_string())
        .add_attribute("old_fee", current_platform_fee.to_string()))
}

pub fn force_withdraw_all(deps: DepsMut, info: MessageInfo) -> Result<Response, ContractError> {
    check_executer_is_authorized_fee_receiver(deps.as_ref(), info.sender.to_string())?;

    // get all offerings, loop through them.
    let offerings = queries::query_offerings(deps.as_ref(), None)?;

    let mut sub_messages_vector: Vec<SubMsg> = vec![];

    for offering in offerings {
        // transfer token back to original owner
        let transfer_cw721_msg = Cw721ExecuteMsg::TransferNft {
            recipient: offering.seller.clone().into_string(),
            token_id: offering.token_id.clone(),
        };

        let exec_cw721_transfer = WasmMsg::Execute {
            contract_addr: offering.contract_addr.into_string(),
            msg: to_binary(&transfer_cw721_msg)?,
            funds: vec![],
        };

        let cw721_transfer_cosmos_msg: CosmosMsg = exec_cw721_transfer.into();

        // remove offering from the OFFERINGS? is this needed or done for them
        OFFERINGS.remove(deps.storage, &offering.offering_id);
        sub_messages_vector.push(SubMsg::new(cw721_transfer_cosmos_msg));
    }

    Ok(Response::new()
        .add_attribute("action", "force_withdraw_all")
        .add_submessages(sub_messages_vector))
}

fn check_executer_is_authorized_fee_receiver(
    deps: Deps,
    executer_address: String,
) -> Result<(), ContractError> {
    let contract_info = CONTRACT_INFO.load(deps.storage)?;
    if executer_address != contract_info.fee_receive_address {
        return Err(ContractError::Unauthorized {
            msg: "You are not the current fee_receiver".to_string(),
        });
    }
    Ok(())
}
