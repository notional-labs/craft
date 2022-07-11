use crate::package::{ContractInfoResponse, OfferingsResponse, QueryOfferingsResult};
use crate::state::{increment_offerings, Offering, CONTRACT_INFO, OFFERINGS};
use cosmwasm_std::{
    from_binary, to_binary, Binary, CosmosMsg, Deps, DepsMut, Env, MessageInfo, Order, Response,
    StdResult, SubMsg, WasmMsg, Coin
};

// https://github.com/InterWasm/cw-contracts/blob/main/contracts/nameservice/src/contract.rs

use cosmwasm_std::BankMsg;
use crate::coin_helpers::assert_sent_sufficient_coin;
use cw721::{Cw721ExecuteMsg, Cw721ReceiveMsg};
use cosmwasm_std::entry_point;
use crate::error::ContractError;
use crate::msg::{HandleMsg, InitMsg, QueryMsg, SellNft}; // BuyNft

// Note, you can use StdResult in some functions where you do not
// make use of the custom errors
#[entry_point]
pub fn instantiate(
    deps: DepsMut,
    _env: Env,
    _info: MessageInfo,
    msg: InitMsg,
) -> StdResult<Response> {
    let info = ContractInfoResponse { 
        name: msg.name,
        denom: msg.denom
    };
    CONTRACT_INFO.save(deps.storage, &info)?;
    Ok(Response::default())
}

#[entry_point]
pub fn execute(
    deps: DepsMut,
    _env: Env,
    info: MessageInfo,
    msg: HandleMsg,
) -> Result<Response, ContractError> {
    match msg {
        HandleMsg::WithdrawNft { offering_id } => try_withdraw(deps, info, offering_id),
        HandleMsg::Receive { offering_id } => try_buy_nft(deps, info, offering_id),
        HandleMsg::ReceiveNft(msg) => try_receive_nft(deps, info, msg),
    }
}

// ============================== Message Handlers ==============================

