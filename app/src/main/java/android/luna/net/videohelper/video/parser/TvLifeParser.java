package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.bean.TvLife;
import android.luna.net.videohelper.bean.TvLiveP;
import android.luna.net.videohelper.bean.TvSource;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.ListUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindCallback;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by bintou on 16/3/24.
 */
public class TvLifeParser {


    String MIGU_URL = "http://app1.tv.cmvideo.cn:8088/migutv-clt/rLive.html?type=2&getway=[channel]&rate=2&plat=a&id=328&deviceId=865372028007927&cid=Xiaomi&cip=172.16.110.1810";
    String LS_URL = "http://play.kanketv.com/playerCode2.0/live/api?channel=[channel]&classname=lslive";

    private Context mContext;
    private TvLiveP mTvLive;
    private String backupUrl;

    public TvLifeParser(Context mContext, TvLiveP tvLive) {
        this.mContext = mContext;
        this.mTvLive = tvLive;
    }

    public Message run() {
        Message msg = new Message();
        msg.what = GlobalConstant.VIDEO_URL_RECEIVE;
        String url = null;
        try {
            ArrayList<TvSource> sourceList = (ArrayList<TvSource>) mTvLive.sourceList;
            LunaLog.d("size: " + sourceList.size());

            if (ListUtils.getSize(sourceList) > 0) {
                for (TvSource source : sourceList) {
                    if (source.className.equals("lslive") && (source.channel.contains("HD_1300"))) {
                        url = lsLiveParser(source);
                        if (!StringUtils.isBlank(url)) {
                            LunaLog.d("url:" + url);
                            msg.obj = url;
                            return msg;
                        }
                    }
                }
                for (TvSource source : sourceList) {
                    if (source.className.equals("lslive") && (source.channel.contains("HD_1800"))) {
                        url = lsLiveParser(source);
                        if (!StringUtils.isBlank(url)) {
                            LunaLog.d("url:" + url);
                            msg.obj = url;
                            return msg;
                        }
                    }
                }
                for (TvSource source : sourceList) {
                    if (source != null && source.className != null) {

                        if (source.className.equals("cmvideo")) {
                            url = migiLiveParser(source);
                        } else if (source.className.equals("lslive")) {
                            url = lsLiveParser(source);
//                        } else if (source.className.equals("m3u8Url")) {
//                            if (source.channel.contains(".m3u8") && StringUtils.isBlank(backupUrl)) {
//                                backupUrl = source.channel;
//                            }
                        }
                        if (!StringUtils.isBlank(url)) {
                            LunaLog.d("url:" + url);
                            msg.obj = url;
                            return msg;
                        }
                    }
                }


            }


        } catch (Exception e) {
            e.printStackTrace();
        }

//        msg.obj = backupUrl;
        return msg;
    }

    private String lsLiveParser(TvSource tvSource) {
        try {
            tvSource.channel = tvSource.channel.replace("-s", "");
            String url = LS_URL.replace("[channel]", tvSource.channel);
            LunaLog.d(url);
            String kankeStr = HttpUtils.httpGetString(url);
            JSONObject kankeJo = JSONUtils.toJsonObject(kankeStr);
            if (kankeJo != null) {
                String playUrl = kankeJo.optString("playurl");
                String lsStr = HttpUtils.httpGetString(playUrl);
                JSONObject lsJo = JSONUtils.toJsonObject(lsStr);
                String location = lsJo.optString("location");
                LunaLog.d("location: " + location);
                if (location != null && !location.contains("stream_id=lb_error")) {
                    return location;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String migiLiveParser(TvSource tvSource) {
        try {
            String url = MIGU_URL.replace("[channel]", tvSource.channel);
            String kankeStr = HttpUtils.httpGetString(url);
            if (!StringUtils.isBlank(kankeStr)) {
                JSONObject jo = JSONUtils.toJsonObject(kankeStr);
                if (jo != null) {
                    url = jo.optString("playUrl");
                    return url;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
