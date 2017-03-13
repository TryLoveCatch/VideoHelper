package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.DigestUtils;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.RandomUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Created by bintou on 16/7/1.
 */
public class BesTvParser {


    public static String BESTV_APP_CHANNEL = "aa2ddfdb-4387-49c0-9651-92cc83b8e905";
    public static String BESTV_APP_KEY = "3a10f1283920d1c86960e22f307968d8";
    private final static String[] MODELS = {"HUAWEI B199", "MI 4LTE", "meizu mx4 ", "sm-i9300", "Lenvov a378t", "vivo Y13iL",
            "SM-G920P", "Lenovo A630T", "HM_NOTE 1S"};
    private final static int[] SDK_INTS = {15, 16, 17, 18, 19, 20, 21};

    public static final String DEFINITION_SUPER = "超清 720p";
    public static final String DEFINITION_HIGH = "高清 540p";
    public static final String DEFINITION_NORMAL = "标清 480p";

    private Context mContext;
    private String mDefinition;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER};

    private final String API_URL = "https://bestvapi.bestv.cn/video/video_rate?fdn_code=[fdn]&vid=[vid]&token=[token]";

    private String mUrl = "";
    private String mVid = "";
    private String mFdn = "";

    private String[] defArrayVideoJj = {"high", "normal", "super"};
    private JSONObject videoJjJson = new JSONObject();

    public BesTvParser(Context context, String url, int definition) {
        mContext = context;
        mDefinition = defArray[definition];
        mUrl = url;
        mVid = RandomUtils.getRandom(100000, 200000) + "";
        if (!StringUtils.isBlank(mUrl)) {
            mFdn = getFdnID(mUrl);
            LunaLog.d("FDN: " + mFdn);
        }
    }

    public Message run() {
        try {
            String token = getDeviceToken();
            String url = API_URL.replace("[fdn]", mFdn).replace("[vid]", mVid).replace("[token]", token);

            videoJjJson.putOpt("site", GlobalConstant.SITE_BESTV);

            String result = HttpUtils.httpGetString(url);
            LunaLog.d("url: " + url);
            if (!StringUtils.isBlank(result)) {
                JSONObject resultJo = JSONUtils.toJsonObject(result);
                JSONObject body = JSONUtils.getJSONObject(resultJo, "data", null);
                JSONArray ary = body.optJSONArray("livestream");
                if (body != null) {
                    String targetUrl = null;
                    JSONObject segs = new JSONObject();
                    //优先获取目标清晰度，否则按清晰度优先级顺序获取
                    if (ary != null && ary.length() > 0) {
                        for (int i = 0; i < ary.length(); i++) {
                            JSONObject jo = ary.getJSONObject(i);
                            String name = jo.optString("name");
                            String playUrl = jo.optString("url");
                            String def = "";
                            if (DEFINITION_SUPER.equals(name)) {
                                def = "super";
                            } else if (DEFINITION_HIGH.equals(name)) {
                                def = "high";
                            } else if (DEFINITION_NORMAL.equals(name)) {
                                def = "normal";
                            }
                            if (!StringUtils.isBlank(playUrl)) {
                                if (mDefinition.equals(name)) {
                                    targetUrl = playUrl;
                                }
                                segs.put(def, playUrl);
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


    public String getDevideId() {
        Random r = new Random();
        int sdkInt = r.nextInt(SDK_INTS.length);
        String str = MODELS[sdkInt];
//		String macAddress = NetWorkUtil.getMacAddress(context);
        String macAddress = null;
        if (macAddress == null || macAddress.equals("00:00:00:00:00:00")) {
            macAddress = "0";
        } else {
            macAddress = macAddress.replace(":", "");
        }
        String result = DigestUtils.UrlEnc(new StringBuilder(String.valueOf(str)).append("_").append(macAddress).append("_").append(getAndroidID()).toString());
        return result;
    }

    public String getAndroidID() {
//		return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        String deviceId = RandomUtils.getRandomNumbersAndLetters(15);
        return deviceId;
    }

    public String getDeviceToken() {
        String str = "https://bestvapi.bestv.cn/app/init";
        String toJson;
        String str2 = "";
        String str3 = getDevideId();
        String str4 = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_id", str3);
            toJson = jsonObject.toString();
        } catch (Exception e) {
            toJson = str4;
        }
        if (toJson != null && toJson.length() > 0) {
            byte[] bytes;
            byte[] bytes2;
            str4 = BESTV_APP_KEY;
            str3 = BESTV_APP_CHANNEL;
            long currentTimeMillis = System.currentTimeMillis();
            String format = String.format("channelid=%s&timestamp=%s", new Object[]{str3, String.valueOf(currentTimeMillis)});
            try {
                bytes = str4.getBytes("utf-8");
            } catch (UnsupportedEncodingException e2) {
                bytes = null;
            }
            try {
                bytes2 = format.getBytes("utf-8");
            } catch (UnsupportedEncodingException e3) {
                bytes2 = null;
            }
            try {
                bytes = DigestUtils.hmacsha256(bytes2, bytes);
            } catch (Exception e4) {
                bytes = null;
            }
            String url = String.format("%s?%s&signature=%s", new Object[]{str, format, DigestUtils.bytesToHexString(bytes).toLowerCase()});
            str4 = HttpUtils.httpPostString(url, toJson);
            if (str4 != null && str4.length() > 0) {
                JSONObject json = JSONUtils.toJsonObject(str4);
                if (json != null) {
                    String token = json.optString("token");
                    if (!StringUtils.isBlank(token)) {
                        return token;
                    }
                }
            }
        }
        return str2;
    }

    private String getFdnID(String body) {
        LunaLog.d(body);
        String result = "FDN";
        try {
            if (body != null) {
                body = body.replace("http://2345.mobile.vod.bestvcdn.com.cn/gslb/program/FDN/", "");
                int end = body.indexOf("/");
                if (end > 0) {
                    result = body.substring(0, end);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}

