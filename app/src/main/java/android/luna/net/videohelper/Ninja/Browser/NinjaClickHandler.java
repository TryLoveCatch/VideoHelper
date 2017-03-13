package android.luna.net.videohelper.Ninja.Browser;

import android.os.Handler;
import android.os.Message;

public class NinjaClickHandler extends Handler {
    private android.luna.net.videohelper.Ninja.View.NinjaWebView webView;

    public NinjaClickHandler(android.luna.net.videohelper.Ninja.View.NinjaWebView webView) {
        super();
        this.webView = webView;
    }

    @Override
    public void handleMessage(Message message) {
        super.handleMessage(message);
        webView.getBrowserController().onLongPress(message.getData().getString("url"));
    }
}
