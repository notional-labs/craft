use crate::msg::{ContractInfoResponse};
use crate::state::{CONTRACT_INFO};
use cosmwasm_std::{to_binary, Binary, Deps, DepsMut, Env, MessageInfo, Response,StdResult};

use crate::execute;

use cosmwasm_std::entry_point;
use crate::error::ContractError;
use crate::msg::{HandleMsg, InitMsg, QueryMsg};

use crate::queries;

// Note, you can use StdResult in some functions where you do not
// make use of the custom errors
#[entry_point]
pub fn instantiate(deps: DepsMut, _env: Env, _info: MessageInfo, msg: InitMsg) -> StdResult<Response> {
    // just ensures this is a valid DAO address
    deps.api.addr_validate(&msg.fee_receive_address)?;

    // TODO: Add contract admin?

    let info = ContractInfoResponse { 
        name: msg.name,
        denom: msg.denom,
        fee_receive_address: msg.fee_receive_address,
        platform_fee: msg.platform_fee,
    };

    CONTRACT_INFO.save(deps.storage, &info)?;
    Ok(Response::default())
}

#[entry_point]
pub fn execute(deps: DepsMut, _env: Env, info: MessageInfo, msg: HandleMsg) -> Result<Response, ContractError> {
    match msg {
        HandleMsg::WithdrawNft { offering_id } => execute::withdraw_offering(deps, info, offering_id),
        HandleMsg::BuyNft { offering_id } => execute::buy_nft(deps, info, offering_id),
        HandleMsg::ReceiveNft(msg) => execute::receive_nft(deps, info, msg),

        HandleMsg::UpdateFeeReceiverAddress { new_address } => execute::update_fee_receiver_address(deps, info, new_address),
    }
}


#[entry_point]
pub fn query(deps: Deps, _env: Env, msg: QueryMsg) -> StdResult<Binary> {
    match msg {        
        QueryMsg::GetOfferings {} => to_binary(&queries::query_offerings(deps)?),
        // QueryMsg::GetPlatformFee {} => to_binary(&queries::query_platform_fee(deps)?),
        // QueryMsg::GetDenom {} => to_binary(&queries::query_denom(deps)?),     
        // QueryMsg::GetDaoAddress {} => to_binary(&queries::query_dao_address(deps)?),     
        QueryMsg::GetContractInfo {} => to_binary(&queries::query_contract_info(deps)?),
    }
}