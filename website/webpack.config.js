const HtmlWebpackPlugin = require("html-webpack-plugin");
const path = require("path");
var webpack = require('webpack');


module.exports = {
    entry: {
        main: "./src/main.js",
    },
    mode: "production",
    output: {
        filename: "bundle.js",
        path: path.resolve(__dirname,  "dist")
    },
    devServer: {
        port: 8081
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: "index.html",
            chunks: ["main"]
        })
    ]
};