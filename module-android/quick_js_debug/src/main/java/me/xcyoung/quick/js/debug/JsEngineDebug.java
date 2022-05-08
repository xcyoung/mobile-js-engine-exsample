package me.xcyoung.quick.js.debug;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import app.cash.quickjs.QuickJs;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JsEngineDebug {
    private static final String TAG = JsEngineDebug.class.getSimpleName();

    private final OkHttpClient okHttpClient;
    private final JsEngineDebugCallback callback;
    private final String host;

    private QuickJs quickJs;

    private JsEngineDebug(@NonNull OkHttpClient okHttpClient, @NonNull String host, @NonNull JsEngineDebugCallback callback) {
        this.okHttpClient = okHttpClient;
        this.host = host;
        this.callback = callback;
    }

    public void start() {
        requestConfig()
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        if (quickJs != null) {
                            quickJs.close();
                            quickJs = null;
                        }
                    }
                })
                .flatMap(new Function<List<JsEngineDebugConfig>, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull List<JsEngineDebugConfig> jsEngineDebugConfigs) throws Exception {
                        return executeTest(jsEngineDebugConfigs);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "All cases passed");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "There are cases that fail");
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                });
    }

    private Observable<List<JsEngineDebugConfig>> requestConfig() {
        return Observable.create(new ObservableOnSubscribe<List<JsEngineDebugConfig>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<JsEngineDebugConfig>> e) throws Exception {
                Request request = new Request.Builder().get().url(host + "/config").build();
                Response response = okHttpClient.newCall(request).execute();

                String configJson = response.body().string();
                List<JsEngineDebugConfig> configs = new Gson().fromJson(configJson, new TypeToken<List<JsEngineDebugConfig>>(){}.getType());
                e.onNext(configs);
            }
        });
    }

    private Observable<Integer> executeTest(List<JsEngineDebugConfig> configs) {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                for(JsEngineDebugConfig config: configs) {
                    try {
                        Request request = new Request.Builder().get().url(host + "/script/" + config.getScript()).build();
                        Response response = okHttpClient.newCall(request).execute();

                        String js = response.body().string();

                        QuickJs quickJs = QuickJs.create();
                        JsEngineDebug.this.quickJs = quickJs;

                        JsEngineDebug.this.callback.onExport(config.getExports(), quickJs);

                        quickJs.evaluate(js);
                        quickJs.evaluate(config.getEval());

                        JsEngineDebug.this.callback.onDispose();
                        JsEngineDebug.this.quickJs.close();
                        JsEngineDebug.this.quickJs = null;
                    } catch (Exception exception) {
                        Log.e(TAG, config.getScript() + " error:", exception);
                        throw exception;
                    }
                    Log.d(TAG, config.getScript() + " pass");
                }

                e.onNext(1);
            }
        });
    }

    public static class Builder {
        private String host = "";
        private JsEngineDebugCallback callback;

        public Builder addCallback(JsEngineDebugCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public JsEngineDebug build() {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

            return new JsEngineDebug(clientBuilder.build(), this.host, this.callback);
        }
    }
}
