package android.luna.net.videohelper.Ninja.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.luna.net.videohelper.Ninja.Database.RecordAction;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelper.activity.VideoActivity;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelptools.R;
import android.net.MailTo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;
import net.luna.common.util.ToastUtils;

import java.net.URISyntaxException;

public class NinjaWebView extends WebView implements android.luna.net.videohelper.Ninja.Browser.AlbumController {
    private static final float[] NEGATIVE_COLOR = {
            -1.0f, 0, 0, 0, 255, // Red
            0, -1.0f, 0, 0, 255, // Green
            0, 0, -1.0f, 0, 255, // Blue
            0, 0, 0, 1.0f, 0     // Alpha
    };

    private Context context;
    private int flag = android.luna.net.videohelper.Ninja.Unit.BrowserUnit.FLAG_NINJA;
    private int dimen144dp;
    private int dimen108dp;
    private int animTime;

    private android.luna.net.videohelper.Ninja.View.Album album;
    private android.luna.net.videohelper.Ninja.Browser.NinjaWebViewClient webViewClient;
    private android.luna.net.videohelper.Ninja.Browser.NinjaWebChromeClient webChromeClient;
    private android.luna.net.videohelper.Ninja.Browser.NinjaDownloadListener downloadListener;
    private android.luna.net.videohelper.Ninja.Browser.NinjaClickHandler clickHandler;
    private GestureDetector gestureDetector;

    private android.luna.net.videohelper.Ninja.Browser.AdBlock adBlock;

    public android.luna.net.videohelper.Ninja.Browser.AdBlock getAdBlock() {
        return adBlock;
    }

    private boolean foreground;

    public boolean isForeground() {
        return foreground;
    }

    private String userAgentOriginal;

    public String getUserAgentOriginal() {
        return userAgentOriginal;
    }

    public String webContent;

    private android.luna.net.videohelper.Ninja.Browser.BrowserController browserController = null;

    public android.luna.net.videohelper.Ninja.Browser.BrowserController getBrowserController() {
        return browserController;
    }

    public void setBrowserController(android.luna.net.videohelper.Ninja.Browser.BrowserController browserController) {
        this.browserController = browserController;
        this.album.setBrowserController(browserController);
    }

    public NinjaWebView(Context context, Handler handler) {
        super(context); // Cannot create a dialog, the WebView context is not an Activity

        this.context = context;
        this.dimen144dp = getResources().getDimensionPixelSize(R.dimen.layout_width_144dp);
        this.dimen108dp = getResources().getDimensionPixelSize(R.dimen.layout_height_108dp);
        this.animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        this.foreground = false;

        this.adBlock = new android.luna.net.videohelper.Ninja.Browser.AdBlock(this.context);
        this.album = new android.luna.net.videohelper.Ninja.View.Album(this.context, this, this.browserController);
        this.webViewClient = new android.luna.net.videohelper.Ninja.Browser.NinjaWebViewClient(this, handler);
        this.webChromeClient = new android.luna.net.videohelper.Ninja.Browser.NinjaWebChromeClient(this, handler);
        this.downloadListener = new android.luna.net.videohelper.Ninja.Browser.NinjaDownloadListener(this.context);
        this.clickHandler = new android.luna.net.videohelper.Ninja.Browser.NinjaClickHandler(this);
        this.gestureDetector = new GestureDetector(context, new android.luna.net.videohelper.Ninja.Browser.NinjaGestureListener(this));

        initWebView();
        initWebSettings();
        initPreferences();
        initAlbum();
    }

    private synchronized void initWebView() {
        setAlwaysDrawnWithCacheEnabled(true);
        setAnimationCacheEnabled(true);
        setDrawingCacheBackgroundColor(0x00000000);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);
        setSaveEnabled(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(null);
            getRootView().setBackground(null);
            setBackgroundColor(context.getResources().getColor(R.color.white));
        } else {
            setBackgroundDrawable(null);
        }

        setFocusable(true);
        setFocusableInTouchMode(true);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setScrollbarFadingEnabled(true);

        setWebViewClient(webViewClient);
        setWebChromeClient(webChromeClient);
        setDownloadListener(downloadListener);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    private synchronized void initWebSettings() {
        WebSettings webSettings = getSettings();
        userAgentOriginal = webSettings.getUserAgentString();

        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(context.getCacheDir().toString());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationDatabasePath(context.getFilesDir().toString());

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);


