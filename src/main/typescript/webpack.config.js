const path = require('path')

module.exports = {
  mode: "development",
  devtool: "inline-source-map",
  target: ['web', 'es5'],
  entry: {
    main: "./src/index.ts",
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
        loader: "ts-loader",
        exclude: /node_modules/,
      }
    ]
  },
  output: {
    path: path.resolve(__dirname, './dist/org/biouno/unochoice/stapler/unochoice/'),
    filename: "unochoice.js",
    libraryTarget: "window",
  },
  externals: [
    'jquery'
  ],
}
