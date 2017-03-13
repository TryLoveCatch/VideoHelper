package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Message;

import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.UrilUtil;

/**
 * Created by bintou on 16/7/4.
 */
public class BaikanParser {

    private String api = "http://sproxy.meiyouad.com/proxy.php?vid=";

    private Context mContext;

    private String mWebUrl;
    private String mContent;

    public BaikanParser(Context context, String webUrl, String content) {
        this.mContext = context;
        this.mWebUrl = webUrl;
        this.mContent = content;
        api = PreferencesUtils.getString(context, GlobalConstant.SP_BAIKAN_API, api);

    }

    public Message run() {

        Message msg = new Message();
        msg.what = 1;
        if (mWebUrl != null) {
            String id = UrilUtil.getVal(mContent, "a:'([a-zA-Z0-9]+)'");
            if (id != null && !id.endsWith("2")) {
                id = id + "2";
            }
            String targetUrl = api + id;
            msg.obj = targetUrl;
        }
        return msg;
    }


}
