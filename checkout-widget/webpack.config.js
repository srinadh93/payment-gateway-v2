const path = require('path');

module.exports = {
  mode: 'production',
  entry: './src/sdk/PaymentGateway.js',
  output: {
    filename: 'checkout.js',
    path: path.resolve(__dirname, 'dist'),
    library: 'PaymentGateway',
    libraryTarget: 'umd',
    globalObject: 'typeof self !== "undefined" ? self : this'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env']
          }
        }
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      }
    ]
  }
};
