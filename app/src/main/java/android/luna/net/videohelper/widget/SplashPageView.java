package android.luna.net.videohelper.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.luna.net.videohelper.activity.GuideMoreActivity;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelptools.R;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import net.luna.common.util.PreferencesUtils;
import net.luna.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bintou on 16/2/19.
 */
public class SplashPageView {

    private View containView;
    private ViewPager mViewPager;
    private SplashAdapter mPageAdapter;
    private ViewPagerPointer mPointerView;
    private Context context;
    private List<View> views = new ArrayList<>();

    private int[] pics = {R.mipmap.guidepage01, R.mipmap.guidepage02, R.mipmap.guidepage03};

    public SplashPageView(Context context, View containView) {
        this.containView = containView;
        this.context = context;
    }

    public void showGuideSplash() {
        if (containView != null) {
            views.add(LayoutInflater.from(context).inflate(R.layout.item_splash_pager, null));
            views.add(LayoutInflater.from(context).inflate(R.layout.item_splash_pager, null));
            views.add(LayoutInflater.from(context).inflate(R.layout.item_splash_pager, null));
            views.add(LayoutInflater.from(context).inflate(R.layout.item_splash_pager, null));
            mViewPager = (ViewPager) containView.findViewById(R.id.viewpage_splash);
            mPageAdapter = new SplashAdapter();
            mViewPager.setAdapter(mPageAdapter);
            mPointerView = (ViewPagerPointer) containView.findViewById(R.id.pointer);
            mPointerView.bindViewPager(mViewPager, views.size()-1);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 3) {
                        containView.setVisibility(View.GONE);
                        String titles = PreferencesUtils.getString(context, GlobalConstant.SP_CATELOGUE_TITLES, "");
                        if (StringUtils.isBlank(titles)) {
                            Intent intent = new Intent(context, GuideMoreActivity.class);
                            intent.putExtra("first", true);
                            ((Activity) context).startActivityForResult(intent, GlobalConstant.INDEX_EDIT_CATELOGUE_RETURN);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public class SplashAdapter extends PagerAdapter {


        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (position < views.size()) {
                View view = views.get(position);
                ImageView img = (ImageView) view.findViewById(R.id.pic);
                if (position < 3) {
                    img.setImageResource(pics[position]);
                }
//                if (position == 2) {
//                    ImageButton btn = (ImageButton) view.findViewById(R.id.btn_enter);
//                    btn.setVisibility(View.VISIBLE);
//                    btn.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                        }
//                    });
//                }
                container.addView(view);
                return views.get(position);
            }
            return super.instantiateItem(container, position);
        }
    }

}