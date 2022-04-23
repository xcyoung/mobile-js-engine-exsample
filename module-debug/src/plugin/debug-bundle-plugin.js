const process = require("child_process");
const fs = require('fs');
const path = require('path')

class DebugBundlePlugin {
    constructor() {
    }
    
    apply(compiler) {
        const outputPath = compiler.options.output.path;
       
        compiler.hooks.entryOption.tap('DebugBundlePlugin', (stats) => {
            // const scriptPath = process.execSync('cd ../module-script && pwd').toString()
            const scriptDistPath = path.resolve('../module-script', 'dist')
            const publicDistPath = path.resolve(__dirname, '../../public/dist')
            if (fs.existsSync(publicDistPath)) {
                fs.unlinkSync(publicDistPath)
            }
            // if (fs.existsSync(scriptDistPath)) {
            //     fs.copyFileSync(scriptDistPath, publicDistPath)
            // }
            // process.execSync('cd ../module-script && npm run build')
        })
    }
}


module.exports = DebugBundlePlugin