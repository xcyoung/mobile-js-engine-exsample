const { defineConfig } = require('@vue/cli-service')
/**
 * MARK: - 解决'ebpack < 5 used to include polyfills for node.js core modules by default. 
 * This is no longer the case. Verify if you need this module and configure a polyfill for it.'
 *  */
const NodePolyfillPlugin = require('node-polyfill-webpack-plugin')
const DebugBundlePlugin = require('./src/plugin/debug-bundle-plugin')

module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack: config => {
    config.module.rule('wasm').test(/\.wasm$/).type('javascript/auto')
  },
  configureWebpack: {
    plugins: [
      new NodePolyfillPlugin(),
      // new DebugBundlePlugin()
    ],
    resolve: {
      fallback: {
        "fs": false
      },
    }
  }
})
