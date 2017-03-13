package android.luna.net.videohelper.widget;

import android.content.Context;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelptools.R;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.view.widget.CustomerDialog;
import net.luna.common.wechat.ShareModel;
import net.luna.common.wechat.WeChatSDKManager;

/**
 * Created by bintou on 16/2/2.
 */
public class ShareDialog implements View.OnClickListener {
    CustomerDialog shareDialog;

    Context context;
    String name = "自发";

    private String url;

    public void setUrl(String url) {
        this.url = url;
    }


    public ShareDialog(Context context) {
        this.context = context;
    }

    public boolean needShowShareDialog(String name) {
        this.name = name;
        int times = PreferencesUtils.getInt(context, GlobalConstant.SP_CAPTURE_TIMES, 0);
        if (times > 3) {
            boolean hasShare = PreferencesUtils.getBoolean(context, GlobalConstant.SP_HAS_SHARE, false);
            if (!hasShare) {
                if (shareDialog == null) {
                    View view = LayoutInflater.from(context).inflate(R.layout.layout_share, null);
                    shareDialog = new CustomerDialog(context, view);
                    view.findViewById(R.id.share_close).setOnClickListener(this);
                    view.findViewById(R.id.btn_share_wechat).setOnClickListener(this);
                    view.findViewById(R.id.btn_share_timeline).setOnClickListener(this);
                }
                if (!shareDialog.isShowing()) {
                    shareDialog.show();
                }
                return true;
            }
        }
        return false;
    }

    public void showShareDialog(String content) {

        if (shareDialog == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_share, null);
            shareDialog = new CustomerDialog(context, view);
            TextView tv = (TextView) view.findViewById(R.id.share_description);
            if (StringUtils.isBlank(content)) {
                tv.setText(context.getResources().getText(R.string.share_description_2));
            } else {
                tv.setText(content);
                name = content;
            }
            view.findViewById(R.id.share_close).setOnClickListener(this);
            view.findViewById(R.id.btn_share_wechat).setOnClickListener(this);
            view.findViewById(R.id.btn_share_timeline).setOnClickListener(this);
        }
        if (!shareDialog.isShowing()) {
            shareDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_close:
                if (shareDialog != null && shareDialog.isShowing()) {
                    shareDialog.dismiss();
                }
                break;
            case R.id.btn_share_wechat:
                share(false);
                break;
            case R.id.btn_share_timeline:
                share(true);
                break;
        }
    }

    private void share(boolean isTimeline) {
        ShareModel model = new ShareModel();
        if (!StringUtils.isBlank(url)) {
            model.url = url;
        } else {
            model.url = "http://m.jiasugou.tv/";
        }
        model.iconId = R.mipmap.appicon_wechat;
        if (StringUtils.isBlank(name)) {
            model.title = "自动屏蔽视频广告，最新大片免费看，就用视频加速狗";
        } else {
            model.title = name;
        }
        model.isTimeLine = isTimeline;
        WeChatSDKManager.getInstanct(context).shareToTimeLine(model);
        UploadEventRecord.recordEvent(context, GlobalConstant.SHARE_EVENT, GlobalConstant.SHARE_CONTENT, name);
    }

}
