//
//  ViewController.swift
//  module-iOS
//
//  Created by 肖楚🐑 on 2022/4/30.
//

import UIKit
import JavaScriptCore
import GRDB
import js_engine_debug

class ViewController: UIViewController {
    private let jsContext: JSContext = {
        let jsVirtualMachine = JSVirtualMachine.init()
        let ctx = JSContext.init(virtualMachine: jsVirtualMachine)!
        
        let jsPath = Bundle.init(for: Database.classForCoder()).path(forResource: "test", ofType: "js")!
        let js = try! String.init(contentsOfFile: jsPath)
        
        ctx.setObject(DeviceInfo.init(), forKeyedSubscript: "deviceInfo" as NSCopying & NSObjectProtocol)
        ctx.setObject(Database.init(), forKeyedSubscript: "database" as NSCopying & NSObjectProtocol)
        
        ctx.evaluateScript(js)
        return ctx
    }()
    
    private var debug: JsEngineDebug? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let btn: UIButton = {
            let btn = UIButton.init(type: .system)
            btn.backgroundColor = UIColor.blue
            btn.frame = CGRect.init(x: self.view.center.x - 50, y: self.view.center.y - 75, width: 100, height: 50)
            return btn
        }()
        
        let testBtn: UIButton = {
            let btn = UIButton.init(type: .system)
            btn.backgroundColor = UIColor.blue
            btn.frame = CGRect.init(x: self.view.center.x - 50, y: self.view.center.y + 75, width: 100, height: 50)
            return btn
        }()
        
        self.view.addSubview(btn)
        self.view.addSubview(testBtn)
        btn.addTarget(self, action: #selector(onClick), for: .touchUpInside)
        testBtn.addTarget(self, action: #selector(onTestClick), for: .touchUpInside)
    }

    @objc private func onClick() {
        let test = jsContext.objectForKeyedSubscript("test")!
        let testFetchOne = test.objectForKeyedSubscript("testFetchOne")!
        let result = testFetchOne.call(withArguments: [])!.toString()!
        
        debugPrint("ViewController", result)
    }
    
    @objc private func onTestClick() {
        self.debug = nil
        
        let debug = JsEngineDebugFactory.create(host: "http://x.x.x.x:8082") { exports, ctx in
            exports.forEach { export in
                if export == "deviceInfo" {
                    ctx.setObject(DeviceInfo.init(), forKeyedSubscript: "deviceInfo" as NSCopying & NSObjectProtocol)
                } else if export == "database" {
                    ctx.setObject(Database.init(), forKeyedSubscript: "database" as NSCopying & NSObjectProtocol)
                }
            }
        } onDispose: {
            
        }
        
        debug.start()
        self.debug = debug
    }
}

@objc protocol DeviceInfoInterface: JSExport {
    func platform() -> String
}

class DeviceInfo: NSObject, DeviceInfoInterface {
    func platform() -> String {
        return "iOS"
    }
}

@objc protocol DatabaseInterface: JSExport {
    func fetchOne(sql: String) -> String
}

class Database: NSObject, DatabaseInterface {
    private let dbQueue: DatabaseQueue

    override init() {
        let config = Configuration.init()
        let dbPath = Bundle.init(for: Database.classForCoder()).path(forResource: "test", ofType: "db")!
        let dbQueue = try! DatabaseQueue(path: dbPath, configuration: config)
        self.dbQueue = dbQueue
        super.init()
    }

    func fetchOne(sql: String) -> String {
        do {
            let result = try dbQueue.read({ db -> Dictionary<String, Any> in
                guard let row = try Row.fetchOne(db, sql: sql) else {
                    return [:]
                }

                var dic = Dictionary<String, Any>()
                row.columnNames.forEach { column in
                    dic[column] = row[column]?.databaseValue.storage.value
                }

                return dic
            })

            let data = try JSONSerialization.data(withJSONObject: result, options: [])
            let json = String.init(data: data, encoding: .utf8)!
            return json
        } catch {
            return "{}"
        }
    }
}
