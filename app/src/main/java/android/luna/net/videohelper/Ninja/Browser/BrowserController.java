package android.luna.net.videohelper.Ninja.Browser;

import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public interface BrowserController {
    void updateAutoComplete();

    void updateBookmarks();

    void updateInputBox(String query);

    void updateProgress(int progress);

    void showAlbum(android.luna.net.videohelper.Ninja.Browser.AlbumController albumController, boolean anim, boolean expand, boolean capture);

    void removeAlbum(android.luna.net.videohelper.Ninja.Browser.AlbumController albumController);

    void openFileChooser(ValueCallback<Uri> uploadMsg);

//    void showFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams);

    void onCreateView(WebView view, Message resultMsg);

    boolean onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback);

    boolean onShowCustomView(View view, WebChromeClient.CustomViewCallback callback);

    boolean onHideCustomView();

    void onLongPress(String url);
}
