package android.luna.net.videohelper.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.luna.net.videohelper.adapter.VideoEpisodeAdapter;
import android.luna.net.videohelper.bean.TvLiveP;
import android.luna.net.videohelper.download.VideosDownloadService;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelper.video.parser.BesTvParser;
import android.luna.net.videohelper.video.parser.CloudVideoParser;
import android.luna.net.videohelper.video.parser.IqiyiParser;
import android.luna.net.videohelper.video.parser.UrlParseHelper;
import android.luna.net.videohelper.widget.EpisodeLayout;
import android.luna.net.videohelptools.R;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.NetWorkUtil;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.ScreenUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;
import net.luna.common.util.ToastUtils;
import net.luna.common.view.widget.Dialog;

import org.json.JSONArray;
import org.json.JSONObject;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.VideoSegsBean;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.UsetMediaContoller;
import io.vov.vitamio.widget.VideoView;


/**
 * Created by bintou on 16/2/22.
 */
public class VideoActivity extends BaseActivity implements View.OnClickListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {


    private Uri uri;
    private VideoView mVideoView;
    private View mLoadBufferView;
    private TextView mLoadBufferTextView;
    private View mLoadView;
    private TextView mLoadText;
    private RelativeLayout mTipsImg;

    private VideoPlayHandler mHandler;
    //清晰度
    private int mDefinition = GlobalConstant.DEF_SUPER;

    private String mWebUrl;
    private String mTargetUrl = "";
    private String mTitle;
    private String mSite;
    private long mPosition = 0;
    private boolean mIsFile;
    private IqiyiParser mIqiyiParser;


    private String[] defArrayVideoJj = {"high", "normal", "super", "original", "low"};

    private boolean isParserReceive = false;
    private boolean justPlayTarget = false;
    private boolean firstStart = true;
    private Dialog deleteDialog;
    private UsetMediaContoller mMediaController;
    private TvLiveP mTvLive;

    //选集
    private EpisodeLayout mEpisodesLayout;
    private VideoEpisodeAdapter mEpisodeAdapter;

