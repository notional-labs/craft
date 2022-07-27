use cosmwasm_std::StdError;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum ContractError {
    #[error("{0}")]
    Std(#[from] StdError),

    #[error("No data in ReceiveMsg")]
    NoData {},

    #[error("Unauthorized. Reason: {msg:?}")]
    Unauthorized {msg: String},
    
    #[error("Insufficient funds. Needed: {needed:?}, Received: {received:?}")]
    InsufficientFundsSend {needed: String, received: String},

    #[error("The ID {id} is not valid. Make sure to check getOfferings{}")]
    NoMarketplaceOfferingWithGivenID {id: String},
    
    // This may be removed in favor of just withdrawing the NFT back to themselves.
    #[error("Trying to purchase your own item")]
    UnableToPurchaseMarketplaceItemYouSold {},

    #[error("You can't set the platform fee >100 (>100%)")]
    PlatformFeeToHigh {},
}
