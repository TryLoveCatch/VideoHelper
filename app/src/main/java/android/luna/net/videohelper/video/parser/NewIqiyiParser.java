package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.os.Handler;

import net.luna.common.debug.LunaLog;
import net.luna.common.entity.HttpResponse;
import net.luna.common.service.HttpCache;
import net.luna.common.util.CacheManager;
import net.luna.common.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by bintou on 15/11/17.
 */
public class NewIqiyiParser implements Runnable {

    public static final String DEFINITION_SUPER = "super";
    public static final String DEFINITION_HIGH = "high";
    public static final String DEFINITION_NORMAL = "normal";
    public static final String DEFINITION_ORIGINAL = "original";

    private String mOriUrl;
    private Context mContext;
    private String mDefinition;
    private Handler mHandler;

    String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_ORIGINAL};

    String apiUrl = "http://cache.m.iqiyi.com/jp/tmts/[tvid]/[vid]/?platForm=h5&qyid=4rhh404o7ooq32xlr3c9ukde&type=m3u8&rate=1&src=[src]&sc=[sc]&__refI=[parseUrl]&t=[t]&qd_jsin=aGFoYQ==&__jsT=";

    public NewIqiyiParser(Context context, String url, String t, String sc, int definition, Handler handler) {
        mOriUrl = url;
        mContext = context;
        mDefinition = defArray[definition];
        mHandler = handler;
    }


    @Override
    public void run() {
        HttpCache httpCache = CacheManager.getHttpCache(mContext);
        HttpResponse httpResponse = httpCache.httpGet(mOriUrl);
        if (httpResponse.getResponseBody() != null) {
            String html = httpResponse.getResponseBody();
            String vid = getVid(html);
            String tvid = getTvid(html);
            if (!StringUtils.isBlank(vid) && !StringUtils.isBlank(tvid)) {
                String src = "d846d0c32d664d32b6b54ea48997a589";

                //未完
            }
        }

//        JSONObject data = VideoCatchManager.getInstanct(mContext).getM3u8Link(mOriUrl);
//        if (data != null) {
//            //优先获取目标清晰度，否则按清晰度优先级顺序获取
//            String targetUrl = data.optString(mDefinition);
//            if (StringUtils.isBlank(targetUrl)) {
//                for (String definition : defArray) {
//                    targetUrl = data.optString(definition);
//                    if (!StringUtils.isBlank(targetUrl)) {
//                        break;
//                    }
//                }
//            }
//            targetUrl = StringUtils.nullStrToEmpty(targetUrl);
//            if (mHandler != null) {
//                Message msg = new Message();
//                msg.what = 1;
//                msg.obj = targetUrl;
//                mHandler.sendMessage(msg);
//            }
//        } else {
//            if (mHandler != null) {
//                Message msg = new Message();
//                msg.what = 1;
//                msg.obj = "";
//                mHandler.sendMessage(msg);
//            }
//        }
    }

    private String getVid(String body) {
        try {
            if (body != null) {
                body = body.replaceAll(" ", "");
                String vidRegex = "\"vid\":\"([\\s\\S]*?)\"";
                Pattern p = Pattern.compile(vidRegex);
                Matcher m = p.matcher(body);
                if (m.find()) {
                    String result = m.group(1);
                    return result;
                } else {
                    LunaLog.d("not found");
                    p = Pattern.compile("Q.PageInfo.playInfo.vid?=?\"([\\s\\S]*?)\"");
                    m = p.matcher(body);
                    if (m.find()) {
                        String result = m.group(1);
                        LunaLog.d("vid: " + result);
                        return result;
                    } else {
                        LunaLog.d("not found too");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getTvid(String body) {
        try {
            if (body != null) {
                body = body.replaceAll(" ", "");
                String vidRegex = "\"tvid\":\"([\\s\\S]*?)\"";
                Pattern p = Pattern.compile(vidRegex);
                Matcher m = p.matcher(body);
                if (m.find()) {
                    String result = m.group(1);
                    return result;
                } else {
                    LunaLog.d("not found");
                    p = Pattern.compile("Q.PageInfo.playInfo.tvid?=?\"(\\d+)\"");
                    m = p.matcher(body);
                    if (m.find()) {
                        String result = m.group(1);
                        LunaLog.d("tvid: " + result);
                        return result;
                    } else {
                        LunaLog.d("not found too");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}