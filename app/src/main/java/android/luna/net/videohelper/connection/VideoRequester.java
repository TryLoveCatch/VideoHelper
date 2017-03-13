package android.luna.net.videohelper.connection;

import android.content.Context;
import android.luna.net.videohelper.Ninja.Database.Record;
import android.luna.net.videohelper.bean.Account;
import android.luna.net.videohelper.bean.TvLiveP;
import android.luna.net.videohelper.bean.TvSource;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelper.bean.VideoInfo;
import android.luna.net.videohelper.bean.VideoSource;
import android.luna.net.videohelper.bean.VideoType;

import net.luna.common.basic.GlobalCharsets;
import net.luna.common.constant.HttpConstants;
import net.luna.common.debug.LunaLog;
import net.luna.common.entity.HttpRequest;
import net.luna.common.entity.HttpResponse;
import net.luna.common.service.HttpCache;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.ListUtils;
import net.luna.common.util.PackageUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by bintou on 15/11/2.
 */
public class VideoRequester {

    private Context mContext;
    private HttpCache mHttpCache;

    public VideoRequester(Context context) {
        mContext = context;
        mHttpCache = new HttpCache(mContext);
    }


    public ArrayList<VideoInfo> requestVideoList(int type, int offset) throws Exception {
        int versionCode = PackageUtils.getAppVersionCode(mContext);
        HttpResponse response = mHttpCache.httpGet("http://api.vipkpsq.com/v1/channel?cid=" + type + "&ofs=" + offset + "&vcode=" + versionCode);
        return parserVideoList(response);
    }

    public ArrayList<VideoDetial> requestVideoChannelDetial(int type, int offset, boolean isRefresh) throws Exception {
        int versionCode = PackageUtils.getAppVersionCode(mContext);
        String url = "http://api.vipkpsq.com/v1/channel_detail?cid=" + type + "&ofs=" + offset + "&vcode=" + versionCode;
        HttpRequest request = new HttpRequest(url);
        if (isRefresh) {
            request.setRequestProperty(HttpConstants.CACHE_CONTROL, "no-cache");
        }
        HttpResponse response = mHttpCache.httpGet(request);
//        HttpResponse response = mHttpCache.httpGet("http://api.vipkpsq.com/v1/channel_detail?cid=" + type + "&ofs=" + offset + "&vcode=" + versionCode);
        return parserVideoDetialList(response);
    }

    public ArrayList<VideoInfo> requestVideoListRefresh(int type, int offset, boolean isRefresh) throws Exception {
        int versionCode = PackageUtils.getAppVersionCode(mContext);
        String url = "http://api.vipkpsq.com/v1/channel?cid=" + type + "&ofs=" + offset + "&vcode=" + versionCode;
        HttpRequest request = new HttpRequest(url);
        if (isRefresh) {
            request.setRequestProperty(HttpConstants.CACHE_CONTROL, "no-cache");
        }
        HttpResponse response = mHttpCache.httpGet(request);
        return parserVideoList(response);
    }

