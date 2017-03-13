package android.luna.net.videohelper.activity;

import android.content.Context;
import android.content.Intent;
import android.luna.net.videohelper.Ninja.Activity.BrowserActivity;
import android.luna.net.videohelper.Ninja.Database.Record;
import android.luna.net.videohelper.Ninja.Database.RecordAction;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelper.Ninja.Unit.IntentUnit;
import android.luna.net.videohelper.Ninja.View.CompleteAdapter;
import android.luna.net.videohelper.adapter.SearchResultAdapter;
import android.luna.net.videohelper.adapter.TvLivesListAdapter;
import android.luna.net.videohelper.bean.CloudVideo;
import android.luna.net.videohelper.bean.TvLiveP;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelper.bean.VideoSource;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.widget.DividerItemDecoration;
import android.luna.net.videohelper.widget.RecyclerViewHeader;
import android.luna.net.videohelptools.R;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.luna.common.util.JSONUtils;
import net.luna.common.util.ListUtils;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;


/**
 * Created by bintou on 15/11/16.
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener {

    private final String KEY_SP_RECORD = "searchrecords";
    private final String KEY_SP_RECORD_TV = "searchrecordtvs";

    private VideoCatchManager mVideoCatchManager;

    private AutoCompleteTextView mEditText;

    private ArrayList<VideoDetial> mSearchList;

    private ArrayList<TvLiveP> mTvLiveArrayList;

    private TvLivesListAdapter mTvLIveListAdapter;

    private SearchResultAdapter mSearchAdapter;

    private TextView mSearchNone;

    private LinearLayout progressbar;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mResultRecyclerView;

    private RecyclerViewHeader mHeader;

    private LinearLayout mRecordLyaout;
    private ListView mReocrdListView;
    private List<String> records = new ArrayList<String>();
    private RelativeLayout mHeaderLayout;
    private ArrayAdapter<String> mRecordsAdapter;

    private int mCid;

    private boolean isFromHome = false;
    private String curSearchContent;

    private List<Record> mSearchTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mEditText = (AutoCompleteTextView) findViewById(R.id.search_edit);
        mCid = getIntent().getIntExtra("cid", 0);
        mSearchNone = (TextView) findViewById(R.id.none_result);
        mResultRecyclerView = (RecyclerView) findViewById(R.id.search_recycleview);

        mLayoutManager = new LinearLayoutManager(this);
        mResultRecyclerView.setLayoutManager(mLayoutManager);
        mResultRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        progressbar = (LinearLayout) findViewById(R.id.progressbar);
        progressbar.setVisibility(View.GONE);


        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        search(mEditText.getText().toString(), true);
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
                return false;
            }
        });

        mRecordLyaout = (LinearLayout) findViewById(R.id.record_layout);
        mReocrdListView = (ListView) findViewById(R.id.search_record_listview);
        mRecordsAdapter = new ArrayAdapter<String>(mContext, R.layout.item_record, R.id.record_tv);
        mReocrdListView.setAdapter(mRecordsAdapter);

        initSearchRecord();
        mReocrdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < records.size()) {
                    search(records.get(position), false);
                }
            }
        });

        if (mCid == 101) {
            mTvLIveListAdapter = new TvLivesListAdapter(mContext, mTvLiveArrayList, "all");
            mResultRecyclerView.setAdapter(mTvLIveListAdapter);
            mEditText.setHint("搜索电视台");
        } else {
            mSearchAdapter = new SearchResultAdapter(mContext, mSearchList);
            mResultRecyclerView.setAdapter(mSearchAdapter);
        }

        mHeaderLayout = (RelativeLayout) findViewById(R.id.layout_header);
        String searchStr = getIntent().getStringExtra("search");
        if (!StringUtils.isBlank(searchStr)) {
            isFromHome = true;
            search(searchStr, true);
        }

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if(mIsShowDropDown) {
                    ThreadUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String str = s.toString();
                                if (!StringUtils.isBlank(str)) {
                                    mSearchTips = mVideoCatchManager.getSearchTips(str);
                                    if (ListUtils.getSize(mSearchTips) > 0) {
                                        mHandler.sendEmptyMessage(3);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    mIsShowDropDown = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }


    private void initSearchRecord() {
        String oriRecordStr = null;
        if (mCid == 101) {
            oriRecordStr = PreferencesUtils.getString(mContext, KEY_SP_RECORD_TV, "");
        } else {
            oriRecordStr = PreferencesUtils.getString(mContext, KEY_SP_RECORD, "");
        }
        if (!StringUtils.isBlank(oriRecordStr)) {
            String[] oriArray = oriRecordStr.split(",,");
            if (oriArray != null) {
//            records = Arrays.asList(oriArray);
                for (String str : oriArray) {
                    records.add(str);
                }
                mRecordsAdapter.addAll(oriArray);
            }
        } else {
            mRecordLyaout.setVisibility(View.GONE);
        }
    }

    private void clearRecord() {
        records.clear();
        mRecordsAdapter.clear();
        mRecordLyaout.setVisibility(View.GONE);
        RecordAction action = RecordAction.getInstance(this);
        action.open(true);
        action.clearHistory();
        action.close();
    }

    private void addRecord(String str) {
        if (records != null) {
            if (records.contains(str)) {
                mRecordsAdapter.remove(str);
                mRecordsAdapter.insert(str, 0);
                records.remove(str);
                records.add(0, str);
            } else {
                mRecordsAdapter.insert(str, 0);
                records.add(0, str);
            }
        }
    }

    BmobQuery<CloudVideo> query = new BmobQuery<>();

    private VideoDetial cloudToDetial(CloudVideo cloudVideo) {
        VideoDetial videoDetial = new VideoDetial();
        try {
            videoDetial.cid = cloudVideo.cid;
            videoDetial.name = cloudVideo.name;
            if (!StringUtils.isBlank(cloudVideo.actors))
                videoDetial.actors = cloudVideo.actors.replace("\n", "");
            if (!StringUtils.isBlank(cloudVideo.year))
                videoDetial.year = cloudVideo.year.replace("\n", "");
            videoDetial.desc = cloudVideo.desc;
            videoDetial.area = cloudVideo.area;
            videoDetial.cover = cloudVideo.cover;
            videoDetial.vid = cloudVideo.getObjectId();
            if (!StringUtils.isBlank(cloudVideo.genres))
                videoDetial.genres = cloudVideo.genres.replace("\n", "");
            videoDetial.rating = cloudVideo.rating;
            JSONArray playLinks = JSONUtils.toJsonArray(cloudVideo.playLinks);
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
            videoDetial.sources = (ArrayList<VideoSource>) videoSources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoDetial;
    }

    private void search(final String searchStr, boolean needAdd) {
        if (!StringUtils.isBlank(searchStr)) {
            curSearchContent = searchStr;
            mEditText.setText(searchStr);
            mRecordLyaout.setVisibility(View.GONE);
            if (needAdd) {
                addRecord(searchStr);
            }

            mSearchList = null;

            if (mCid == 101) {
                String q = StringUtils.nullStrToEmpty(searchStr);
                mTvLiveArrayList = (ArrayList<TvLiveP>) mVideoCatchManager.getSearchTvLive(q);
                if (mTvLiveArrayList != null && mTvLiveArrayList.size() > 0) {
                    mHandler.sendEmptyMessage(1);
                } else {
                    mHandler.sendEmptyMessage(2);
                }
            } else {
                mHeaderLayout.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.VISIBLE);

                query.addWhereContains("name", searchStr);
                query.setLimit(30);
                query.setCachePolicy(BmobQuery.CachePolicy.IGNORE_CACHE);
                query.order("-time");
                query.findObjects(mContext, new FindListener<CloudVideo>() {
                    @Override
                    public void onSuccess(List<CloudVideo> list) {

                        try {
                            if (ListUtils.getSize(list) > 0) {
                                mSearchAdapter.setmCloudVideos(list);
                                if (mSearchList == null) {
                                    mSearchList = new ArrayList<VideoDetial>();
                                }
                                for (CloudVideo cloudVideo : list) {
                                    VideoDetial videoDetial = cloudToDetial(cloudVideo);
                                    mSearchList.add(videoDetial);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        hasLoadInternal = true;
//                        mHandler.sendEmptyMessage(1);
                    }

                    @Override
                    public void onError(int i, String s) {
                        hasLoadInternal = true;
//                        mHandler.sendEmptyMessage(1);
                    }
                });

                ThreadUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (!hasLoadInternal) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        String q = StringUtils.nullStrToEmpty(searchStr);
                        List<VideoDetial> list = mVideoCatchManager.catchSearchResult(q);
                        if (mSearchList == null) {
                            mSearchList = new ArrayList<VideoDetial>();
                        }
                        if (list == null) {
                            list = new ArrayList<VideoDetial>();
                        }
                        mSearchList.addAll(list);

                        hasLoadInternal = false;
                        if (mSearchList != null && mSearchList.size() > 0) {
                            mHandler.sendEmptyMessage(1);
                        } else {
                            mHandler.sendEmptyMessage(2);
                        }
                    }
                });
            }
        }
    }

    boolean hasLoadInternal = false;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 1:
                    mResultRecyclerView.setVisibility(View.VISIBLE);
                    if (mCid == 101) {
                        mTvLIveListAdapter.updateData(mTvLiveArrayList);
                    } else {
                        mSearchAdapter.updateSearchResult(mSearchList);
                    }
                    mSearchNone.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
//                    mEditText.setText("");
                    break;
                case 2:
                    mResultRecyclerView.setVisibility(View.GONE);
                    if (mCid == 101) {
                        mTvLIveListAdapter.updateData(null);
                    } else {
                        mSearchAdapter.updateSearchResult(null);
                    }
                    mSearchNone.setVisibility(View.VISIBLE);
                    mRecordLyaout.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);
//                    mEditText.setText("");
                    break;
                case 3:
                    try {
                        if (mSearchTips != null) {
                            if (mTipsAdapter == null) {
                                mTipsAdapter = new CompleteAdapter(mContext, R.layout.complete_item, mSearchTips, false);
                                mEditText.setAdapter(mTipsAdapter);
                                mEditText.setDropDownWidth(mEditText.getWidth());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    mEditText.setDropDownVerticalOffset(getResources().getDimensionPixelOffset(R.dimen.layout_height_6dp));
                                }
                                mEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        TextView tv = (TextView) view.findViewById(R.id.complete_item_title);
                                        if (tv != null) {
                                            String title = tv.getText().toString();
                                            mIsShowDropDown = false;
                                            mEditText.clearFocus();
                                            hideSoftInput(mEditText);
                                            search(title, true);


                                        }
                                    }
                                });
                            } else {
                                mTipsAdapter.switchRecord(mSearchTips);
                            }
                            mTipsAdapter.notifyDataSetChanged();
                            mEditText.showDropDown();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private CompleteAdapter mTipsAdapter;
    private boolean mIsShowDropDown = true;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_search:
                search(mEditText.getText().toString(), true);
                break;
            case R.id.clear_records:
                clearRecord();
                break;
            case R.id.layout_header:
                Intent intent = new Intent();
                String url;
                try {
                    url = URLEncoder.encode(curSearchContent, BrowserUnit.URL_ENCODING);
                    url = BrowserUnit.SEARCH_ENGINE_IQIYI + url;
                    if (isFromHome) {
                        intent.putExtra(IntentUnit.URL, url);
                        setResult(GlobalConstant.INDEX_SEARCH_RETURN, intent);
                        finish();
                    } else {
                        intent = new Intent(mContext, BrowserActivity.class);
                        intent.putExtra(GlobalConstant.EXTRA_INTENT_IS_SEARCH, true);
                        intent.putExtra(IntentUnit.URL, url);
                        startActivity(intent);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        StringBuilder sb = new StringBuilder();
        for (String record : records) {
            sb.append(record).append(",,");
        }
        if (sb.length() > 2) {
            sb.delete(sb.length() - 2, sb.length());
        }
        if (mCid == 101) {
            PreferencesUtils.putString(mContext, KEY_SP_RECORD_TV, sb.toString());
        } else {
            PreferencesUtils.putString(mContext, KEY_SP_RECORD, sb.toString());
        }

        super.onDestroy();
    }

    private void hideSoftInput(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
