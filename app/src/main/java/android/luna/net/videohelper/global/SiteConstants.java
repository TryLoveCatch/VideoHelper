package android.luna.net.videohelper.global;

import android.content.Context;
import android.luna.net.videohelper.activity.ShortfilmActivity;
import android.luna.net.videohelper.activity.TvLiveActivity;
import android.luna.net.videohelper.activity.VideoGridActivity;
import android.luna.net.videohelper.bean.Catelogue;
import android.luna.net.videohelptools.R;

import java.util.ArrayList;

/**
 * Created by bintou on 16/3/22.
 */
public class SiteConstants {


    //所有的导航
    public static final String[] ALL_TITILES = {"无广告", "优酷", "土豆", "搜狐视频", "腾讯网", "乐视", "芒果TV", "AcFun", "哔哩哔哩", "PPTV", "华数", "风行", "音悦台"};
    public static final String[] ALL_SITES = {GlobalConstant.SITE_BAIKAN, GlobalConstant.SITE_YOUKU, GlobalConstant.SITE_TUDOU, GlobalConstant.SITE_SOHU,
            GlobalConstant.SITE_QQ, GlobalConstant.SITE_LETV, GlobalConstant.SITE_MANGO, GlobalConstant.SITE_ACFUN
            , GlobalConstant.SITE_BILIBILI, GlobalConstant.SITE_PPTV, GlobalConstant.SITE_WASU, GlobalConstant.SITE_FUN, GlobalConstant.SITE_YINYUETAI};
    public static final int[] ALL_ICONS = {R.mipmap.home_ic_noad03, R.mipmap.home_ic_youku, R.mipmap.home_ic_tudou, R.mipmap.home_ic_sohu,
            R.mipmap.home_ic_vqq, R.mipmap.home_ic_letv, R.mipmap.home_ic_mango, R.mipmap.home_ic_acfun, R.mipmap.home_ic_bilibili, R.mipmap.home_ic_pptv, R.mipmap.home_ic_huashu, R.mipmap.home_ic_fengxing, R.mipmap.home_ic_yinyuetai};
    public static final String[] ALL_WEBSITES = {
            "http://www.meiyouad.com/",
            "http://www.youku.com/",
            "http://www.tudou.com/",
//            "http://m.iqiyi.com/dianshiju/",
            "http://m.tv.sohu.com/",
            "http://3g.v.qq.com/",
            "http://m.letv.com/",
            "http://m.mgtv.com/",
            "http://www.acfun.tv/",
            "http://www.bilibili.com/mobile/index.html",
            "http://m.pptv.com/?f=pptv",
            "http://www.wasu.cn/wap/",
            "http://m.fun.tv/",
            "http://m.yinyuetai.com/"

    };


    public final static ArrayList<Catelogue> getInternalCatelogue(Context context) {
        ArrayList<Catelogue> catelogueList = new ArrayList<Catelogue>();

        Catelogue videoFeatured = new Catelogue();
        videoFeatured.title = context.getResources().getString(R.string.tag_name_film);
        videoFeatured.icon = R.mipmap.home_ic_movie;
        videoFeatured.site = GlobalConstant.SITE_FILM;
        videoFeatured.tartget = VideoGridActivity.class;
        videoFeatured.cid = 1;
        videoFeatured.type = 2;
        videoFeatured.isSelected = true;
        catelogueList.add(videoFeatured);

        Catelogue tvPlayFeatured = new Catelogue();
        tvPlayFeatured.title = context.getResources().getString(R.string.tag_name_episode);
        tvPlayFeatured.icon = R.mipmap.home_ic_tv;
        tvPlayFeatured.site = GlobalConstant.SITE_TVPLAY;
        tvPlayFeatured.tartget = VideoGridActivity.class;
        tvPlayFeatured.cid = 2;
        tvPlayFeatured.isSelected = true;
        tvPlayFeatured.type = 2;
        catelogueList.add(tvPlayFeatured);

        Catelogue shortfilmFeature = new Catelogue();
        shortfilmFeature.title = context.getResources().getString(R.string.tag_name_shortfilm);
        shortfilmFeature.icon = R.mipmap.home_ic_shortvideo;
        shortfilmFeature.site = GlobalConstant.SITE_SHORT_FILM;
        shortfilmFeature.tartget = ShortfilmActivity.class;
        shortfilmFeature.cid = 14;
        shortfilmFeature.type = 2;
        shortfilmFeature.isSelected = true;
        catelogueList.add(shortfilmFeature);

        Catelogue cloudVideoFeatured = new Catelogue();
        cloudVideoFeatured.title = context.getResources().getString(R.string.tag_name_cloud_video);
        cloudVideoFeatured.icon = R.mipmap.home_ic_cloudtv;
        cloudVideoFeatured.site = GlobalConstant.SITE_CLOUD_VIDEO;
        cloudVideoFeatured.tartget = VideoGridActivity.class;
        cloudVideoFeatured.cid = 102;
        cloudVideoFeatured.type = 2;
        cloudVideoFeatured.isSelected = true;
        catelogueList.add(cloudVideoFeatured);


        Catelogue cartoonFeatured = new Catelogue();
        cartoonFeatured.title = context.getResources().getString(R.string.tag_name_cartoon);
        cartoonFeatured.icon = R.mipmap.home_ic_comic;
        cartoonFeatured.site = GlobalConstant.SITE_CARTOON;
        cartoonFeatured.tartget = VideoGridActivity.class;
        cartoonFeatured.cid = 3;
        cartoonFeatured.type = 2;
        catelogueList.add(cartoonFeatured);

        Catelogue tvLifeFeatured = new Catelogue();
        tvLifeFeatured.title = context.getResources().getString(R.string.tag_name_tvlife);
        tvLifeFeatured.icon = R.mipmap.home_ic_zhibo;
        tvLifeFeatured.site = GlobalConstant.SITE_TV_LIVE;
        tvLifeFeatured.tartget = TvLiveActivity.class;
        tvLifeFeatured.cid = 101;
        tvLifeFeatured.type = 2;
        catelogueList.add(tvLifeFeatured);

        return catelogueList;

    }

}
