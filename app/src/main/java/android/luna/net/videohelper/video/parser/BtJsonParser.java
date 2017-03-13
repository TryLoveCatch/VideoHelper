package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.DigestUtils;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONObject;

/**
 * Created by bintou on 16/1/12.
 */
public class BtJsonParser {

    public static final String DEFINITION_SUPER = "super";
    public static final String DEFINITION_HIGH = "high";
    public static final String DEFINITION_NORMAL = "normal";

    private Context mContext;
    private String mDefinition;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER};

    String VD_API = "http://api.vipkpsq.com/v1/parse_third?s=btjson&v=[v]&t=[t]&sign=[sign]";

    private String mUrl = "";
    private String mSite = "";

    private VideoCatchManager mVideoCatchManager;


    public BtJsonParser(Context context, String url, String site, int definition) {
        mContext = context;
        mDefinition = defArray[definition];
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mSite = StringUtils.nullStrToEmpty(site);
        mUrl = StringUtils.nullStrToEmpty(url);
    }

    private String generateUrl(String url) {
        try {
            if (!StringUtils.isBlank(url)) {
                long t = System.currentTimeMillis();
                String sign = t + url + GlobalConstant.REQUEST_CONSTANCT;

                sign = DigestUtils.md5(Base64.encodeToString(sign.getBytes(), Base64.NO_WRAP));
                LunaLog.d("sign: " + sign);
                url = Base64.encodeToString(url.getBytes(), Base64.NO_WRAP);

                return VD_API.replace("[v]", url).replace("[t]", t + "").replace("[sign]", sign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public Message run() {
        try {
            String parserUrl = mUrl;
            if (GlobalConstant.SITE_LETV.equals(mSite)) {
                parserUrl = parserUrl.replace("m.le", "www.le").replace("vplay_", "vplay/");
                LunaLog.d(parserUrl);
            }
            if (GlobalConstant.SITE_PPTV.equals(mSite)) {
                parserUrl = parserUrl.replace("m.pptv.com", "v.pptv.com");
            }

            if (GlobalConstant.SITE_QQ.equals(mSite)) {
                parserUrl = parserUrl.replace("m.v.qq.com", "v.qq.com");
            }

            if (GlobalConstant.SITE_FUN.equals(mSite)) {
                parserUrl = parserUrl.replace("mplay", "vplay");
                parserUrl = parserUrl.replace("m.fun", "www.fun").replace("?vid=", "v-").replace("?mid=", "g-");
                parserUrl = parserUrl + "/";
            }


            String url = generateUrl(parserUrl);
            LunaLog.e(url);

            String result = HttpUtils.httpGetString(url);
            if (!StringUtils.isBlank(result)) {
                LunaLog.d("lwodiu :" + result);
                JSONObject resultJo = JSONUtils.toJsonObject(result);
                JSONObject body = JSONUtils.getJsonObject(resultJo, "video", null);
                if (body != null) {
                    String targetUrl = body.optString("file");
                    if (!StringUtils.isBlank(targetUrl)) {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        msg.setData(bundle);
                        msg.obj = targetUrl;
                        return msg;
                    }
                }
            }
        } catch (Exception e) {
            LunaLog.e(e);
        }
        Message msg = new Message();
        msg.what = 1;
        msg.obj = "";
        return msg;
    }
}


