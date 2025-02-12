const path = require("path");

module.exports = (env, argv) => ({
    mode: "development",
    entry: {
        "UnoChoice": [
            path.join(__dirname, "src/main/resources/org/biouno/unochoice/stapler/unochoice/UnoChoice.es6"),
        ],
        "Util": [
            path.join(__dirname, "src/main/resources/org/biouno/unochoice/stapler/unochoice/Util.ts"),
        ],
    },
    output: {
        path: path.join(__dirname, "target/classes/org/biouno/unochoice/stapler/unochoice"),
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
