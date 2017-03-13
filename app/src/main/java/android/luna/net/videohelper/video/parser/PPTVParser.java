package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.entity.HttpResponse;
import net.luna.common.service.HttpCache;
import net.luna.common.util.CacheManager;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.UrilUtil;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by bintou on 16/2/15.
 */
public class PPTVParser {

    public static final int DEFINITION_ORIGINAL = 4;
    public static final int DEFINITION_SUPER = 3;
    public static final int DEFINITION_HIGH = 2;
    public static final int DEFINITION_NORMAL = 1;

    private String mOriUrl;
    private Context mContext;
    private int mDefinition;
    private int mDefint;

    private int[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_ORIGINAL};

    private final String API_URL = "http://web-play.pptv.com/webplay3-0-[id].xml?version=4&type=mpptv&kk=[kk]&cb=getPlayEncode";
    private final String URL_M3U8 = "http://[ip]/[rid].m3u8?type=mpptv&k=[key]";
    private final String URL_SEGS = "http://[ip]/[seg]/[rid]?fpp.ver=1.3.0.19&k=[key]&key=[id]&type=web.fpp";

    private String mVid = "";

    private VideoCatchManager mVideoCatchManager;

    public PPTVParser(Context context, String url, int definition) {
        mOriUrl = url;
        mContext = context;
        mDefint = definition;
        mDefinition = defArray[definition];
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
    }

    public Message run() {
        HttpCache httpCache = CacheManager.getHttpCache(mContext);
        HttpResponse httpResponse = httpCache.httpGet(mOriUrl);
        if (httpResponse.getResponseBody() != null) {
            String html = httpResponse.getResponseBody();
            mVid = UrilUtil.getVal(html, "\"?channel_id\"?:\"?(\\d+)\"?");
            String kk = UrilUtil.getVal(html, "\"kk\":\"([\\s\\S]*?)\"");
            LunaLog.d(mVid);
            if (!StringUtils.isBlank(mVid)) {
                String url = API_URL.replace("[vid]", mVid);
                LunaLog.d(url);
                String result = mVideoCatchManager.catchUrlResult(url);
                JSONObject data = JSONUtils.toJsonObject(result);
                if (data != null) {
                    try {
                        JSONArray videoList = data.optJSONArray("videoList");
                        if (videoList != null && videoList.length() > 0) {
                            //优先获取目标清晰度，否则按清晰度优先级顺序获取
                            JSONObject targetJo = videoList.optJSONObject(mDefinition);
                            if (targetJo == null) {
                                for (int definition : defArray) {
                                    targetJo = videoList.optJSONObject(definition);
                                    if (targetJo != null) {
                                        break;
                                    }
                                }
                            }
                            if (targetJo != null) {
                                String targetUrl = targetJo.optString("playUrl");
                                if (!StringUtils.isBlank(targetUrl) && targetUrl.startsWith("http://")) {
                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = targetUrl;
                                    return msg;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        Message msg = new Message();
        msg.what = 1;
        msg.obj = "";
        return msg;
    }


}
