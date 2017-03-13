package android.luna.net.videohelper.activity;

import android.content.Intent;
import android.luna.net.videohelper.adapter.VideoIntroduceAdapter;
import android.luna.net.videohelper.fragment.TvLifeFragment;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bintou on 16/3/30.
 */
public class TvLiveActivity extends BaseActivity implements View.OnClickListener {

    private ViewPager mViewPager;
    private VideoIntroduceAdapter mPageAdapter;


    private String[] tvChannelTypes = {"CCTV", "SATELLITE", "PLACE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvlife);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getResources().getString(R.string.tag_name_tvlife));
        findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
        initviewPager();
    }

    private void initviewPager() {
        mViewPager = (ViewPager) findViewById(R.id.tv_life_viewpager);
        mPageAdapter = new VideoIntroduceAdapter(mContext, getSupportFragmentManager(), 4);
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        for (int i = 0; i < tvChannelTypes.length; i++) {
            TvLifeFragment fragment = new TvLifeFragment(tvChannelTypes[i]);
            fragments.add(fragment);
        }
        mPageAdapter.setFragments(fragments);
        mViewPager.requestDisallowInterceptTouchEvent(false);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setCurrentItem(1);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            finish();
        } else if (v.getId() == R.id.btn_search) {
            UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "电视剧");
            Intent intent = new Intent(mContext, SearchActivity.class);
            intent.putExtra("cid", 101);
            startActivity(intent);
        }
    }
}
