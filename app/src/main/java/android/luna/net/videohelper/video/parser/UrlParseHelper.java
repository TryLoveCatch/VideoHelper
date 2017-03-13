package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.bean.TvLife;
import android.luna.net.videohelper.bean.TvLiveP;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.ListUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by bintou on 16/1/13.
 * 获取视频地址并通过handler返回。
 */
public class UrlParseHelper implements Runnable {


    private String url;
    private String site;
    private Handler handler;
    private String vid;
    private int definition;
    private Context context;
    private boolean getAllDef;
    private String name;
    private TvLiveP tvLive;
    private boolean isMp4First = false;
    private String content;

    public UrlParseHelper(Context context, String url, String site, String vid, String name, int definition, Handler handler, boolean isMp4First) {
        this.context = context;
        this.url = url;
        this.site = site;
        this.handler = handler;
        this.vid = vid;
        this.name = name;
        this.isMp4First = false;
        this.definition = definition;
    }

    public UrlParseHelper(Context context, String url, String site, String vid, Handler handler, boolean getAllDef) {
        this.context = context;
        this.url = url;
        this.site = site;
        this.handler = handler;
        this.vid = vid;
        this.getAllDef = getAllDef;
        definition = GlobalConstant.DEF_SUPER;
    }

    public UrlParseHelper(Context context, String url, String site, String vid, int definition, Handler handler) {
        this.context = context;
        this.url = url;
        this.site = site;
        this.handler = handler;
        this.vid = vid;
        this.definition = definition;
    }

    public UrlParseHelper(Context context, String url, String site, String vid, int definition, String content, Handler handler) {
        this.context = context;
        this.url = url;
        this.site = site;
        this.handler = handler;
        this.vid = vid;
        this.definition = definition;
        this.content = content;
    }

    public UrlParseHelper(Context context, TvLiveP tvLive, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.site = GlobalConstant.SITE_TV_LIVE;
        this.tvLive = tvLive;

    }


