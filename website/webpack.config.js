const HtmlWebpackPlugin = require("html-webpack-plugin");

const path = require("path");

module.exports = {
    entry: {
        main: "./src/main.js",
    },
    output: {
        filename: "bundle.js",
        path: path.resolve(__dirname,  "/dist/")
    },
    devServer: {
        port: 80
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: "index.html",
            chunks: ["main"]
        })
    ],  
    resolve: {
        fallback: {
          "fs": false,
          "tls": false,
          "net": false,
          "path": false,
          "zlib": false,
          "http": false,
          "https": false,
          "stream": false,
          "crypto": false
        } 
      }
};