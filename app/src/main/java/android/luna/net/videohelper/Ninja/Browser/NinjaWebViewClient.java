package android.luna.net.videohelper.Ninja.Browser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelper.Ninja.Unit.IntentUnit;
import android.luna.net.videohelptools.R;
import android.net.MailTo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import net.luna.common.debug.LunaLog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NinjaWebViewClient extends WebViewClient {
    private android.luna.net.videohelper.Ninja.View.NinjaWebView ninjaWebView;
    private Context context;

    private android.luna.net.videohelper.Ninja.Browser.AdBlock adBlock;

    private boolean white;

    private Handler controlHandler;

    public void updateWhite(boolean white) {
        this.white = white;
    }

    private boolean enable;

    public void enableAdBlock(boolean enable) {
        this.enable = enable;
    }

    public NinjaWebViewClient(android.luna.net.videohelper.Ninja.View.NinjaWebView ninjaWebView, Handler handler) {
        super();
        this.ninjaWebView = ninjaWebView;
        this.context = ninjaWebView.getContext();
        this.adBlock = ninjaWebView.getAdBlock();
        this.white = false;
        this.enable = true;
        controlHandler = handler;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        if (view.getTitle() == null || view.getTitle().isEmpty()) {
            ninjaWebView.update(context.getString(R.string.album_untitled), url);
        } else {
            ninjaWebView.update(view.getTitle(), url);
        }
        if (controlHandler != null) {
            Message msg = new Message();
            msg.what = BrowserUnit.MESSAGE_PAGE_START;
            msg.obj = url;
            controlHandler.sendMessage(msg);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (!ninjaWebView.getSettings().getLoadsImagesAutomatically()) {
            ninjaWebView.getSettings().setLoadsImagesAutomatically(true);
        }

        if (view.getTitle() == null || view.getTitle().isEmpty()) {
            ninjaWebView.update(context.getString(R.string.album_untitled), url);
        } else {
            ninjaWebView.update(view.getTitle(), url);
        }

        if (ninjaWebView.isForeground()) {
            ninjaWebView.invalidate();
        } else {
            ninjaWebView.postInvalidate();
        }
        if (controlHandler != null) {
            Message msg = new Message();
            msg.what = BrowserUnit.MESSAGE_PAGE_FINISH;
            msg.obj = url;
            controlHandler.sendMessage(msg);
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (controlHandler != null) {
            Message msg = new Message();
            msg.what = BrowserUnit.MESSAGE_LOAD_RESOURCE;
            msg.obj = url;
            controlHandler.sendMessage(msg);
        }
        super.onLoadResource(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)) {
            Intent intent = IntentUnit.getEmailIntent(MailTo.parse(url));
            context.startActivity(intent);
            view.reload();
            return true;
        } else if (url.startsWith(BrowserUnit.URL_SCHEME_INTENT)) {
            Intent intent;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
            } // When intent fail will crash8
        }

        url = ninjaWebView.urlUaFilter(view, url);

        //不跳转APK下载
        if (url.endsWith(".apk")) {
            return true;
        }

        LunaLog.d(url);

        //针对爱奇艺不适用IOS的ua,deng
        if (url.equals("http://m.iqiyi.com/")) {
            url = "https://m.baidu.com/from=844b/bd_page_type=1/ssid=0/uid=0/pu=usm%404%2Csz%401320_1001%2Cta%40iphone_2_6.0_3_537/baiduid=77DA72BC6248F3BE409D115B709DA755/w=0_10_/t=iphone/l=1/tc?ref=www_iphone&lid=7451592737901930104&order=2&fm=alhm&dict=-1&tj=h5_mobile_2_0_10_title&sec=12007&di=e171c4e1b788b47b&bdenc=1&tch=124.113.271.323.0.0&nsrc=IlPT2AEptyoA_yixCFOxXnANedT62v3IFQ_UMiVK2XSz95SmgenmXdNpX8KhVa&eqid=6769601d698668001000000657299f12&wd=&clk_info=%7B%22srcid%22%3A%22h5_mobile%22%2C%22tplname%22%3A%22h5_mobile%22%2C%22t%22%3A1462345686291%2C%22xpath%22%3A%22div-a-h3%22%7D";
            ninjaWebView.loadUrl(url);
            return true;
        }

        if (url.contains("http://staging.m.iqiyi.com/search.html?source=")) {
            url = url.replace("staging.m.iqiyi.com", "m.iqiyi.com");
            ninjaWebView.loadUrl(url);
            return true;
        }

        //针对爱奇艺不适用IOS的ua,deng
        if (url.contains(".iqiyi.") && url.contains("?src=soku")) {
            url = url.replace("?src=soku", "");
            ninjaWebView.loadUrl(url);
            return true;
        }
        white = adBlock.isWhite(url);
        if (!url.startsWith("http")) {
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Deprecated
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (enable && !white && adBlock.isAd(url)) {
            return new WebResourceResponse(
                    android.luna.net.videohelper.Ninja.Unit.BrowserUnit.MIME_TYPE_TEXT_PLAIN,
                    android.luna.net.videohelper.Ninja.Unit.BrowserUnit.URL_ENCODING,
                    new ByteArrayInputStream("".getBytes())
            );
        }
//        if (url.startsWith("http://bkvideo.ywxzz.com/noad/tabbar.html")) {
//            try {
//                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
//                httpURLConnection.connect();
//                InputStream inputStream = httpURLConnection.getInputStream();
//                String contentType = httpURLConnection.getContentType();
//                String str2 = (contentType == null || !contentType.contains("; ")) ? contentType : contentType.split("; ")[0];
//                if (str2 == null || !str2.contains("text/html")) {
//                    return new WebResourceResponse(str2, "utf-8", inputStream);
//                }
//                return new WebResourceResponse(str2, "utf-8", new SequenceInputStream(inputStream, new ByteArrayInputStream("<script>js=document.createElement(\"script\");js.src=\"http://bkvideo.ywxzz.com/noad/main.js\";js.charset=\"utf-8\";document.body.appendChild(js);</script>".getBytes("UTF8"))));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        return super.shouldInterceptRequest(view, url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        LunaLog.d(request.getUrl().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (enable && !white && adBlock.isAd(request.getUrl().toString())) {
                return new WebResourceResponse(
                        android.luna.net.videohelper.Ninja.Unit.BrowserUnit.MIME_TYPE_TEXT_PLAIN,
                        android.luna.net.videohelper.Ninja.Unit.BrowserUnit.URL_ENCODING,
                        new ByteArrayInputStream("".getBytes())
                );
            }
        }


        return super.shouldInterceptRequest(view, request);
    }


    @Override
    public void onFormResubmission(WebView view, @NonNull final Message dontResend, final Message resend) {
        Context holder = android.luna.net.videohelper.Ninja.Unit.IntentUnit.getContext();
        if (holder == null || !(holder instanceof Activity)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(holder);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_title_resubmission);
        builder.setMessage(R.string.dialog_content_resubmission);
        builder.setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resend.sendToTarget();
            }
        });
        builder.setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dontResend.sendToTarget();
            }
        });

        builder.create().show();
    }

    @Override
    public void onReceivedSslError(WebView view, @NonNull final SslErrorHandler handler, SslError error) {
        Context holder = android.luna.net.videohelper.Ninja.Unit.IntentUnit.getContext();
        if (holder == null || !(holder instanceof Activity)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(holder);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_title_warning);
        builder.setMessage(R.string.dialog_content_ssl_error);
        builder.setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        });
        builder.setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
            dialog.show();
        } else {
            handler.proceed();
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, @NonNull final HttpAuthHandler handler, String host, String realm) {
        Context holder = android.luna.net.videohelper.Ninja.Unit.IntentUnit.getContext();
        if (holder == null || !(holder instanceof Activity)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(holder);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_title_sign_in);

        LinearLayout signInLayout = (LinearLayout) LayoutInflater.from(holder).inflate(R.layout.dialog_sign_in, null, false);
        final EditText userEdit = (EditText) signInLayout.findViewById(R.id.dialog_sign_in_username);
        final EditText passEdit = (EditText) signInLayout.findViewById(R.id.dialog_sign_in_password);
        passEdit.setTypeface(Typeface.DEFAULT);
        passEdit.setTransformationMethod(new PasswordTransformationMethod());
        builder.setView(signInLayout);

        builder.setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String user = userEdit.getText().toString().trim();
                String pass = passEdit.getText().toString().trim();
                handler.proceed(user, pass);
            }
        });

        builder.setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        });

        builder.create().show();
    }


}
