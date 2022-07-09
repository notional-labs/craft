use crate::package::{ContractInfoResponse, OfferingsResponse, QueryOfferingsResult};
use crate::state::{increment_offerings, Offering, CONTRACT_INFO, OFFERINGS};
use cosmwasm_std::{
    from_binary, to_binary, Binary, CosmosMsg, Deps, DepsMut, Env, MessageInfo, Order, Response,
    StdResult, SubMsg, WasmMsg
};
use cw20::{Cw20ExecuteMsg, Cw20ReceiveMsg};
use cw721::{Cw721ExecuteMsg, Cw721ReceiveMsg};
use cosmwasm_std::entry_point;
use crate::error::ContractError;
use crate::msg::{BuyNft, HandleMsg, InitMsg, QueryMsg, SellNft};

// Note, you can use StdResult in some functions where you do not
// make use of the custom errors
#[entry_point]
pub fn instantiate(
    deps: DepsMut,
    _env: Env,
    _info: MessageInfo,
    msg: InitMsg,
) -> StdResult<Response> {
    let info = ContractInfoResponse { name: msg.name };
    CONTRACT_INFO.save(deps.storage, &info)?;
    Ok(Response::default())
}

// And declare a custom Error variant for the ones where you will want to make use of it
#[entry_point]
pub fn execute(
    deps: DepsMut,
    _env: Env,
    info: MessageInfo,
    msg: HandleMsg,
) -> Result<Response, ContractError> {
    match msg {
        HandleMsg::WithdrawNft { offering_id } => try_withdraw(deps, info, offering_id),
        HandleMsg::Receive(msg) => try_receive(deps, info, msg),
        HandleMsg::ReceiveNft(msg) => try_receive_nft(deps, info, msg),
    }
}

// ============================== Message Handlers ==============================

pub fn try_receive(
    deps: DepsMut,
    info: MessageInfo,
    rcv_msg: Cw20ReceiveMsg,
) -> Result<Response, ContractError> {
    let msg: BuyNft = from_binary(&rcv_msg.msg)?;

    // check if offering exists
    let off = OFFERINGS.load(deps.storage, &msg.offering_id)?;

    // check for enough coins
    if rcv_msg.amount < off.list_price.amount {
        return Err(ContractError::InsufficientFunds {});
    }

    // create transfer cw20 msg
    let transfer_cw20_msg = Cw20ExecuteMsg::Transfer {
        recipient: off.seller.clone().into_string(),
        amount: rcv_msg.amount,
    };
    let exec_cw20_transfer = WasmMsg::Execute {
        contract_addr: info.sender.clone().into_string(),
        msg: to_binary(&transfer_cw20_msg)?,
        funds: vec![],
    };

    // create transfer cw721 msg
    let transfer_cw721_msg = Cw721ExecuteMsg::TransferNft {
        recipient: rcv_msg.sender.clone(),
        token_id: off.token_id.clone(),
    };
    let exec_cw721_transfer = WasmMsg::Execute {
        contract_addr: off.contract_addr.clone().into_string(),
        msg: to_binary(&transfer_cw721_msg)?,
        funds: vec![],
    };

    // if everything is fine transfer cw20 to seller
    let cw20_transfer_cosmos_msg: CosmosMsg = exec_cw20_transfer.into();
    // transfer nft to buyer
    let cw721_transfer_cosmos_msg: CosmosMsg = exec_cw721_transfer.into();

    let cw20_submsg = SubMsg::new(cw20_transfer_cosmos_msg);
    let cw721_submsg = SubMsg::new(cw721_transfer_cosmos_msg);

    let cosmos_msgs = vec![cw20_submsg, cw721_submsg];

    //delete offering
    OFFERINGS.remove(deps.storage, &msg.offering_id);

    let price_string = format!("{} {}", rcv_msg.amount, info.sender);

    Ok(Response::new()
        .add_attribute("action", "buy_nft")
        .add_attribute("buyer", rcv_msg.sender)
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
        list_price: msg.list_price.clone(),
    };

    OFFERINGS.save(deps.storage, &id, &off)?;

    let price_string = format!("{} {}", msg.list_price.amount, msg.list_price.address);

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
    if off.seller == info.sender.clone() {
        // transfer token back to original owner
        let transfer_cw721_msg = Cw721ExecuteMsg::TransferNft {
            recipient: off.seller.clone().into_string(),
            token_id: off.token_id.clone(),
        };

        let exec_cw721_transfer = WasmMsg::Execute {
            contract_addr: off.contract_addr.clone().into_string(),
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
        .map(|kv_item| parse_offering(kv_item))
        .collect();
    Ok(OfferingsResponse {
        offerings: res?, // Placeholder
    })
}

fn parse_offering(item: StdResult<(String, Offering)>) -> StdResult<QueryOfferingsResult> {
    item.and_then(|(k, offering)| {
        Ok(QueryOfferingsResult {
            id: k.to_string(),
            token_id: offering.token_id,
            list_price: offering.list_price,
            contract_addr: offering.contract_addr.clone(),
            seller: offering.seller.clone(),
        })
    })
}
// ============================== Test ==============================

#[cfg(test)]
mod tests {
    use super::*;
    use cosmwasm_std::testing::{mock_dependencies, mock_env, mock_info};
    use cosmwasm_std::{coins, from_binary, Uint128};
    use cw20::Cw20Coin;

    #[test]
    fn sell_offering_happy_path() {
        let mut deps = mock_dependencies();

        let msg = InitMsg {
            name: String::from("test market"),
        };
        let info = mock_info("creator", &coins(2, "token"));
        let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

        // beneficiary can release it
        let info = mock_info("anyone", &coins(2, "token"));

        let sell_msg = SellNft {
            list_price: Cw20Coin {
                address: String::from("cw20ContractAddr"),
                amount: Uint128::new(5),
            },
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

        let buy_msg = BuyNft {
            offering_id: value.offerings[0].id.clone(),
        };

        let msg2 = HandleMsg::Receive(Cw20ReceiveMsg {
            sender: String::from("buyer"),
            amount: Uint128::new(5),
            msg: to_binary(&buy_msg).unwrap(),
        });

        let info_buy = mock_info("cw20ContractAddr", &coins(2, "token"));

        let _res = execute(deps.as_mut(), mock_env(), info_buy, msg2).unwrap();

        // check offerings again. Should be 0
        let res2 = query(deps.as_ref(), mock_env(), QueryMsg::GetOfferings {}).unwrap();
        let value2: OfferingsResponse = from_binary(&res2).unwrap();
        assert_eq!(0, value2.offerings.len());
    }

    #[test]
    fn withdraw_offering_happy_path() {
        let mut deps = mock_dependencies();

        let msg = InitMsg {
            name: String::from("test market"),
        };
        let info = mock_info("creator", &coins(2, "token"));
        let _res = instantiate(deps.as_mut(), mock_env(), info, msg).unwrap();

        // beneficiary can release it
        let info = mock_info("anyone", &coins(2, "token"));

        let sell_msg = SellNft {
            list_price: Cw20Coin {
                address: String::from("cw20ContractAddr"),
                amount: Uint128::new(5),
            },
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
        let withdraw_info = mock_info("seller", &coins(2, "token"));
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