package android.luna.net.videohelper.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.luna.net.videohelper.Ninja.Database.Record;
import android.luna.net.videohelper.Ninja.Database.RecordAction;
import android.luna.net.videohelper.Ninja.Unit.ViewUnit;
import android.luna.net.videohelper.adapter.VideoIntroduceAdapter;
import android.luna.net.videohelper.bean.CloudVideo;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelper.bean.VideoSource;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.download.VideosDownloadService;
import android.luna.net.videohelper.fragment.EpisodeFragment;
import android.luna.net.videohelper.fragment.IntroduceFragment;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelper.widget.DefinitionDialog;
import android.luna.net.videohelper.widget.ShareDialog;
import android.luna.net.videohelptools.R;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dtr.zxing.activity.CaptureActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.CompatUtils;
import net.luna.common.util.FastBlurUtil;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;
import net.luna.common.util.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;


/**
 * Created by bintou on 15/11/4.
 */
public class VideoIntroduceActivity extends FragmentActivity implements View.OnClickListener {

    //fragment
    IntroduceFragment mIntroduceFragment;
    EpisodeFragment mEpisodeFragment;

    ImageLoader imageLoader;

    private Context mContext;

    private VideoCatchManager mVideoCatchManager;
    private String mVid;
    private VideoDetial mVideoDetial;
    private ViewPager mViewPager;
    private VideoIntroduceAdapter mPageAdapter;

    private LinearLayout mProgressBar;

    Spinner mSpinner;

    private int mVideoType;

    private final int[] iconArray = {R.mipmap.zdy_ic_mango, R.mipmap.zdy_ic_aiqiyi, R.mipmap.zdy_ic_letv, R.mipmap.zdy_ic_tudou,
            R.mipmap.zdy_ic_youku, R.mipmap.zdy_ic_vqq, R.mipmap.zdy_ic_fengxing, R.mipmap.zdy_ic_huashu, R.mipmap.zdy_ic_default, R.mipmap.zdy_ic_default};
    private final String[] platformArray = {"芒果", "爱奇艺", "乐视", "土豆", "优酷", "腾讯视频", "风行网", "华数TV", "其他", "其他源"};
    private final String[] siteArrays = {"mango", "iqiyi", "letv", "tudou", "youku", "qq", "fun", "wasu", "bestv"};

    private ShareDialog mShareDialog;

    private ImageButton mBookMarkBtn;

