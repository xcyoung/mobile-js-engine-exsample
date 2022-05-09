import Database from 'better-sqlite3'

export default {
    loadDB: (path: string, objcetName: string) => {
        const db = new Database(path, {
            fileMustExist: true
        })
        const fetchOne = function fetchOne(sql: string) {
            const enity = db.prepare(sql).get()
            if (enity == undefined) return '{}'
            return JSON.stringify(enity)
        }

        const fetchAll = function (sql: string) {
            const enities = db.prepare(sql).all()
            if (enities == undefined) return '[]'
            return JSON.stringify(enities)
        }

        const execute = function (sql: string) {
            db.exec(sql)
            return true
        }

        global[objcetName] = {
            db: db,
            fetchOne: fetchOne,
            fetchAll: fetchAll,
            execute: execute,
            fetchOneWithSql: fetchOne,
            fetchAllWithSql: fetchAll,
            executeWithSql: execute
        }
    },
    closeDB: (objcetName: string) => {
        const db = global[objcetName]
        if (db != undefined) {
            db.db.close()
        }
    }
}