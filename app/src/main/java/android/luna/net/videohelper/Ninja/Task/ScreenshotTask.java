package android.luna.net.videohelper.Ninja.Task;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.luna.net.videohelptools.R;
import android.os.AsyncTask;

import net.luna.common.util.ToastUtils;

public class ScreenshotTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private ProgressDialog dialog;
    private android.luna.net.videohelper.Ninja.View.NinjaWebView webView;
    private int windowWidth;
    private float contentHeight;
    private String title;
    private String path;

    public ScreenshotTask(Context context, android.luna.net.videohelper.Ninja.View.NinjaWebView webView) {
        this.context = context;
        this.dialog = null;
        this.webView = webView;
        this.windowWidth = 0;
        this.contentHeight = 0f;
        this.title = null;
        this.path = null;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.toast_wait_a_minute));
        dialog.show();

        windowWidth = android.luna.net.videohelper.Ninja.Unit.ViewUnit.getWindowWidth(context);
        contentHeight = webView.getContentHeight() * android.luna.net.videohelper.Ninja.Unit.ViewUnit.getDensity(context);
        title = webView.getTitle();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Bitmap bitmap = android.luna.net.videohelper.Ninja.Unit.ViewUnit.capture(webView, windowWidth, contentHeight, false, Bitmap.Config.ARGB_8888);
            path = android.luna.net.videohelper.Ninja.Unit.BrowserUnit.screenshot(context, bitmap, title);
        } catch (Exception e) {
            path = null;
        }
        return path != null && !path.isEmpty();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.hide();
        dialog.dismiss();

        if (result) {
            ToastUtils.show(context, context.getString(R.string.toast_screenshot_successful) + path);
        } else {
            ToastUtils.show(context, R.string.toast_screenshot_failed);
        }
    }
}
