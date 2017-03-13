package android.luna.net.videohelper.Ninja.Task;

import android.app.ProgressDialog;
import android.content.Context;
import android.luna.net.videohelptools.R;
import android.os.AsyncTask;

import net.luna.common.util.ToastUtils;

public class ExportWhitelistTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private ProgressDialog dialog;
    private String path;

    public ExportWhitelistTask(Context context) {
        this.context = context;
        this.dialog = null;
        this.path = null;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.toast_wait_a_minute));
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        path = android.luna.net.videohelper.Ninja.Unit.BrowserUnit.exportWhitelist(context);

        if (isCancelled()) {
            return false;
        }
        return path != null && !path.isEmpty();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.hide();
        dialog.dismiss();

        if (result) {
            ToastUtils.show(context, context.getString(R.string.toast_export_whitelist_successful) + path);
        } else {
            ToastUtils.show(context, R.string.toast_export_whitelist_failed);
        }
    }
}
