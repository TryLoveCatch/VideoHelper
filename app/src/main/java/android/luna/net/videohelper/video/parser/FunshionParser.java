package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by bintou on 15/11/17.
 */
public class FunshionParser {

    public static final String DEFINITION_SUPER = "highdvd";
    public static final String DEFINITION_HIGH = "dvd";
    public static final String DEFINITION_NORMAL = "tv";

    private String mOriUrl;
    private Context mContext;
    private String mDefinition;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER};

    private final String API_URL = "http://jsonfe.funshion.com/media/?cli=aphone&ver=2.0.0.1&ta=0&mid=";

    private String mVid = "";

    private VideoCatchManager mVideoCatchManager;


    public FunshionParser(Context context, String url, int definition) {
        mOriUrl = url;
        mContext = context;
        mDefinition = defArray[definition];
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        try {
            int start = url.lastIndexOf("mid=") + 4;
            mVid = url.substring(start, url.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message run() {
        String url = API_URL + mVid;
        LunaLog.e(url);
        String result = mVideoCatchManager.catchUrlResult(url);
        if (!StringUtils.isBlank(result)) {
            JSONObject resultJo = JSONUtils.toJsonObject(result);
            JSONObject body = JSONUtils.getJSONObject(resultJo, "data", null);
            if (body != null) {
                JSONObject videofile = JSONUtils.getJSONObject(body, "pinfos", null);
                JSONObject infos = JSONUtils.getJSONObject(videofile, "mpurls", null);
                if (infos != null) {
                    //优先获取目标清晰度，否则按清晰度优先级顺序获取
                    JSONObject targetJo = infos.optJSONObject(mDefinition);
                    if (targetJo == null) {
                        for (String definition : defArray) {
                            targetJo = infos.optJSONObject(definition);
                            if (targetJo != null) {
                                break;
                            }
                        }
                    }
                    try {
                        String mainUrl = JSONUtils.getString(targetJo, "url", "");
                        String mainStr = mVideoCatchManager.catchUrlResult(mainUrl);
                        JSONObject mainJo = JSONUtils.toJsonObject(mainStr);
                        JSONArray playlist = JSONUtils.getJSONArray(mainJo, "playlist", null);
                        JSONArray urls = null;
                        if (playlist != null && playlist.length() > 0) {

                            urls = playlist.optJSONObject(0).optJSONArray("urls");
                        }
                        String targetUrl = JSONUtils.getString(urls, 0, "");
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = targetUrl;
                        return msg;
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
