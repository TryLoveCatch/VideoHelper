package android.luna.net.videohelper.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.luna.net.videohelper.Ninja.Browser.BrowserController;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelper.Ninja.Unit.ViewUnit;
import android.luna.net.videohelper.Ninja.View.NinjaRelativeLayout;
import android.luna.net.videohelper.activity.GuideMoreActivity;
import android.luna.net.videohelper.adapter.CatelogueAdapter;
import android.luna.net.videohelper.adapter.HotVideoGridAdapter;
import android.luna.net.videohelper.bean.Catelogue;
import android.luna.net.videohelper.bean.HotWord;
import android.luna.net.videohelper.bean.OlParam;
import android.luna.net.videohelper.bean.VideoInfo;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.SiteConstants;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelptools.R;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.ListUtils;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;

/**
 * Created by bintou on 15/10/29.
 */
public class HomeContent {

    private List<Catelogue> mCatelogueList = new ArrayList<Catelogue>();

    private CatelogueAdapter mAdapter;

    private List<HotWord>[] mHotwordLists = new List[5];

    private Activity mActivity;
    private NinjaRelativeLayout mContainView;
    private Handler mHandler;

    private LinearLayout mContentLayout;
    private LinearLayout[] mHotwordLayouts = new LinearLayout[5];
    private GridviewInScroll[] hotVideoGridView = new GridviewInScroll[5];
    private HotVideoGridAdapter[] hotVideoAdapter = new HotVideoGridAdapter[5];
    private String[] mHotTitles = {"热门推荐", "电影", "电视剧", "动漫", "综艺"};

    public HomeContent(Activity context, Handler handler) {
        this.mActivity = context;
        mHandler = handler;
        createView();
    }

    protected void createView() {
        mContainView = (NinjaRelativeLayout) mActivity.getLayoutInflater().inflate(R.layout.layout_home, null, false);
        mContainView.setBrowserController((BrowserController) mActivity);
        mContainView.setFlag(BrowserUnit.FLAG_HOME);

        float dimen144dp = mActivity.getResources().getDimensionPixelSize(R.dimen.layout_width_144dp);
        float dimen108dp = mActivity.getResources().getDimensionPixelSize(R.dimen.layout_height_108dp);
        mContainView.setAlbumTitle(mActivity.getString(R.string.album_title_home));
        mContentLayout = (LinearLayout) mContainView.findViewById(R.id.content_layout);


        GridviewInScroll gridView = (GridviewInScroll) mContainView.findViewById(R.id.gridview_guide);
        mAdapter = new CatelogueAdapter(mActivity, mCatelogueList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new OnIconClickListener());
        initData();
        checkHasNewFilm();


        initHotword();
    }

    public void onResume() {

    }

    public void updateCatelogue() {
        initData();
    }


    public NinjaRelativeLayout getContainView() {
        return mContainView;
    }