        webSettings.setDefaultTextEncodingName(BrowserUnit.URL_ENCODING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webSettings.setLoadsImagesAutomatically(true);
        } else {
            webSettings.setLoadsImagesAutomatically(false);
        }
    }

    public synchronized void initPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        WebSettings webSettings = getSettings();

        webSettings.setLoadWithOverviewMode(true);
        webSettings.setTextZoom(100);
        webSettings.setUseWideViewPort(true);

        webSettings.setBlockNetworkImage(!sp.getBoolean(context.getString(R.string.sp_images), true));
        webSettings.setJavaScriptEnabled(sp.getBoolean(context.getString(R.string.sp_javascript), true));
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setGeolocationEnabled(sp.getBoolean(context.getString(R.string.sp_location), true));
        webSettings.setSupportMultipleWindows(sp.getBoolean(context.getString(R.string.sp_multiple_windows), false));
        webSettings.setSaveFormData(sp.getBoolean(context.getString(R.string.sp_passwords), true));


        boolean textReflow = sp.getBoolean(context.getString(R.string.sp_text_reflow), true);
        if (textReflow) {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
                } catch (Exception e) {
                }
            }
        } else {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        }

        //4.4以下可能有不适应
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            userAgentOriginal = webSettings.getDefaultUserAgent(context);
//            webSettings.setUserAgentString(userAgentOriginal);
//        } else {
//            int userAgent = Integer.valueOf(sp.getString(context.getString(R.string.sp_user_agent), "0"));
//            if (userAgent == 1) {
//                webSettings.setUserAgentString(android.luna.net.videohelper.Ninja.Unit.BrowserUnit.UA_DESKTOP_WINDOWS);
//            } else if (userAgent == 2) {
//                webSettings.setUserAgentString(sp.getString(context.getString(R.string.sp_user_agent_custom), userAgentOriginal));
//            } else {
//                webSettings.setUserAgentString(userAgentOriginal);
//            }
//        }

        //导致显示问题，注销
//        int mode = Integer.valueOf(sp.getString(context.getString(R.string.sp_rendering), "0"));
//        initRendering(mode);

        //开启有可能会有问题，注销
