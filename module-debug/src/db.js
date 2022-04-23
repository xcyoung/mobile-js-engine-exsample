import initSqlJs from "sql.js";
import sqlWasm from "!!file-loader?name=sql-wasm-[contenthash].wasm!sql.js/dist/sql-wasm.wasm";

export async function loadDB(options) {
    const sqlPromise = await initSqlJs({ locateFile: () => sqlWasm });
    let databasePath = options.dbPath;
    const dataPromise = fetch(databasePath).then((res) => res.arrayBuffer());
    const [SQL, buf] = await Promise.all([sqlPromise, dataPromise]);
    let db = new SQL.Database(new Uint8Array(buf));

    return {
        db: db,
        fetchAll: function (sql) {
            const execRes = db.exec(sql)
            const columns = execRes[0]['columns']
            const values = execRes[0]['values']

            let res = []
            values.forEach((valueArr) => {
                let object = {}
                valueArr.forEach((value, index) => {
                    object[columns[index]] = value
                })
                res.push(object)
            })
            return JSON.stringify(res)
        },
        fetchOne: function (sql) {
            const execRes = db.exec(sql)
            if (!execRes.length) {
                return '{}'
            }
            const columns = execRes[0]['columns']
            const values = execRes[0]['values']
            const valueArr = values[0]
            let object = {}
            valueArr.forEach((value, index) => {
                object[columns[index]] = value
            })

            return JSON.stringify(object)
        }
    }
}