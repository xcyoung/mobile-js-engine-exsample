//
//  JsEngineDebug.swift
//  js-engine-debug
//
//  Created by idt on 2022/5/9.
//

import UIKit
import JavaScriptCore
import RxSwift

public class JsEngineDebugFactory: NSObject {
    public static func create(host: String, onExport: @escaping ((_ exports: [String], _ ctx: JSContext) -> Void), onDispose: @escaping (() -> Void)) -> JsEngineDebug {
        return JsEngineDebug(host: host, onExport: onExport, onDispose: onDispose)
    }
}

public class JsEngineDebug: NSObject {
    private static let TAG = "JsEngineDebug"

    private let host: String
    private let onExport: ((_ exports: [String], _ ctx: JSContext) -> Void)
    private let onDispose: (() -> Void)
    
    private let disposeBag = DisposeBag.init()
    
    private var jsContext: JSContext? = nil

    fileprivate init(host: String,
        onExport: @escaping ((_ exports: [String], _ ctx: JSContext) -> Void),
        onDispose: @escaping (() -> Void)) {
        self.host = host
        self.onExport = onExport
        self.onDispose = onDispose
    }

    public func start() {
        requestConfig()
            .do(onSubscribe: {
                if self.jsContext != nil {
                    jsContext = nil
                }
            })
            .observeOn(MainScheduler.instance)
            .flatMap({ configs in
                self.executeTest(configs: configs)
            })
            .observeOn(MainScheduler.instance)
            .subscribeOn(SerialDispatchQueueScheduler.init(internalSerialQueueName: JsEngineDebug.TAG))
            .subscribe(onNext: { _ in
                debugPrint(JsEngineDebug.TAG, "All cases passed")
            }, onError: { error in
                debugPrint(JsEngineDebug.TAG, "There are cases that fail")
            }).disposed(by: disposeBag)
    }

    private func requestConfig() -> Observable<[JsEngineDebugConfig]> {
        return Observable<[JsEngineDebugConfig]>.create { [weak self] ob -> Disposable in
            guard let weak = self else {
                ob.on(.error(NSError.init(domain: "disposed", code: -1, userInfo: nil)))
                return Disposables.create()
            }

            URLSession.shared.dataTask(with: URL.init(string: weak.host + "/config")!) { data, response, error in
                if let error = error {
                    ob.on(.error(error))
                } else if let data = data {
                    let decoder = JSONDecoder.init()
                    do {
                        let configs = try decoder.decode([JsEngineDebugConfig].self, from: data)
                        ob.on(.next(configs))
                    } catch {
                        ob.on(.error(error))
                    }
                }
            }.resume()

            return Disposables.create()
        }
    }
    
    private func executeTest(configs: [JsEngineDebugConfig]) -> Observable<Int> {
        return Observable<Int>.create { [weak self] ob -> Disposable in
            guard let weak = self else {
                ob.on(.error(NSError.init(domain: "disposed", code: -1, userInfo: nil)))
                return Disposables.create()
            }
            
            let semaphore = DispatchSemaphore.init(value: 0)
            for config in configs {
                do {
                    var err: Error? = nil
                    let dataTask = URLSession.shared.dataTask(with: URL.init(string: weak.host + "/script/" + config.script)!) { data, response, error in
                        if let error = error {
                            err = error
                        } else if let data = data {
                            let js = String.init(data: data, encoding: .utf8)!
                            
                            let jsVirtualMachine = JSVirtualMachine.init()
                            let ctx = JSContext.init(virtualMachine: jsVirtualMachine)!
                            
                            weak.onExport(config.exports, ctx)
                            weak.jsContext = ctx
                            
                            let res = ctx.evaluateScript(js)
                            if let exception = res?.context?.exception, !exception.isUndefined {
                                err = NSError.init(domain: config.script + " \(exception.toString())", code: -1, userInfo: nil)
                                return
                            }
                            let res2 = ctx.evaluateScript(config.eval)
                            if let exception = res2?.context?.exception, !exception.isUndefined {
                                err = NSError.init(domain: config.script + " \(exception.toString())", code: -1, userInfo: nil)
                                return
                            }
                            
                            weak.jsContext = nil
                        } else {
                            err = NSError.init(domain: config.script + " data is nil", code: -1, userInfo: nil)
                        }
                        semaphore.signal()
                    }
                    dataTask.resume()
                    semaphore.wait()
                    if let e = err {
                        throw e
                    }
                } catch {
                    debugPrint(JsEngineDebug.TAG, config.script + " error:", error.localizedDescription)
                    ob.on(.error(error))
                    return Disposables.create()
                }
            }
            
            ob.on(.next(1))
            
            return Disposables.create()
        }
    }
}

struct JsEngineDebugConfig: Codable {
    let script: String
    let exports: [String]
    let eval: String
}
