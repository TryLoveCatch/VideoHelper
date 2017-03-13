package android.luna.net.videohelper.video.parser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by bintou on 15/11/4.
 * 原理是打开webview，注入代码
 */
public class IqiyiParser {

    private WebView webView;
    private myWebChromeClient mWebChromeClient;
    private myWebViewClient mWebViewClient;
    private Handler mHandler;
    private Context mContext;
    private String mTargetUrl;
    private String mTargetWebName;
    private boolean isOutsideWebView = false;

    @SuppressLint("JavascriptInterface")
    public IqiyiParser(Context context, String url, Handler handler) {
        mContext = context;
        mHandler = handler;
        mTargetUrl = url;
//        webView = (WebView) findViewById(R.id.webView);
        webView = new WebView(mContext);
        webView.setVisibility(View.INVISIBLE);
        mWebViewClient = new myWebViewClient();
        webView.setWebViewClient(mWebViewClient);

        mWebChromeClient = new myWebChromeClient();
        webView.setWebChromeClient(mWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(BrowserUnit.UA_DESKTOP_IPAD);


        // 重要！！，必须添加提供给js页面操作方法，并在loadurl的时候加入方法名。
        webView.addJavascriptInterface(new JsInterface(), "javaMethod");
        webView.setVisibility(View.GONE);
        isOutsideWebView = false;
    }

    public IqiyiParser(Context context, Handler handler, WebView webView) {
        mContext = context;
        mHandler = handler;
//        webView = (WebView) findViewById(R.id.webView);
        this.webView = webView;
        this.webView.getSettings().setJavaScriptEnabled(true);
        // 重要！！，必须添加提供给js页面操作方法，并在loadurl的时候加入方法名。
        this.webView.addJavascriptInterface(new JsInterface(), "javaMethod");
        isOutsideWebView = true;
    }

    public IqiyiParser(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        isOutsideWebView = true;
    }

    public void addJsInterface(WebView webView) {
        // 重要！！，必须添加提供给js页面操作方法，并在loadurl的时候加入方法名。
        webView.addJavascriptInterface(new JsInterface(), "javaMethod");
    }

    public void setUrl(String url) {
        mTargetUrl = url;
    }


    public void setName(String name) {
        mTargetUrl = StringUtils.isBlank(name) ? "" : name;
    }

    public void startParser() {
        webView.loadUrl(mTargetUrl);
    }


    public WebView getWebView() {
        return webView;
    }


    class myWebChromeClient extends WebChromeClient {

    }

    class myWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            loadJsCode(webView);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    String jsCode;

    public void loadJsCode(WebView toWebView) {
        try {
            if (StringUtils.isBlank(jsCode)) {
                InputStream is = mContext.getAssets().open("iqiyi.js");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                jsCode = new String(buffer);
            }
            toWebView.loadUrl("javascript:" + jsCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public class JsInterface {
        @JavascriptInterface
        public void getJsonString(String json) {
            try {
                LunaLog.e(json);
                JSONObject jo = JSONUtils.toJsonObject(json);
                JSONObject data = JSONUtils.getJSONObject(jo, "data", null);
                final String url = JSONUtils.getString(data, "m3u", "");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (mHandler != null) {
                            LunaLog.e("url:" + url);
                            Message msg = new Message();
                            msg.what = 1;
                            msg.arg2 = 1001;
                            msg.obj = StringUtils.nullStrToEmpty(url);
                            mHandler.sendMessage(msg);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void getUrl(final String url) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mHandler != null) {
                        LunaLog.e("cacheUrl:" + url);
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = StringUtils.nullStrToEmpty(url);
                        mHandler.sendMessage(msg);
                    }
                }
            });

        }
    }
}
