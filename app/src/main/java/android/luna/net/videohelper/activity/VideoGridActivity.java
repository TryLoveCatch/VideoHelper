package android.luna.net.videohelper.activity;

import android.content.Intent;
import android.luna.net.videohelper.adapter.VideoGridAdapter;
import android.luna.net.videohelper.bean.CloudVideo;
import android.luna.net.videohelper.bean.VideoInfo;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelper.widget.GridViewWithHeaderAndFooter;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.luna.common.download.AppDownloadManager;
import net.luna.common.download.model.AppModel;
import net.luna.common.util.CompatUtils;
import net.luna.common.util.ListUtils;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;
import net.luna.common.view.widget.Dialog;
import net.luna.common.view.widget.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;


/**
 * Created by bintou on 15/11/12.
 */
public class VideoGridActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    int cid;

    private VideoGridAdapter mAdapter;

    private ArrayList<VideoInfo> mVideoInfos;
    private RefreshLayout mSwipeRefreshLayout;
    private VideoCatchManager mVideoCatchManager;

    private VideoRequestHandler mHandler;

    private LinearLayout footerLayout;

    private LinearLayout progressDialog;
    private BmobQuery<CloudVideo> query = new BmobQuery<>();
    private List<CloudVideo> mCloudVideos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mHandler = new VideoRequestHandler(Looper.getMainLooper());
        setContentView(R.layout.activity_video_grid);
        footerLayout = (LinearLayout) findViewById(R.id.footer);
        Intent intent = getIntent();
        cid = intent.getIntExtra("cid", 0);

        mVideoInfos = (ArrayList<VideoInfo>) intent.getSerializableExtra("videotypes");
        ((TextView) findViewById(R.id.title)).setText(intent.getStringExtra("name"));
        GridViewWithHeaderAndFooter gridView = (GridViewWithHeaderAndFooter) findViewById(R.id.video_gridview);
        if (cid == 1) {
//        initRecommend();
            View headerView = LayoutInflater.from(mContext).inflate(R.layout.header_vqu, null);
            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dialog = new Dialog(mContext, "提示", "是否下载微趣视频");
                    dialog.addCancelButton("取消");
                    dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppDownloadManager appDownloadManager = AppDownloadManager.getInstance(mContext);
                            AppModel model = new AppModel();
                            model.setAppName("微趣视频");
                            String downloadUrl = "http://vipkpsq.com/apk/VQ_oppo.apk";
                            model.setDownloadUrl(downloadUrl);
                            appDownloadManager.downloadApp(model);
                        }
                    });
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                }
            });
            gridView.addHeaderView(headerView);
        }
