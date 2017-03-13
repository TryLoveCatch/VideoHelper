package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.DigestUtils;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zyq 16-6-7
 */
public class QiyiParser {
    public static final String DEFINITION_ORIGINAL = "1080P";
    public static final String DEFINITION_SUPER = "超清";
    public static final String DEFINITION_HIGH = "高清";
    public static final String DEFINITION_NORMAL = "标清";
    public static final String DEFINITION_LOW = "低清";

    private String mOriUrl;
    private Context mContext;

    private String mDefinition;
    private int mDefint;

    private String[] defArray = {DEFINITION_HIGH, DEFINITION_NORMAL, DEFINITION_SUPER, DEFINITION_LOW, DEFINITION_ORIGINAL};

    private String tvid, vid, aid;
    private String videoTitle, thumbUrl, videoName, albumThumbUrl, targetUrl, tags;
    private int head, tail;
    private String M3U8_API = "http://cache.m.iqiyi.com/dc/dt/f4e2840x201d2eb3x777a6171[m3u8]?qd_src=5be6a2fdfe4f4a1a8c7b08ee46a18887";
    private String INTERNAL_API = "http://api.vipkpsq.com/v1/vparse/vj?ckid=[url]";
    //    private static final String M3U8_API = "http://cache.m.iqiyi.com/dc/dt/mobile%s?qd_src=5be6a2fdfe4f4a1a8c7b08ee46a18887";
    private static final String TAG = "qiyiParser";
    private static final String API = "http://cache.video.qiyi.com/vms?key=fvip&src=1702633101b340d8917a69cf8a4b8c7&tvId=%1$s&vid=%2$s&vinfo=1&tm=%3$s&enc=%4$s&qyid=%5$s&tn=%6$s";
    private static final String API_VIP = "http://cache.video.qiyi.com/vms?key=fvip&src=1702633101b340d8917a69cf8a4b8c7&tvId=%1$s&vid=%2$s&vinfo=1&tm=%3$s&enc=%4$s&qyid=%5$s&tn=%6$s";
    private static final String VIP_TOKEN = "http://api.vip.iqiyi.com/services/ck.action";
    public static final String NAME = "QIYI";
    private static final String TIME_JSON_URL = "http://data.video.qiyi.com/t?tn=%1$f";
    private static final String VI_SERVER_URL = "http://cache.video.qiyi.com/vi/";
    private static final String VP_SERVER_URL = "http://cache.video.qiyi.com/vp/";
    private static final Pattern idPattern = Pattern.compile("\\bdata-player-tvid\\s*=\\s*\"([\\d]*)\"");
    private static final Pattern idPattern2 = Pattern.compile("#curid=(.+)_");
    private static final Pattern memberPattern = Pattern.compile("\\bdata-player-ismember\\s*=\\s*\"(.*)\"");
    private static String uid;
    private static final Pattern vidPattern = Pattern.compile("\\bdata-player-videoid\\s*=\\s*\"([A-Z0-9a-z]*)\"");
    private static final Pattern vidPattern2 = Pattern.compile("#curid=.+_(.*)$");
    private static final Pattern aidPattern = Pattern.compile("\\bdata-player-albumid\\s*=\\s*\"(.*)\"");


    private static final String agent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36";
    String cacheUrl;
    Random random = new Random(999);
    private int retryCount = 0;
    private long vidDuration = 0;
    private JSONObject videoJjJson = new JSONObject();
    private String isMember = " false";


    public QiyiParser(Context context, String url, int definition) {
//        this.cacheUrl = url;
        mOriUrl = url;
        mContext = context;
        mDefint = definition;
        mDefinition = defArray[definition];
        String api = PreferencesUtils.getString(mContext, GlobalConstant.SP_IQIYI_NORMAL_M3U8);
        M3U8_API = StringUtils.isBlank(api) ? M3U8_API : api;
    }


