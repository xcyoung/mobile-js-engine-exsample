describe('Test', () => {
    beforeEach(() => {
        jest.resetModules()
    });

    test('Android', () => {
        process.env.OS = 'Android'

        const dbObj = require('./exports/db').default
        dbObj.loadDB('./test/sqlite/test.db', 'database')

        const deviceInfoObj = require('./exports/deviceInfo').default
        deviceInfoObj.loadDeviceInfo('Android')

        require('../src/test')

        const inferInterface = global['test']
            
        inferInterface.testFetchOne()

        dbObj.closeDB('database')
    })

    test('iOS', () => {
        process.env.OS = 'iOS'

        const dbObj = require('./exports/db').default
        dbObj.loadDB('./test/sqlite/test.db', 'database')

        const deviceInfoObj = require('./exports/deviceInfo').default
        deviceInfoObj.loadDeviceInfo('iOS')

        require('../src/test')

        const inferInterface = global['test']
            
        inferInterface.testFetchOne()

        dbObj.closeDB('database')
    })

    afterEach(() => {
        delete process.env.OS
    })
})