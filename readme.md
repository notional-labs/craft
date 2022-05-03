# craft
**craft** is a DAO that operates minecraft server(s).


## How it work:

This chain is a DAO.

* Non transferrable staking token called `exp`
* Monetary token called craft for the purchase of in-game items
* Flutter mobile app for smooth signing


## Basic Dev Roadmap

* ~~Testnet one makes sure that we got the vesting right and we didn't make a horrible mistake choosing SDK 0.46.0 and tm 0.35.1~~
  * ~~ibc-go v3.0.0~~
  * ~~cosmos-sdk v0.46.0~~
  * ~~x/wasm with support for ibc v3.0.0 and interchain accounts~~


Craft economy is functionally complete, and features:

* group module daos
* native cosmos NFTs
* ibc v3 with interchain accounts
* a novel dao construction that governs the chain 
* integration with minecraft


* More feature development occurs while tn one is running.  We will use tn one to provide feedback and insights to the sdk team.  Both Seahorse and An1 are looking into these versions, too.
* When feature-complete (implmentation of mint-burn-redeem economy controlled by dao) there will be a second testnet & validator slashing gov prop
  * The second testnet will be used to look into the minecraft integrations and overall product quality
* Testnet3, is hopefully a brief affair that leads directly into mainnet, with a feature freeze starting at the launch of tn2.  

## EXP Governance
 - Tax/Revenue paid directly to a wallet (automation with modulewallet in future)
 - Taxes are "optional" in game (If not paid in time, lose that item/asset you were paying tax on).

 - Dao needs to mint exp with a gov tx. incuding when someone wants to exit dao @ NAV.
 - Every Validator gets 1EXP.
 - 28 day unbonding time (limits DAO transactions)
 - 1 year vesting schedule for 30 holders.  
   - Vested account can be staked, but can’t do anything else (auth module, vesting - capabilities are limited due to vesting)
   - Instead of new vesting type, add burn features later (Cabcon gov proposal to add software to allow EXP to be burned. Gives time)
 - People request mint from gov proposal
 - DAO then votes to apporve/reject mint
 - If dao wants to sell exp, we want to make sure they have to vest 1yr+


 Technical:
 - Governance needs to wrap mint module (where coins come from). Mint - interacts with bank & auth.
 The weapon - allows validators to be slashed by governance proposals. Makes gov wrap slashing module. Now proposal type which allows activating slashing module:  https://github.com/cosmos/cosmos-sdk/pull/11024/files/2c70217489539bf6552356ef3a3978b7c14377f5

 - We need this custom gov proposal type (wraps bank mint & auth) to mint/burn exp from DAO
  



