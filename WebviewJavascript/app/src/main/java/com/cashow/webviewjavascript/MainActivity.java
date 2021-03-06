package com.cashow.webviewjavascript;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

/**
 * 这个项目用了2种方法实现android与网页之间的通信
 * 网页通知android端：
 *    1. webview拦截跳转链接：
 *       如果网页要传输字符串"hello"给android，可以在要传输的字符串前面加上约定好的前缀（例如本项目中的"jstag://"），
 *       并在网页里给按钮加上 前缀 + 字符串（例如"jstag://hello"）的链接。
 *       webview在监测到带有约定前缀的链接后，可以拦截页面的跳转并将跳转链接按约定的格式进行处理。
 *    2. webview里定义JavascriptInterface：
 *       这个方法是将java的对象映射到JavaScript上。
 *       例如在本项目中，是将java的 JsInterface 对象映射到了JavaScript的 window.androidtag。
 *       如果在JavaScript里调用 window.androidtag.showMessage,会调用 JsInterface 对象的showMessage()方法
 *  android端通知网页：
 *    1. 直接调用webview上的JavaScript
 *       webview.loadUrl("javascript:xxxxxxx()")
 *    2. 在android 4.4以后，可以通过evaluateJavascript调用JavaScript，并且能获得JavaScript的返回值
 *       webview.evaluateJavascript("xxxxxxx()", new ValueCallback<String>() {
 *           @Override
 *           public void onReceiveValue(String value) {
 *           }
 *       );
 * ps：这个项目对应的html代码链接：https://github.com/cashow/cashow.github.io/blob/master/html/android_webview.html
 */
public class MainActivity extends AppCompatActivity {

    private WebView webview;
    private Button button_loadurl;
    private Button button_evaluatejs;

    private boolean isPageFinished;

    private final static String test_url = "http://cashow.github.io/html/android_webview";
    private final static String WEBVIEW_JS_TAG = "jstag://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isPageFinished = false;

        findView();
        initWebview();
        initButton();
    }

    private void findView() {
        webview = (WebView) findViewById(R.id.mywebview);
        button_loadurl = (Button) findViewById(R.id.button_loadurl);
        button_evaluatejs = (Button) findViewById(R.id.button_evaluatejs);
    }

    private void initWebview() {
        // 初始化webview
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(false);
        // 在android版本号低于17时可能会有安全性的问题
        // 详情：https://developer.android.com/reference/android/webkit/WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)
        webview.addJavascriptInterface(new JsInterface(), "androidtag");
        webview.setWebChromeClient(new WebChromeClient() {});
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 如果发现跳转链接是以 WEBVIEW_JS_TAG 开头的，拦截这次跳转链接并将这个链接当做字符串进行处理
                if (url.startsWith(WEBVIEW_JS_TAG)) {
                    handleJsNativeCall(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isPageFinished = true;
            }
        });
        webview.loadUrl(test_url);
    }

    private void initButton() {
        button_loadurl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPageFinished) {
                    webview.loadUrl("javascript:alertMessage(\"hello from java\")");
                } else {
                    Toast.makeText(getApplicationContext(), "网页还在加载中", Toast.LENGTH_SHORT).show();
                }

            }
        });
        button_evaluatejs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPageFinished) {
                    webview.evaluateJavascript("getResult(\"hahaha\")", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Toast.makeText(getApplicationContext(), "接收到网页传来的字符串：" + value, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "网页还在加载中", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void handleJsNativeCall(String url) {
        String js_string = url.replaceFirst(WEBVIEW_JS_TAG, "");
        Toast.makeText(getApplicationContext(), "接收到网页传来的字符串：" + js_string, Toast.LENGTH_SHORT).show();
    }

    public class JsInterface {
        @JavascriptInterface
        public void showMessage(String js_string) {
            Toast.makeText(getApplicationContext(), "接收到网页传来的字符串：" + js_string, Toast.LENGTH_SHORT).show();
        }
    }
}