// receive funds & buy NFT if funds are enough
pub fn try_buy_nft(
    deps: DepsMut,
    info: MessageInfo,
    offering_id: String,
) -> Result<Response, ContractError> {
    // let msg: BuyNft = from_binary(&rcv_msg.msg)?;

    // check if offering exists
    // let off = OFFERINGS.load(deps.storage, &offering_id)?;

    // load offering from storage if a given offering_id exist, if not, return NoMarketplaceOfferingWithGivenID
    let off = OFFERINGS.may_load(deps.storage, &offering_id)?;
    if off.is_none() {
        return Err(ContractError::NoMarketplaceOfferingWithGivenID { id: offering_id });
    } 

    let off = off.unwrap();

    // Check if the seller of the NFT is the sender
    if off.seller == info.sender {
        // let err = try_withdraw(deps, info, offering_id).unwrap();
        // return Ok(err);
        return Err(ContractError::UnableToPurchaseMarketplaceItemYouSold {});
    }

    let denom = CONTRACT_INFO.load(deps.storage)?.denom;

    // check for enough coins (>= the listing price with the same denom)
    // change this to match like coin_helper has?
    assert_sent_sufficient_coin(&info.funds, Some(Coin::new(off.list_price.clone().u128(), &denom)))?;

    // convert off.list_price to a vector of coins
    let list_price_coins = vec![Coin::new(off.list_price.clone().u128(), &denom)];


    // send the ucraft -> the off.seller using BankMsg
    let transfer_tokens = BankMsg::Send {
        to_address: off.seller.to_string(),
        amount: list_price_coins,
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

    // if everything is fine transfer ucraft to seller
    let transfer_cosmos_msg: CosmosMsg = transfer_tokens.into();

    // transfer nft to buyer
    let cw721_transfer_cosmos_msg: CosmosMsg = exec_cw721_transfer.into();

    let ucraft_submsg = SubMsg::new(transfer_cosmos_msg);
    let cw721_submsg = SubMsg::new(cw721_transfer_cosmos_msg);

    let cosmos_msgs = vec![ucraft_submsg, cw721_submsg];

    //delete offering
    OFFERINGS.remove(deps.storage, &offering_id);

    let price_string = format!("{} {}", off.list_price.clone().u128(), info.sender);

    Ok(Response::new()
        .add_attribute("action", "buy_nft")
        .add_attribute("buyer", info.sender.to_string())
        .add_attribute("seller", off.seller)
        .add_attribute("paid_price", price_string)
        .add_attribute("token_id", off.token_id)
        .add_attribute("contract_addr", off.contract_addr)
        .add_submessages(cosmos_msgs))
}

pub fn try_receive_nft(
    deps: DepsMut,
    info: MessageInfo,
    rcv_msg: Cw721ReceiveMsg,
) -> Result<Response, ContractError> {
    let msg: SellNft = from_binary(&rcv_msg.msg)?;

    // check if same token Id form same original contract is already on sale
    // get OFFERING_COUNT
    let id = increment_offerings(deps.storage)?.to_string();

    // save Offering
    let off = Offering {
        contract_addr: info.sender.clone(),
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

pub fn try_withdraw(
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
    Err(ContractError::Unauthorized {})
}

#[entry_point]
pub fn query(deps: Deps, _env: Env, msg: QueryMsg) -> StdResult<Binary> {
    match msg {
        QueryMsg::GetOfferings {} => to_binary(&query_offerings(deps)?),
    }
}

// ============================== Query Handlers ==============================
fn query_offerings(deps: Deps) -> StdResult<OfferingsResponse> {
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
    // item.and_then(|(k, offering)| {
    //     Ok(QueryOfferingsResult {
    //         id: k,
    //         token_id: offering.token_id,
    //         list_price: offering.list_price,
    //         contract_addr: offering.contract_addr,
    //         seller: offering.seller,
    //     })
    // })
    item.map(|(k, offering)| {
        QueryOfferingsResult {
            id: k,
            token_id: offering.token_id,
            list_price: offering.list_price,
            contract_addr: offering.contract_addr,
            seller: offering.seller,
        }
    })
}
// ============================== Test ==============================

#[cfg(test)]
mod tests {
    use super::*;
    use cosmwasm_std::testing::{mock_dependencies, mock_env, mock_info};
    use cosmwasm_std::{coins, from_binary, Uint128};

    #[test]
    fn test_sell_offering() {
        let mut deps = mock_dependencies();

        let msg = InitMsg {
            name: String::from("test market"),
            denom: String::from("ucraft"),
        };

        let denom = msg.denom.clone();

        let info = mock_info("creator", &coins(2, &denom));
        let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

        // beneficiary can release it
        let info = mock_info("anyone", &coins(2, &denom));

        let sell_msg = SellNft {
            list_price: Uint128::new(2),
        };

        let msg = HandleMsg::ReceiveNft(Cw721ReceiveMsg {
            sender: String::from("seller"),
            token_id: String::from("SellableNFT"),
            msg: to_binary(&sell_msg).unwrap(),
        });
        let _res = execute(deps.as_mut(), mock_env(), info, msg).unwrap();

        // Offering should be listed
        let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
        let value: OfferingsResponse = from_binary(&res).unwrap();
        assert_eq!(1, value.offerings.len());

        // Purchase the NFT from the store
        let msg2 = HandleMsg::Receive { offering_id: value.offerings[0].id.to_string() };
        let info_buy = mock_info("addr1", &coins(2, &denom));
        let _res = execute(deps.as_mut(), mock_env(), info_buy, msg2);

        // panic!("{}", _res.unwrap_err());

        // check offerings again. Should be 0 since the NFT is bought
        let res2 = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
        let value2: OfferingsResponse = from_binary(&res2).unwrap();
        assert_eq!(0, value2.offerings.len()); 
    }

    #[test]
    fn test_withdraw_offering() {
        let mut deps = mock_dependencies();

        let msg = InitMsg {
            name: String::from("test market"),
            denom: String::from("token"),
        };
        let denom = msg.denom.clone();

        let info = mock_info("creator", &coins(2, &denom));
        let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

        // beneficiary can release it
        let info = mock_info("anyone", &coins(2, &denom));
        
        let sell_msg = SellNft {
            list_price: Uint128::new(5),
        };

        let msg = HandleMsg::ReceiveNft(Cw721ReceiveMsg {
            sender: String::from("seller"),
            token_id: String::from("SellableNFT"),
            msg: to_binary(&sell_msg).unwrap(),
        });
        let _res = execute(deps.as_mut(), mock_env(), info, msg).unwrap();

        // Offering should be listed
        let res = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
        let value: OfferingsResponse = from_binary(&res).unwrap();
        assert_eq!(1, value.offerings.len());

        // withdraw offering
        let withdraw_info = mock_info("seller", &coins(2, &denom));
        let withdraw_msg = HandleMsg::WithdrawNft {
            offering_id: value.offerings[0].id.clone(),
        };
        let _res = execute(deps.as_mut(), mock_env(), withdraw_info, withdraw_msg).unwrap();

        // Offering should be removed
        let res2 = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
        let value2: OfferingsResponse = from_binary(&res2).unwrap();
        assert_eq!(0, value2.offerings.len());
    }
}