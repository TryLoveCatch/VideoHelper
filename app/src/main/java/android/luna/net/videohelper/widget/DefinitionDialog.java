package android.luna.net.videohelper.widget;

import android.content.Context;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.video.parser.UrlParseHelper;
import android.luna.net.videohelptools.R;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import net.luna.common.util.ThreadUtils;
import net.luna.common.view.widget.CustomerDialog;

/**
 * Created by bintou on 16/3/10.
 */
public class DefinitionDialog implements View.OnClickListener {

    public interface OnDefinitionSelectedListener {
        public void onSelected(int def);
    }

    CustomerDialog shareDialog;
    Context context;

    private String url;
    private String site;
    private String vid;
    private String name;
    private int definition;
    private Handler handler;
    private OnDefinitionSelectedListener listener;

    public DefinitionDialog(Context context) {
        this.context = context;
    }

    public void showDialog(String url, String site, String vid, String name, Handler handler) {
        this.url = url;
        this.site = site;
        this.vid = vid;
        this.name = name;
        this.handler = handler;
        showDialog();
    }


    public void showDialog(OnDefinitionSelectedListener listener) {
        this.listener = listener;
        showDialog();
    }

    private void showDialog() {
        if (shareDialog == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_definition, null);
            shareDialog = new CustomerDialog(context, view);
            view.findViewById(R.id.super_defini).setOnClickListener(this);
            view.findViewById(R.id.high_defini).setOnClickListener(this);
            view.findViewById(R.id.nor_defini).setOnClickListener(this);
        }
        if (!shareDialog.isShowing()) {
            shareDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.super_defini:
                definition = GlobalConstant.DEF_SUPER;
                break;
            case R.id.high_defini:
                definition = GlobalConstant.DEF_HIGH;
                break;
            case R.id.nor_defini:
                definition = GlobalConstant.DEF_NORMAL;
                break;
        }
        if (listener == null) {
            UrlParseHelper helper = new UrlParseHelper(context, url, site, vid, name, definition, handler, true);
            ThreadUtils.execute(helper);
            shareDialog.dismiss();
        } else {
            listener.onSelected(definition);
            shareDialog.dismiss();
        }
    }

    public void dismiss() {
        if (shareDialog != null && shareDialog.isShowing()) {
            shareDialog.dismiss();
        }
    }
}
