package android.luna.net.videohelper.connection;

import android.content.Context;
import android.luna.net.videohelper.Ninja.Database.Record;
import android.luna.net.videohelper.bean.Account;
import android.luna.net.videohelper.bean.TvLiveP;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelper.bean.VideoInfo;
import android.luna.net.videohelper.bean.VideoType;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.ListUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by bintou on 15/11/2.
 */
public class VideoCatchManager {

    private List<VideoRequestListener> mListeners;

    private static VideoCatchManager mInstanct;

    private ArrayList<VideoType> videoTypes;
    private ArrayList<VideoInfo> focusList;

    private Context mContext;
    private VideoRequester mVideoRequester;


    private VideoCatchManager(Context context) {
        mContext = context;
        mVideoRequester = new VideoRequester(mContext);
        videoTypes = new ArrayList<VideoType>();
        mListeners = new ArrayList<VideoRequestListener>();
    }

    public static VideoCatchManager getInstanct(Context context) {
        if (mInstanct == null) {
            mInstanct = new VideoCatchManager(context);
        }
        return mInstanct;
    }


    public void asyRequestData() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<VideoType> tempList = mVideoRequester.requestVideoType();

                    if (tempList != null) {
                        videoTypes.clear();
                        for (int i = 0; i < tempList.size(); i++) {
                            VideoType videoType = tempList.get(i);
                            videoType.infoList = mVideoRequester.requestVideoList(videoType.cid, 0);
                            if (videoType.infoList != null && videoType.infoList.size() > 0) {
                                videoTypes.add(videoType);
                                //通知
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = videoType;
                                mHandler.sendMessage(msg);
                            }
                        }
                        if (videoTypes.size() > 0) {
                            mHandler.sendEmptyMessage(2);
                        } else {
                            mHandler.sendEmptyMessage(3);
                        }
                    } else {
                        mHandler.sendEmptyMessage(3);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (!ListUtils.isEmpty(mListeners))
                switch (msg.what) {
                    case 1:
                        for (VideoRequestListener listener : mListeners) {
                            if (listener != null) {
                                VideoType videoType = (VideoType) msg.obj;
                                listener.onRequestUpdate(videoType);
                            }
                        }
                        break;
                    case 2:
                        if (videoTypes.size() > 0) {
                            for (VideoRequestListener listener : mListeners) {
                                if (listener != null) {
                                    listener.onRequestAllData(videoTypes);
                                }
                            }
                        }
                        break;
                    case 3:
                        if (videoTypes.size() > 0) {
                            for (VideoRequestListener listener : mListeners) {
                                if (listener != null) {
                                    listener.onRequestFail();
                                }
                            }
                        }
                        break;
                    case 4:
                        for (VideoRequestListener listener : mListeners) {
                            if (listener != null) {
                                listener.onGetFocuses(focusList);
                            }
                        }
                        break;
                    default:
                        break;
                }

        }
    };

    /**
     * 异步请求视频列表s
     *
     * @param cid
     * @param offset
     * @return
     */
    public ArrayList<VideoInfo> getVideoInfos(int cid, int offset) {
        try {
            ArrayList<VideoInfo> infoList = mVideoRequester.requestVideoList(cid, offset);
            return infoList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 异步请求
     *
     * @param cid
     * @param offset
     * @return
     */
    public ArrayList<VideoDetial> getVideoChannelDetial(int cid, int offset, boolean isRefresh) {
        try {
            ArrayList<VideoDetial> infoList = mVideoRequester.requestVideoChannelDetial(cid, offset, isRefresh);
            return infoList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<VideoDetial> catchSearchShortVideo(String q, String d) {
        try {
            return mVideoRequester.searchShortVideo(q, d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 异步请求视频列表s,非缓存
     *
     * @param cid
     * @param offset
     * @return
     */
    public ArrayList<VideoInfo> getVideoInfosRefresh(int cid, int offset, boolean isRefresh) {
        try {
            ArrayList<VideoInfo> infoList = mVideoRequester.requestVideoListRefresh(cid, offset, isRefresh);

            return infoList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 请求视频源，耗时方法，异步使用
     *
     * @param vid
     * @return
     */
    public VideoDetial getVideoDetial(String vid) {
        try {
            VideoDetial videoDetial = mVideoRequester.requestVideoDetial(vid);
            return videoDetial;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请求封面内容
     *
     * @return
     */
    public void asyCatchFocus() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                focusList = mVideoRequester.requestFocuses();
                Message msg = new Message();
                msg.what = 4;
                mHandler.sendMessage(msg);
            }
        });
    }

    public ArrayList<VideoDetial> catchSearchResult(String q) {
        return mVideoRequester.requestSearch(q);
    }

    public ArrayList<Account> getAccount(String site) {
        try {
            return mVideoRequester.requestAccount(site);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 内部解析接口，请求M3U8地址
     */
    public JSONObject getM3u8Link(String webUrl) {
        try {
            return mVideoRequester.requestM3u8Link(webUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<VideoType> getCurrentVideoTypeList() {
        return videoTypes;
    }

    public ArrayList<VideoInfo> getFocusList() {
        return focusList;
    }

    public void setFocusList(ArrayList<VideoInfo> focusList) {
        this.focusList = focusList;
    }

    public void registerVideoRequestListener(VideoRequestListener listener) {
        synchronized (mListeners) {
            if (listener != null) {
                mListeners.add(listener);
            }
        }
    }

    public void unRegisterVideoRequestListener(VideoRequestListener listener) {
        synchronized (mListeners) {
            if (listener != null) {
                int index = mListeners.indexOf(listener);
                if (index >= 0) {
                    mListeners.remove(index);
                }
            }
        }
    }

    public String catchUrlResult(String url) {
        try {
            return mVideoRequester.getUrlResponse(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public JSONObject catchUrlData(String url) {
        try {
            return mVideoRequester.getUrlData(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void playVideoInWeb(final String downloadUrl, final Map<String, String> map) {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String url = "http://jiasugou.tv/bind";
                if (!StringUtils.isBlank(downloadUrl)) {
                    String type = judgeType(downloadUrl);
                    map.put("type", type);
                }
                HttpUtils.httpPostString(url, map);
            }
        });
    }

    private static String judgeType(String link) {
        String type = "";
        String contentType = "";
        HttpURLConnection con = null;
        try {
            LunaLog.d("link:  " + link);
            URL url = new URL(link);
            con = (HttpURLConnection) url.openConnection();
            contentType = con.getContentType();
            LunaLog.d("contentType:      " + contentType);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
            if (contentType != null) {
                if (contentType.contains("video/mp4") || contentType.contains("application/octet-stream")) {
                    type = "mp4";
                } else {
                    type = "m3u8";
                }
                if (link.contains(".m3u8")) {
                    type = "m3u8";
                }
            }
        }
        return type;
    }

    public static void uploadRecord(final String action, final String content) {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String url = "http://api.vipkpsq.com/v1/up";
                Map<String, String> map = new HashMap<String, String>();
                map.put("action", action);
                map.put("content", content);
                HttpUtils.httpPostString(url, map);
            }
        });
    }

    public List<TvLiveP> cctvTvLives;
    public List<TvLiveP> satelliteTvLives;
    public List<TvLiveP> placeTvLives;


    public List<TvLiveP> getTvLiveBeans(String type) {
        if ("CCTV".equals(type)) {
            if (ListUtils.getSize(cctvTvLives) > 0) {
                return cctvTvLives;
            }
        } else if ("SATELLITE".equals(type)) {
            if (ListUtils.getSize(satelliteTvLives) > 0) {
                return satelliteTvLives;
            }
        } else if ("PLACE".equals(type)) {
            if (ListUtils.getSize(placeTvLives) > 0) {
                return placeTvLives;
            }
        }
        List<TvLiveP> tvLiveList = mVideoRequester.getTvLiveBeans(type);
        if (ListUtils.getSize(tvLiveList) > 0) {
            if ("CCTV".equals(type)) {
                cctvTvLives = tvLiveList;
            } else if ("SATELLITE".equals(type)) {
                satelliteTvLives = tvLiveList;
            } else if ("PLACE".equals(type)) {
                placeTvLives = tvLiveList;
            }
        }
        return tvLiveList;
    }

    public List<TvLiveP> getSearchTvLive(String searchStr) {
        List<TvLiveP> resultTvLives = new ArrayList<>();
        if (ListUtils.getSize(satelliteTvLives) > 0) {
            for (TvLiveP tvLive : satelliteTvLives) {
                if (tvLive != null && tvLive.channelName != null && tvLive.channelName.contains(searchStr)) {
                    resultTvLives.add(tvLive);
                }
            }
        }
        if (ListUtils.getSize(cctvTvLives) > 0) {
            for (TvLiveP tvLive : cctvTvLives) {
                if (tvLive != null && tvLive.channelName != null && tvLive.channelName.contains(searchStr)) {
                    resultTvLives.add(tvLive);
                }
            }
        }
        if (ListUtils.getSize(placeTvLives) > 0) {
            for (TvLiveP tvLive : placeTvLives) {
                if (tvLive != null && tvLive.channelName != null && tvLive.channelName.contains(searchStr)) {
                    resultTvLives.add(tvLive);
                }
            }
        }

        return resultTvLives;
    }

    public ArrayList<Record> getSearchTips(String searchStr) {
        return mVideoRequester.getSearchTips(searchStr);
    }

}
