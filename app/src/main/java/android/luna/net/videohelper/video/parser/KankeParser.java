package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.RandomUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.UrilUtil;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by bintou on 16/1/19.
 */
public class KankeParser {

    public static final String DEFINITION_ORIGINAL = "蓝光";
    public static final String DEFINITION_SUPER = "超清";
    public static final String DEFINITION_HIGH = "高清";
    public static final String DEFINITION_NORMAL = "标清";
    public static final String DEFINITION_LOW = "流畅";

    private Context mContext;
    private String mDefinition;
    private int mDefInt;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_LOW, DEFINITION_ORIGINAL};
    private String[] defArrayVideoJj = {"high", "normal", "super", "low", "original"};
    private String API_URL = "http://play.kanketv.com/playerCode2.0/play/api?videoType=M&playerId=[id]&linkUrl=[linkUrl]";

    private String mSite = "";

    private String oriUrl;
    private String mWebUrl;

    //后备解析地址
    private String backupTargetUrl;

    private boolean mGetAllDef;
    //全播放地址列表
    private JSONObject dataJo = new JSONObject();

    private VideoCatchManager mVideoCatchManager;

    private JSONObject videoJjJson = new JSONObject();


    public KankeParser(Context context, String url, String site, int definition, boolean getAllDef) {
        mContext = context;
        mDefinition = defArray[definition];
        mDefInt = definition;
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mSite = site;
        oriUrl = url;
        mWebUrl = url;
        mGetAllDef = getAllDef;

        int playerId = RandomUtils.getRandom(300000, 399999);
        API_URL = API_URL.replace("[id]", playerId + "");
        if (GlobalConstant.SITE_QQ.equals(mSite)) {
            oriUrl = oriUrl.replace("http://m.", "http://");
        }

        if (GlobalConstant.SITE_IQIYI.equals(mSite)) {
            oriUrl = oriUrl.replace("http://m.", "http://www.");
        }

        if (GlobalConstant.SITE_YOUKU.equals(mSite)) {
            if (oriUrl.contains("c-h5.youku.com/co_show/h5")) {
                String vid = UrilUtil.getQueryString(oriUrl, "x#vid");
                oriUrl = "http://v.youku.com/v_show/id_" + vid + ".html";
            } else {
                oriUrl = oriUrl.replace("_ev_1", "");
            }
        }

        if (GlobalConstant.SITE_ACFUN.equals(site)) {
            oriUrl = oriUrl.replace("m.", "www.").replace("?ac=", "ac");
        }
    }

    public Message run() {
        try {
            if (mSite.equals(GlobalConstant.SITE_YOUKU) || mSite.equals(GlobalConstant.SITE_TUDOU)) {
                ShuoshuParser shuoshuParser = new ShuoshuParser(mContext, oriUrl, mDefInt, mSite);
                Message msg = shuoshuParser.run();
                backupTargetUrl = (String) msg.obj;
            }
            if (GlobalConstant.SITE_FUN.equals(mSite)) {
                oriUrl = oriUrl.replace("mplay", "vplay");
                oriUrl = oriUrl.replace("m.fun", "www.fun").replace("?vid=", "v-").replace("?mid=", "g-");
                oriUrl = oriUrl + "/";
            }

            String url = API_URL.replace("[linkUrl]", oriUrl);
            LunaLog.e(url);
            videoJjJson.putOpt("site", mSite);
            String result = mVideoCatchManager.catchUrlResult(url);
            if (!StringUtils.isBlank(result)) {
                JSONArray resultJo = JSONUtils.toJsonArray(result);
                if (resultJo != null && resultJo.length() > 0) {
                    //优先获取目标清晰度，否则按清晰度优先级顺序获取
                    String targetUrl = "";
                    String desUrl = "";
                    JSONObject defJo = new JSONObject();
                    JSONObject segs = new JSONObject();
                    for (int i = 0; i < resultJo.length(); i++) {

                        JSONArray ary = resultJo.optJSONArray(i);
                        JSONObject jo = JSONUtils.getJsonObject(ary, 0, null);
                        if (jo != null) {
                            String high = jo.optString("high");
                            for (int j = 0; j < defArray.length; j++) {
                                String def = defArray[j];
                                if (def.equals(high)) {
                                    targetUrl = jo.optString("link");
                                    defJo.putOpt(defArrayVideoJj[j], targetUrl);
                                    segs.putOpt(defArrayVideoJj[j], targetUrl);
                                    break;
                                }
                            }

                            if (mDefinition.equals(high)) {
                                desUrl = jo.optString("link");
                            }

                            if (i == (resultJo.length() - 1) && StringUtils.isBlank(desUrl)) {
                                //如果找不到就选择最后一个
                                desUrl = jo.optString("link");
                            }
                        }
                    }

                    videoJjJson.putOpt("segs", segs);

                    if (mGetAllDef) {
                        dataJo.putOpt("m3u8", defJo);
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = dataJo;
                        LunaLog.d(dataJo.toString());
                        return msg;
                    } else {
                        if (!StringUtils.isBlank(targetUrl)) {
                            Message msg = new Message();
                            msg.what = 1;
                            Bundle bundle = new Bundle();
                            bundle.putString("segs", videoJjJson.toString());
                            msg.setData(bundle);
                            if (!StringUtils.isBlank(backupTargetUrl)) {
                                msg.obj = backupTargetUrl;
                            } else {
                                msg.obj = desUrl;
                            }
                            return msg;
                        } else if (!StringUtils.isBlank(backupTargetUrl)) {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = backupTargetUrl;
                            return msg;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (GlobalConstant.SITE_QQ.equals(mSite)) {
            QQParser qqParser = new QQParser(mContext, oriUrl, mDefInt);
            return qqParser.run();
        } else if (GlobalConstant.SITE_IQIYI.equals(mSite)) {
            Message msg = new Message();
            msg.what = 1;
            msg.arg2 = 1003;
            msg.obj = "";
            return msg;
        } else if (GlobalConstant.SITE_LETV.equals(mSite) || GlobalConstant.SITE_IQIYI.equals(mSite) || GlobalConstant.SITE_ACFUN.equals(mSite)) {
            CloudVideoParser cloudVideoParser;
            if (mDefInt == GlobalConstant.DEF_SUPER) {
                cloudVideoParser = new CloudVideoParser(mContext, oriUrl, mSite, 3);
            } else if (mDefInt == GlobalConstant.DEF_HIGH) {
                cloudVideoParser = new CloudVideoParser(mContext, oriUrl, mSite, 2);
            } else {
                cloudVideoParser = new CloudVideoParser(mContext, oriUrl, mSite, 1);
            }
            return cloudVideoParser.run();
        } else if (GlobalConstant.SITE_FUN.equals(mSite)) {
            BtJsonParser btJsonParser = new BtJsonParser(mContext, oriUrl, mSite, mDefInt);
            return btJsonParser.run();

        } else {
            ShuoshuParser shuoshuParser = new ShuoshuParser(mContext, oriUrl, mDefInt, mSite);
            return shuoshuParser.run();
        }
    }

}
