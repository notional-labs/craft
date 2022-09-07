use crate::msg::{MigrateMsg, ContractInformationResponse};
use crate::state::{RECENTLY_SOLD, Offering, CONTRACT_INFORMATION};
use cosmwasm_std::{
    to_binary, Binary, Deps, DepsMut, Env, MessageInfo, Response, StdResult,
};

use cw2::set_contract_version;

use crate::execute;

use crate::error::ContractError;
use crate::msg::{ExecuteMsg, InitMsg, QueryMsg};
use crate::queries;
use cosmwasm_std::entry_point;

const CONTRACT_NAME: &str = "crates.io:craft-marketplace";
const CONTRACT_VERSION: &str = env!("CARGO_PKG_VERSION"); // Config.toml -> [package] -> version

// Note, you can use StdResult in some functions where you do not
// make use of the custom errors
#[entry_point]
pub fn instantiate(
    deps: DepsMut,
    _env: Env,
    _info: MessageInfo,
    msg: InitMsg,
) -> StdResult<Response> {
    // just ensures this is a valid DAO address
    deps.api.addr_validate(&msg.fee_receive_address)?;

    set_contract_version(deps.storage, CONTRACT_NAME, CONTRACT_VERSION)?;    

    let info = ContractInformationResponse {
        name: msg.name,
        denom: msg.denom,
        fee_receive_address: msg.fee_receive_address, // contract admin as well
        platform_fee: msg.platform_fee,
        version: CONTRACT_VERSION.to_string(),
        is_selling_allowed: true,
        contact: "reece@crafteconomy.io".to_string(),
    };    

    let sold: Vec<Offering> = vec![];
    RECENTLY_SOLD.save(deps.storage, &sold)?;    

    CONTRACT_INFORMATION.save(deps.storage, &info)?;
    Ok(Response::new().add_attribute("action", "instantiate"))
}

#[entry_point]
pub fn execute(
    deps: DepsMut,
    _env: Env,
    info: MessageInfo,
    msg: ExecuteMsg,
) -> Result<Response, ContractError> {
    match msg {
        ExecuteMsg::WithdrawNft { offering_id } => {
            execute::withdraw_offering(deps, info, offering_id)
        }
        ExecuteMsg::BuyNft { offering_id } => execute::buy_nft(deps, info, offering_id),
        ExecuteMsg::ReceiveNft(msg) => execute::receive_nft(deps, info, msg),

        ExecuteMsg::UpdateListingPrice {
            offering_id,
            new_price,
        } => execute::update_listing_price(deps, info, offering_id, new_price),


        ExecuteMsg::UpdateFeeReceiverAddress { new_address } => {
            execute::update_fee_receiver_address(deps, info, new_address)
        }
        ExecuteMsg::UpdatePlatformFee { new_fee } => {
            execute::update_platform_fee(deps, info, new_fee)
        }
        ExecuteMsg::ForceWithdrawAll {} => execute::force_withdraw_all(deps, info),

        ExecuteMsg::ToggleAbilityToSell { status } => execute::toggle_selling_status(deps, info, status),
    }
}

#[entry_point]
pub fn query(deps: Deps, _env: Env, msg: QueryMsg) -> StdResult<Binary> {
    match msg {
        QueryMsg::GetOfferings { filter_seller } => to_binary(&queries::query_offerings(deps, filter_seller)?),

        QueryMsg::GetContractInfo {} => to_binary(&queries::query_contract_info(deps)?),

        // deprecated by GetCollectionData
        // QueryMsg::GetCollectionVolume { address } => {
        //     to_binary(&queries::query_collection_volume(deps, &address)?)
        // }

        // also gets volume data, maybe just merge the 2?
        QueryMsg::GetCollectionData { address } => {
            to_binary(&queries::query_collection_data(deps, &address)?)
        }

        QueryMsg::GetRecentlySold { } => {
            to_binary(&queries::query_recently_sold(deps)?)
        }
    }
}

#[entry_point]
pub fn migrate(deps: DepsMut, _env: Env, _msg: MigrateMsg) -> Result<Response, ContractError> {
    // https://docs.cosmwasm.com/docs/1.0/smart-contracts/migration/
    // let ver = cw2::get_contract_version(deps.storage)?;
    // // ensure we are migrating from an allowed contract
    // if ver.contract != CONTRACT_NAME {
    //     return Err(StdError::generic_err("Can only upgrade from same type").into());
    // }
    // // note: better to do proper semver compare, but string compare *usually* works
    // if ver.version >= (*CONTRACT_VERSION).to_string() {
    //     return Err(StdError::generic_err("Cannot upgrade from a newer version").into());
    // }

    // set the new version
    cw2::set_contract_version(deps.storage, CONTRACT_NAME, CONTRACT_VERSION)?;

    // do any desired state migrations here...
    CONTRACT_INFORMATION.update(deps.storage, 
        |mut config: ContractInformationResponse| -> StdResult<_> {
            config.version = CONTRACT_VERSION.to_string();
            // config.is_selling_allowed = true;
            Ok(config)
        }
    )?;    

    // TODO: update version of the contract config

    Ok(Response::default()
        .add_attribute("action", "migration")
        .add_attribute("version", CONTRACT_VERSION)
        .add_attribute("contract", CONTRACT_NAME))    
}
