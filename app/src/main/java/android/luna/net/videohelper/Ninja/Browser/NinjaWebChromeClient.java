package android.luna.net.videohelper.Ninja.Browser;

import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class NinjaWebChromeClient extends WebChromeClient {
    private android.luna.net.videohelper.Ninja.View.NinjaWebView ninjaWebView;
    private Handler controlHandler;

    public NinjaWebChromeClient(android.luna.net.videohelper.Ninja.View.NinjaWebView ninjaWebView, Handler handler) {
        super();
        this.ninjaWebView = ninjaWebView;
        controlHandler = handler;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        ninjaWebView.getBrowserController().onCreateView(view, resultMsg);
        return isUserGesture;
    }

    @Override
    public void onCloseWindow(WebView view) {
        super.onCloseWindow(view);
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        super.onProgressChanged(view, progress);
        ninjaWebView.update(progress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (controlHandler != null) {
            Message msg = new Message();
            msg.what = BrowserUnit.MESSAGE_PAGE_RECEIVE_TITLE;
            msg.obj = view.getUrl();
            controlHandler.sendMessage(msg);
        }
        ninjaWebView.update(title, view.getUrl());
        ninjaWebView.updateHistory();
    }

    @Deprecated
    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        ninjaWebView.getBrowserController().onShowCustomView(view, requestedOrientation, callback);
        super.onShowCustomView(view, requestedOrientation, callback);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        ninjaWebView.getBrowserController().onShowCustomView(view, callback);
        super.onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        ninjaWebView.getBrowserController().onHideCustomView();
        super.onHideCustomView();
    }

    /* For 4.1 to 4.4 */
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        ninjaWebView.getBrowserController().openFileChooser(uploadMsg);
    }

    /* For 4.1 to 4.4 */
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        ninjaWebView.getBrowserController().openFileChooser(uploadMsg);
    }

    /* For 4.1 to 4.4 */
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        ninjaWebView.getBrowserController().openFileChooser(uploadMsg);
    }

//    @Override
//    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//        ninjaWebView.getBrowserController().showFileChooser(filePathCallback, fileChooserParams);
//        return true;
//    }

    /**
     * TODO: ?support this method
     *
     * @param origin
     * @param callback
     * @link http://developer.android.com/reference/android/webkit/WebChromeClient.html#onGeolocationPermissionsShowPrompt%28java.lang.String,%20android.webkit.GeolocationPermissions.Callback%29
     */
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    @Override
    public View getVideoLoadingProgressView() {
        FrameLayout frameLayout = new FrameLayout(ninjaWebView.getActivityContext());
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        return frameLayout;
    }



}