    private void startParser() {
        Message msg = null;
        LunaLog.d("site: " + site);
        try {
            if (StringUtils.isBlank(url) && StringUtils.isBlank(vid) && tvLive == null) {
                handler.sendEmptyMessage(GlobalConstant.VIDEO_URL_RECEIVE);
            }
            if (!StringUtils.isBlank(site)) {
                if (site.equals(GlobalConstant.SITE_IQIYI)) {
//                    KankeParser kankeParser = new KankeParser(context, url, site, definition, getAllDef);
//                    msg = kankeParser.run();
                    QiyiParser qiyiParser = new QiyiParser(context, url, definition);
                    msg = qiyiParser.run();
                } else if (site.equals(GlobalConstant.SITE_MANGO)) {
                    MangoParser internalParser = new MangoParser(context, url, definition);
                    msg = internalParser.run();
                } else if (site.equals(GlobalConstant.SITE_BAIKAN)) {
                    BaikanParser baikanParser = new BaikanParser(context, url, content);
                    msg = baikanParser.run();
                } else if (site.equals(GlobalConstant.SITE_LETV)) {
                    LetvParser letvParser = new LetvParser(context, url, definition);
                    msg = letvParser.run();
                } else if (site.equals(GlobalConstant.SITE_FUN)) {
                    KankeParser kankeParser = new KankeParser(context, url, site, definition, getAllDef);
                    msg = kankeParser.run();
                } else if (site.equals(GlobalConstant.SITE_SOHU)) {
                    MamaParser mamaParser = new MamaParser(context, url, site, definition, isMp4First);
                    msg = mamaParser.run();
                } else if (site.equals(GlobalConstant.SITE_BESTV)) {
                    InternalParser internalParser = new InternalParser(context, vid, GlobalConstant.SITE_BESTV, definition);
                    msg = internalParser.run();
                } else if (site.equals(GlobalConstant.SITE_QQ)) {
                    MamaParser mamaParser = new MamaParser(context, url, site, definition, isMp4First);
                    msg = mamaParser.run();
                } else if (site.equals(GlobalConstant.SITE_PPTV)) {
                    BtJsonParser btJsonParser = new BtJsonParser(context, url, site, definition);
                    msg = btJsonParser.run();
                } else if (site.equals(GlobalConstant.SITE_ACFUN)) {
                    KankeParser kankeParser = new KankeParser(context, url, site, definition, getAllDef);
                    msg = kankeParser.run();
                } else if (site.equals(GlobalConstant.SITE_BILIBILI)) {
                    MamaParser mamaParser = new MamaParser(context, url, site, definition, isMp4First);
                    msg = mamaParser.run();
                } else if (site.equals(GlobalConstant.SITE_TV_LIVE)) {
                    TvLifeParser tvLifeParser = new TvLifeParser(context, tvLive);
                    msg = tvLifeParser.run();
                    if (msg.obj == null) {
                        getTvSiteFromBmob();
                        return;
                    }
                } else if (site.equals(GlobalConstant.SITE_CLOUD_VIDEO)) {
                    CloudVideoParser cloudVideoParser;
                    if (definition == GlobalConstant.DEF_SUPER) {
                        cloudVideoParser = new CloudVideoParser(context, url, GlobalConstant.SITE_CLOUD_VIDEO, 3);
                    } else if (definition == GlobalConstant.DEF_HIGH) {
                        cloudVideoParser = new CloudVideoParser(context, url, GlobalConstant.SITE_CLOUD_VIDEO, 2);
                    } else {
                        cloudVideoParser = new CloudVideoParser(context, url, GlobalConstant.SITE_CLOUD_VIDEO, 1);
                    }
                    msg = cloudVideoParser.run();
                } else if (site.equals(GlobalConstant.SITE_YINYUETAI)) {
                    MamaParser mamaParser = new MamaParser(context, url, site, definition, isMp4First);
                    msg = mamaParser.run();
                } else {
                    if (site.equals(GlobalConstant.SITE_TUDOU)) {
                        //处理一下视频的地址
                        url = url.replace("albumcover", "albumplay");
                    }
                    KankeParser kankeParser = new KankeParser(context, url, site, definition, getAllDef);
                    msg = kankeParser.run();
                }
            } else {
                ShuoshuParser shuoshuParser = new ShuoshuParser(context, url, definition, site);
                msg = shuoshuParser.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (handler != null) {
            if (msg == null) {
                msg = new Message();
            }
            if (!StringUtils.isBlank(name)) {
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                msg.setData(bundle);
            }
            handler.sendMessage(msg);
        }
    }

    private void getTvSiteFromBmob() {
        BmobQuery<TvLife> query = new BmobQuery<>();
        query.addWhereEqualTo("name", tvLive.channelName);
        query.order("-updatedAt");
        query.findObjects(context, new FindListener<TvLife>() {
            @Override
            public void onSuccess(final List<TvLife> list) {
                ThreadUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        String targetUrl = null;
                        if (ListUtils.getSize(list) > 0) {
                            for (TvLife temp : list) {
                                if (temp != null && !StringUtils.isBlank(temp.url)) {
                                    String content = HttpUtils.httpGetString(temp.url);
                                    if (content != null && content.length() > 30) {
                                        targetUrl = temp.url;
                                        break;
                                    }
                                }
                            }
                            if (!StringUtils.isBlank(targetUrl)) {
                                Message msg = new Message();
                                msg.obj = targetUrl;
                                LunaLog.d("tv target : " + targetUrl);
                                msg.what = GlobalConstant.VIDEO_URL_RECEIVE;
                                if (handler != null) {
                                    if (msg == null) {
                                        msg = new Message();
                                    }
                                    if (!StringUtils.isBlank(name)) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("name", name);
                                        msg.setData(bundle);
                                    }
                                    handler.sendMessage(msg);
                                }
                            }
                        }
                    }
                });

            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    public void getFinalUrl(final String targetUrl) {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                String finalUrl = targetUrl;
                try {
                    URL tvSiteURL = new URL(targetUrl);
                    connection = (HttpURLConnection) tvSiteURL.openConnection();
                    connection.setInstanceFollowRedirects(true);
                    connection.connect();
                    finalUrl = connection.getHeaderField("Location");
                    LunaLog.d("location : " + finalUrl);
                } catch (Exception e) {
                    LunaLog.e(e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                Message message = new Message();
                message.what = 1;
                message.obj = finalUrl;
                if (handler != null) {
                    handler.sendMessage(message);
                }
            }
        });

    }

    @Override
    public void run() {
        startParser();
    }
}
