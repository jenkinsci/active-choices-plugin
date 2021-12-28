const path = require('path')

module.exports = {
  mode: "development",
  devtool: "inline-source-map",
  entry: {
    main: "./src/index.ts",
  },
  output: {
    path: path.resolve(__dirname, './dist/org/biouno/unochoice/stapler/unochoice/'),
    filename: "unochoice.js"
  },
  resolve: {
    extensions: [".ts", ".tsx", ".js"],
    modules: [
      path.resolve('node_modules')
    ]
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        loader: "ts-loader"
      }
    ]
  }
}