    private CloudVideo mCloudVideo;
    private DisplayImageOptions options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_introduce);
        mContext = this;
        UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "详情页");
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.mipmap.zdy_img_default_200x280)
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();

        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mVid = getIntent().getStringExtra("vid");
        mCloudVideo = (CloudVideo) getIntent().getSerializableExtra("cloudVideo");
        mProgressBar = (LinearLayout) findViewById(R.id.progressbar);
        mShareDialog = new ShareDialog(mContext);
        mBookMarkBtn = (ImageButton) findViewById(R.id.btn_collect);
        selectedColor = CompatUtils.getColor(mContext, R.color.text_black);
        unSelectedColor = CompatUtils.getColor(mContext, R.color.text_black_secondary);

        loadVideoData();
        updateBookmarks();
    }

    private void showDetial() {
        findViewById(R.id.video_introduce_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.video_introduce_viewpager).setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        checkVideoType();
        initIntroduce();
        initviewPager();
        LunaLog.e("" + mVideoDetial.vid);
    }

    private void cloudToDetial() {
        mVideoDetial = new VideoDetial();
        mVideoDetial.cid = mCloudVideo.cid;
        mVideoDetial.name = mCloudVideo.name;
        mVideoDetial.actors = mCloudVideo.actors;
        if (!StringUtils.isBlank(mCloudVideo.year))
            mVideoDetial.year = mCloudVideo.year.replace("\n", "");
        mVideoDetial.desc = mCloudVideo.desc;
        mVideoDetial.area = mCloudVideo.area;
        mVideoDetial.cover = mCloudVideo.cover;
        mVideoDetial.vid = mCloudVideo.getObjectId();
        mVideoDetial.genres = mCloudVideo.genres;
        mVideoDetial.rating = mCloudVideo.rating;
        JSONArray playLinks = JSONUtils.toJsonArray(mCloudVideo.playLinks);
        List<VideoSource> videoSources = new ArrayList<>();
        if (playLinks != null && playLinks.length() > 0) {
            for (int i = 0; i < playLinks.length(); i++) {
                VideoSource videoSource = new VideoSource();
                JSONObject jo = JSONUtils.getJsonObject(playLinks, i, null);
                if (jo != null) {
                    videoSource.site = GlobalConstant.SITE_CLOUD_VIDEO;
                    videoSource.siteName = jo.optString("site");
                    videoSource.url = jo.optString("url");
                    videoSource.targetUrl = jo.optString("targetUrl");
                    videoSources.add(videoSource);
                }
            }
        }
        mVideoDetial.sources = (ArrayList<VideoSource>) videoSources;
    }

    private void loadVideoData() {
        if (mCloudVideo != null) {
            cloudToDetial();
            showDetial();
        } else {
            ThreadUtils.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        mVideoDetial = mVideoCatchManager.getVideoDetial(mVid);
                                        if (mVideoDetial != null && mVideoDetial.sources != null && mVideoDetial.sources.size() > 0) {
                                            ThreadUtils.runInUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    showDetial();
                                                }
                                            });
                                        } else {
                                            BmobQuery<CloudVideo> query = new BmobQuery<CloudVideo>();
                                            query.getObject(mContext, mVid, new GetListener<CloudVideo>() {
                                                        @Override
                                                        public void onSuccess(final CloudVideo cloudVideo) {
                                                            ThreadUtils.runInUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (cloudVideo != null) {
                                                                        mCloudVideo = cloudVideo;
                                                                        cloudToDetial();
                                                                        showDetial();
                                                                    }
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(int i, String s) {

                                                        }
                                                    }

                                            );
                                        }
                                    }
                                }

            );
        }
    }

    public void checkVideoType() {
        int cid = mVideoDetial.cid;
        if (cid == 1 || cid == 8 || cid == 9) {
            mVideoType = VideoIntroduceAdapter.TYPE_FILM;
        } else if (cid == 2 || cid == 3) {
            mVideoType = VideoIntroduceAdapter.TYPE_EPISODE;
        } else if (cid == 4) {
            mVideoType = VideoIntroduceAdapter.TYPE_VARIETY;
        } else {
            mVideoType = VideoIntroduceAdapter.TYPE_OTHER;
        }
    }

    private void initIntroduce() {
        try {

            ImageView thumbnail = (ImageView) findViewById(R.id.video_thumbnail);
            ImageLoader imageLoader = ImageLoader.getInstance();

            imageLoader.displayImage(mVideoDetial.cover, thumbnail, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    ImageView blurImgView = (ImageView) findViewById(R.id.img_blur);

                    //scaledBitmap为目标图像，10是缩放的倍数（越大模糊效果越高）
                    Bitmap blurBitmap = FastBlurUtil.toBlur(loadedImage, 30);
                    blurImgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    blurImgView.setImageBitmap(blurBitmap);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });


            TextView title = (TextView) findViewById(R.id.title);
            title.setText(mVideoDetial.name);

            TextView score = (TextView) findViewById(R.id.score);
            mVideoDetial.rating = StringUtils.nullStrToEmpty(mVideoDetial.rating);
            if (!StringUtils.isBlank(mVideoDetial.rating))
                mVideoDetial.rating = mVideoDetial.rating.replace("\n", "");
            if (mVideoDetial.rating.contains("分")) {
                score.setText(getResources().getString(R.string.score_open) + mVideoDetial.rating);
            } else {
                score.setText(getResources().getString(R.string.score_open) + mVideoDetial.rating + " 分");
            }
            if (StringUtils.isBlank(mVideoDetial.rating) || mVideoDetial.rating.equals("0")) {
                score.setVisibility(View.GONE);
            }

            TextView cast = (TextView) findViewById(R.id.cast);
            if (!StringUtils.isBlank(mVideoDetial.actors))
                mVideoDetial.actors = mVideoDetial.actors.replace("\n", "");
            mVideoDetial.actors = StringUtils.nullStrToEmpty(mVideoDetial.actors);
            cast.setText(getResources().getString(R.string.actor_open) + mVideoDetial.actors);

            TextView region = (TextView) findViewById(R.id.region);
            String regionStr = "";
            if (!StringUtils.isBlank(mVideoDetial.area)) {
                regionStr = getResources().getString(R.string.area_open) + mVideoDetial.area;
            } else if (!StringUtils.isBlank(mVideoDetial.year)) {
                regionStr = getResources().getString(R.string.year_open)
                        + mVideoDetial.year;
            }

            region.setText(regionStr);
            TextView episode = (TextView) findViewById(R.id.episode);
            String episodeStr = "";

            if (mVideoDetial.cid == 2 || mVideoDetial.cid == 3 || mVideoDetial.cid == 5) {
                if (mVideoDetial.episodes != null && mVideoDetial.sources != null && mVideoDetial.sources.size() > 0) {
                    String site = mVideoDetial.sources.get(0).site;
                    JSONObject episodesJo = JSONUtils.toJsonObject(mVideoDetial.episodes);
                    JSONArray episodeJo = episodesJo.optJSONArray(site);
                    if (episodeJo != null) {
                        episodeStr = getResources().getString(R.string.episode_open)
                                + episodeJo.length() + "集";
                    }
                }
            } else {
                mVideoDetial.genres = StringUtils.nullStrToEmpty(mVideoDetial.genres);
                if (!StringUtils.isBlank(mVideoDetial.genres))
                    mVideoDetial.genres = mVideoDetial.genres.replace("\n", "");
                episodeStr = getResources().getString(R.string.type_open)
                        + mVideoDetial.genres;
            }
            episode.setText(episodeStr);
            mSpinner = (Spinner) findViewById(R.id.source_spinner);

            if (mVideoDetial.sources != null) {
                ArrayList<String> data_list = new ArrayList<String>();
                if (mCloudVideo != null) {
                    for (VideoSource source : mVideoDetial.sources) {
                        data_list.add(source.siteName);
                    }
                } else {
                    for (VideoSource source : mVideoDetial.sources) {
                        String name = getPlatformName(source.site);
                        data_list.add(name);
                    }
                }
                ArrayAdapter<String> arr_adapter;
                //适配器
                arr_adapter = new SourceSelection(this, R.layout.item_source_selection, R.id.source, data_list);
                //加载适配器
                mSpinner.setAdapter(arr_adapter);
            }

            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (mVideoType == VideoIntroduceAdapter.TYPE_EPISODE || mVideoType == VideoIntroduceAdapter.TYPE_VARIETY) {
                        if (mEpisodeFragment != null) {
                            String site = mVideoDetial.sources.get(position).site;
                            mEpisodeFragment.changeSite(site, (String) mSpinner.getSelectedItem());
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void CaptureVideo() {
        if (!mShareDialog.needShowShareDialog(mVideoDetial.name)) {
            int position = mSpinner.getSelectedItemPosition();
            String site = mVideoDetial.sources.get(position).site;
            if (mVideoType == VideoIntroduceAdapter.TYPE_FILM) {
                String url = mVideoDetial.sources.get(position).url;
                Intent intent = new Intent(mContext, CaptureActivity.class);
                intent.putExtra("webTitle", mVideoDetial.name);
                intent.putExtra("webUrl", url);
                intent.putExtra("site", site);
                intent.putExtra("vid", mVideoDetial.vid);
                String targetUrl = mVideoDetial.sources.get(position).targetUrl;
                if (!StringUtils.isBlank(targetUrl)) {
                    intent.putExtra("targetUrl", targetUrl);
                }
                startActivity(intent);
                UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "自有视频扫码");
            } else if (mVideoType == VideoIntroduceAdapter.TYPE_EPISODE) {
                Intent intent = new Intent(mContext, EpisodeActivity.class);
                intent.putExtra("videoDetial", mVideoDetial);
                intent.putExtra("site", site);
                intent.putExtra("action", GlobalConstant.ACTION_CAPTURE);
                startActivity(intent);
            }
        }
    }

    private String getPlatformName(String site) {
        if (!StringUtils.isBlank(site)) {
            for (int i = 0; i < siteArrays.length; i++) {
                if (site.equals(siteArrays[i])) {
                    return platformArray[i];
                }
            }
        }
        return platformArray[9];
    }

    private TextView mIntroduceTag, mEpisodeTag;

    int selectedColor, unSelectedColor;

    private void initviewPager() {
        mIntroduceTag = (TextView) findViewById(R.id.introduce_tag);
        mEpisodeTag = (TextView) findViewById(R.id.episode_tag);
        mViewPager = (ViewPager) findViewById(R.id.video_introduce_viewpager);
        mPageAdapter = new VideoIntroduceAdapter(mContext, getSupportFragmentManager(), mVideoType);
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        mIntroduceFragment = new IntroduceFragment(mVideoDetial);
        String site = null;
        if (mCloudVideo != null) {
            site = GlobalConstant.SITE_CLOUD_VIDEO;
        } else {
            try {
                site = mVideoDetial.sources.get(mSpinner.getSelectedItemPosition()).site;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        switch (mVideoType) {
            case VideoIntroduceAdapter.TYPE_FILM:
                mEpisodeTag.setVisibility(View.GONE);
                break;
            case VideoIntroduceAdapter.TYPE_EPISODE:
                mEpisodeFragment = new EpisodeFragment(mVideoDetial, site, (String) mSpinner.getSelectedItem());
                fragments.add(mEpisodeFragment);
                mIntroduceTag.setTextColor(unSelectedColor);
                findViewById(R.id.btn_play_introduction).setVisibility(View.GONE);
                break;
            case VideoIntroduceAdapter.TYPE_VARIETY:
                mEpisodeFragment = new EpisodeFragment(mVideoDetial, site, (String) mSpinner.getSelectedItem());
                fragments.add(mEpisodeFragment);
                mIntroduceTag.setTextColor(unSelectedColor);
                findViewById(R.id.btn_play_introduction).setVisibility(View.GONE);
                findViewById(R.id.btn_download_introduction).setVisibility(View.GONE);
                break;
            default:
                break;
        }
        fragments.add(new IntroduceFragment(mVideoDetial));
        mPageAdapter.setFragments(fragments);
        mViewPager.requestDisallowInterceptTouchEvent(false);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mViewPager.getCurrentItem() == 0) {
                    mEpisodeTag.setTextColor(selectedColor);
                    mIntroduceTag.setTextColor(unSelectedColor);
                } else if (mViewPager.getCurrentItem() == 1) {
                    mEpisodeTag.setTextColor(unSelectedColor);
                    mIntroduceTag.setTextColor(selectedColor);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        try {
            String site = null;
            String url;
            Intent intent;
            int position;
            switch (v.getId()) {
                case R.id.btn_play_introduction:
                    position = mSpinner.getSelectedItemPosition();
                    if (mVideoDetial != null && mVideoDetial.sources != null && mVideoDetial.sources.size() > position) {
                        site = mVideoDetial.sources.get(position).site;
                    }
                    if (mVideoDetial != null && mVideoDetial.sources != null && mVideoDetial.sources.size() > position) {
                        url = mVideoDetial.sources.get(position).url;
                        intent = new Intent(mContext, VideoActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("name", mVideoDetial.name);
                        intent.putExtra("vid", mVideoDetial.vid);
                        intent.putExtra("site", site);
                        try {
                            intent.putExtra("sitename", (String) mSpinner.getSelectedItem());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (mCloudVideo != null && !StringUtils.isBlank(mVideoDetial.sources.get(position).targetUrl)) {
                                intent.putExtra("targetUrl", mVideoDetial.sources.get(position).targetUrl);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        startActivityForResult(intent, GlobalConstant.RESULT_VISIT_BEGIN);
                        UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "自有视频播放");
                        UploadEventRecord.recordEventinternal(mContext, GlobalConstant.P_VIDEO_NAME, mVideoDetial.name);
                    }
                    break;
                case R.id.btn_download_introduction:
                    position = mSpinner.getSelectedItemPosition();
                    if (mVideoDetial != null && mVideoDetial.sources != null && mVideoDetial.sources.size() > position) {
                        site = mVideoDetial.sources.get(position).site;
                    }
                    if (mVideoDetial != null && mVideoDetial.sources != null && mVideoDetial.sources.size() > position) {
                        if (mVideoType == VideoIntroduceAdapter.TYPE_FILM) {
                            url = mVideoDetial.sources.get(position).url;
                            DefinitionDialog definitionDialog = new DefinitionDialog(mContext);
                            definitionDialog.showDialog(url, site, mVideoDetial.vid, mVideoDetial.name, downloadHandler);
//                            UrlParseHelper helper = new UrlParseHelper(mContext, url, site, mVideoDetial.vid, mVideoDetial.name, GlobalConstant.DEF_SUPER, downloadHandler, true);
//                            ThreadUtils.execute(helper);
                            UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "自有视频下载");
                        } else if (mVideoType == VideoIntroduceAdapter.TYPE_EPISODE) {
                            intent = new Intent(mContext, EpisodeActivity.class);
                            intent.putExtra("videoDetial", mVideoDetial);
                            intent.putExtra("site", site);
                            intent.putExtra("action", GlobalConstant.ACTION_DOWNLOAD);
                            startActivity(intent);
                        }
                    }
                    break;
                case R.id.btn_back:
                    finish();
                    break;
                case R.id.btn_qr_code:
                    CaptureVideo();
                    break;
                case R.id.btn_collect:
//                    if(mEpisodeFragment!=null){
//                        mEpisodeFragment.getLastVistPositon();
//                    }
                    RecordAction action = RecordAction.getInstance(mContext);
                    action.open(true);
                    if (action.checkBookmark(mVid)) {
                        action.deleteBookmark(mVid);
                        ToastUtils.show(mContext, R.string.toast_delete_bookmark_successful);
                    } else {
                        String name = mVideoDetial.name + "iandi";
                        if (mVideoDetial.cid == 1) {
                            name += getResources().getString(R.string.tag_name_film);
                        } else if (mVideoDetial.cid == 2) {
                            name += getResources().getString(R.string.tag_name_episode);
                        } else if (mVideoDetial.cid == 3) {
                            name += getResources().getString(R.string.tag_name_cartoon);
                        } else if (mVideoDetial.cid == 4) {
                            name += getResources().getString(R.string.tag_name_variety);
                        } else {
                            name += getResources().getString(R.string.tag_name_other);
                        }
                        action.addBookmark(new Record(name, mVid, System.currentTimeMillis()));
                        ToastUtils.show(mContext, R.string.toast_add_bookmark_successful);
                    }
                    action.close();
                    updateBookmarks();
                    break;
                case R.id.introduce_tag:
                    if (mPageAdapter.getCount() > 1) {
                        mViewPager.setCurrentItem(1, true);
                    }
                    break;
                case R.id.episode_tag:
                    if (mPageAdapter.getCount() > 1) {
                        mViewPager.setCurrentItem(0, true);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBookmarks() {

        RecordAction action = RecordAction.getInstance(this);
        action.open(false);
        if (action.checkBookmark(mVid)) {
            mBookMarkBtn.setImageDrawable(ViewUnit.getDrawable(this, R.mipmap.nav_ic_collect_down));
        } else {
            mBookMarkBtn.setImageDrawable(ViewUnit.getDrawable(this, R.drawable.ic_nav_collect_white));
        }
        action.close();
    }


    private Handler downloadHandler = new Handler(Looper.getMainLooper()) {
        String title = "";

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == GlobalConstant.VIDEO_URL_RECEIVE) {
                String url = (String) msg.obj;
                Bundle bundle = msg.getData();
                title = bundle.getString("name");
                if (StringUtils.isBlank(url)) {
                    if (msg.arg2 == 1003) {
                        ToastUtils.show(mContext, getResources().getString(R.string.can_not_download_video));
                    } else {
                        ToastUtils.show(mContext, getResources().getString(R.string.can_not_get_video_address));
                    }
                    return;
                }
                Intent i = new Intent(mContext, VideosDownloadService.class);
                i.setAction(Intent.ACTION_INSERT);
                Uri uri = Uri.parse(url);
                i.setData(uri);
                i.putExtra(Intent.EXTRA_TITLE, title);
                startService(i);
            }
        }
    };


    class SourceSelection extends ArrayAdapter<String> {

        private int textViewResourceId;

        public SourceSelection(Context context, int resource, int textViewResourceId, List<String> objects) {
            super(context, resource, textViewResourceId, objects);
            this.textViewResourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView tv = (TextView) view.findViewById(textViewResourceId);
            int iconResId = getIconResId(tv.getText().toString());
            Drawable drawable = CompatUtils.getDrawable(mContext, iconResId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv.setCompoundDrawables(drawable, null, null, null);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView tv = (TextView) view.findViewById(textViewResourceId);
            int iconResId = getIconResId(tv.getText().toString());
            Drawable drawable = CompatUtils.getDrawable(mContext, iconResId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv.setCompoundDrawables(drawable, null, null, null);
            return view;
        }

        private int getIconResId(String str) {
            for (int i = 0; i < iconArray.length; i++) {
                if (str.equals(platformArray[i])) {
                    return iconArray[i];
                }
            }
            return R.mipmap.zdy_ic_default;
        }
    }

    RecordAction mRecordAction;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == GlobalConstant.RESULT_VISIT_SUCCESS) {
                long playTime = data.getLongExtra("play_time", 0l);
                String title = data.getStringExtra("name");
                Record record = new Record(title, mVideoDetial.vid, playTime, System.currentTimeMillis());
                if (mRecordAction == null) {
                    mRecordAction = RecordAction.getInstance(mContext);
                }
                mRecordAction.open(true);
                if (mRecordAction.checkPlayRecord(mVideoDetial.vid)) {
                    mRecordAction.updatePlayRecord(record);
                } else {
                    mRecordAction.addPlayRecord(record);
                }
                mRecordAction.close();


                int position = mSpinner.getSelectedItemPosition();
                String siteName = (String) mSpinner.getSelectedItem();
                if (siteName.contains("磁力链")) {
                    String targetUrl = data.getStringExtra("targetUrl");
                    String oriTargetUrl = mVideoDetial.sources.get(position).targetUrl;
                    if (!targetUrl.equals(oriTargetUrl)) {
                        if (mCloudVideo != null) {
                            mVideoDetial.sources.get(position).targetUrl = targetUrl;
                            JSONArray playLinks = JSONUtils.toJsonArray(mCloudVideo.playLinks);
                            if (playLinks != null && playLinks.length() > 0) {
                                for (int i = 0; i < playLinks.length(); i++) {
                                    JSONObject jo = JSONUtils.getJsonObject(playLinks, i, null);
                                    if (jo != null) {
                                        String inSite = jo.optString("site");
                                        if (inSite.equals(siteName)) {
                                            jo.put("targetUrl", targetUrl);
                                            playLinks.put(i, jo);
                                            mCloudVideo.playLinks = playLinks.toString();
                                            mCloudVideo.update(mContext);
                                            LunaLog.d("更新地址成功");
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