//        addContentView(mHeaderView,new RelativeLayout.LayoutParams(-2,-2));
        gridView.setOnItemClickListener(this);
        mAdapter = new VideoGridAdapter(mContext, mVideoInfos);
        gridView.setAdapter(mAdapter);
        gridView.setSelector(CompatUtils.getDrawable(mContext, R.drawable.grid_selector));
        mSwipeRefreshLayout = (RefreshLayout) findViewById(R.id.video_list_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        mSwipeRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                footerLayout.setVisibility(View.VISIBLE);
                addData();
            }
        });

        progressDialog = (LinearLayout) findViewById(R.id.progress_dialog);
        findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
        if (mVideoInfos == null || ListUtils.getSize(mVideoInfos) < 1) {
            progressDialog.setVisibility(View.VISIBLE);
            initData();
        } else {
            progressDialog.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (!ListUtils.isEmpty(mVideoInfos)) {
            PreferencesUtils.putString(mContext, GlobalConstant.LOCAL_NEWEST_FILE, mVideoInfos.get(0).name);
        }
        super.onDestroy();
    }

    private void refreshData() {
        if (cid == 102) {
            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
            query.addWhereEqualTo("canPlay", true);
            query.order("-time");
            query.setLimit(30);
            query.findObjects(mContext, new FindListener<CloudVideo>() {
                @Override
                public void onSuccess(List<CloudVideo> list) {
                    if (ListUtils.getSize(list) > 0) {
                        if (mVideoInfos == null) {
                            mVideoInfos = new ArrayList<VideoInfo>();
                        } else {
                            mVideoInfos.clear();
                        }
                        mCloudVideos = list;
                        changeCloudToInfo(list);
                        mHandler.sendEmptyMessage(1);
                    }
                }

                @Override
                public void onError(int i, String s) {
                    mHandler.sendEmptyMessage(3);
                }
            });
        } else {
            ThreadUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<VideoInfo> videoInfos = mVideoCatchManager.getVideoInfosRefresh(cid, 0, true);
                        if (videoInfos != null) {
                            mVideoInfos = videoInfos;
                            mHandler.sendEmptyMessage(1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void changeCloudToInfo(List<CloudVideo> list) {
        for (CloudVideo cloudVideo : list) {
            VideoInfo videoinfo = new VideoInfo();
            videoinfo.vid = cloudVideo.getObjectId();
            videoinfo.name = cloudVideo.name;
            videoinfo.cover = cloudVideo.cover;
            if (!StringUtils.isBlank(cloudVideo.rating)) {
                cloudVideo.rating = cloudVideo.rating.replace("分", "").replace("\n", "");
            }
            videoinfo.title = cloudVideo.name;
            videoinfo.rating = cloudVideo.rating;
            mVideoInfos.add(videoinfo);
        }
    }

    private void initData() {
        if (cid == 102) {
            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
            query.addWhereEqualTo("canPlay", true);
            query.order("-time");
            query.setLimit(30);
            query.findObjects(mContext, new FindListener<CloudVideo>() {
                @Override
                public void onSuccess(List<CloudVideo> list) {
                    if (ListUtils.getSize(list) > 0) {
                        if (mVideoInfos == null) {
                            mVideoInfos = new ArrayList<VideoInfo>();
                        } else {
                            mVideoInfos.clear();
                        }
                        mCloudVideos = list;
                        changeCloudToInfo(list);
                        mHandler.sendEmptyMessage(1);
                    }
                }

                @Override
                public void onError(int i, String s) {
                    mHandler.sendEmptyMessage(3);
                }
            });
        } else {
            ThreadUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<VideoInfo> videoInfos = mVideoCatchManager.getVideoInfos(cid, 0);
                        if (videoInfos != null) {
                            mVideoInfos = videoInfos;
                            mHandler.sendEmptyMessage(1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void addData() {
        try {
            if (cid == 102) {
                query.addWhereEqualTo("canPlay", true);
                query.setLimit(30);
                query.order("-time");
                query.setSkip(ListUtils.getSize(mVideoInfos));
                query.findObjects(mContext, new FindListener<CloudVideo>() {
                    @Override
                    public void onSuccess(List<CloudVideo> list) {
                        if (ListUtils.getSize(list) > 0) {
                            if (mVideoInfos == null) {
                                mVideoInfos = new ArrayList<VideoInfo>();
                            }
                            if (mCloudVideos == null) {
                                mCloudVideos = new ArrayList<CloudVideo>();
                            }
                            mCloudVideos.addAll(list);
                            changeCloudToInfo(list);
                            mHandler.sendEmptyMessage(2);
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        mHandler.sendEmptyMessage(3);
                    }
                });


            } else {
                ThreadUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mVideoInfos == null) {
                                mVideoInfos = new ArrayList<VideoInfo>();
                            }
                            int offset = ListUtils.getSize(mVideoInfos);
                            ArrayList<VideoInfo> videoInfos = mVideoCatchManager.getVideoInfos(cid, offset);
                            if (videoInfos != null) {
                                mVideoInfos.addAll(videoInfos);
                                mHandler.sendEmptyMessage(2);
                            } else {
                                mHandler.sendEmptyMessage(3);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_search:
                UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "自有搜索");
                Intent intent = new Intent(mContext, SearchActivity.class);
                intent.putExtra("cid", cid);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, VideoIntroduceActivity.class);
        intent.putExtra("vid", mVideoInfos.get(position).vid);
        if (mCloudVideos != null) {
            intent.putExtra("cloudVideo", mCloudVideos.get(position));
        }
        UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_NAME, mVideoInfos.get(position).name);
        mContext.startActivity(intent);
    }

    class VideoRequestHandler extends Handler {
        public VideoRequestHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 1:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mAdapter.notifyDataSetChanged(mVideoInfos);
                    progressDialog.setVisibility(View.GONE);
                    break;
                case 2:
                    mSwipeRefreshLayout.setLoading(false);
                    footerLayout.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    break;
                case 3:
                    footerLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

//    private AutoScrollViewPager mHotViewPager;
//    private RecommendPagerAdapter mHotVideoAdapter;
//    private List<Recommend> mRecommendList = new ArrayList<Recommend>();
//    private ViewPagerPointer mPointerView;
//    private View mHeaderView;
//
//    private void initRecommend() {
//        try {
//
//            mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.header_viewpage, null);
////            mHeaderView.setVisibility(View.GONE);
//            mHotViewPager = (AutoScrollViewPager) mHeaderView.findViewById(R.id.top_video);
//            Handler handler = ((VideoHelperApplication) getApplication()).getWebHandler();
//            mHotVideoAdapter = new RecommendPagerAdapter(mContext, (ArrayList<Recommend>) mRecommendList, handler).setInfiniteLoop(true);
//            mHotViewPager.setInterval(4000);
//            mHotViewPager.startAutoScroll();
//            if (ListUtils.getSize(mRecommendList) > 0) {
//                mHotViewPager.setCurrentItem(Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % ListUtils.getSize(mRecommendList));
//            }
//            mPointerView = (ViewPagerPointer) mHeaderView.findViewById(R.id.pointer);
//            mPointerView.bindViewPager(mHotViewPager, ListUtils.getSize(mRecommendList));
//
//            if (cid > 100) {
//                mHeaderView.setVisibility(View.GONE);
//                return;
//            }
//
//            BmobQuery<Recommend> query = new BmobQuery<Recommend>();
//
//            query.order("-createdAt");
//            query.setLimit(6);
//            query.addWhereEqualTo("cid", cid);
//            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
//            query.findObjects(mContext, new FindListener<Recommend>() {
//                @Override
//                public void onSuccess(List<Recommend> list) {
//                    if (ListUtils.getSize(list) > 0) {
//                        mRecommendList.clear();
//                        for (Recommend recommend : list) {
//                            mRecommendList.add(recommend);
//                        }
//                    }
//
//                    if (ListUtils.getSize(mRecommendList) > 0) {
//                        if (mHeaderView.getVisibility() == View.GONE) {
//                            mHeaderView.setVisibility(View.VISIBLE);
//                        }
//                        mHotViewPager.setAdapter(mHotVideoAdapter);
//                        mPointerView.updateCount(ListUtils.getSize(mRecommendList));
//                        mHotVideoAdapter.updateHotList((ArrayList<Recommend>) mRecommendList);
//                    } else {
//                        mHeaderView.setVisibility(View.GONE);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                }
//
//                @Override
//                public void onError(int i, String s) {
//                    LunaLog.d("get hotword error:" + s);
//                    mHeaderView.setVisibility(View.GONE);
//                    mAdapter.notifyDataSetChanged();
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}
