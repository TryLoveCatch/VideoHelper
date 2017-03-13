package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.UrilUtil;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by bintou on 16/1/12.
 */
public class QQParser {

    public static final String DEFINITION_SUPER = "1300";
    public static final String DEFINITION_HIGH = "1000";
    public static final String DEFINITION_NORMAL = "350";
    public static final String DEFINITION_LOW = "mp4";

    private String mOriUrl;
    private Context mContext;
    private String mDefinition;
    private int mDefInt;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_LOW};

    private final String API_URL = "http://vv.video.qq.com/gethls?otype=json&vid=[vid]";

    private String mVid = "";

    private VideoCatchManager mVideoCatchManager;

    public QQParser(Context context, String url, int definition) {
        mOriUrl = url;
        mContext = context;
        mDefinition = defArray[definition];
        mDefInt = definition;
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        try {
            int start, end;
            if (url.contains("vid=")) {
                mVid = UrilUtil.getQueryString(url, "vid");
            } else if (url.contains("vids=")) {
                mVid = UrilUtil.getQueryString(url, "vids");
            } else {
                start = url.lastIndexOf("/") + 1;
                end = url.indexOf(".html");
                mVid = url.substring(start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message run() {
        if (!StringUtils.isBlank(mVid)) {
            String url = API_URL.replace("[vid]", mVid);
            LunaLog.e(url);
            String result = mVideoCatchManager.catchUrlResult(url);
            result = result.replace("QZOutputJson=", "");
            if (!StringUtils.isBlank(result)) {
                try {
                    JSONObject resultJo = JSONUtils.toJsonObject(result);
                    JSONObject body = JSONUtils.getJSONObject(resultJo, "vd", null);
                    if (body != null) {
                        JSONArray vi = JSONUtils.getJsonArray(body, "vi", null);
                        if (vi != null && vi.length() > 0) {
                            JSONObject targetJo = vi.optJSONObject(0);
                            if (targetJo != null) {
                                String targetUrl = targetJo.optString("url");
                                if (!StringUtils.isBlank(targetUrl)) {
                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = targetUrl;
                                    return msg;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        ShuoshuParser shuoshuParser = new ShuoshuParser(mContext, mOriUrl, mDefInt, GlobalConstant.SITE_QQ);
        return shuoshuParser.run();
    }


}
