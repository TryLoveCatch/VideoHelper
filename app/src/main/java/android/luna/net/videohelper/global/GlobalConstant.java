package android.luna.net.videohelper.global;

/**
 * Created by bintou on 15/10/28.
 */
public class GlobalConstant {

    public final static String GDT_APPID = "1105424722";
    public final static String GDT_ADPOSID = "5010913135950914";

    //数据记录
    public final static String VIDEO_ENTER_EVENT = "enter_event";
    public final static String FUNTION_EVENT = "funtion_event";
    public final static String DOG_CLICK_EVENT = "dog_click";
    public final static String AD_EVENT = "ad_event";

    public final static String P_VIDEO_SEARCH = "video_search";
    public final static String P_SEARCH_CONTENT = "search_content";
    public final static String P_HOT_SEARCH = "hot_search";
    public final static String P_RECOMMEND = "recommend";
    public final static String P_GUIDE_CLICK = "guide_click";
    public final static String P_BROWSER_FUNs = "browser_fun";
    public final static String P_VIDEO_ENTER = "video_enter";
    public final static String P_VIDEO_NAME = "video_name";
    public final static String P_TV_LIVE_NAME = "tv_live_name";
    public final static String P_VIDEO_NOTIFICATION = "video_notification";

    public final static String P_GDT_EVENT = "gdt_event";


    public final static String P_VIDEO_CLICK = "video_activity_click";

    public final static String P_DEF_CLICK = "video_activity_def";

    public final static String SHARE_EVENT = "share_event";

    public final static String SHARE_CONTENT = "share_content";


    public final static String ACTION_PLAY_VIDEO_WEB = "PLAY_VIDEO_WEB";

    //站点标示
    public final static String SITE_BAIKAN = "baikan";
    public final static String SITE_YOUKU = "youku";
    public final static String SITE_TUDOU = "tudou";
    public final static String SITE_IQIYI = "iqiyi";
    public final static String SITE_LETV = "letv";
    public final static String SITE_SOHU = "sohu";
    public final static String SITE_QQ = "qq";
    public final static String SITE_MANGO = "mango";
    public final static String SITE_FUN = "fun";
    public final static String SITE_WASU = "wasu";
    public final static String SITE_BESTV = "bestv";
    public final static String SITE_FILM = "film";
    public final static String SITE_TVPLAY = "tvplay";
    public final static String SITE_CARTOON = "cartoon";
    public final static String SITE_BILIBILI = "bilibili";
    public final static String SITE_ACFUN = "acfun";
    public final static String SITE_PPTV = "pptv";
    public final static String SITE_TV_LIVE = "tv_live";
    public final static String SITE_SHORT_FILM = "short_film";
    public final static String SITE_CLOUD_VIDEO = "cloud_video";
    public final static String SITE_YINYUETAI = "yinyuetai";
    public final static String SITE_YOUKU_VIP = "youku_vip";


    //清晰度
    public static final int DEF_ORIGINAL = 4;
    public static final int DEF_SUPER = 2;
    public static final int DEF_HIGH = 0;
    public static final int DEF_NORMAL = 1;
    public static final int DEF_LOW = 3;

    //
    public static final int ARG_VIP_SITE = 1004;


    public static final String LOCAL_NEWEST_FILE = "newest_file";

    public static final String USERAGENT_W = "Mozilla/5.0 (Windows NT 5.1; rv\\:9.0.1) Gecko/20100101 Firefox/9.0.1";


    //sharepreference key
    public static final String SP_CAPTURE_TIMES = "capture_times";
    public static final String SP_HAS_SHARE = "hasShare";
    public static final String SP_FIRST_PARSER = "first_parser";
    public static final String SP_RECORD_ARRAY = "record_array";
    public static final String SP_FILE_BASE_PATH = "file_base_path";
    public static final String SP_CATELOGUE_TITLES = "catelogue_titles";
    public static final String SP_HAS_SHOW_TIPS = "has_show_tips";
    public static final String SP_SHOW_AD_DECLARATION = "has_show_ad_declaration";
    public static final String SP_SHOW_AD_SWITCH = "ad_switch";
    public static final String SP_IQIYI_VIP_DOMAIN = "iqiyi_vip_domain";
    public static final String SP_IQIYI_NORMAL_M3U8 = "iqiyi_normal_api";
    public static final String SP_BAIKAN_API = "baikan_api";

    //操作指令
    public static final int ACTION_DOWNLOAD = 51;
    public static final int ACTION_CAPTURE = 52;
    public static final int ACTION_PLAY_VIDEO = 53;

    public static final int VIDEO_URL_RECEIVE = 1;
    public static final int VIDEO_URL_PARSER_TIME_OUT = 2;

    public static final int RESULT_VISIT_BEGIN = 976;
    public static final int RESULT_VISIT_SUCCESS = 977;


    //preference key
    public static final String KEY_IS_FIRST_IN = "first_in";

    //视频观看次数
    public static final String KEY_VIDEO_VISIT_TIMES = "video_visit_times";


    /**
     * Download
     */
    public static final String INTENT_FORBID_TOAST = "download_forbid_toast";

    public static final int ACTIVITY_TYPE_BOOKMARK = 1;
    public static final int ACTIVITY_TYPE_RECORD = 2;

    public static final int INDEX_BOOKMARK_RETURN = 1001;
    public static final int INDEX_VIDEO_RETURN = 1002;
    public static final int INDEX_EDIT_CATELOGUE_RETURN = 1003;
    public static final int INDEX_SEARCH_RETURN = 1004;

    public static final String EXTRA_INTENT_IS_SEARCH = "isFromSearch";

    public static final String AD_DECLARATION = "各位新老用户朋友，\n" +
            "\n" +
            "视频加速狗将开始尝试广告商业化。\n" +
            "\n" +
            "非常感谢所有用户朋友在过去7个月里对加速狗的信赖和支持，陪伴我们经历了30个版本的迭代优化。与此同时，人力、硬件等成本也不断增加，为了更持续地提供优质服务，我们决定开始尝试嵌入广告的商业化尝试。\n" +
            "\n" +
            "请各位朋友放心，用户体验和商业化的平衡是我们的首要原则。加速狗的初衷是为了提供极速播放的体验，路还很长，愿有你们相伴。";

    public static final String REQUEST_CONSTANCT = "speeddog!@#";

    public static String IQIYI_VIP_DOMAIN = "http://iqiyi.sturgeon.mopaas.com";

}