    private void getInfoFromUrl(String url) throws Exception {
//        LunaLog.d("初始url:" + url);
        if (url.startsWith("http://m.iqiyi")) {
            url = url.replaceFirst("http://m.iqiyi", "http://www.iqiyi");
        }
        String[] splitStrings = url.split("#");
        url = splitStrings[0];
        if (splitStrings.length > 1) {
            url = new StringBuilder(String.valueOf(url)).append("#").append(splitStrings[1]).toString();
        }
        for (int i = 2; i < splitStrings.length; i++) {
            url = new StringBuilder(String.valueOf(url)).append("%23").append(splitStrings[i]).toString();
        }


        String string = HttpUtils.httpGetString(url, agent);
//		String string = VideoParserBase.getData(url, "utf-8");


        Matcher matcher = vidPattern.matcher(string);
        if (matcher.find()) {
            vid = matcher.group(1);
        }
        matcher = idPattern.matcher(string);
        if (matcher.find()) {
            tvid = matcher.group(1);
        }
        matcher = vidPattern2.matcher(url);
        if (matcher.find()) {
            vid = matcher.group(1);
        }
        matcher = idPattern2.matcher(url);
        if (matcher.find()) {
            tvid = matcher.group(1);
        }

        matcher = memberPattern.matcher(string);
        if (matcher.find()) {
            isMember = matcher.group(1);
            LunaLog.d("isMember: " + isMember);
        }

        matcher = aidPattern.matcher(string);
        if (matcher.find()) {
            aid = matcher.group(1);
            LunaLog.d("aid : " + aid);
        }

        LunaLog.d("tvid:" + tvid + "  vid:" + vid);

        JSONObject jsonObject = new JSONObject(HttpUtils.httpGetString(new StringBuilder(VI_SERVER_URL).append(tvid).append("/").append(vid).append("/").toString()));
        this.videoTitle = jsonObject.optString("shortTitle");
        this.thumbUrl = jsonObject.optString("vpic", null);
        this.videoName = this.videoTitle;
        if (TextUtils.isEmpty(this.videoTitle)) {
            this.videoTitle = jsonObject.optString("an");
            this.videoName = this.videoTitle;
            if (this.videoTitle != null && jsonObject.has("pd")) {
                this.videoTitle += "_" + jsonObject.getInt("pd");
            }
        }


//        LunaLog.d("getInfoFromUrl: " + url + " videoTitle:" + this.videoTitle + " this.videoName:" + this.videoName + " this.thumbUrl:" + this.thumbUrl);
    }

