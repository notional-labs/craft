use crate::error::ContractError;
use cosmwasm_std::Coin;

// Modified:
// https://github.com/InterWasm/cw-contracts/blob/main/contracts/nameservice/src/contract.rs
// just instead of
// coin.amount.u128() >= required_amount
// it is `coin.amount.u128() == required_amount`
pub fn assert_sent_exact_coin(
    sent: &[Coin],
    required: Option<Coin>,
) -> Result<(), ContractError> {
    if let Some(required_coin) = required {
        let required_amount = required_coin.amount.u128();
        if required_amount > 0 {
            let mut received_amount = 0;
            let sent_sufficient_funds = sent.iter().any(|coin| {
                // check if a given sent coin matches denom
                // and has sufficient amount
                received_amount = coin.amount.u128();
                coin.denom == required_coin.denom && coin.amount.u128() == required_amount
            });

            if sent_sufficient_funds {
                return Ok(());
            } else {
                return Err(ContractError::InsufficientFundsSend { 
                    needed: required_amount.to_string(), 
                    received: received_amount.to_string()
                });                
            }
        }
    }
    Ok(())
}