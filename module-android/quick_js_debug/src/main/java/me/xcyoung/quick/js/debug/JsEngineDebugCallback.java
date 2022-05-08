package me.xcyoung.quick.js.debug;

import app.cash.quickjs.QuickJs;

public interface JsEngineDebugCallback {
    void onExport(String[] exports, QuickJs js);
    void onDispose();
}
