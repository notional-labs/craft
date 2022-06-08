$('.keplr-connect').click(async function(event) {

  // If `window.getOfflineSigner` or `window.keplr` is null, Keplr extension may be not installed on browser.
  if (!window.getOfflineSigner || !window.keplr) {
      alert("Please install the Keplr Wallet extension");
  } else {
      if (window.keplr.experimentalSuggestChain) {
          // Keplr v0.6.4 introduces an experimental feature that supports the feature to suggests the chain from a webpage.
          // cosmoshub-3 is integrated to Keplr so the code should return without errors.
          // The code below is not needed for cosmoshub-3, but may be helpful if you’re adding a custom chain.
          // If the user approves, the chain will be added to the user's Keplr extension.
          // If the user rejects it or the suggested chain information doesn't include the required fields, it will throw an error.
          // If the same chain id is already registered, it will resolve and not require the user interactions.
          await window.keplr.experimentalSuggestChain({
              // Chain-id of the Cosmos SDK chain.
              chainId: "craft",
              // The name of the chain to be displayed to the user.
              chainName: "Craft",
              // RPC endpoint of the chain.
              rpc: "http://143.198.94.140:26657",
              // REST endpoint of the chain.
              rest: "http://143.198.94.140:1317",
              // Staking coin information
              stakeCurrency: {
                  // Coin denomination to be displayed to the user.
                  coinDenom: "CRAFT",
                  // Actual denom (i.e. uatom, uscrt) used by the blockchain.
                  coinMinimalDenom: "ucraft",
                  // # of decimal points to convert minimal denomination to user-facing denomination.
                  coinDecimals: 6,
                  // (Optional) Keplr can show the fiat value of the coin if a coingecko id is provided.
                  // You can get id from https://api.coingecko.com/api/v3/coins/list if it is listed.
                  // coinGeckoId: ""
              },
              // (Optional) If you have a wallet webpage used to stake the coin then provide the url to the website in `walletUrlForStaking`.
              // The 'stake' button in Keplr extension will link to the webpage.
              // walletUrlForStaking: "",
              // The BIP44 path.
              bip44: {
                  // You can only set the coin type of BIP44.
                  // 'Purpose' is fixed to 44.
                  coinType: 118,
              },
              // Bech32 configuration to show the address to user.
              // This field is the interface of
              // {
              //   bech32PrefixAccAddr: string;
              //   bech32PrefixAccPub: string;
              //   bech32PrefixValAddr: string;
              //   bech32PrefixValPub: string;
              //   bech32PrefixConsAddr: string;
              //   bech32PrefixConsPub: string;
              // }
              bech32Config: {
                  bech32PrefixAccAddr: "craft",
                  bech32PrefixAccPub: "craft" + "pub",
                  bech32PrefixValAddr: "craft" + "valoper",
                  bech32PrefixValPub: "craft" + "valoperpub",
                  bech32PrefixConsAddr: "craft" + "valcons",
                  bech32PrefixConsPub: "craft" + "valconspub",
              },
              // List of all coin/tokens used in this chain.
              currencies: [{
                  // Coin denomination to be displayed to the user.
                  coinDenom: "CRAFT",
                  // Actual denom (i.e. uatom, uscrt) used by the blockchain.
                  coinMinimalDenom: "ucraft",
                  // # of decimal points to convert minimal denomination to user-facing denomination.
                  coinDecimals: 6,
                  // (Optional) Keplr can show the fiat value of the coin if a coingecko id is provided.
                  // You can get id from https://api.coingecko.com/api/v3/coins/list if it is listed.
                  // coinGeckoId: ""
              }],
              // List of coin/tokens used as a fee token in this chain.
              feeCurrencies: [{
                  // Coin denomination to be displayed to the user.
                  coinDenom: "CRAFT",
                  // Actual denom (i.e. uatom, uscrt) used by the blockchain.
                  coinMinimalDenom: "ucraft",
                  // # of decimal points to convert minimal denomination to user-facing denomination.
                  coinDecimals: 6,
                  // (Optional) Keplr can show the fiat value of the coin if a coingecko id is provided.
                  // You can get id from https://api.coingecko.com/api/v3/coins/list if it is listed.
                  // coinGeckoId: ""
              }],
              // (Optional) The number of the coin type.
              // This field is only used to fetch the address from ENS.
              // Ideally, it is recommended to be the same with BIP44 path's coin type.
              // However, some early chains may choose to use the Cosmos Hub BIP44 path of '118'.
              // So, this is separated to support such chains.
              // coinType: 118,
              // (Optional) This is used to set the fee of the transaction.
              // If this field is not provided, Keplr extension will set the default gas price as (low: 0.01, average: 0.025, high: 0.04).
              // Currently, Keplr doesn't support dynamic calculation of the gas prices based on on-chain data.
              // Make sure that the gas prices are higher than the minimum gas prices accepted by chain validators and RPC/REST endpoint.
              gasPriceStep: {
                  low: 0.025,
                  average: 0.03,
                  high: 0.04
              }
          });
      } else {
          alert("Please update the Keplr Wallet extension to its latest version");
      }
  }

  const chainId = "craft";

  // This method will ask the user whether or not to allow access if they haven't visited this website.
  // Also, it will request user to unlock the wallet if the wallet is locked.
  await window.keplr.enable(chainId);

  const offlineSigner = window.getOfflineSigner(chainId);

  // You can get the address/public keys by `getAccounts` method.
  // It can return the array of address/public key.
  const accounts = await offlineSigner.getAccounts();

  //Stringify the Wallet ID and save it on localStorage
  localStorage.setItem("keplr_me", JSON.stringify(accounts[0]));

  // Check if this user is already linked or link him
  checkLinks();
});