    private void initData() {
        mCatelogueList.clear();
        ArrayList<Catelogue> catelogueList = new ArrayList<Catelogue>();
        catelogueList.addAll(SiteConstants.getInternalCatelogue(mActivity));
        String[] titles = SiteConstants.ALL_TITILES;
        String[] sites = SiteConstants.ALL_SITES;
        int[] icons = SiteConstants.ALL_ICONS;
        String[] websites = SiteConstants.ALL_WEBSITES;
        for (int i = 0; i < sites.length; i++) {
            Catelogue catelogue = new Catelogue();
            catelogue.site = sites[i];
            catelogue.icon = icons[i];
            catelogue.url = websites[i];
            catelogue.title = titles[i];
            catelogueList.add(catelogue);
        }

        try {
            String[] validTitles = PreferencesUtils.getString(mActivity, GlobalConstant.SP_CATELOGUE_TITLES, "").split("\\;");
            int index = 0;
            if (validTitles != null && validTitles.length > 3) {
                for (Catelogue catelogue : catelogueList) {
                    if (index < validTitles.length && catelogue.title.equals(validTitles[index])) {
                        mCatelogueList.add(catelogue);
                        index++;
                    }
                }
            } else {
                mCatelogueList = catelogueList.subList(0, 12);
            }
        } catch (Exception e) {
            mCatelogueList = catelogueList.subList(0, 4);
            e.printStackTrace();
        }
        mAdapter.updateList(mCatelogueList);


        BmobQuery<OlParam> query1 = new BmobQuery<>();
        query1.getObject(mActivity, "25ZfLLLQ", new GetListener<OlParam>() {

            @Override
            public void onFailure(int i, String s) {

            }

            @Override
            public void onSuccess(OlParam olParam) {
                if (olParam != null && StringUtils.isEquals("iqiyi_site", olParam.getKey()) && !StringUtils.isBlank(olParam.getValue())) {
                    for (Catelogue catelogue : mCatelogueList) {
                        if (!StringUtils.isBlank(catelogue.title) && catelogue.title.equals("爱奇艺")) {
                            catelogue.url = olParam.getValue();
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        });

        mContentLayout.findViewById(R.id.guide_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.startActivityForResult(new Intent(mActivity, GuideMoreActivity.class), 0);
            }
        });
    }


    private void initHotword() {
        try {
            boolean isZh = false;
            Locale locale = mActivity.getResources().getConfiguration().locale;
            String language = locale.getCountry();
            LunaLog.d("language: "+language);
            if (language.endsWith("TW")||language.endsWith("HK")) {
                isZh = true;
            } else {
                isZh = false;
            }

            for (int i = 0; i < mHotwordLayouts.length; i++) {
                mHotwordLists[i] = new ArrayList<>();
                mHotwordLayouts[i] = (LinearLayout) LayoutInflater.from(mActivity).inflate(R.layout.layout_recommend, null);
                TextView title = (TextView) mHotwordLayouts[i].findViewById(R.id.hotword_title);
                title.setText(mHotTitles[i]);
                hotVideoGridView[i] = (GridviewInScroll) mHotwordLayouts[i].findViewById(R.id.hot_video);
                hotVideoAdapter[i] = new HotVideoGridAdapter(mActivity, (ArrayList<HotWord>) mHotwordLists[i], mHandler,isZh);
                hotVideoGridView[i].setAdapter(hotVideoAdapter[i]);
                hotVideoGridView[i].setOnItemClickListener(new OnImageClickListener(i));
                mContentLayout.addView(mHotwordLayouts[i]);
            }
            BmobQuery<HotWord> query = new BmobQuery<HotWord>();

            query.order("-createdAt");
            query.setLimit(30);
            query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
            query.findObjects(mActivity, new HotWordFindListener());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class HotWordFindListener extends FindListener<HotWord> {

        @Override
        public void onSuccess(List<HotWord> list) {
            for (int i = 0; i < mHotwordLists.length; i++) {
                mHotwordLists[i].clear();
            }
            if (list != null && list.size() > 0) {
                for (HotWord hotWord : list) {
                    if (hotWord != null && hotWord.getCid() > -1 && hotWord.getCid() < 5)
                        mHotwordLists[hotWord.getCid()].add(hotWord);
                }
            }
            for (int i = 0; i < mHotwordLayouts.length; i++) {
                if (mHotwordLists[i].size() > 0) {
                    if (hotVideoGridView[i].getVisibility() == View.GONE) {
                        hotVideoGridView[i].setVisibility(View.VISIBLE);
                    }
                    hotVideoAdapter[i].updateList((ArrayList<HotWord>) mHotwordLists[i]);
                } else {
                    mHotwordLayouts[i].setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onError(int i, String s) {
            LunaLog.d("get hotword error:" + s);
        }
    }

    private void hideSoftInput(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void checkHasNewFilm() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<VideoInfo> videoInfos = VideoCatchManager.getInstanct(mActivity.getApplicationContext()).getVideoInfos(1, 0);
                    if (videoInfos != null && videoInfos.size() > 0) {
                        String localNewestFilm = PreferencesUtils.getString(mActivity.getApplicationContext(), GlobalConstant.LOCAL_NEWEST_FILE, "");
                        if (!videoInfos.get(0).name.equals(localNewestFilm)) {
                            if (!ListUtils.isEmpty(mCatelogueList)) {
                                if (mCatelogueList.get(2).site.equals(GlobalConstant.SITE_SHORT_FILM)) {
                                    mCatelogueList.get(2).showPoint = true;
                                    ThreadUtils.runInUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            synchronized (mAdapter) {
                                                mAdapter.updateList(mCatelogueList);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public class OnIconClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {

                if (mCatelogueList != null && mCatelogueList.size() > position && mHandler != null) {
                    if (mCatelogueList.get(position).type == 1) {
                        Message msg = new Message();
                        msg.what = BrowserUnit.MESSAGE_OPEN_URL;
                        msg.obj = mCatelogueList.get(position).url;
                        mHandler.sendMessage(msg);
                    } else {
                        Intent intent = new Intent(mActivity, mCatelogueList.get(position).tartget);
                        intent.putExtra("cid", mCatelogueList.get(position).cid);
                        intent.putExtra("name", mCatelogueList.get(position).title);
                        mActivity.startActivity(intent);
                        if (!ListUtils.isEmpty(mCatelogueList)) {
                            if (mCatelogueList.get(position).site.equals(GlobalConstant.SITE_SHORT_FILM) && mCatelogueList.get(position).showPoint) {
                                mCatelogueList.get(position).showPoint = false;
                                mAdapter.updateList(mCatelogueList);
                            }
                        }
                    }
                    UploadEventRecord.recordEvent(mActivity, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_GUIDE_CLICK, mCatelogueList.get(position).title);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class OnImageClickListener implements AdapterView.OnItemClickListener {

        int index;

        public OnImageClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mHandler != null && ListUtils.getSize(mHotwordLists[index]) > position && mHotwordLists[index] != null) {
                Message msg = new Message();
                msg.what = BrowserUnit.MESSAGE_OPEN_URL;
                HotWord hotWord = mHotwordLists[index].get(position);
                String query;
                if (hotWord != null && StringUtils.isBlank(hotWord.getUrl())) {
                    query = hotWord.getWord();
                    try {
                        if (!StringUtils.isBlank(query)) {
                            query = URLEncoder.encode(query, BrowserUnit.URL_ENCODING);
                        }
                        query = BrowserUnit.SEARCH_ENGINE_SOUKU + query;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    query = hotWord.getUrl();
                }
                UploadEventRecord.recordEvent(mActivity, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_HOT_SEARCH, hotWord.getWord());

                msg.obj = query;
                if (mHandler != null) {
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

}
