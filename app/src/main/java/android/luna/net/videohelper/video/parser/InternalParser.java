package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.os.Bundle;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONObject;

/**
 * Created by bintou on 16/1/12.
 */
public class InternalParser {

    public static final String DEFINITION_SUPER = "super";
    public static final String DEFINITION_HIGH = "high";
    public static final String DEFINITION_NORMAL = "normal";

    private Context mContext;
    private String mDefinition;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER};

    private final String API_URL = "http://api.vipkpsq.com/v1/parse/getVideo?vid=[vid]&source=[source]";

    private String mVid = "";
    private String mSite = "";

    private VideoCatchManager mVideoCatchManager;

    private String[] defArrayVideoJj = {"high", "normal", "super"};
    private JSONObject videoJjJson = new JSONObject();

    public InternalParser(Context context, String vid, String site, int definition) {
        mContext = context;
        mDefinition = defArray[definition];
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mVid = vid;
        mSite = site;
        mVid = StringUtils.nullStrToEmpty(mVid);
    }

    public Message run() {
        try {
            String url = API_URL.replace("[vid]", mVid).replace("[source]", mSite);
            LunaLog.e(url);

            videoJjJson.putOpt("site", mSite);

            String result = mVideoCatchManager.catchUrlResult(url);
            if (!StringUtils.isBlank(result)) {
                JSONObject resultJo = JSONUtils.toJsonObject(result);
                JSONObject body = JSONUtils.getJSONObject(resultJo, "data", null);
                if (body != null) {
                    JSONObject segs = new JSONObject();
                    //优先获取目标清晰度，否则按清晰度优先级顺序获取
                    String targetUrl = body.optString(mDefinition);
                    for (int i = 0; i < defArray.length; i++) {
                        String def = defArray[i];
                        String videoUrl = body.optString(def);

                        if (!StringUtils.isBlank(videoUrl)) {
                            segs.putOpt(defArrayVideoJj[i], videoUrl);
                            if (StringUtils.isBlank(targetUrl)) {
                                targetUrl = videoUrl;
                            }
                        }
                    }
                    videoJjJson.putOpt("segs", segs);

                    if (!StringUtils.isBlank(targetUrl)) {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("segs", videoJjJson.toString());
                        msg.setData(bundle);
                        msg.obj = targetUrl;
                        return msg;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Message msg = new Message();
        msg.what = 1;
        msg.obj = "";
        return msg;
    }
}


