package android.luna.net.videohelper.Ninja.Browser;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class NinjaGestureListener extends GestureDetector.SimpleOnGestureListener {
    private android.luna.net.videohelper.Ninja.View.NinjaWebView webView;
    private boolean longPress = true;

    public NinjaGestureListener(android.luna.net.videohelper.Ninja.View.NinjaWebView webView) {
        super();
        this.webView = webView;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (longPress) {
            webView.onLongPress();
        }
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        longPress = false;
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        longPress = true;
    }
}
