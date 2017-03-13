package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by bintou on 15/11/17.
 */
public class LetvParser {

    public static final String DEFINITION_SUPER = "1300";
    public static final String DEFINITION_HIGH = "1000";
    public static final String DEFINITION_NORMAL = "350";
    public static final String DEFINITION_LOW = "mp4";

    private String mOriUrl;
    private Context mContext;
    private String mDefinition;
    private int mDefInt;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_LOW};

    //    private final String API_URL = "http://api.mob.app.letv.com/play?vid=";
    private final String API_URL = "http://api.letv.com/mms/out/video/playJsonH5?platid=3&splatid=301&detect=0&tss=no&id=[vid]&dvtype=1000&accessyx=1&domain=m.le.com&tkey=[tkey]";


    private String mVid = "";
    long tkey = 0;

    private String[] defArrayVideoJj = {"high", "normal", "super", "low"};
    private JSONObject videoJjJson = new JSONObject();

    public LetvParser(Context context, String url, int definition) {
        mOriUrl = url;
        mContext = context;
        mDefinition = defArray[definition];
        mDefInt = definition;
        try {
            int end = url.lastIndexOf(".html");
            mVid = url.substring(end - 8, end);
            tkey = System.currentTimeMillis() / 1000;
            tkey = getMmsKey(tkey);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Message run() {
        try {
            String url = API_URL.replace("[vid]", mVid).replace("[tkey]", tkey + "");
            LunaLog.e(url);
            videoJjJson.putOpt("site", GlobalConstant.SITE_LETV);
            String result = HttpUtils.httpGetString(url);
            LunaLog.d(result);
            if (!StringUtils.isBlank(result)) {
                JSONObject resultJo = JSONUtils.toJsonObject(result);
                String isTryLook = resultJo.optString("isTryLook", "");
                if ("1".equals(isTryLook)) {
                    KankeParser kankeParser = new KankeParser(mContext, mOriUrl, GlobalConstant.SITE_LETV, mDefInt, false);
                    return kankeParser.run();
                }

                JSONObject body = JSONUtils.getJSONObject(resultJo, "playurl", null);
                if (body != null) {
                    JSONArray domain = JSONUtils.getJsonArray(body, "domain", null);
                    if (domain != null && domain.length() > 0) {
                        String domainStr = null;
                        for (int i = 0; i < domain.length(); i--) {
                            domainStr = domain.getString(i);
                            if (!domainStr.contains("play.g3proxy.lecloud.com")) {
                                domainStr = "";
                            }
                            if (!TextUtils.isEmpty(domainStr)) {
                                break;
                            }
                        }
                        if (!StringUtils.isBlank(domainStr)) {
                            JSONObject dispatch = JSONUtils.getJSONObject(body, "dispatch", null);

                            if (dispatch != null) {
                                //优先获取目标清晰度，否则按清晰度优先级顺序获取
                                JSONObject segs = new JSONObject();
                                JSONArray targetJos = null;
                                String targetUrl = null;
                                for (int i = 0; i < defArray.length; i++) {
                                    String def = defArray[i];
                                    targetJos = JSONUtils.getJSONArray(dispatch, def, null);
                                    if (targetJos != null && targetJos.length() > 0) {
                                        String videoUrl = domainStr + targetJos.optString(0);
                                        LunaLog.d("videoUrl :" + videoUrl);
                                        if (!StringUtils.isBlank(videoUrl)) {
                                            segs.putOpt(defArrayVideoJj[i], videoUrl);
                                            if (mDefinition.equals(def)) {
                                                targetUrl = videoUrl;
                                            }
                                        }
                                    }
                                }
                                videoJjJson.putOpt("segs", segs);
                                if (StringUtils.isBlank(targetUrl)) {
                                    for (String def : defArrayVideoJj) {
                                        targetUrl = segs.optString(def);
                                        if (!StringUtils.isBlank(targetUrl)) {
                                            break;
                                        }
                                    }
                                }
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        KankeParser kankeParser = new KankeParser(mContext, mOriUrl, GlobalConstant.SITE_LETV, mDefInt, false);
        return kankeParser.run();
    }


    private long getMmsKey(long e) {
        long t = 185025305;
        long r = (t % 17);
        long n = rotateRight(e, r);
        long o = n ^ t;
        return o;
    }

    private long rotateRight(long e, long t) {
        long r;
        for (int i = 0; i < t; i++) {
            r = 1 & e;
            e >>= 1;
            r <<= 31;
            e += r;
        }
        return e;
    }

}
