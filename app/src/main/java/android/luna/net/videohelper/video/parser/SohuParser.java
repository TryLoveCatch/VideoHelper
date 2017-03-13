package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.entity.HttpResponse;
import net.luna.common.service.HttpCache;
import net.luna.common.util.CacheManager;
import net.luna.common.util.StringUtils;
import net.luna.common.util.UrilUtil;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by bintou on 15/11/17.
 */
public class SohuParser {

    public static final String DEFINITION_SUPER = "url_super";
    public static final String DEFINITION_HIGH = "url_high";
    public static final String DEFINITION_NORMAL = "url_nor";
    public static final String DEFINITION_LOW = "download_url";

    private String mOriUrl;
    private Context mContext;

    private String mDefinition;
    private int mDefint;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_LOW};

    private final String API_URL = "http://api.tv.sohu.com/v4/video/info/[vid].json?site=1&api_key=695fe827ffeb7d74260a813025970bd5&plat=17&sver=1.0&partner=1";

    private String mVid = "";

    private VideoCatchManager mVideoCatchManager;

    private String[] defArrayVideoJj = {"high", "normal", "super", "low", "original"};
    private JSONObject videoJjJson = new JSONObject();

    public SohuParser(Context context, String url, int definition) {
        mOriUrl = url;
        mContext = context;
        mDefint = definition;
        mDefinition = defArray[definition];
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
    }

    public Message run() {
        try {
            HttpCache httpCache = CacheManager.getHttpCache(mContext);
            HttpResponse httpResponse = httpCache.httpGet(mOriUrl);
            videoJjJson.putOpt("site", GlobalConstant.SITE_SOHU);
            if (httpResponse.getResponseBody() != null) {
                String html = httpResponse.getResponseBody();
                mVid = getVid(html);
                if (!StringUtils.isBlank(mVid)) {
                    String url = API_URL.replace("[vid]", mVid);
                    LunaLog.d(url);
                    JSONObject data = mVideoCatchManager.catchUrlData(url);
                    if (data != null) {
                        try {
                            //优先获取目标清晰度，否则按清晰度优先级顺序获取
                            String targetUrl = data.optString(mDefinition);
                            JSONObject segs = new JSONObject();
                            for (int i = 0; i < defArray.length; i++) {
                                String def = defArray[i];
                                String videoUrl = data.optString(def);
                                if (!StringUtils.isBlank(videoUrl)) {
                                    segs.putOpt(defArrayVideoJj[i], videoUrl);
                                    if (StringUtils.isBlank(targetUrl)) {
                                        targetUrl = videoUrl;
                                    }
                                }
                            }
                            videoJjJson.putOpt("segs", segs);

                            if (!StringUtils.isBlank(targetUrl)) {
                                String plat = UrilUtil.getQueryString(targetUrl, "plat");
                                targetUrl = targetUrl.replace("plat=" + plat, "") + "prod=app";
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = targetUrl;
                                Bundle bundle = new Bundle();
                                bundle.putString("segs", videoJjJson.toString());
                                msg.setData(bundle);
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
        MamaParser mamaParser = new MamaParser(mContext, mOriUrl, GlobalConstant.SITE_SOHU, mDefint,false);
        return mamaParser.run();
    }


    private String getVid(String body) {
        String result = "";
        try {
            if (body != null) {
                body = body.replaceAll(" ", "");
                String vidRegex = "vid:(\\d+)";
                Pattern p = Pattern.compile(vidRegex);
                Matcher m = p.matcher(body);
                if (m.find()) {
                    result = m.group(1);
                } else {
                    p = Pattern.compile("vid:\"([\\s\\S]*?)\"");
                    m = p.matcher(body);
                    if (m.find()) {
                        result = m.group(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
