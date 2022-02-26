const HtmlWebpackPlugin = require("html-webpack-plugin");
const path = require("path");

module.exports = {
    entry: {
        main: "./src/main.js",
    },
    output: {
        filename: "bundle.js",
        path: path.resolve(__dirname,  "dist")
    },
    devServer: {
        host: '127.0.0.1',
        port: 8081
    },
    // https://github.com/googleapis/google-api-nodejs-client/issues/1732
    node: {
        child_process: 'empty',
        fs: 'empty',
        crypto: 'empty',
        net: 'empty',
        tls: 'empty'
      },
    plugins: [
        new HtmlWebpackPlugin({
            template: "index.html",
            chunks: ["main"]
        })
    ]
};

