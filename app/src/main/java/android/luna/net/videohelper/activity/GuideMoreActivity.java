package android.luna.net.videohelper.activity;

import android.content.Intent;
import android.luna.net.videohelper.Ninja.Unit.IntentUnit;
import android.luna.net.videohelper.adapter.CatelogueAdapter;
import android.luna.net.videohelper.bean.Catelogue;
import android.luna.net.videohelper.bean.OlParam;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.SiteConstants;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.luna.common.util.StringUtils;
import net.luna.common.util.ToastUtils;

import java.util.ArrayList;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;


/**
 * Created by bintou on 15/11/12.
 */
public class GuideMoreActivity extends BaseActivity implements View.OnClickListener {


    private ArrayList<Catelogue> mCatelogueList = new ArrayList<Catelogue>();

    private CatelogueAdapter mAdapter;

    private LinearLayout mProgressDialog;

    private EditText mSearchEditor;
    private TextView mEitTv;

    boolean firstIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_more);
        mProgressDialog = (LinearLayout) findViewById(R.id.progress_dialog);
        mProgressDialog.setVisibility(View.GONE);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getResources().getString(R.string.guide));

        initGuideData();
        mSearchEditor = (EditText) findViewById(R.id.search_edit);

        GridView gridView = (GridView) findViewById(R.id.gridview_guide);
        mAdapter = new CatelogueAdapter(mContext, mCatelogueList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new OnIconClickListener());

        mEitTv = (TextView) findViewById(R.id.btn_title_edit);
        mEitTv.setVisibility(View.VISIBLE);

        firstIn = getIntent().getBooleanExtra("first", false);
        if (firstIn) {
            findViewById(R.id.title_bar).setVisibility(View.GONE);
            findViewById(R.id.layout_init_tv).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_complete).setVisibility(View.VISIBLE);
            mEitTv.setText(getResources().getString(R.string.complete));
            mAdapter.setEditMode(true);
            mAdapter.notifyDataSetChanged();
            setResult(GlobalConstant.INDEX_EDIT_CATELOGUE_RETURN);
        }
    }


    private void initGuideData() {
        mCatelogueList.addAll(SiteConstants.getInternalCatelogue(mContext));
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
            mCatelogueList.add(catelogue);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_clear:
                mSearchEditor.setText("");
                break;
            case R.id.btn_complete:
            case R.id.btn_title_edit:
                if (mAdapter.isEditMode()) {
                    mEitTv.setText(getResources().getString(R.string.edit));
                    mAdapter.setEditMode(false);
                    mAdapter.saveCatelogueStatus();
                    setResult(GlobalConstant.INDEX_EDIT_CATELOGUE_RETURN);
//                    if (firstIn) {
                    finish();
//                    }
                } else {
                    findViewById(R.id.layout_init_tv).setVisibility(View.VISIBLE);
                    mEitTv.setText(getResources().getString(R.string.complete));
                    mAdapter.setEditMode(true);
                    mAdapter.notifyDataSetChanged();
                    ToastUtils.show(mContext, getResources().getString(R.string.choose_favorite));
                }
                break;
        }
    }

    public class OnIconClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                if (mCatelogueList != null && mCatelogueList.size() > position) {
//                    if (!mAdapter.isEditMode()) {
                        if (mCatelogueList.get(position).type == 1) {
                            Intent intent = new Intent();
                            intent.putExtra(IntentUnit.URL, mCatelogueList.get(position).url);
                            setResult(GlobalConstant.INDEX_BOOKMARK_RETURN, intent);
                            finish();
                        } else {
                            Intent intent = new Intent(mContext, mCatelogueList.get(position).tartget);
                            intent.putExtra("cid", mCatelogueList.get(position).cid);
                            intent.putExtra("name", mCatelogueList.get(position).title);
                            startActivity(intent);
                        }
                        UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_GUIDE_CLICK, mCatelogueList.get(position).title);
//                    } else {
//                        if (position > 3) {
//                            mAdapter.changeCatelogueStatus(position);
//                        } else {
//                            ToastUtils.show(mContext, "该栏目为固定栏目，无法取消选择");
//                        }
//                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
