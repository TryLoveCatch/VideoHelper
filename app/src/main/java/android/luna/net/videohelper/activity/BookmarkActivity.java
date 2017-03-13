package android.luna.net.videohelper.activity;

import android.luna.net.videohelper.Ninja.Database.Record;
import android.luna.net.videohelper.Ninja.Database.RecordAction;
import android.luna.net.videohelper.adapter.BookmarkListAdapter;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.widget.BookMarkMenu;
import android.luna.net.videohelper.widget.DividerItemDecoration;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.ListUtils;

import java.util.List;

/**
 * Created by bintou on 15/12/7.
 */
public class BookmarkActivity extends BaseActivity implements View.OnClickListener {


    private BookmarkListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecordAction mRecordAction;

    private ImageButton mEditBtn;
    private TextView mTitleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        mTitleTv = (TextView) findViewById(R.id.title);
        mEditBtn = (ImageButton) findViewById(R.id.btn_search);
        mEditBtn.setVisibility(View.VISIBLE);
        mEditBtn.setImageResource(R.drawable.ic_delete);
        RecyclerView bookmarkRecyclerView = (RecyclerView) findViewById(R.id.recycleview_bookmark);
        mRecordAction = RecordAction.getInstance(mContext);
        mRecordAction.open(true);
        int type = getIntent().getIntExtra("type", 1);
        if (type == GlobalConstant.ACTIVITY_TYPE_BOOKMARK) {
            mTitleTv.setText(getResources().getString(R.string.bookmark));
            List<Record> records = mRecordAction.listBookmarks();
            if (ListUtils.getSize(records) == 0) {
                findViewById(R.id.bookmark_none_layout).setVisibility(View.VISIBLE);
            }
            mAdapter = new BookmarkListAdapter(this, records, mRecordAction, type);
        } else {
            mTitleTv.setText(getResources().getString(R.string.visit_record));
            List<Record> records = mRecordAction.listPlayRecords();
            if (ListUtils.getSize(records) == 0) {
                findViewById(R.id.record_none_layout).setVisibility(View.VISIBLE);
            }
            mAdapter = new BookmarkListAdapter(this, records, mRecordAction, type);
        }
        bookmarkRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mContext);
        bookmarkRecyclerView.setLayoutManager(mLayoutManager);
        bookmarkRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        changeEditStatus();
    }

    public void changeEditStatus() {
        if (mAdapter.getItemCount() > 0) {
//            mEditBtn.setImageResource(R.drawable.ic_edit);
        } else {
//            mEditBtn.setImageResource(R.mipmap.ic_edit_disable);
        }
    }


    private BookMarkMenu mBookMarkMenu;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_search:
                if (mAdapter.getItemCount() > 0) {
                    mAdapter.changeEditStatus();
                    if (mBookMarkMenu == null) {
                        mBookMarkMenu = new BookMarkMenu(BookmarkActivity.this, mAdapter);
                    }
                    if (mBookMarkMenu.isShowing()) {
                        mBookMarkMenu.dismiss();
                    } else {
                        mBookMarkMenu.show();
                    }
                }
                break;
            case R.id.btn_right:
                if (mBookMarkMenu.isHasSelectAll()) {
                    mAdapter.selectAllBookMark(false);
                } else {
                    mAdapter.selectAllBookMark(true);
                }
                if (mBookMarkMenu != null) {
                    mBookMarkMenu.changeSelectedStatus();
                }
                break;
            case R.id.btn_delete:
                mAdapter.deleteBookmark();
                changeEditStatus();
                break;
        }
    }

    public void showMenu() {
        if (mBookMarkMenu == null) {
            mBookMarkMenu = new BookMarkMenu(BookmarkActivity.this, mAdapter);
        }
        if (!mBookMarkMenu.isShowing()) {
            mBookMarkMenu.show();
        }
    }

    @Override
    protected void onDestroy() {
        mRecordAction.close();
        super.onDestroy();
    }
}
