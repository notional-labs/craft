import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
import router from './router'
import vueLib from '@starport/vue'

const app = createApp(App)
app.config.globalProperties._depsLoaded = true
app.use(store).use(router).use(vueLib).mount('#app')

document.addEventListener("DOMContentLoaded", function(event) {
    async function connect() {
        await window.keplr.experimentalSuggestChain({
            chainId: "craft",
            chainName: "Craft Economy",
            rpc: "http://143.198.94.140:26657",
            rest: "http://143.198.94.140:1317",
            bip44: {
                coinType: 118,
            },
            bech32Config: {
                bech32PrefixAccAddr: "craft",
                bech32PrefixAccPub: "craft" + "pub",
                bech32PrefixValAddr: "craft" + "valoper",
                bech32PrefixValPub: "craft" + "valoperpub",
                bech32PrefixConsAddr: "craft" + "valcons",
                bech32PrefixConsPub: "craft" + "valconspub",
           },
            currencies: [
                {
                    coinDenom: "CRAFT",
                    coinMinimalDenom: "ucraft",
                    coinDecimals: 6,
                    coinGeckoId: "craft",
                },
            ],
            feeCurrencies: [
                {
                    coinDenom: "CRAFT",
                    coinMinimalDenom: "ucraft",
                    coinDecimals: 6,
                    coinGeckoId: "craft",
                },
            ],
            stakeCurrency: {
                coinDenom: "CRAFT",
                coinMinimalDenom: "ucraft",
                coinDecimals: 6,
                coinGeckoId: "craft",
            },
            coinType: 118,
            gasPriceStep: {
                low: 0.01,
                average: 0.025,
                high: 0.03,
            },
        });
    }
    connect();

}); 
