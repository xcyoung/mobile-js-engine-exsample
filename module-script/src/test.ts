// let nativeObject: { deviceInfo: any; database: any }

function requireModule(moduleName: string): any {
    const object = global[moduleName]
    if (object == undefined) {
        throw EvalError(`${moduleName} is not exit`)
    } else {
        return object
    }
}

// (function () {
//     const deviceInfo = global['deviceInfo']
//     const database = global['database']
//     if (deviceInfo == undefined) throw EvalError('deviceInfo is not exit')
//     if (database == undefined) throw EvalError('database is not exit')

//     nativeObject = {
//         deviceInfo: deviceInfo,
//         database: database
//     }
// })()

class DBMoudle {
    fetchOne(sql: string): string {
        const deviceInfo = requireModule('deviceInfo')
        const platform = deviceInfo.platform()
        if (platform == 'Android') {
            return this.fetchOneFromAndroid(sql)
        } else if (platform == 'iOS') {
            return this.fetchOneFromiOS(sql)
        }
        throw EvalError('unknown platform')
    }

    private fetchOneFromAndroid(sql: string): string {
        const database = requireModule('database')
        return database.fetchOne(sql)
    }

    private fetchOneFromiOS(sql: string): string {
        const database = requireModule('database')
        return database.fetchOneWithSql(sql)
    }
}

global['test'] = {
    testFetchOne: function testFetchOne(id: string) {
        const db = new DBMoudle()
        db.fetchOne('')
    }
}