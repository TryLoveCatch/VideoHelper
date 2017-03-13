package android.luna.net.videohelper.activity;

import android.luna.net.videohelper.adapter.DownloadPageAdapter;
import android.luna.net.videohelper.fragment.DownloadedFragment;
import android.luna.net.videohelper.fragment.DownloadingFragment;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.luna.common.util.CompatUtils;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.ToastUtils;
import net.luna.common.view.filepicker.picker.FilePicker;

import org.kgmeng.dmlib.config.DownloadConstants;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by bintou on 15/12/30.
 */
public class DownloadActivity extends BaseActivity implements View.OnClickListener {

    private ViewPager mViewPager;
    private DownloadPageAdapter mPageAdapter;
    private DownloadedFragment mDownloadedFragment;
    private DownloadingFragment mDownloadingFragment;

    private TextView mDownloadedTag;
    private TextView mDownloadingTag;

    int selectedColor, unSelectedColor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        selectedColor = CompatUtils.getColor(mContext, R.color.text_black);
        unSelectedColor = CompatUtils.getColor(mContext, R.color.text_black_secondary);

        ((TextView) findViewById(R.id.title)).setText(getResources().getText(R.string.download_name));
        ImageButton deleteBtn = (ImageButton) findViewById(R.id.btn_search);
        deleteBtn.setVisibility(View.VISIBLE);
        deleteBtn.setImageResource(R.drawable.ic_delete);
        ImageButton floderBtn = (ImageButton) findViewById(R.id.btn_funtion_2);
        floderBtn.setVisibility(View.VISIBLE);
        floderBtn.setImageResource(R.drawable.ic_floder);


        initviewPager();
    }

    private void initviewPager() {
        mDownloadedTag = (TextView) findViewById(R.id.downloaded_tag);
        mDownloadingTag = (TextView) findViewById(R.id.downloading_tag);
        mDownloadedTag.setTextColor(selectedColor);
        mViewPager = (ViewPager) findViewById(R.id.download_page_viewpager);
        mPageAdapter = new DownloadPageAdapter(mContext, getSupportFragmentManager());
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        mDownloadedFragment = new DownloadedFragment();
        mDownloadingFragment = new DownloadingFragment(downloadHander);
        fragments.add(mDownloadedFragment);
        fragments.add(mDownloadingFragment);
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
                    mDownloadedTag.setTextColor(selectedColor);
                    mDownloadingTag.setTextColor(unSelectedColor);
                } else if (mViewPager.getCurrentItem() == 1) {
                    mDownloadedTag.setTextColor(unSelectedColor);
                    mDownloadingTag.setTextColor(selectedColor);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public Handler downloadHander = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 1001:
                    if (mDownloadedFragment != null) {
                        mDownloadedFragment.updateList();
                    }
                    break;
            }
        }
    };

    int PICK_REQUEST_CODE = 0;

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_back:
                    finish();
                    break;
                case R.id.btn_funtion_2:
                    ToastUtils.show(mContext, getResources().getString(R.string.choose_download_file));
                    FilePicker picker = new FilePicker(this, FilePicker.DIRECTORY);
                    if (!"/".equals(DownloadConstants.FILE_BASE_PATH)) {
                        File rootFile = new File(DownloadConstants.FILE_BASE_PATH);
                        if (rootFile.exists() && rootFile.isDirectory())
                            picker.setRootPath(rootFile.getParent());
                    }
                    picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
                        @Override
                        public void onFilePicked(String currentPath) {
                            DownloadConstants.FILE_BASE_PATH = currentPath;
                            PreferencesUtils.putString(mContext, GlobalConstant.SP_FILE_BASE_PATH, currentPath);
                            ToastUtils.show(mContext, getResources().getString(R.string.choosen_path) + currentPath, Toast.LENGTH_LONG);
                        }
                    });
                    picker.show();
                    break;
                case R.id.downloaded_tag:
                    if (mViewPager.getCurrentItem() == 0) {
                        mDownloadedFragment.dismissMenu();
                    } else {
                        mDownloadingFragment.dismissMenu();
                    }
                    mViewPager.setCurrentItem(0, true);
                    break;
                case R.id.downloading_tag:
                    if (mViewPager.getCurrentItem() == 0) {
                        mDownloadedFragment.dismissMenu();
                    } else {
                        mDownloadingFragment.dismissMenu();
                    }
                    mViewPager.setCurrentItem(1, true);
                    break;
                case R.id.btn_search:
                    if (mViewPager.getCurrentItem() == 0) {
                        mDownloadedFragment.showMenu();
                    } else {
                        mDownloadingFragment.showMenu();
                    }
                    break;
                case R.id.btn_right:
                    if (mViewPager.getCurrentItem() == 0) {
                        mDownloadedFragment.selectAll();
                    } else {
                        mDownloadingFragment.selectAll();
                    }
                    break;
                case R.id.btn_delete:
                    if (mViewPager.getCurrentItem() == 0) {
                        mDownloadedFragment.delete();
                    } else {
                        mDownloadingFragment.delete();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDownloadedFragment != null) {
            mDownloadedFragment.destroy();
        }
        if (mDownloadingFragment != null) {
            mDownloadingFragment.destroy();
        }
    }


}
