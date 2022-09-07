use std::env::current_dir;
use std::fs::create_dir_all;

use cosmwasm_schema::{export_schema, remove_schemas, schema_for};

use craft_marketplace::msg::{ContractInformationResponse, OfferingsResponse, QueryOfferingsResult};
use craft_marketplace::msg::{ExecuteMsg, InitMsg, QueryMsg, SellNft};

fn main() {
    let mut out_dir = current_dir().unwrap();
    out_dir.push("schema");
    create_dir_all(&out_dir).unwrap();
    remove_schemas(&out_dir).unwrap();

    export_schema(&schema_for!(InitMsg), &out_dir);
    export_schema(&schema_for!(ExecuteMsg), &out_dir);
    export_schema(&schema_for!(QueryMsg), &out_dir);
    export_schema(&schema_for!(SellNft), &out_dir);
    export_schema(&schema_for!(OfferingsResponse), &out_dir);
    export_schema(&schema_for!(ContractInformationResponse), &out_dir);
    export_schema(&schema_for!(QueryOfferingsResult), &out_dir);
}
