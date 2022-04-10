const {
    SigningCosmosClient
} = require("@cosmjs/launchpad");
import {
    DirectSecp256k1HdWallet
} from '@cosmjs/proto-signing'

import {
    assertIsBroadcastTxSuccess,
    SigningStargateClient,
} from '@cosmjs/stargate'

// do not use Nodejs 17, nvm use 16.x.x. nvm run 16.13.1
const request = require('request');

// npm install webpack-dev-server -g

const name = "Craft Testnet v4"
const CHAIN_ID = "craft-v4";
const RPC_ENDPOINT = "http://65.108.125.182:26657/";
const REST_ENDPOINT = "http://65.108.125.182:1317/";

window.onload = async () => {
    // Keplr extension injects the offline signer that is compatible with cosmJS.
    // You can get this offline signer from `window.getOfflineSigner(chainId:string)` after load event.
    // And it also injects the helper function to `window.keplr`.
    // If `window.getOfflineSigner` or `window.keplr` is null, Keplr extension may be not installed on browser.
    if (!window.getOfflineSigner || !window.keplr) {
        alert("Please install keplr extension");
    } else {
        if (window.keplr.experimentalSuggestChain) {
            try {
                await window.keplr.experimentalSuggestChain({
                    // Chain-id of the Osmosis chain.
                    chainId: CHAIN_ID,
                    // The name of the chain to be displayed to the user.
                    chainName: name,
                    // RPC endpoint of the chain. In this case we are using blockapsis, as it's accepts connections from any host currently. No Cors limitations.
                    rpc: RPC_ENDPOINT,
                    // REST endpoint of the chain.
                    rest: REST_ENDPOINT,
                    // Staking coin information
                    stakeCurrency: {
                        // Coin denomination to be displayed to the user.
                        coinDenom: "EXP",
                        // Actual denom (i.e. uatom, uscrt) used by the blockchain.
                        coinMinimalDenom: "uexp",
                        // # of decimal points to convert minimal denomination to user-facing denomination.
                        coinDecimals: 6,
                        // (Optional) Keplr can show the fiat value of the coin if a coingecko id is provided.
                        // You can get id from https://api.coingecko.com/api/v3/coins/list if it is listed.
                        // coinGeckoId: ""
                    },
                    bip44: {
                        // You can only set the coin type of BIP44.
                        // 'Purpose' is fixed to 44.
                        coinType: 118,
                    },
                    // Bech32 configuration to show the address to user.
                    // This field is the interface of
                    bech32Config: {
                        bech32PrefixAccAddr: "craft",
                        bech32PrefixAccPub: "craftpub",
                        bech32PrefixValAddr: "craftvaloper",
                        bech32PrefixValPub: "craftvaloperpub",
                        bech32PrefixConsAddr: "craftvalcons",
                        bech32PrefixConsPub: "craftvalconspub"
                    },
                    // List of all coin/tokens used in this chain.
                    currencies: [{
                        // Coin denomination to be displayed to the user.
                        coinDenom: "EXP",
                        // Actual denom (i.e. uatom, uscrt) used by the blockchain.
                        coinMinimalDenom: "uexp",
                        // # of decimal points to convert minimal denomination to user-facing denomination.
                        coinDecimals: 6,
                        // (Optional) Keplr can show the fiat value of the coin if a coingecko id is provided.
                        // You can get id from https://api.coingecko.com/api/v3/coins/list if it is listed.
                        // coinGeckoId: ""
                    },{
                        // Coin denomination to be displayed to the user.
                        coinDenom: "CRAFT",
                        // Actual denom (i.e. uatom, uscrt) used by the blockchain.
                        coinMinimalDenom: "ucraft",
                        // # of decimal points to convert minimal denomination to user-facing denomination.
                        coinDecimals: 6,
                        // (Optional) Keplr can show the fiat value of the coin if a coingecko id is provided.
                        // You can get id from https://api.coingecko.com/api/v3/coins/list if it is listed.
                        coinGeckoId: "dig-chain"
                    }],
                    // List of coin/tokens used as a fee token in this chain.
                    feeCurrencies: [{
                        // Coin denomination to be displayed to the user.
                        coinDenom: "CRAFT",
                        // Actual denom (i.e. uosmo, uscrt) used by the blockchain.
                        coinMinimalDenom: "ucraft",
                        // # of decimal points to convert minimal denomination to user-facing denomination.
                        coinDecimals: 6,
                        // (Optional) Keplr can show the fiat value of the coin if a coingecko id is provided.
                        // You can get id from https://api.coingecko.com/api/v3/coins/list if it is listed.
                        coinGeckoId: "dig-chain"
                    }],
                    coinType: 118,
                    // Make sure that the gas prices are higher than the minimum gas prices accepted by chain validators and RPC/REST endpoint.
                    gasPriceStep: {
                        low: 0.01,
                        average: 0.025,
                        high: 0.04
                    }
                });
            } catch {
                alert("Failed to suggest the chain");
            }
        } else {
            alert("Please use the recent version of keplr extension");
        }
    }

    // You should request Keplr to enable the wallet.
    // This method will ask the user whether or not to allow access if they haven't visited this website.
    // Also, it will request user to unlock the wallet if the wallet is locked.
    // If you don't request enabling before usage, there is no guarantee that other methods will work.
    await window.keplr.enable(CHAIN_ID);

    const offlineSigner = window.getOfflineSigner(CHAIN_ID);

    // You can get the address/public keys by `getAccounts` method.
    // It can return the array of address/public key.
    // But, currently, Keplr extension manages only one address/public key pair.
    // XXX: This line is needed to set the sender address for SigningCosmosClient.
    const accounts = await offlineSigner.getAccounts();

    accounts.forEach(element => {
        console.log(element);
    });

    // Initialize the gaia api with the offline signer that is injected by Keplr extension.
    const cosmJS = new SigningCosmosClient(
        RPC_ENDPOINT,
        accounts[0].address,
        offlineSigner,
    );

    document.getElementById("address").append(accounts[0].address);

    // tools.getKeys().forEach(key => {
    //     document.getElementById("txs").append(key);
    // });
    // document.getElementById("txs").append(tools.getKeys());

    const myWallet = accounts[0].address
    
    // https://api.crafteconomy.io/v1/tx/all/craft10r39fueph9fq7a6lgswu4zdsg8t3gxlqd6lnf0
    request('https://api.crafteconomy.io/v1/tx/all/'+myWallet, { json: true }, (err, res, body) => {
        if (err) { return console.log(err); }

        // gets all Txs for an address which the user generated and wants to sign

        // loop through body keys
        for (var txID in body) {
            console.log(txID);  
            // document.getElementById("txs").append(txID);   

            // trying to add it to a nice collapsable object, reload this when a Tx is signed (or remove from an array or something)
            var txDiv = document.createElement("div" + txID)
            txDiv.innerHTML = `
            <button class="btn btn-primary" type="button" data-toggle="collapse" data-target="#collapseExample" aria-expanded="false" aria-controls="collapseExample">
                ${txID}
            </button>
            <div class="collapse" id="collapseExample">
                <div class="card card-body">
                <li>Desc: ${body[txID]["description"]}</li>
                <li>Paying: ${body[txID]["to_address"]}</li>
                <li>Amount: ${String(body[txID]["amount"])} ${body[txID]["denom"]}</li> 
                <li>Tax: ${String(body[txID]["tax"]["amount"])} ${body[txID]["denom"]}</li> 
                <li>ID: ${txID}</li> 
                </div>
            </div>
            `
            // create button with function
            var btn = document.createElement("BUTTON");
            btn.innerHTML = "Sign " + txID;
            btn.onclick = function() {
                // Shows the Tx Value
                alert(JSON.stringify(body[txID]));

                sendTx(
                    body[txID]["to_address"],                     
                    parseInt(body[txID]["amount"]), 
                    body[txID]["denom"],
                    txID,
                    body[txID]["description"]
                );
            }
            txDiv.append(btn);

            document.getElementById("txs").append(txDiv);




            // whitespace
            document.getElementById("txs").append(document.createElement("br"));       
        }

        // for (var i = 0; i < body.length; i++) {
        //     // console.log(body[i]);
        //     getTxInfoFromKey(body[i]);
        // }

    });
};

