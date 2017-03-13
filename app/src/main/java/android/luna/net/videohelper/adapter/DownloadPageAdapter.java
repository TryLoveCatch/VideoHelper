package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.luna.common.util.ListUtils;

import java.util.List;

/**
 * Created by bintou on 15/11/5.
 */
public class DownloadPageAdapter extends FragmentPagerAdapter {


    private final String[] titles = {"已下载", "下载中"};

    private List<Fragment> mFragments;
    private Context mContext;

    public DownloadPageAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    public void setFragments(List<Fragment> fragments) {
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return (mFragments == null || ListUtils.getSize(mFragments) == 0) ? null : mFragments.get(position);
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return (titles.length > position) ? titles[position] : "";
    }

}
