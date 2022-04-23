const path = require('path');
const { argv } = require('process');
const webpack = require('webpack');

module.exports = (env, arg) => {
    return {
        entry: './src/test.ts',   // 打包对入口文件，期望打包对文件入口。 这里配置tsc05.ts的位置
        output: {
            filename: 'test.js',   // 输出文件名称
            path: path.resolve(__dirname, `./dist`)  //获取输出路径
        },
        mode: 'production',   // 整个mode 可以不要，模式是生产坏境就是压缩好对，这里配置开发坏境方便看生成对代码
        module: {
            rules: [{
                test: /\.ts?$/,
                use: 'babel-loader',
                exclude: /node_modules/
            }]
        },
        resolve: {
            extensions: ['.ts']      // 解析对文件格式
        }
    }
}