// Discord Connect functionality
const clientId = '';
const clientSecret = '';
const redirectUri = 'https://crafteconomy.io/';

// Btn to redirect
$('.discord-connect').click(function() {
  window.location.href = ``;
});

$('.minecraft-connect').click(function() {
    const code = window.prompt("Enter your code!", "");
    // console.log("[minecraft-connect] Got code: " + code);
    
    // get request to get their UUID
    $.getJSON('https://api.crafteconomy.io/v1/connections/code/' + code, function(data, status){
        // alert("Data: " + data + "\nStatus: " + status);        
        console.log("UUID: " + data["_id"] + "\nStatus: " + status);
        localStorage.setItem("minecraft_me", JSON.stringify(data["_id"]));
        checkLinks();
    });
});

// Page load
$(function() {
  const params = new URLSearchParams(window.location.search);

  if (params.has('code')) {
      const code = params.get('code');

      $.ajax({
          type: "POST",
          url: 'https://discord.com/api/oauth2/token',
          headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
          },
          data: {
              'client_id': clientId,
              'client_secret': clientSecret,
              'grant_type': 'authorization_code',
              'code': code,
              'redirect_uri': redirectUri
          },
          success: function(token) {
              const accessToken = token["access_token"];

              $.ajax({
                  type: "GET",
                  url: 'https://discord.com/api/users/@me',
                  headers: {
                      'Content-Type': 'application/x-www-form-urlencoded',
                      'Authorization': `Bearer ${accessToken}`
                  },
                  success: function(data) {
                      localStorage.setItem("discord_me", JSON.stringify(data));
                      defaultExport();
                  },
                  error: function() {
                      alert('Failed to get your Discord account, please try again later.');
                  }
              });
          },
          error: function() {
              window.location = '/';
          }
      });
  }
});

const apiConnectionsLinkUrl = 'https://api.crafteconomy.io/v1/connections/link';

/**
* Check if a link was formed using 2 or more accounts
*/
function checkLinks() {

  // Check if Discord exists and Keplr exists & minecraft is linked
  if (localStorage.getItem("discord_me") !== null && localStorage.getItem("keplr_me") !== null && localStorage.getItem("minecraft_me") !== null) {    
      const discord = localStorage.getItem("discord_me");      
      const keplr = localStorage.getItem("keplr_me");
      const minecraftUUID = localStorage.getItem("minecraft_me");
    //   console.log("UUID from localStorage: " + minecraftUUID);
    
      $.ajax({
          type: "POST",
          url: apiConnectionsLinkUrl,
          headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
          },
          data: {
              discordId: JSON.parse(discord).id,
              keplrId: JSON.parse(keplr).address,
              minecraftId: minecraftUUID
          },
          success: function() {
              alert('Successfully linked your Discord and Keplr wallet together')
          },
          error: function() {
              alert('Failed to link your accounts, one or more account is already linked elsewhere');
          }
      });
  }
}