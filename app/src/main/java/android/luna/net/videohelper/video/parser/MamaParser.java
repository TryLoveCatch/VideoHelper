package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONObject;

/**
 * Created by bintou on 16/1/12.
 */
public class MamaParser {

    public static String DEFINITION_ORIGINAL = "1080P";
    public static String DEFINITION_SUPER = "超清";
    public static String DEFINITION_HIGH = "高清";
    public static String DEFINITION_NORMAL = "标清";
    public static String DEFINITION_LOW = "低清";

    private Context mContext;
    private String mDefinition;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_LOW, DEFINITION_ORIGINAL};

    private final String API_URL = "http://acfunfix.sinaapp.com/mama.php?url=[url]&callback=MAMA2_HTTP_JSONP_CALLBACK0";

    private String mSite = "";
    private String webUrl = "";
    private int defInt;

    private VideoCatchManager mVideoCatchManager;

    private boolean isMp4First = false;

    private String[] defArrayVideoJj = {"high", "normal", "super", "low", "original"};
    private JSONObject videoJjJson = new JSONObject();

    public MamaParser(Context context, String url, String site, int definition, boolean isMp4First) {
        mContext = context;
        mDefinition = defArray[definition];
        defInt = definition;
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mSite = site;
        webUrl = url;
        this.isMp4First = isMp4First;
        LunaLog.d(mSite);
        if (GlobalConstant.SITE_PPTV.equals(mSite)) {
            DEFINITION_SUPER = "720P";
            defArray[2] = DEFINITION_SUPER;
        }
        if (GlobalConstant.SITE_ACFUN.equals(mSite) || GlobalConstant.SITE_BILIBILI.equals(mSite)) {
            DEFINITION_ORIGINAL = "原画";
            defArray[4] = DEFINITION_ORIGINAL;
        }
    }

    public Message run() {
        try {
            videoJjJson.putOpt("site", mSite);
            String url = webUrl;
            if (GlobalConstant.SITE_ACFUN.equals(mSite)) {
                url = url.replace("m.acfun", "www.acfun").replace("v/?", "v/").replace("=", "");
            } else if (GlobalConstant.SITE_BILIBILI.equals(mSite)) {
                url = url.replace("com/mobile/", "com/").replace(".html", "/");
            }
            if (GlobalConstant.SITE_FUN.equals(mSite)) {
                url = url.replace("mplay", "vplay");
                url = url.replace("m.fun", "www.fun").replace("?vid=", "v-").replace("?mid=", "g-");
                url = url + "/";
                webUrl = url;
            }

            if (GlobalConstant.SITE_SOHU.equals(mSite) && url.contains("/u/vw/")) {
                url = url.replace("http://m", "http://my");
            }


            url = API_URL.replace("[url]", url);
            LunaLog.e(url);
            String result = mVideoCatchManager.catchUrlResult(url);
            result = result.replace("MAMA2_HTTP_JSONP_CALLBACK0(", "");
            result = result.substring(0, result.length() - 1);
            if (!StringUtils.isBlank(result)) {
                JSONObject resultJo = JSONUtils.toJsonObject(result);
                if (resultJo != null) {
                    //优先获取目标清晰度，否则按清晰度优先级顺序获取
                    JSONObject targetJo = null;
                    if (isMp4First) {
                        targetJo = resultJo.optJSONObject("mp4");
                        if (targetJo == null || StringUtils.isBlank(targetJo.optString(mDefinition))) {
                            targetJo = resultJo.optJSONObject("m3u8");
                        }
                    } else {
                        targetJo = resultJo.optJSONObject("m3u8");
                        if (targetJo == null || StringUtils.isBlank(targetJo.optString(mDefinition))) {
                            targetJo = resultJo.optJSONObject("mp4");
                        }
                    }
                    JSONObject segs = new JSONObject();
                    if (targetJo != null) {
                        String targetUrl = targetJo.optString(mDefinition);
                        for (int i = 0; i < defArray.length; i++) {
                            String def = defArray[i];
                            String videoUrl = targetJo.optString(def);

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (GlobalConstant.SITE_QQ.equals(mSite)) {
            BtJsonParser btJsonParser = new BtJsonParser(mContext, webUrl, mSite, defInt);
            return btJsonParser.run();
        } else if (GlobalConstant.SITE_BILIBILI.equals(mSite)) {
            BilibiliParser bilibiliParser = new BilibiliParser(mContext, webUrl, defInt);
            return bilibiliParser.run();
        } else if (GlobalConstant.SITE_FUN.equals(mSite) || GlobalConstant.SITE_YINYUETAI.equals(mSite)) {
            CloudVideoParser cloudVideoParser;
            if (defInt == GlobalConstant.DEF_SUPER) {
                cloudVideoParser = new CloudVideoParser(mContext, webUrl, mSite, 3);
            } else if (defInt == GlobalConstant.DEF_HIGH) {
                cloudVideoParser = new CloudVideoParser(mContext, webUrl, mSite, 2);
            } else {
                cloudVideoParser = new CloudVideoParser(mContext, webUrl, mSite, 1);
            }
            return cloudVideoParser.run();
        } else if (GlobalConstant.SITE_MANGO.equals(mSite)) {
            return null;
        } else {
            ShuoshuParser shuoshuParser = new ShuoshuParser(mContext, webUrl, defInt, mSite);
            return shuoshuParser.run();
        }
    }
}


