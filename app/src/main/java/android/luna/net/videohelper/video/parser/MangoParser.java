package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by bintou on 15/11/17.
 */
public class MangoParser {

    public static final String DEFINITION_ORIGINAL = "原画";
    public static final String DEFINITION_SUPER = "超清";
    public static final String DEFINITION_HIGH = "高清";
    public static final String DEFINITION_NORMAL = "标清";
    public static final String DEFINITION_LOW = "流畅";

    private String mOriUrl;
    private Context mContext;
    private String mDefinition;

    String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_LOW, DEFINITION_ORIGINAL};

    private String apiUrl = "http://mobile.api.hunantv.com/v3/video/getSource?appVersion=4.5.2&osType=ios&osVersion=9.000000&ticket=&videoId=[id]";
    private String mVid;

    private String pcWebUrl = "http://www.mgtv.com/v/[rootid]/[collectionId]/f/[vid].html";

    private String[] defArrayVideoJj = {"high", "normal", "super", "low", "original"};
    private JSONObject videoJjJson = new JSONObject();
    private int mDefini;

    public MangoParser(Context context, String url, int definition) {
        mOriUrl = url;
        mContext = context;
        mDefini = definition;
        mDefinition = defArray[definition];
        try {
            if (!StringUtils.isBlank(mOriUrl)) {
                mVid = mOriUrl.substring(mOriUrl.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mVid = StringUtils.nullStrToEmpty(mVid);
    }

    public Message run() {
        try {
            videoJjJson.putOpt("site", GlobalConstant.SITE_MANGO);
            String url = apiUrl.replace("[id]", mVid);
            LunaLog.d(url);
            JSONObject data = VideoCatchManager.getInstanct(mContext).catchUrlData(url);
            try {
                int rootid = data.optInt("rootid");
                int collectionId = data.getInt("collectionId");
                pcWebUrl = pcWebUrl.replace("[rootid]", rootid + "").replace("[collectionId]", collectionId + "").replace("[vid]", mVid);
                LunaLog.d("pcWebUrl"+ pcWebUrl);
                MamaParser mamaParser = new MamaParser(mContext, pcWebUrl, GlobalConstant.SITE_MANGO, mDefini, false);
                Message mamaMsg = mamaParser.run();
                if (mamaMsg != null && mamaMsg.obj != null) {
                    return mamaMsg;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONArray videoSources = JSONUtils.getJSONArray(data, "videoSources", null);
            if (videoSources != null && videoSources.length() > 0) {
                //优先获取目标清晰度，否则获取第一个清晰度
                String targetUrl = "";
                JSONObject segs = new JSONObject();
                for (int i = 0; i < videoSources.length(); i++) {
                    JSONObject videoSource = JSONUtils.getJsonObject(videoSources, i, null);
                    String name = JSONUtils.getString(videoSource, "name", "");
                    for (int j = 0; j < defArray.length; j++) {
                        String videoUrl = null;
                        if (defArray[j].equals(name)) {
                            videoUrl = videoSource.optString("url");
                            if (!StringUtils.isBlank(videoUrl)) {
                                String targetResult = VideoCatchManager.getInstanct(mContext).catchUrlResult(videoUrl);
                                if (!StringUtils.isBlank(targetResult)) {
                                    JSONObject resultJo = JSONUtils.toJsonObject(targetResult);
                                    String info = resultJo.optString("info", "");
                                    segs.putOpt(defArrayVideoJj[j], info);
                                    if (name.equals(mDefinition)) {
                                        targetUrl = JSONUtils.getString(videoSource, "url", "");
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                videoJjJson.putOpt("segs", segs);
                //如果没有目标清晰度，则选择列表最先有的清晰度
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        Message msg = new Message();
        msg.what = 1;
        msg.obj = "";
        return msg;
    }
}
