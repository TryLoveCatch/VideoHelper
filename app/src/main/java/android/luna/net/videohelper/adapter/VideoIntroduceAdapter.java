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
public class VideoIntroduceAdapter extends FragmentPagerAdapter {

    public static final int TYPE_FILM = 1;
    public static final int TYPE_EPISODE = 2;
    public static final int TYPE_VARIETY = 3;
    public static final int TYPE_TV_LIFE = 4;
    public static final int TYPE_OTHER = -1;


    //    private final String[] filmTitle = {"类似电影", "豆瓣点评", "电影简介"};
    private final String[] filmTitle = {"电影简介"};
    //    private final String[] episodeTitle = {"剧集", "类似", "简介"};
    private final String[] episodeTitle = {"剧集", "简介"};
    private final String[] varietyTitle = {"剧集", "简介"};
    private final String[] otherTitle = {"简介"};
    private final String[] tvLifeTitle = {"中央台", "卫视台","地方台"};

    public String[] curTitle;

    private List<Fragment> mFragments;
    private Context mContext;

    public VideoIntroduceAdapter(Context context, FragmentManager fm, int type) {
        super(fm);
        mContext = context;
        switch (type) {
            case TYPE_FILM:
                curTitle = filmTitle;
                break;
            case TYPE_EPISODE:
                curTitle = episodeTitle;
                break;
            case TYPE_VARIETY:
                curTitle = varietyTitle;
                break;
            case TYPE_TV_LIFE:
                curTitle = tvLifeTitle;
                break;

            default:
                curTitle = otherTitle;
                break;
        }
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
        return curTitle.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return (curTitle.length > position) ? curTitle[position] : "";
    }
}
