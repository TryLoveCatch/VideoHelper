package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.os.Message;
import android.util.Base64;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by bintou on 15/10/27.
 */
public class YoukuParser {


    public static final String DEFINITION_SUPER = "mp4hd3";
    public static final String DEFINITION_HIGH = "mp4hd2";
    public static final String DEFINITION_NORMAL = "mp4hd";
    public static final String DEFINITION_FLV = "flvhd";
    public static final String DEFINITION_LOW = "3gphd";


    private Context mContext;
    private String mDefinition;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_FLV, DEFINITION_LOW};

    private final String API_URL = "http://play.youku.com/play/get.json?vid=[id]&ct=62";
    private final String PLAY_URL = "http://k.youku.com/player/getFlvPath/sid/[sid]_00/st/mp4/fileid/[fileid]?ctype=62&ev=1&hd=1&myp=0&ts=[ts]&token=[token]&oip=[oip]&ep=[ep]&K=[K]&callback=jsonp4&_=[time]";

    private final String N = "5de254f8";

    private String mSite = "";

    private String oriUrl;

    private String mVid;

    private VideoCatchManager mVideoCatchManager;

    public YoukuParser(Context context, String url, String site, int definition) {
        mContext = context;
//        mDefinition = defArray[definition];
        mDefinition = DEFINITION_LOW;
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mSite = site;
        oriUrl = url;
        try {
            if (oriUrl.contains("vid=")) {
                int start = oriUrl.indexOf("vid=") + 4;
                int end = oriUrl.lastIndexOf("==") + 2;
                mVid = oriUrl.substring(start, end);
            } else {
                int start = oriUrl.indexOf("id_") + 3;
                int end = oriUrl.indexOf("==") + 2;
                mVid = oriUrl.substring(start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LunaLog.d("vid: " + mVid);
    }

    public Message run() {
        String targetUrl = "";
        try {
            String url = API_URL.replace("[id]", mVid);
            LunaLog.e(url);
//            String result = mVideoCatchManager.catchUrlResult(url);
            String result = HttpUtils.httpGetString(url);
            if (!StringUtils.isBlank(result)) {
                JSONObject resultJo = JSONUtils.toJsonObject(result);
                JSONObject data = JSONUtils.getJSONObject(resultJo, "data", null);
                JSONArray stream = JSONUtils.getJSONArray(data, "stream", null);
                if (stream != null && stream.length() > 0) {
                    //优先获取目标清晰度，否则按清晰度优先级顺序获取
                    JSONObject targetJo = null;
                    for (int i = 0; i < stream.length(); i++) {
                        JSONObject jo = JSONUtils.getJsonObject(stream, i, null);
                        if (jo != null) {
                            String high = jo.optString("stream_type");
                            if (mDefinition.equals(high)) {
                                targetJo = jo;
                                break;
                            }
                            if (i == (stream.length() - 1)) {
                                //如果找不到就那最清晰的
                                targetJo = jo;
                            }
                        }
                    }

                    if (targetJo != null) {
                        String fileid = targetJo.optString("stream_fileid");
                        long time = System.currentTimeMillis();
                        JSONObject security = JSONUtils.getJSONObject(data, "security", null);
                        if (security != null) {
                            String ip = security.optString("ip");
                            String encrypt_string = security.optString("encrypt_string");
                            byte[] decrypt_code = (Base64.decode(URLDecoder.decode(encrypt_string), Base64.DEFAULT));
                            String[] array = myEncoder(N, decrypt_code, false).split("_");
                            String sid = array[0];
                            String tk = array[1];
//                          JSONObject seg = stream.optJSONObject(0).optJSONArray("segs").optJSONObject(0);
                            JSONObject seg = JSONUtils.getJSONArray(targetJo, "segs", null).optJSONObject(0);
                            long seconds = 0;
                            String K = "";
                            if (seg != null) {
                                seconds = Long.parseLong(seg.optString("total_milliseconds_video"));
                                K = seg.optString("key");
                            }
                            seconds = Math.round(seconds / 1e3);
                            String ep = S(sid, fileid, tk, false);
                            LunaLog.d("ep: " + ep);
                            String playUrl = PLAY_URL.replace("[K]", K).replace("[fileid]", fileid).replace("[ts]", seconds + "").replace("[sid]", sid).replace("[token]", tk)
                                    .replace("[oip]", ip).replace("[ep]", ep).replace("[time]", time + "");
                            String targetResult = HttpUtils.httpGetString(playUrl);
                            targetResult = targetResult.replace("jsonp4(", "");
                            targetResult = targetResult.substring(0, targetResult.length() - 1);
                            JSONArray targetArray = JSONUtils.toJsonArray(targetResult);
                            if (targetArray != null && targetArray.length() > 0) {
                                targetUrl = targetArray.optJSONObject(0).optString("server");
                            }
                            LunaLog.d(targetUrl);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LunaLog.e(e);
        } finally {
            Message msg = new Message();
            if (StringUtils.isBlank(targetUrl)) {
                msg.arg2 = 1001;
            }
            msg.what = 1;
            msg.obj = targetUrl;
            return msg;
        }
    }

    private String S(String e, String t, String i, boolean n) {
        String s = e + "_" + t + "_" + i;
        String r = myEncoder("7217f103", s.getBytes(), true);

        if (!n)
            try {
                r = URLEncoder.encode(r, "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

        return r;
    }


    private String myEncoder(String a, byte[] c, boolean isToBase64) {
        String result = "";
        ArrayList<Byte> bytesR = new ArrayList<Byte>();
        int f = 0, h = 0, q = 0;
        int[] b = new int[256];
        for (int i = 0; i < 256; i++)
            b[i] = i;
        while (h < 256) {
            f = (f + b[h] + a.charAt(h % a.length())) % 256;
            int temp = b[h];
            b[h] = b[f];
            b[f] = temp;
            h++;
        }
        f = 0;
        h = 0;
        q = 0;
        while (q < c.length) {
            h = (h + 1) % 256;
            f = (f + b[h]) % 256;
            int temp = b[h];
            b[h] = b[f];
            b[f] = temp;
            byte[] bytes = new byte[]{(byte) (c[q] ^ b[(b[h] + b[f]) % 256])};
            bytesR.add(bytes[0]);
//            result += System.Text.ASCIIEncoding.ASCII.GetString(bytes);
            result += new String(bytes);
            q++;
        }
        if (isToBase64) {
            int length = bytesR.size();
            byte[] byteR = new byte[length];
            for (int i = 0; i < bytesR.size(); i++) {
                byteR[i] = bytesR.get(i).byteValue();
            }
            result = Base64.encodeToString(byteR, Base64.DEFAULT);
        }
        LunaLog.d("result: " + result);
        return result;
    }


}