//        setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        webViewClient.enableAdBlock(sp.getBoolean(context.getString(R.string.sp_ad_block), true));
    }

    private synchronized void initAlbum() {
        album.setAlbumCover(null);
        album.setAlbumTitle(context.getString(R.string.album_untitled));
        album.setBrowserController(browserController);
    }

    private void initRendering(int mode) {
        Paint paint = new Paint();

        switch (mode) {
            case 0: { // Default
                paint.setColorFilter(null);
                break;
            }
            case 1: { // Grayscale
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                paint.setColorFilter(filter);
                break;
            }
            case 2: { // Inverted
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(NEGATIVE_COLOR);
                paint.setColorFilter(filter);
                break;
            }
            case 3: { // Inverted grayscale
                ColorMatrix matrix = new ColorMatrix();
                matrix.set(NEGATIVE_COLOR);

                ColorMatrix gcm = new ColorMatrix();
                gcm.setSaturation(0);

                ColorMatrix concat = new ColorMatrix();
                concat.setConcat(matrix, gcm);

                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(concat);
                paint.setColorFilter(filter);

                break;
            }
            default: {
                paint.setColorFilter(null);
                break;
            }
        }

        // maybe sometime LAYER_TYPE_NONE would better?
        setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    @Override
    public synchronized void loadUrl(String url) {

        if (url == null || url.trim().isEmpty()) {
            ToastUtils.show(context, R.string.toast_load_error);
            return;
        }

        //加载js文件
        if (url.startsWith(BrowserUnit.URL_SCHEME_JAVASCRIPT)) {
            super.loadUrl(url);
            return;
        }

        url = android.luna.net.videohelper.Ninja.Unit.BrowserUnit.queryWrapper(context, url.trim());
        if (url.startsWith(android.luna.net.videohelper.Ninja.Unit.BrowserUnit.URL_SCHEME_MAIL_TO)) {
            Intent intent = android.luna.net.videohelper.Ninja.Unit.IntentUnit.getEmailIntent(MailTo.parse(url));
            context.startActivity(intent);
            reload();

            return;
        } else if (url.startsWith(android.luna.net.videohelper.Ninja.Unit.BrowserUnit.URL_SCHEME_INTENT)) {
            Intent intent;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                context.startActivity(intent);
            } catch (URISyntaxException u) {
            }

            return;
        }

        url = urlUaFilter(this, url);


        webViewClient.updateWhite(adBlock.isWhite(url));
        super.loadUrl(url);
        if (browserController != null && foreground) {
            browserController.updateBookmarks();
        }
    }

    public String urlUaFilter(WebView webView, String url) {
        if (url.contains(".iqiyi.")) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                webView.getSettings().setUserAgentString(getSettings().getDefaultUserAgent(context));
//            } else {
//                webView.getSettings().setUserAgentString(BrowserUnit.UA_DESKTOP_AOS);
//            }
            webView.getSettings().setUserAgentString(getUserAgentOriginal());
//            webView.getSettings().setUserAgentString(BrowserUnit.UA_DESKTOP_IPAD);
        } else if (url.contains(".soku.")) {
            webView.getSettings().setUserAgentString(BrowserUnit.UA_DESKTOP_IOS);
        }
        return url;
    }

    @Override
    public void reload() {
        webViewClient.updateWhite(adBlock.isWhite(getUrl()));
        super.reload();
    }

    @Override
    public int getFlag() {
        return flag;
    }

    @Override
    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public View getAlbumView() {
        return album.getAlbumView();
    }

    @Override
    public void setAlbumCover(Bitmap bitmap) {
        album.setAlbumCover(bitmap);
    }

    @Override
    public String getAlbumTitle() {
        return album.getAlbumTitle();
    }

    @Override
    public void setAlbumTitle(String title) {
        album.setAlbumTitle(title);
    }

    @Override
    public synchronized void activate() {
        requestFocus();
        foreground = true;
        album.activate();
    }

    @Override
    public synchronized void deactivate() {
        clearFocus();
        foreground = false;
        album.deactivate();
    }

    public synchronized void update(int progress) {
        if (foreground) {
            browserController.updateProgress(progress);
        }

//        setAlbumCover(android.luna.net.videohelper.Ninja.Unit.ViewUnit.capture(this, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
        if (isLoadFinish()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (sp.getBoolean(context.getString(R.string.sp_scroll_bar), true)) {
                setHorizontalScrollBarEnabled(true);
                setVerticalScrollBarEnabled(true);
            } else {
                setHorizontalScrollBarEnabled(false);
                setVerticalScrollBarEnabled(false);
            }
            setScrollbarFadingEnabled(true);

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    setAlbumCover(android.luna.net.videohelper.Ninja.Unit.ViewUnit.capture(NinjaWebView.this, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
//                }
//            }, animTime);
        }
    }

    public synchronized void updateHistory() {
        if (prepareRecord()) {
            android.luna.net.videohelper.Ninja.Database.RecordAction action = RecordAction.getInstance(context);
            action.open(true);
            action.addHistory(new android.luna.net.videohelper.Ninja.Database.Record(getTitle(), getUrl(), System.currentTimeMillis()));
            action.close();
            browserController.updateAutoComplete();
        }
    }

    public synchronized void update(String title, String url) {
        album.setAlbumTitle(title);
        if (foreground) {
            browserController.updateBookmarks();
            browserController.updateInputBox(url);
        }
    }

    public synchronized void pause() {
        onPause();
        pauseTimers();
    }

    public synchronized void resume() {
        onResume();
        resumeTimers();
    }

    @Override
    public synchronized void destroy() {
        try {
            stopLoading();
            onPause();
            clearHistory();
            setVisibility(GONE);
            removeAllViews();
            destroyDrawingCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.destroy();
    }

    public boolean isLoadFinish() {
        return getProgress() >= android.luna.net.videohelper.Ninja.Unit.BrowserUnit.PROGRESS_MAX;
    }

    public void onLongPress() {
        if (clickHandler != null) {
            Message click = clickHandler.obtainMessage();
            if (click != null) {
                click.setTarget(clickHandler);
                requestFocusNodeHref(click);
            }
        }
    }

    private boolean prepareRecord() {
        String title = getTitle();
        String url = getUrl();

        if (title == null
                || title.isEmpty()
                || url == null
                || url.isEmpty()
                || url.startsWith(android.luna.net.videohelper.Ninja.Unit.BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(android.luna.net.videohelper.Ninja.Unit.BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(android.luna.net.videohelper.Ninja.Unit.BrowserUnit.URL_SCHEME_INTENT)) {
            return false;
        }
        return true;
    }

    public void loadCheckVideoJs(String site) {
        if (!StringUtils.isBlank(site)) {
            if (GlobalConstant.SITE_YOUKU.equals(site)) {
                loadUrl("javascript:(function(){l=document.getElementsByClassName('yk-footer');if (l[0] != null )l[0].style.display='none';l=document.getElementById('app-download');if (l != null )l.style.display='none';l=document.getElementById('sideBar');if (l != null )l.style.display='none';l=document.getElementById('sideBar');if (l != null )l.style.display='none';})()");
                loadUrl("javascript:(function(){var obj = document.getElementById(\"x-video-button\"); obj.onclick=function()      {          window._videoJSface.sendPlayMessage('youku');      }  })()");
                loadUrl("javascript:(function(){var obj = document.getElementById(\"yk-player\"); obj.onclick=function()      {          window._videoJSface.sendPlayMessage('youku');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('x-video-play-ico')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('youku');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('x-trigger')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('youku');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('x-video-poster')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('youku');      }  })()");
                loadUrl("javascript:(function(){var obj = document.getElementById(\"x-player\"); obj.onclick=function()      {          window._videoJSface.sendPlayMessage('youku');      }  })()");
            } else if (GlobalConstant.SITE_IQIYI.equals(site)) {
                loadUrl("javascript:(function(){l=document.getElementsByClassName('c-videoplay')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('qiyi');      }  })()");
                loadUrl("javascript:(function(){var obj = document.getElementById(\"player-trigger\"); obj.onclick=function()      {          window._videoJSface.sendPlayMessage('qiyi');      }  })()");
                loadUrl("javascript:(function(){var obj = document.getElementById(\"player-bigBtn\"); obj.onclick=function()      {          window._videoJSface.sendPlayMessage('qiyi');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('mod-video_wrap')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('qiyi');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('videoCon')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('qiyi');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('mod-video-player')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('qiyi');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('player-poster')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('qiyi');      }  })()");
            } else if (GlobalConstant.SITE_QQ.equals(site)) {
                loadUrl("javascript:(function(){var obj = document.getElementById(\"tenvideo_video_player_0\"); obj.onclick=function()      {          window._videoJSface.sendPlayMessage('qq');      }  })()");
                loadUrl("javascript:(function(){var obj = document.getElementById(\"mod_tenvideo_video_player_0\"); obj.onclick=function()      {          window._videoJSface.sendPlayMessage('qq');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('tvp_video')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('qq');      }  })()");
            } else if (GlobalConstant.SITE_MANGO.equals(site)) {
//        ninjaWebView.loadUrl("javascript:(function(){l=document.getElementById('appBanner');if (l != null)l.style.display='none';l=document.getElementById('appADBar');if (l != null)l.style.display='none';l=document.getElementsByClassName('mg-app')[0];if (l != null)l.style.display='none';})()");
                loadUrl("javascript:(function(){var obj = document.getElementById(\"player\"); obj.onclick=function()      {          window._videoJSface.sendPlayMessage('mango');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('video0-area')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('mango');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('video-poster')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('mango');      }  })()");
                loadUrl("javascript:(function(){l=document.getElementsByClassName('mg-video')[0];if (l != null)l.onclick=function()      {          window._videoJSface.sendPlayMessage('mango');      }  })()");
            } else if (GlobalConstant.SITE_BAIKAN.equals(site)) {
                loadUrl("javascript:window._videoJSface.showSource('<head>'+"
                        + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        }
    }

    public void loadJsWhenFinish(String site) {
        if (GlobalConstant.SITE_BAIKAN.equals(site)) {
            if (!loginOnce) {
                try {
                    CookieManager cookieManager = CookieManager.getInstance();
                    if (cookieManager != null) {
                        String cookieStr = cookieManager.getCookie(getUrl());
                        LunaLog.d(cookieStr);
//                if (cookieStr == null || !cookieStr.contains("token")) {
                        loadUrl("javascript:$(\".login input[name=email]\").val(\"hjb1991@126.com\");$(\".login input[name=passwd]\").val(\"123456789\");login();");
                        loginOnce = true;
//                }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean loginOnce;

    private Handler webHandler;

    public void addCheckFileJsInterface(Handler handler) {
        webHandler = handler;
        addJavascriptInterface(new JsInterface(), "_videoJSface");
        addJavascriptInterface(new JsInterface2(), "Android");
    }

    public class JsInterface {
        @JavascriptInterface
        public void sendPlayMessage(final String site) {
            LunaLog.d("site video click: " + site);
            if (webHandler != null) {
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String webUrl = getUrl();
                        String title = getTitle();
                        Intent intent = new Intent(context, VideoActivity.class);
                        intent.putExtra("webUrl", webUrl);
                        intent.putExtra("url", webUrl);
                        intent.putExtra("name", title);
                        intent.putExtra("site", site);
                        intent.putExtra("fromWebview", true);
                        ((Activity) context).startActivityForResult(intent, GlobalConstant.INDEX_VIDEO_RETURN);
                        reload();
                    }
                });

            }
        }

        @JavascriptInterface
        public void showSource(String html) {
            webContent = html;
        }
    }

    public class JsInterface2 {
        @JavascriptInterface
        public void passValid(String str) {
            loadUrl(str);
        }
    }

    public String getWebContent() {
        return webContent;
    }

    //防止webview在后台播放
    private boolean is_gone = false;

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        try {
            if (visibility == View.GONE) {
                try {
                    WebView.class.getMethod("onPause").invoke(this);// stop flash
                } catch (Exception e) {
                }
                this.pauseTimers();
                this.is_gone = true;
            } else if (visibility == View.VISIBLE) {
                try {
                    WebView.class.getMethod("onResume").invoke(this);// resume flash
                } catch (Exception e) {
                }
                this.resumeTimers();
                this.is_gone = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    protected void onDetachedFromWindow() {
//        LunaLog.d("webview wocao");
//        if (this.is_gone) {
//            try {
//                this.destroy();
//            } catch (Exception e) {
//            }
//        }
//    }

    public Context getActivityContext() {
        return context;
    }


}