    public Message run() {
        Message msg = new Message();
        msg.what = 1;
        try {
//            LunaLog.d("doParse():parserInfo" + " cacheUrl:" + this.cacheUrl);
            if (this.cacheUrl == null) {
                getInfoFromUrl(mOriUrl);
            }
            videoJjJson.putOpt("site", GlobalConstant.SITE_IQIYI);
            JSONObject segs = new JSONObject();
//            JSONArray audioTracks = getVMS();
            String url = mOriUrl;
            if (url.startsWith("http://m.iqiyi")) {
                url = url.replaceFirst("http://m.iqiyi", "http://www.iqiyi");
            }
            url = INTERNAL_API.replace("[url]", url);
            LunaLog.d("cache url:" + url);
            JSONArray audioTracks = JSONUtils.toJsonArray(HttpUtils.httpGetString(url));
            if (audioTracks != null) {
                for (int i = 0; i < audioTracks.length(); i++) {
                    JSONObject track = audioTracks.optJSONObject(i);
//                    LunaLog.d(track.toString());
                    if (track != null) {
                        int bid = track.optInt("bid");
                        if ((bid >= 1 && bid <= 4) || bid == 96) {
                            String m3u8Url = track.optString("m3u8Url");
                            m3u8Url = M3U8_API.replace("[m3u8]", m3u8Url);
                            LunaLog.d("m3u8url:" + m3u8Url);
//                            HttpResponse response = HttpUtils.httpGet(m3u8Url);
//                            LunaLog.d("response.getResponseCode(): " + response.getResponseCode());
//                            if ((response == null || response.getResponseCode() < 200 || response.getResponseCode() > 302) && response.getResponseBody().length() < 100) {
//                                break;
//                            }
                            String def = "normal";
                            if (bid == 96) {
                                def = "low";
                            } else if (bid == 1) {
                                def = "normal";
                                if (mDefint == 1) {
                                    msg.obj = m3u8Url;
                                }
                            } else if (bid == 2) {
                                def = "high";
                                if (mDefint == 0) {
                                    msg.obj = m3u8Url;
                                }
                            } else if (bid == 3) {
                                def = "super";
                                if (mDefint == 2) {
                                    msg.obj = m3u8Url;
                                }
                            } else if (bid == 4) {
                                def = "super";
                                if (mDefint == 2) {
                                    msg.obj = m3u8Url;
                                }
                            }
                            segs.put(def, m3u8Url);
                        }
                    }
                }
                if (segs.length() > 0) {
                    videoJjJson.putOpt("segs", segs);
                    if ("false".equals(isMember)) {
                        videoJjJson.put("isMember", false);
                    } else {
                        videoJjJson.put("isMember", true);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("segs", videoJjJson.toString());
                    msg.setData(bundle);
                    LunaLog.d("url: " + msg.obj.toString());
                    return msg;
                }
            }
        } catch (Exception e) {
            LunaLog.e(e);
        }
        CloudVideoParser cloudVideoParser = new CloudVideoParser(mContext, mOriUrl, GlobalConstant.SITE_IQIYI, mDefint);
        msg = cloudVideoParser.run();
        LunaLog.d("url: " + msg.obj.toString());
        return msg;
    }


    public void getVideoInfo(String info, String crackInfo, int lang) throws Exception {
//        super.getVideoInfo(info, crackInfo, lang);
//        VideoSegInfo videoInfo = new VideoSegInfo();
        long size = 0;
        int t = getTime();
        int bid = new JSONObject(info).optInt("bid");
//        this.parserInfo = new ParserInfo(new JSONObject(crackInfo));

        JSONObject defObject = new JSONObject(info);
        JSONArray fsArray = defObject.getJSONArray("fs");
        String dd = defObject.getString("dd");
        if (fsArray != null && fsArray.length() > 0) {
            for (int j = 0; j < fsArray.length(); j++) {
                JSONObject segObject = fsArray.getJSONObject(j);
                String link = segObject.getString("l");
                if ((bid == 4 || bid == 5 || bid == 10) && link.indexOf(47) == -1) {
                    link = getVrsEncodeCode(link);
                }
                link = new StringBuilder(String.valueOf(dd)).append(link).toString();
                size += segObject.getLong("b");
                String urlString = getRealUrl(link, getDispatchKey(t, getRid(link)));
                if (urlString != null) {
                    LunaLog.d("targetUrl: " + urlString);
//                    videoInfo.trueUrls.put(urlString);
//                    videoInfo.lengthArray.put(segObject.getInt("d") / 1000);
                }
            }
        }
    }

    private int getTime() {
        int time = 0;
        try {
            time = JSONUtils.toJsonObject(HttpUtils.httpGetString(String.format(TIME_JSON_URL, new Object[]{Double.valueOf(Math.random())}))).getInt("t");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    private JSONArray getVMS() throws Exception {
        try {
            int tm = this.random.nextInt(2000) + 2000;
//            LunaLog.d("this.parserInfo.key:" + key);
            String md5 = DigestUtils.md5(new StringBuilder("4a1caba4b4465345366f28da7c117d20").append(tm).append(tvid).toString());
//            LunaLog.d("this.md5:" + md5);
            String authKey = DigestUtils.md5(new StringBuilder(String.valueOf(tm)).append(tvid).toString());
//            LunaLog.d("authKey:" + authKey);
            String res = HttpUtils.httpGetString("http://data.video.qiyi.com/uid").substring(8);
            uid = new JSONObject(res.substring(0, res.length() - 1)).getString("uid");
//            LunaLog.d("uid :" + uid);
            JSONObject dataObject = null;
            if (isMember.equals("true")) {
//                long ut = System.currentTimeMillis();
//                String uuid = DigestUtils.md5(UniqId.getInstance().getUniqID());
//                LunaLog.d("UUID: " + uuid);
//                String utt = ((ut % 1000) * 14 + 100) + "";
//                String cid = "afbe8fd3d73448c9";
//                String v = aid + "_" + cid + "_" + vid + "_" + ut + "_" + utt + "_2391461978";
//                Map<String, String> map = new HashMap<>();
//                map.put("uuid", uuid);
//                map.put("deviceId", uuid);
//                map.put("cid", cid);
//                map.put("version", "1.0");
//                map.put("vid", vid);
//                map.put("platform", "b6c13e26323c537d");
//                map.put("v", v);
//                map.put("ut", ut + "");
//                map.put("playType", "main");
//                map.put("aid", aid);
//                map.put("utt", utt);
//
//                String postResult = HttpUtils.httpPostString(VIP_TOKEN, map);
//                LunaLog.d("post: " + postResult);
//                dataObject = JSONUtils.toJsonObject(HttpUtils.httpGetString(String.format(API, new Object[]{tvid, vid, Integer.valueOf(tm), md5, uid, Double.valueOf(Math.random()), authKey}))).optJSONObject("data");
                String url = mOriUrl;
                if (url.startsWith("http://m.iqiyi")) {
                    url = url.replaceFirst("http://m.iqiyi", "http://www.iqiyi");
                }
                url = INTERNAL_API.replace("[url]", url);
                LunaLog.d("cache url:" + url);
                JSONArray jsonArray = JSONUtils.toJsonArray(HttpUtils.httpGetString(url));
                return jsonArray;
            } else {
                dataObject = JSONUtils.toJsonObject(HttpUtils.httpGetString(String.format(API, new Object[]{tvid, vid, Integer.valueOf(tm), md5, uid, Double.valueOf(Math.random()), authKey}))).optJSONObject("data");
                LunaLog.d("真正的请求地址是 :" + String.format(API, new Object[]{tvid, vid, Integer.valueOf(tm), md5, uid, Double.valueOf(Math.random()), authKey}));
                LunaLog.d("getVMS():这里是真正的数据了-->" + dataObject.toString());
            }

            JSONObject viObject = dataObject.optJSONObject("vi");
            JSONObject jsonObject = dataObject.optJSONObject("vp");
            this.videoTitle = viObject.optString("vn");
            this.videoName = viObject.optString("an");
            this.thumbUrl = viObject.optString("vpic");
            this.albumThumbUrl = viObject.optString("apic");
            this.head = viObject.optInt("startTime");
            this.tail = viObject.optInt("endTime");
            String tvEname = viObject.optString("tvEname");
            this.tags = viObject.optString("tg");
            int streamType = jsonObject.getInt("t");
            String dd = jsonObject.getString("dd");
            if (streamType != 1) {
                dd = jsonObject.getString("du");
            }
            JSONArray audioTracks = jsonObject.getJSONArray("tkl");
            if (audioTracks == null || audioTracks.length() <= 0) {
                return null;
            }
            JSONObject firstTrack = audioTracks.getJSONObject(0);
            JSONArray definitions = firstTrack.getJSONArray("vs");
            return definitions;
//            if (definitions == null || definitions.length() <= 0) {
//                return firstTrack;
//            }
//
//            return firstTrack;
        } catch (Exception e) {
            LunaLog.e(e);
        }
        return null;
    }

    public static String getVrsEncodeCode(String _arg1) {
        String _local2 = "";
        String[] _local3 = _arg1.split("-");
        int _local4 = _local3.length;
        for (int _local5 = _local4 - 1; _local5 >= 0; _local5--) {
            _local2 = new StringBuilder(String.valueOf((char) getVRSXORCode(Integer.parseInt(_local3[(_local4 - _local5) - 1], 16), _local5))).append(_local2).toString();
        }
        return _local2;
    }

    private static int getVRSXORCode(int _arg1, int _arg2) {
        int _local3 = _arg2 % 3;
        if (_local3 == 1) {
            return _arg1 ^ 121;
        }
        if (_local3 == 2) {
            return _arg1 ^ 72;
        }
        return _arg1 ^ 103;
    }

    public String getUrl() {
        return mOriUrl;
    }

    public String getVideoId() {
        return "";
    }

    public String getVideoTitle() {
        if (this.videoTitle != null) {
            this.videoTitle = this.videoTitle.replaceAll("​", "");
        }
        return this.videoTitle;
    }

    public long getVideoDuration() {
        if (this.vidDuration > 0) {
            return this.vidDuration / 1000;
        }
        return 0;
    }


    public String getParserName() {
        return NAME;
    }

    public static String getDispatchKey(int _arg1, String _arg2) {
        return DigestUtils.md5(new StringBuilder(String.valueOf((int) Math.floor((double) (_arg1 / 600)))).append(")(*&^flash@#$%a").append(_arg2).toString());
    }

    public static String getSecondDispatchUrl(String _arg1, String _arg2) {
        int _local4 = 0;
        int _local5 = 0;
        while (_local5 < _arg1.length()) {
            if (_arg1.charAt(_local5) == '/') {
                _local4++;
            }
            if (_local4 == 3) {
                break;
            }
            _local5++;
        }
        return _arg1.substring(0, _local5 + 1) + _arg2 + _arg1.substring(_local5);
    }

    private String getRid(String _url) {
        String[] _local9 = _url.split("/");
        if (_local9 != null && _local9.length > 0) {
            _local9 = _local9[_local9.length - 1].split("\\.");
            if (_local9 != null && _local9.length > 0) {
                return _local9[0];
            }
        }
        return null;
    }

    private String getRealUrl(String link, String key) throws Exception {
        String dispatchUrl = getSecondDispatchUrl(link, key);
        if (uid != null) {
            dispatchUrl = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(dispatchUrl)).append(dispatchUrl.indexOf(63) == -1 ? "?" : "&").toString())).append("su=").append(uid).toString();
        }
        JSONObject jsonObject = JSONUtils.toJsonObject(HttpUtils.httpGetString(new StringBuilder(String.valueOf(dispatchUrl)).append("&client=&z=&bt=&ct=&tn=").append((this.random.nextInt(999) + 1000) * 10).toString()));
        if (jsonObject != null) {
            return jsonObject.getString("l");
        }
        return null;
    }

    private String getDefString(int def) {
        switch (def) {
            case 1:
                return "标清";
            case 2:
                return "高清";
            case 4:
                return "超清";
            case 5:
                return "1080P";
            case 10:
                return "4K";
            case 96:
                return "流畅";
            default:
                return null;
        }
    }

}