    private JSONArray episodes;
    private boolean isMember;
    private String content;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        setContentView(R.layout.activity_video);
        mVideoView = (VideoView) findViewById(R.id.video);
        mMediaController = new UsetMediaContoller(this);
        mVideoView.setMediaController(mMediaController);
        mVideoView.requestFocus();
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);

        mVideoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        mLoadView = findViewById(R.id.sdk_ijk_progress_bar_layout);
        mLoadText = (TextView) findViewById(R.id.sdk_ijk_progress_bar_text);
        mLoadBufferView = findViewById(R.id.sdk_load_layout);
        mLoadBufferTextView = (TextView) findViewById(R.id.sdk_sdk_ijk_load_buffer_text);
        mLoadBufferTextView.setTextColor(getResources().getColor(R.color.text_progress_tv));
        mEpisodesLayout = (EpisodeLayout) findViewById(R.id.layout_episodes);

        mHandler = new VideoPlayHandler(Looper.getMainLooper());

        mWebUrl = getIntent().getStringExtra("url");
        //        mVideoView.setHudView(mHudView);
        mTitle = getIntent().getStringExtra("name");
        content = getIntent().getStringExtra("content");
        mVideoView.setTitle(mTitle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        boolean isHardWare = PreferencesUtils.getBoolean(mContext, "sp_is_hardware_decode", false);
        if (isHardWare) {
            mVideoView.setHardwareDecoder(true);
        } else {
            mVideoView.setHardwareDecoder(false);
        }

        GlobalConstant.IQIYI_VIP_DOMAIN = PreferencesUtils.getString(mContext, GlobalConstant.SP_IQIYI_VIP_DOMAIN);

        int width = ScreenUtils.heightPixels(mContext);
        if (width <= 480) {
            mDefinition = GlobalConstant.DEF_NORMAL;
        } else if (width < 1080) {
            mDefinition = GlobalConstant.DEF_HIGH;
        } else {
            mDefinition = GlobalConstant.DEF_SUPER;
        }

        mIsFile = getIntent().getBooleanExtra("isFile", false);

        int visit_time = PreferencesUtils.getInt(this, GlobalConstant.KEY_VIDEO_VISIT_TIMES, 0);
        visit_time += 1;
        mSite = getIntent().getStringExtra("site");
        PreferencesUtils.putInt(this, GlobalConstant.KEY_VIDEO_VISIT_TIMES, visit_time);


        isParserReceive = false;

        String episodesStr = getIntent().getStringExtra("episod" +
                "es");
        episodes = JSONUtils.toJsonArray(episodesStr);
        if (episodes != null && episodes.length() > 0) {
            TextView episodeTv = mMediaController.getEpisodeBtn();
            episodeTv.setVisibility(View.VISIBLE);
            episodeTv.setOnClickListener(mEpisodeOnClickListener);
            mEpisodeAdapter = new VideoEpisodeAdapter(mContext, episodes);
            GridView gridView = (GridView) findViewById(R.id.gridview_episode);
            int num = getIntent().getIntExtra("position", 0);
            mEpisodeAdapter.setCurEpisode(num);
            gridView.setAdapter(mEpisodeAdapter);
            gridView.setOnItemClickListener(mEpisodeOnItemClickListener);
            gridView.smoothScrollToPosition(num);
        }
        String sitename = getIntent().getStringExtra("sitename");
        if (sitename != null && sitename.contains("磁力链")) {
            mLoadText.setText(getResources().getString(R.string.video_loading_magnet));
        }
        mTipsImg = (RelativeLayout) findViewById(R.id.layout_tips_video);
        if (!PreferencesUtils.getBoolean(mContext, GlobalConstant.SP_HAS_SHOW_TIPS)) {
            mTipsImg.setVisibility(View.VISIBLE);
        } else {
            playVideo();
        }

//        IqiyiParser iqiyiParser = new IqiyiParser(mContext, mWebUrl, mHandler);
//        iqiyiParser.startParser();
    }

    public void playVideo() {
        if (mIsFile) {
            mTargetUrl = mWebUrl;
            LunaLog.d("file: " + mTargetUrl);
            mVideoView.setVideoPath(mTargetUrl);
        } else {
            mTargetUrl = getIntent().getStringExtra("targetUrl");

            if (StringUtils.isBlank(mTargetUrl)) {
                LunaLog.d("site : " + mSite);
                parseTargetUrl();
            } else {
                LunaLog.d("targetUrl: " + mTargetUrl);
                justPlayTarget = true;
                mVideoView.setVideoPath(mTargetUrl);
//                String[] urls = {"", ""};
//                mVideoView.setVideoUrls(urls,null);
            }
        }
    }

    private AdapterView.OnItemClickListener mEpisodeOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                JSONObject eplsode = JSONUtils.getJsonObject(episodes, position, null);
                if (eplsode != null) {
                    String url = eplsode.optString("url");
                    String num = eplsode.optString("num");
                    boolean dog = eplsode.optBoolean("dog", false);
                    if (!StringUtils.isBlank(mTitle) && mTitle.length() > 3) {
                        mTitle = mTitle.substring(0, mTitle.indexOf("第") + 1).concat(num + "集");
                    }
                    if (mEpisodeAdapter != null) {
                        mEpisodeAdapter.setCurEpisode(position);
                        mEpisodeAdapter.notifyDataSetChanged();
                    }
                    mVideoView.setTitle(mTitle);
                    if (dog) {
                        mTargetUrl = url;
                    } else {
                        mWebUrl = url;
                    }
                    playVideo();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener mEpisodeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mEpisodesLayout.getVisibility() == View.GONE) {
                mEpisodesLayout.setVisibility(View.VISIBLE);
            } else {
                mEpisodesLayout.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 解析最终目标地址
     */
    private void parseTargetUrl() {
        justPlayTarget = false;
        try {
            if (GlobalConstant.SITE_TV_LIVE.equals(mSite)) {
                TvLiveP tvLive = (TvLiveP) getIntent().getSerializableExtra("tvLive");
                UrlParseHelper urlParseHelper = new UrlParseHelper(this, tvLive, mHandler);
                ThreadUtils.execute(urlParseHelper);
            } else {
                mSite = StringUtils.nullStrToEmpty(mSite);
                if (mSite.equals(GlobalConstant.SITE_IQIYI)) {
                    mWebUrl = getIntent().getStringExtra("webUrl");
                    if (StringUtils.isBlank(mWebUrl)) {
                        mWebUrl = getIntent().getStringExtra("url");
                    }
                }
                LunaLog.e("mWebUrl: " + mWebUrl);
                String vid = getIntent().getStringExtra("vid");
                UrlParseHelper urlParseHelper = new UrlParseHelper(this, mWebUrl, mSite, vid, mDefinition, content, mHandler);
                ThreadUtils.execute(urlParseHelper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void iqiyiJsParser() {
        mWebUrl = mWebUrl.replace("www.", "m.");
        mIqiyiParser = new IqiyiParser(this, mWebUrl, mHandler);
        addContentView(mIqiyiParser.getWebView(), new RelativeLayout.LayoutParams(-2, -2));
        mIqiyiParser.startParser();
    }

    boolean hasClickOnce = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.media_controller_download:
                if (NetWorkUtil.isWifiAvailable()) {
                    downloadVideo();
                } else {
                    showDeleteDialog();
                }
                break;
            case R.id.btn_episode_back:
                if (mEpisodesLayout.getVisibility() == View.VISIBLE) {
                    mEpisodesLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_knowed:
                mTipsImg.setVisibility(View.GONE);
                PreferencesUtils.putBoolean(mContext, GlobalConstant.SP_HAS_SHOW_TIPS, true);
                playVideo();
                break;
        }
    }


    public void showDeleteDialog() {
        if (deleteDialog == null) {
            deleteDialog = new Dialog(mContext, getResources().getString(R.string.video_download), getResources().getString(R.string.download_not_in_wifi) + "《" + mTitle + "》？");
            deleteDialog.addCancelButton(getResources().getString(R.string.cancel));
        }
        deleteDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVideo();
                deleteDialog.dismiss();
            }
        });
        deleteDialog.show();
    }

    private void downloadVideo() {
        Intent i = new Intent(mContext, VideosDownloadService.class);
        i.setAction(Intent.ACTION_INSERT);
        Uri uri = mVideoView.getUri();
        i.setData(uri);
        i.putExtra(Intent.EXTRA_TITLE, mTitle);
        startService(i);
        UploadEventRecord.recordEvent(mContext, GlobalConstant.FUNTION_EVENT, GlobalConstant.P_VIDEO_CLICK, "离线");
    }

    /**
     * 每段清晰度的地址
     */
    JSONObject mSegsJo;

    public boolean parseJsonAndPlayVideo(String str) {
        try {
            VideoSegsBean bean = new VideoSegsBean();
            JSONObject jo = JSONUtils.toJsonObject(str);
            if (jo != null) {
                isMember = jo.optBoolean("isMember", false);
                String site = jo.optString("site", "");
                bean.setSite(site);
                mSegsJo = jo.optJSONObject("segs");
                if (mSegsJo != null) {
                    boolean hasUrl = false;
                    for (String def : defArrayVideoJj) {
                        String videoUrl = mSegsJo.optString(def);
                        if (!StringUtils.isBlank(videoUrl)) {
                            hasUrl = true;
                            if (def.equals("original")) {
                                bean.setM1080p(videoUrl);
                            } else if (def.equals("super")) {
                                bean.setM720p(videoUrl);
                            } else if (def.equals("high")) {
                                bean.setM480p(videoUrl);
                            } else if (def.equals("normal")) {
                                bean.setM320p(videoUrl);
                            } else if (def.equals("low")) {
                                bean.setM240p(videoUrl);
                            }
                        }
                    }
                    if (hasUrl) {
                        mVideoView.setVideoBean(bean, mDefinition);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                mLoadView.setVisibility(View.VISIBLE);
                mLoadText.setText("loading");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mLoadView.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                if (mMediaController.isShowing()) {
                    String speed = "" + extra + "kb/s" + "  ";
                    if (mMediaController != null) {
                        mMediaController.setSpeed(speed);
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (firstStart) {
            if (mPosition == 0) {
                long exitPosition = PreferencesUtils.getLong(mContext, mWebUrl, 0);
                mVideoView.seekTo(exitPosition);
            }
        }
        if (mIsFile) {
            if (mMediaController != null) {
                mMediaController.hideDownloadBtn();
            }
        }
        if (GlobalConstant.SITE_TV_LIVE.equals(mSite)) {
            mMediaController.hideDownloadBtn();
            mMediaController.hideBottomBar();
        }
        firstStart = false;
        mLoadView.setVisibility(View.GONE);

        if (!StringUtils.isBlank(mTargetUrl) && mTargetUrl.startsWith("http")) {
            mVideoView.setBufferSize(512 * 1024);
        }
        LunaLog.d("onprepare: " + mSite);
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        try {
            LunaLog.e("open Fail");

            if (mSite != null && (GlobalConstant.SITE_YOUKU.equals(mSite) || GlobalConstant.SITE_TUDOU.equals(mSite))) {
                //看客解析出来的视频有时候看不到，这个时候换成后备
                mTargetUrl = StringUtils.nullStrToEmpty(mTargetUrl);
                LunaLog.d("backupUrl = " + mTargetUrl);
                if (mVideoView != null && mVideoView.getUri() != null && !mVideoView.getUri().toString().equals(mTargetUrl)) {
                    mTargetUrl = StringUtils.nullStrToEmpty(mTargetUrl);
                    mVideoView.setVideoURI(Uri.parse(mTargetUrl));
                    if (mMediaController != null)
                        mMediaController.hideMessBtn();
                    mVideoView.start();
                    return true;
                }
            }
            if (mSite != null && (GlobalConstant.SITE_IQIYI.equals(mSite))) {
                mSite = GlobalConstant.SITE_CLOUD_VIDEO;
//                mWebUrl = GlobalConstant.IQIYI_VIP_DOMAIN + "/?vid=" + mWebUrl.replace("m.", "www.");
                LunaLog.d("weburl: " + mWebUrl);
                ThreadUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        CloudVideoParser cloudVideoParser = new CloudVideoParser(mContext, mWebUrl, GlobalConstant.SITE_IQIYI, mDefinition);
                        Message msg = null;
//                        if (!isMember) {
                        msg = cloudVideoParser.run();
                        LunaLog.d("url: " + msg.obj.toString());
//                        }
                        mHandler.sendMessage(msg);
                    }
                });
                return true;
            }
            if (mSite != null && (GlobalConstant.SITE_BESTV.equals(mSite))) {
                mSite = "";
                ThreadUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        BesTvParser besTvParser = new BesTvParser(mContext, mVideoView.getUri().toString(), mDefinition);
                        Message msg = besTvParser.run();
                        mHandler.sendMessage(msg);
                    }
                });
                return true;
            }

            if (GlobalConstant.SITE_CLOUD_VIDEO.equals(mSite) && justPlayTarget) {
                if (mVideoView != null) {
                    parseTargetUrl();
                }
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (episodes != null && episodes.length() > 0) {
            ToastUtils.show(mContext, getResources().getString(R.string.next_episode));
            int position = 0;
            if (mEpisodeAdapter != null) {
                position = mEpisodeAdapter.getCurEpisode() - 1;
            }
            JSONObject eplsode = JSONUtils.getJsonObject(episodes, position, null);
            if (eplsode != null) {
                String url = eplsode.optString("url");
                String num = eplsode.optString("num");
                boolean dog = eplsode.optBoolean("dog", false);
                if (!StringUtils.isBlank(mTitle) && mTitle.length() > 3) {
                    mTitle = mTitle.substring(0, mTitle.indexOf("第") + 1).concat(num + "集");
                }
                if (mEpisodeAdapter != null) {
                    mEpisodeAdapter.setCurEpisode(position);
                    mEpisodeAdapter.notifyDataSetChanged();
                }
                mVideoView.setTitle(mTitle);
                if (dog) {
                    mTargetUrl = url;
                } else {
                    mWebUrl = url;
                }
                playVideo();
            }
        }
    }

    private class VideoPlayHandler extends Handler {
        public VideoPlayHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            try {
                if (msg.what == GlobalConstant.VIDEO_URL_RECEIVE) {
                    if (mVideoView != null) {
                        if (msg.arg2 == 1003) {
                            //看客失效，使用原来的解析方法。
                            iqiyiJsParser();
                            return;
                        }
                        String url = (String) msg.obj;

                        Bundle bundle = msg.getData();
                        String jsonStr = bundle.getString("segs");
                        if (!StringUtils.isBlank(jsonStr)) {
                            LunaLog.d(jsonStr);
                            if (parseJsonAndPlayVideo(jsonStr)) {
                                mTargetUrl = url;
                                return;
                            }
                        }

                        if (StringUtils.isBlank(url) && !mVideoView.isPlaying()) {
                            return;
                        }
                        mTargetUrl = StringUtils.nullStrToEmpty(mTargetUrl);
                        if (!mTargetUrl.equals(url)) {
                            mPosition = mVideoView.getCurrentPosition() > 0 ? (mVideoView.getCurrentPosition() / 1000) : 0;
                            if (mVideoView.isPlaying()) {
                                mVideoView.suspend();
                            }
                            url = StringUtils.nullStrToEmpty(url);
                            if (mVideoView.isPlaying()) {
                            }
                            mVideoView.seekTo(Long.valueOf(mPosition));
                            uri = Uri.parse(url);
                            if (mMediaController != null)
                                mMediaController.hideMessBtn();
                            prepare();
                            LunaLog.d("targetUrl:" + url);
                            mVideoView.setVideoURI(uri);
                            mTargetUrl = url;
                            mVideoView.start();
                        } else {
                            //返回error
                            uri = Uri.parse("error");
                            mVideoView.setVideoURI(uri);
                            mVideoView.start();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void prepare() {
        isParserReceive = true;
        if (mSite.equals(GlobalConstant.SITE_CLOUD_VIDEO)) {
            mLoadText.setText(getResources().getString(R.string.video_loading_magnet));
        } else {
            mLoadText.setText(getResources().getString(R.string.video_loading));
        }
    }

    @Override
    public void finish() {
        try {
            if (mVideoView != null && mVideoView.getCurrentPosition() > 0) {
                Intent intent = getIntent();
                intent.putExtra("play_time", mVideoView.getCurrentPosition());
                intent.putExtra("name", mTitle);
                intent.putExtra("url", mWebUrl);
                if (GlobalConstant.SITE_CLOUD_VIDEO.equals(mSite)) {
                    intent.putExtra("targetUrl", mTargetUrl);
                }
                setResult(GlobalConstant.RESULT_VISIT_SUCCESS, intent);
            }

            super.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
            mVideoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            PreferencesUtils.putLong(this, mWebUrl, mVideoView.getCurrentPosition());
            if (mHandler != null) {
//                mHandler.removeMessages(GlobalConstant.VIDEO_URL_PARSER_TIME_OUT);
                mHandler.removeMessages(GlobalConstant.VIDEO_URL_RECEIVE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}