document.sendForm.onsubmit = () => {
    // let recipient = document.sendForm.recipient.value;
    // let amount = document.sendForm.amount.value;
    // // let memo = document.sendForm.memo.value; // from TX
    // // No TX ID since this is just sending to some address.
    // sendTx(recipient, amount, "uosmo", "", "blank memo");

    fireSuccessfulBroadcast(document.sendForm.recipient.value);

    return false;
};

function fireSuccessfulBroadcast(txID) {
    // request('https://api.crafteconomy.io/v1/tx/sign/'+txID, { json: true }, (err, res, body) => {
    //     if (err) { return console.log(err); }
    // });

    request.post(
        "https://api.crafteconomy.io/v1/tx/sign/" + txID,
        { json: { key: 'value' } },
        function (error, response, body) {
            if (!error && response.statusCode == 200) {
                console.log(body);
                window.location.reload();
            }
        }
    );    
}

// create a function to send a transaction
function sendTx(recipient, amount, denom, txID, memo) {
    amount = parseFloat(amount);
    if (isNaN(amount)) {
        alert("Invalid amount");
        return false;
    }

    // TODO: if u<denom>, *1mil. Else, its normal number
    // if (denom.startsWith("u")) {
    //     amount *= 1000000;
    // }
    amount = Math.floor(amount);

    (async () => {
        await window.keplr.enable(CHAIN_ID);
        const offlineSigner = window.getOfflineSigner(CHAIN_ID);
        const accounts = await offlineSigner.getAccounts();

        const client = await SigningStargateClient.connectWithSigner(
            RPC_ENDPOINT,
            offlineSigner
        )

        const amountFinal = {
            denom: denom,
            amount: amount.toString(),
        }
        const fee = {
            amount: [{
                denom: denom,
                amount: '4000',
            }, ],
            gas: '200000',
        }
        const result = await client.sendTokens(accounts[0].address, recipient, [amountFinal], fee, memo)
        // TODO: Also add tax message here, which is paid to another address 
        // assertIsBroadcastTxSuccess(result)

        console.log(result);


        // does this ever run??
        if (result.code !== undefined && result.code !== 0) {
            alert("Failed to send tx: " + result.log || result.rawLog);
        } else {
            //alert("Succeed to send tx");
            if(txID !== "") {
                alert("SENT TO CHAIN! Once broadcasted, ping api.crafteconomy.io/v1/tx/sign/" + txID);
            }

            alert("Firing signed_" + txID + " to redis server!");     
            fireSuccessfulBroadcast(txID);                   
        }
    })();

    return false;
}
