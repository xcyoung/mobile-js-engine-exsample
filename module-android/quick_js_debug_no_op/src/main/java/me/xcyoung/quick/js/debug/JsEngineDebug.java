package me.xcyoung.quick.js.debug;

public class JsEngineDebug {
    public void start() {

    }

    public static class Builder {
        public Builder addCallback(JsEngineDebugCallback callback) {

            return this;
        }

        public Builder host(String host) {

            return this;
        }

        public JsEngineDebug build() {

            return new JsEngineDebug();
        }
    }
}
