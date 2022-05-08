package me.xcyoung.quick.js.debug;

public class JsEngineDebugConfig {
    private String script;
    private String[] exports;
    private String eval;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String[] getExports() {
        return exports;
    }

    public void setExports(String[] exports) {
        this.exports = exports;
    }

    public String getEval() {
        return eval;
    }

    public void setEval(String eval) {
        this.eval = eval;
    }
}