const path = require("path");

module.exports = (env, argv) => ({
    mode: "development",
    entry: {
        "UnoChoice": [
            path.join(__dirname, "src/main/js/UnoChoice.es6"),
        ],
        "Util": [
            path.join(__dirname, "src/main/js/Util.ts"),
        ],
    },
    output: {
        path: path.join(__dirname, "src/main/webapp/js"),
        iife: false,
    },
    devtool: argv.mode === "production" ? "source-map" : "eval-source-map",
    module: {
        rules: [
            {use: "ts-loader", test: /\.ts$/},
            {use: "babel-loader", test: /\.es6$/},
        ],
    },
    externals: {
        jquery: 'jQuery'
    }
});