    public ArrayList<VideoInfo> parserVideoList(HttpResponse response) {
        if (response != null) {
            String result = response.getResponseBody();
            JSONArray data = getTargetArrayData(result);
            if (data != null) {
                ArrayList<VideoInfo> videoList = new ArrayList<VideoInfo>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jo = JSONUtils.getJsonObject(data, i, null);
                    if (jo != null) {
                        VideoInfo videoInfo = new VideoInfo();
                        videoInfo.vid = jo.optString("vid");
                        videoInfo.name = jo.optString("name");
                        videoInfo.cover = jo.optString("cover");
                        videoInfo.title = jo.optString("title");
                        videoInfo.rating = jo.optString("rating");
                        videoList.add(videoInfo);
                    }
                }
                if (videoList.size() > 0) {
                    return videoList;
                }
            }
        }
        return null;
    }

    public ArrayList<VideoDetial> searchShortVideo(String videoName, String authorName) throws Exception {
        videoName = StringUtils.nullStrToEmpty(videoName);
        authorName = StringUtils.nullStrToEmpty(authorName);
        String url = "http://api.vipkpsq.com/v1/short_video_search?q=" + videoName + "&d=" + authorName;
        HttpRequest request = new HttpRequest(url);
        HttpResponse response = mHttpCache.httpGet(request);
        return parserVideoDetialList(response);
    }

    public ArrayList<VideoDetial> parserVideoDetialList(HttpResponse response) {
        if (response != null) {
            String result = response.getResponseBody();
            JSONArray data = getTargetArrayData(result);
            if (data != null) {
                ArrayList<VideoDetial> videoList = new ArrayList<VideoDetial>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jo = JSONUtils.getJsonObject(data, i, null);
                    if (jo != null) {
                        VideoDetial videoInfo = new VideoDetial();
                        videoInfo.vid = jo.optString("vid");
                        videoInfo.name = jo.optString("name");
                        videoInfo.cover = jo.optString("cover");
                        videoInfo.desc = jo.optString("title");
                        videoInfo.rating = jo.optString("rating");
                        videoInfo.icon = jo.optString("icon");
                        videoInfo.directors = jo.optString("directors");
                        videoInfo.videoUrl = jo.optString("video_url");
                        videoList.add(videoInfo);
                    }
                }
                if (videoList.size() > 0) {
                    return videoList;
                }
            }
        }
        return null;
    }


    public ArrayList<VideoType> requestVideoType() throws Exception {
        HttpResponse response = mHttpCache.httpGet("http://api.vipkpsq.com/v1/channel_list");
        if (response != null) {
            String result = response.getResponseBody();
            JSONArray data = getTargetArrayData(result);
            if (data != null) {
                ArrayList<VideoType> typeList = new ArrayList<VideoType>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jo = JSONUtils.getJsonObject(data, i, null);
                    if (jo != null) {
                        VideoType videoType = new VideoType();
                        videoType.cid = jo.optInt("cid");
                        videoType.name = jo.optString("name");
                        typeList.add(videoType);
                    }
                }
                if (typeList.size() > 0) {
                    return typeList;
                }
            }
        }
        return null;
    }

    public VideoDetial requestVideoDetial(String vid) throws Exception {
        HttpResponse response = mHttpCache.httpGet("http://api.vipkpsq.com/v1/detail?vid=" + vid);

        if (response != null) {
            String result = response.getResponseBody();
            JSONObject data = getTargetData(result);
            if (data != null) {
                VideoDetial videoDetial = new VideoDetial();
                videoDetial.desc = data.optString("desc");
                videoDetial.name = data.optString("name");
                videoDetial.cover = data.optString("cover");
                videoDetial.actors = data.optString("actors");
                videoDetial.directors = data.optString("directors");
                videoDetial.area = data.optString("area");
                videoDetial.genres = data.optString("genres");
                videoDetial.rating = data.optString("rating");
                videoDetial.year = data.optString("year");
                try {
                    videoDetial.cid = Integer.parseInt(data.optString("cid"));
                } catch (Exception e) {
                    e.printStackTrace();
                    videoDetial.cid = 1;
                }
                JSONObject episodesJo = data.optJSONObject("episodes");
                if (episodesJo != null) {
                    videoDetial.episodes = episodesJo.toString();
                }

                videoDetial.vid = vid;
                JSONArray sourcesJo = JSONUtils.getJSONArray(data, "source", null);
                if (sourcesJo != null && sourcesJo.length() > 0) {
                    ArrayList<VideoSource> sources = new ArrayList<VideoSource>();
                    for (int i = 0; i < sourcesJo.length(); i++) {
                        JSONObject jo = sourcesJo.optJSONObject(i);
                        VideoSource source = new VideoSource();
                        source.site = jo.optString("site");
                        source.url = jo.optString("url");
                        source.defs = jo.optString("defs");
                        sources.add(source);
                    }
                    if (sources.size() > 0) {
                        videoDetial.sources = sources;
                    }
                }
                return videoDetial;
            }
        }
        return null;
    }

    private JSONArray getTargetArrayData(String str) {
        if (!StringUtils.isBlank(str)) {
            JSONObject resultObj = JSONUtils.toJsonObject(str);
            if (resultObj != null) {
                int c = resultObj.optInt("c", -1);
                if (c == 0) {
                    JSONArray data = resultObj.optJSONArray("data");
                    if (data != null && data.length() > 0) {
                        return data;
                    }
                }
            }
        }
        return null;
    }

    private JSONObject getTargetData(String str) {
        if (!StringUtils.isBlank(str)) {
            JSONObject resultObj = JSONUtils.toJsonObject(str);
            if (resultObj != null) {
                int c = resultObj.optInt("c", -1);
                if (c == 0) {
                    JSONObject data = resultObj.optJSONObject("data");
                    if (data != null && data.length() > 0) {
                        return data;
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<Account> requestAccount(String site) throws Exception {
        String url = "http://api.vipkpsq.com/v1/vip_account?site=" + site;
        HttpResponse response = mHttpCache.httpGet(url);
        if (response != null) {
            JSONArray data = getTargetArrayData(response.getResponseBody());
            if (data != null && data.length() > 0) {
                ArrayList<Account> accounts = new ArrayList<Account>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jo = data.optJSONObject(i);
                    if (jo != null) {
                        Account account = new Account();
                        account.site = site;
                        account.user = jo.optString("user");
                        account.password = jo.optString("password");
                        account.remark = jo.optString("remark", "");
                        accounts.add(account);
                    }
                }
                return accounts;
            }
        }
        return null;
    }

    public ArrayList<VideoInfo> requestFocuses() {
        String url = "http://api.vipkpsq.com/v1/focus";
        HttpResponse response = mHttpCache.httpGet(url);
        if (response != null) {
            JSONArray data = getTargetArrayData(response.getResponseBody());
            ArrayList<VideoInfo> videoInfos = new ArrayList<>();
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jo = JSONUtils.getJsonObject(data, i, null);
                    if (jo != null) {
                        VideoInfo videoInfo = new VideoInfo();
                        videoInfo.vid = jo.optString("vid");
                        videoInfo.title = jo.optString("title");
                        videoInfo.img = jo.optString("img");
                        videoInfos.add(videoInfo);
                    }
                }
                return videoInfos;
            }
        }
        return null;
    }

    public ArrayList<VideoDetial> requestSearch(String q) {
        try {
            q = URLEncoder.encode(q, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "http://api.vipkpsq.com/v1/search?q=" + q;
        HttpResponse response = mHttpCache.httpGet(url);
        if (response != null) {
            JSONArray data = getTargetArrayData(response.getResponseBody());
            ArrayList<VideoDetial> videoDetials = new ArrayList<>();
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jo = JSONUtils.getJsonObject(data, i, null);
                    if (jo != null) {
                        VideoDetial videoDetial = new VideoDetial();
                        videoDetial.vid = jo.optString("vid");
                        videoDetial.name = jo.optString("name");
                        videoDetial.cover = jo.optString("cover");
                        videoDetial.actors = jo.optString("actors");
                        videoDetial.area = jo.optString("area");
                        videoDetial.genres = jo.optString("genres");
                        videoDetial.cid = jo.optInt("cid");
                        videoDetials.add(videoDetial);
                    }
                }
                return videoDetials;
            }
        }
        return null;
    }

    public JSONObject requestM3u8Link(String weburl) throws Exception {
        String url = "http://api.vipkpsq.com/v1/parse/getVideo?url=" + URLEncoder.encode(weburl, "utf-8") + "&type=m3u8";
        LunaLog.d(url);
        HttpResponse response = mHttpCache.httpGet(url);
        if (response != null) {
            JSONObject data = getTargetData(response.getResponseBody());
            return data;
        }
        return null;
    }

    public String getUrlResponse(String url) {
        HttpResponse response = mHttpCache.httpGet(url);
        if (response != null) {
            return response.getResponseBody();
        }
        return "";
    }

    public JSONObject getUrlData(String url) {
        HttpResponse response = mHttpCache.httpGet(url);
        if (response != null) {
            if (!StringUtils.isBlank(response.getResponseBody())) {
                JSONObject resultObj = JSONUtils.toJsonObject(response.getResponseBody());
                if (resultObj != null) {
                    JSONObject data = resultObj.optJSONObject("data");
                    if (data != null) {
                        return data;
                    }
                }
            }
        }
        return null;
    }

    public List<TvLiveP> getTvLiveBeans(String type) {
        try {
            type = StringUtils.isBlank(type) ? "SATELLITE" : type;
            String apiUrl = "http://api2.kanketv.com/api/v1/epg/liveCate.json?appKey=34DB874AF269B539&appScrect=40&typeIds=" + type;
//            String jsonStr = HttpUtils.httpGetString(apiUrl);
            String jsonStr = mHttpCache.httpGetString(apiUrl);
            JSONObject json = JSONUtils.toJsonObject(jsonStr);
            JSONObject kanke = JSONUtils.getJsonObject(json, "kanke", null);
            JSONArray listAry = JSONUtils.getJsonArray(kanke, "list", null);
            JSONObject target = JSONUtils.getJsonObject(listAry, 0, null);
            JSONArray channels = JSONUtils.getJSONArray(target, "channels", null);
//            LunaLog.d(channels.toString());
            if (channels != null && channels.length() > 0) {
                List<TvLiveP> tvLiveList = new ArrayList<>();
                for (int i = 0; i < channels.length(); i++) {
                    JSONObject channel = JSONUtils.getJsonObject(channels, i, null);
                    if (channel != null) {
                        TvLiveP tvLive = new TvLiveP();
                        tvLive.channelId = channel.optString("channelId");
                        tvLive.channelName = channel.optString("zh_name");
                        tvLive.city = channel.optString("city");
                        tvLive.icon = channel.optString("icon");
                        tvLive.epgs = channel.optString("title");
                        JSONArray m3u8json = channel.optJSONArray("m3u8Json");
                        if (m3u8json != null) {
                            for (int j = 0; j < m3u8json.length(); j++) {
                                JSONObject sourceJo = m3u8json.optJSONObject(j);
                                if (sourceJo != null) {
                                    String classNameStr = sourceJo.optString("class_name");
                                    if (classNameStr.contains("cmvideo") || classNameStr.contains("lslive") || classNameStr.contains("m3u8Url")) {
                                        TvSource source = new TvSource();
                                        source.channel = sourceJo.optString("channel");
                                        source.className = classNameStr;
                                        tvLive.sourceList.add(source);
                                    }
                                }
                            }
                        }
                        if (ListUtils.getSize(tvLive.sourceList) > 0) {
                            tvLiveList.add(tvLive);
                        }
                    }
                }
                return tvLiveList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Record> getSearchTips(String searchStr) {
        try {
            if (!StringUtils.isBlank(searchStr)) {
                searchStr = URLEncoder.encode(searchStr, GlobalCharsets.UTF_8);
                String url = "http://api.tudou.com/doubao/soku/kwsuggest?kw=" + searchStr;
                String body = mHttpCache.httpGetString(url);
                if (!StringUtils.isBlank(body)) {
                    JSONObject jo = JSONUtils.toJsonObject(body);
                    JSONObject multiResult = jo.optJSONObject("multiResult");
                    if (multiResult != null) {
                        JSONArray results = multiResult.optJSONArray("results");
                        if (results != null && results.length() > 0) {
                            ArrayList<Record> resultsAry = new ArrayList<>();
                            for (int i = 0; i < results.length(); i++) {
                                Record record = new Record();
                                record.setTitle(results.optString(i));
                                resultsAry.add(record);
                            }
                            return resultsAry;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
