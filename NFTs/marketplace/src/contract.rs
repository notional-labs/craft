use crate::msg::{ContractInfoResponse, MigrateMsg};
use crate::state::{CONTRACT_INFO};
use cosmwasm_std::{to_binary, Binary, Deps, DepsMut, Env, MessageInfo, Response,StdResult, StdError};

use cw2::set_contract_version;

use crate::execute;

use cosmwasm_std::entry_point;
use crate::error::ContractError;
use crate::msg::{HandleMsg, InitMsg, QueryMsg};
use crate::queries;

const CONTRACT_NAME: &str = "crates.io:craft-marketplace";
const CONTRACT_VERSION: &str = env!("CARGO_PKG_VERSION"); // Config.toml -> [package] -> version

// Note, you can use StdResult in some functions where you do not
// make use of the custom errors
#[entry_point]
pub fn instantiate(deps: DepsMut, _env: Env, _info: MessageInfo, msg: InitMsg) -> StdResult<Response> {
    // just ensures this is a valid DAO address
    deps.api.addr_validate(&msg.fee_receive_address)?;

    set_contract_version(deps.storage, CONTRACT_NAME, CONTRACT_VERSION)?;

    // TODO: Add contract admin?

    let info = ContractInfoResponse { 
        name: msg.name,
        denom: msg.denom,
        fee_receive_address: msg.fee_receive_address,
        platform_fee: msg.platform_fee,
    };

    CONTRACT_INFO.save(deps.storage, &info)?;
    Ok(Response::new().add_attribute("action", "instantiate"))
}

#[entry_point]
pub fn execute(deps: DepsMut, _env: Env, info: MessageInfo, msg: HandleMsg) -> Result<Response, ContractError> {
    match msg {
        HandleMsg::WithdrawNft { offering_id } => execute::withdraw_offering(deps, info, offering_id),
        HandleMsg::BuyNft { offering_id } => execute::buy_nft(deps, info, offering_id),
        HandleMsg::ReceiveNft(msg) => execute::receive_nft(deps, info, msg),

        HandleMsg::UpdateListingPrice { offering_id, new_price } => execute::update_listing_price(deps, info, offering_id, new_price),

        HandleMsg::UpdateFeeReceiverAddress { new_address } => execute::update_fee_receiver_address(deps, info, new_address),
        HandleMsg::UpdatePlatformFee { new_fee } => execute::update_platform_fee(deps, info, new_fee),
        HandleMsg::ForceWithdrawAll {} => execute::force_withdraw_all(deps, info),
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


#[entry_point]
pub fn migrate(deps: DepsMut, _env: Env, _msg: MigrateMsg) -> Result<Response, ContractError> {
    // https://docs.cosmwasm.com/docs/1.0/smart-contracts/migration/
    
    let ver = cw2::get_contract_version(deps.storage)?;
    // ensure we are migrating from an allowed contract
    if ver.contract != CONTRACT_NAME {
        return Err(StdError::generic_err("Can only upgrade from same type").into());
    }
    // note: better to do proper semver compare, but string compare *usually* works
    if ver.version >= (*CONTRACT_VERSION).to_string() {
        return Err(StdError::generic_err("Cannot upgrade from a newer version").into());
    }

    // set the new version
    cw2::set_contract_version(deps.storage, CONTRACT_NAME, CONTRACT_VERSION)?;

    // do any desired state migrations...
    Ok(Response::default())
}