package android.luna.net.videohelper.activity;

import android.content.Context;
import android.content.Intent;
import android.luna.net.videohelper.adapter.ShortfilmAdapter;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import net.luna.common.util.ListUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;

import java.util.ArrayList;

/**
 * Created by bintou on 16/4/11.
 */
public class ShortfilmActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {


    private ShortfilmAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<VideoDetial> mShortfilmList;

    private TextView mTvNone;

    private VideoCatchManager mVideoCatchManager;
    private VideoRequestHandler mHandler;

    RecyclerView mRecyclerView;
    SwipeRefreshLayout mSwipeRefreshWidget;

    private boolean mRequestingList = false;

    private Context mContext;

    private String mAuthorName, mVideoName;

    private EditText mEditText;
    private ImageButton mClearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_shortfilm);

        mAuthorName = getIntent().getStringExtra("authorName");
        mVideoName = getIntent().getStringExtra("videoName");

        mVideoCatchManager = VideoCatchManager.getInstanct(mContext);
        mSwipeRefreshWidget = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshWidget.setOnRefreshListener(this);

        mSwipeRefreshWidget.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        mTvNone = (TextView) findViewById(R.id.tv_search_none);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycleview_shortfilm);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ShortfilmAdapter(mContext, mShortfilmList);
        mRecyclerView.setAdapter(mAdapter);
        mHandler = new VideoRequestHandler(Looper.getMainLooper());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                registerOnBottomBarAnimation(dx, dy);
                if (StringUtils.isBlank(mAuthorName) && StringUtils.isBlank(mVideoName)) {
                    registerOnRequestList(recyclerView);
                }
            }
        });

        mEditText = (EditText) findViewById(R.id.search_edit);
//        CompatUtils.setBackground(mEditText, getDrawable(R.drawable.edit_text_bg));
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        String searchStr = mEditText.getText().toString();
                        if (!StringUtils.isBlank(searchStr)) {
                            Intent intent = new Intent(mContext, ShortfilmActivity.class);
                            intent.putExtra("videoName", searchStr);
                            mContext.startActivity(intent);
                        }
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
                return false;
            }
        });
        mClearBtn = (ImageButton) findViewById(R.id.btn_search_clear);
        mEditText.addTextChangedListener(mTextWatcher);

        if (!StringUtils.isBlank(mAuthorName) || !StringUtils.isBlank(mVideoName)) {
            TextView tv = (TextView) findViewById(R.id.title);
            if (!StringUtils.isBlank(mAuthorName)) {
                tv.setText(mAuthorName);
                mAdapter.setInAuthorList(true);
            } else {
                tv.setText("搜索结果");
            }
            findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
            requestData(mVideoName, mAuthorName);

        } else {
            findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
            requestData(false);
        }
    }


    private void registerOnRequestList(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager.getItemCount() > 0) {
            if (layoutManager instanceof LinearLayoutManager) {
                int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();
                if (lastVisibleItemPosition == layoutManager.getItemCount() - 1) {
                    if (!mRequestingList) {
                        requestData(false);
                    }
                }
            }
        }
    }


    private void requestData(final boolean isRefresh) {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mRequestingList = true;
                    ArrayList<VideoDetial> videoInfos = mVideoCatchManager.getVideoChannelDetial(14, ListUtils.getSize(mShortfilmList), isRefresh);
                    if (videoInfos != null) {
                        if (mShortfilmList == null) {
                            mShortfilmList = new ArrayList<>();
                        }
                        mShortfilmList.addAll(videoInfos);
                        mHandler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestData(final String videoName, final String authorName) {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mRequestingList = true;
                    ArrayList<VideoDetial> videoInfos = mVideoCatchManager.catchSearchShortVideo(videoName, authorName);
                    if (videoInfos != null) {
                        if (mShortfilmList == null) {
                            mShortfilmList = new ArrayList<>();
                        }
                        mShortfilmList.addAll(videoInfos);
                        if (ListUtils.getSize(mShortfilmList) > 0) {
                            mHandler.sendEmptyMessage(1);
                        } else {
                            mHandler.sendEmptyMessage(2);
                        }
                    } else {
                        mHandler.sendEmptyMessage(2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_search_back:
                findViewById(R.id.search_top_bar).setVisibility(View.GONE);
                findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
                break;
            case R.id.btn_search:
                findViewById(R.id.search_top_bar).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_search).setVisibility(View.GONE);
                break;
            case R.id.btn_search_clear:
                mEditText.setText("");
                break;
        }
        if (v.getId() == R.id.btn_back) {
            finish();
        }
    }

    @Override
    public void onRefresh() {
        if (StringUtils.isBlank(mAuthorName) && StringUtils.isBlank(mVideoName)) {
            if (mShortfilmList != null) {
                mShortfilmList.clear();
            }
            requestData(true);
        } else {
            mSwipeRefreshWidget.setRefreshing(false);
        }
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
                    mSwipeRefreshWidget.setRefreshing(false);
                    mAdapter.updateResult(mShortfilmList);
                    mTvNone.setVisibility(View.GONE);
                    mRequestingList = false;
//                    progressDialog.setVisibility(View.GONE);
                    break;
                case 2:
                    mTvNone.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    break;
                case 3:
//                    footerLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                mClearBtn.setVisibility(View.GONE);
            } else {
                mClearBtn.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
