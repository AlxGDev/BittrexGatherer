const path = require('path');
const webpack = require('webpack');

module.exports = {
  context: path.resolve(__dirname, ''),
  entry: {
    dependencies: './dependencies.js',
  },
  output: {
    path: path.resolve(__dirname, '../resources/static/js/core'),
    filename: '[name].bundle.js',
  }
};