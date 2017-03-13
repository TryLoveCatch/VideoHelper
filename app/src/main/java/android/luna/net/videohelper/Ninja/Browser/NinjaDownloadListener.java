package android.luna.net.videohelper.Ninja.Browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.luna.net.videohelptools.R;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

public class NinjaDownloadListener implements DownloadListener {
    private Context context;

    public NinjaDownloadListener(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
        final Context holder = android.luna.net.videohelper.Ninja.Unit.IntentUnit.getContext();
        if (holder == null || !(holder instanceof Activity)) {
            android.luna.net.videohelper.Ninja.Unit.BrowserUnit.download(context, url, contentDisposition, mimeType);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(holder);
        builder.setCancelable(false);

        builder.setTitle(R.string.dialog_title_download);
        builder.setMessage(URLUtil.guessFileName(url, contentDisposition, mimeType));

        builder.setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.luna.net.videohelper.Ninja.Unit.BrowserUnit.download(holder, url, contentDisposition, mimeType);
            }
        });

        builder.setNegativeButton(R.string.dialog_button_negative, null);
        builder.create().show();
    }
}
