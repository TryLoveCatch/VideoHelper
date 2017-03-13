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

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bintou on 16/1/12.
 */
public class BilibiliParser {

    public static final String DEFINITION_SUPER = "super";
    public static final String DEFINITION_HIGH = "high";
    public static final String DEFINITION_NORMAL = "normal";

    private Context mContext;
    private String mDefinition;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER};

    private final String API_URL = "http://www.bilibili.com/m/html5?aid=[aid]&page=[page]&sid=[sid]";

    private String mVid = "";
    private String mSite = "";
    private String mOriUrl;
    private VideoCatchManager mVideoCatchManager;

    public BilibiliParser(Context context, String url, int definition) {
        mContext = context;
        mDefinition = defArray[definition];
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mVid = UrilUtil.getVal(url, "/video/av(\\d+)/");
        if (StringUtils.isBlank(mVid)) {
            mVid = UrilUtil.getVal(url, "/video/av(\\d+).html");
        }
        LunaLog.d(mVid);
        mVid = StringUtils.nullStrToEmpty(mVid);
        mOriUrl = url;
    }

    public Message run() {
        try {
            HttpCache httpCache = CacheManager.getHttpCache(mContext);
            HttpResponse httpResponse = httpCache.httpGet(mOriUrl);
            if (httpResponse != null && httpResponse.getResponseBody() != null) {
                String html = httpResponse.getResponseBody();
                String mid = UrilUtil.getVal(html, "mid=(\\d+)\"");
                String cookieUrl = "http://interface.bilibili.com/count?aid=[aid]&mid=[mid]".replace("[aid]", mVid).replace("[mid]", mid);
                String page = UrilUtil.getVal(mOriUrl, "index_(\\d+).html");
                httpResponse = httpCache.httpGet(cookieUrl);
                LunaLog.d(mid);
                String sid = null;
                if (httpResponse != null && httpResponse.getResponseBody() != null) {
                    sid = UrilUtil.getVal(httpResponse.getResponseBody(), "sid=([\\s\\S]*?);");
                }
                if (!StringUtils.isBlank(mVid)) {
                    if (StringUtils.isBlank(page)) {
                        page = "1";
                    }
                    sid = StringUtils.nullStrToEmpty(sid);
                    String url = API_URL.replace("[aid]", mVid).replace("[page]", page).replace("[sid]", sid);
                    LunaLog.d(url);
                    String result = mVideoCatchManager.catchUrlResult(url);
                    JSONObject data = JSONUtils.toJsonObject(result);
                    if (data != null) {
                        try {
                            String targetUrl = data.optString("src");
                            LunaLog.e(targetUrl);
                            if (!StringUtils.isBlank(targetUrl)) {
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = targetUrl;
                                return msg;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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


