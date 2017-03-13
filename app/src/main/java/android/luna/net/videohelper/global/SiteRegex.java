package android.luna.net.videohelper.global;

import net.luna.common.util.StringUtils;

/**
 * Created by bintou on 15/11/26.
 */
public class SiteRegex {

    //解析入口正则
    public final static String BAIKAN_REGEX = "http://www\\.meiyouad\\.com/vplay/(\\d+)\\.html";


    public final static String SOHU_REGEX = "^http://m\\.tv\\.sohu\\.com/v.*\\.shtml?.*$";
    public final static String SOHU_REGEX_2 = "^http://m\\.tv\\.sohu\\.com/u/vw/.*\\.shtml?.*$";
    public final static String SOHU_REGEX_3 = "^http://m\\.tv\\.sohu\\.com/20[0-9]{6}/.*\\.shtml?.*$";

    public final static String FUNSHION_FUN_TV = "^http://m\\.fun\\.tv/[v/m]play/\\?[v/m]id=.*$";

    public final static String QQ_REGEX = "^http://m\\.v\\.qq\\.com/.*vid=.*$";
    public final static String QQ_REGEX_2 = "^http://m\\.v\\.qq\\.com/cover/[a-zA-Z0-9]/.[a-zA-Z0-9]*\\.html.*$";
    public final static String QQ_REGEX_3 = "^https://m\\.v\\.qq\\.com/cover/[a-zA-Z0-9]/.[a-zA-Z0-9]*\\.html.*$";
    public final static String QQ_REGEX_4 = "^http://m\\.v\\.qq\\.com/page/.*/.[a-zA-Z0-9]*\\.html$";

    public final static String YOUKU_REGEX = "^http://v\\.youku\\.com/.*$";
    public final static String YOUKU_REGEX_2 = "^http://c-h5\\.youku\\.com/co_show/h5/id_.*$";
    public final static String TUDOU_REGEX = "^http://www\\.tudou\\.com/.*play/.*$";
    public final static String TUDOU_REGEX_2 = "^http://www\\.tudou\\.com/programs/view/.*$";

    public final static String LETV_REGEX = "^http://m\\.letv\\.com/vplay_.*$";
    public final static String LETV_REGEX_2 = "^http://m\\.le\\.com/vplay_.*$";

    public final static String IQIYI_REGEX = "^http://m\\.iqiyi\\.com/[v|w]_.*$";
    public final static String IQIYI_REGEX_2 = "^http://m\\.iqiyi\\.com/[a-z]+/\\d{8}/.*$";

    public final static String PPS_REGEX = "^http://v\\.pps\\.tv/.*$";

    public final static String HUNAN_REGEX = "^http://m\\.mgtv\\.com/#/play/.*$";

    public final static String HUNAN_SITE_REGEX = "^http://m\\.mgtv\\.com/.*$";

    public final static String M1905_REGEX = "^http://www\\.1905\\.com/.*$";

    public final static String WASU_REGEX = "^http://www\\.wasu\\.cn/wap/.*/id/.*$";

    public final static String ACFUN_REGEX = "^http://acfun\\.tudou\\.com/v/a[b|c].*$";

    public final static String ACFUN_REGEX_2 = "^http://m\\.acfun\\.tv/v/\\?a[b|c]=.*$";

    public final static String BILIBILI_REGEX = "^http://www.bilibili.com/video/av(\\d+)/.*$";

    public final static String BILIBILI_REGEX_2 = "^http://www.bilibili.com/mobile/video/av(\\d+)\\..*$";

    public final static String PPTV_REGEX = "^http://m\\.pptv\\.com/show/.*\\.html.*$";

    public final static String YYT_REGEX = "^http://m.yinyuetai.com/video/\\d+$";

    public static String[] regexes = {IQIYI_REGEX,IQIYI_REGEX_2,BAIKAN_REGEX, SOHU_REGEX, SOHU_REGEX_2, SOHU_REGEX_3, FUNSHION_FUN_TV, QQ_REGEX, QQ_REGEX_2, QQ_REGEX_3, QQ_REGEX_4,
            YOUKU_REGEX, YOUKU_REGEX_2, TUDOU_REGEX, TUDOU_REGEX_2, HUNAN_REGEX, M1905_REGEX, PPS_REGEX, PPTV_REGEX, WASU_REGEX, LETV_REGEX, LETV_REGEX_2, ACFUN_REGEX, ACFUN_REGEX_2,
            BILIBILI_REGEX, BILIBILI_REGEX_2, PPTV_REGEX, YYT_REGEX};


    public static boolean checkUrlcanParser(String url) {
        try {
            if (url != null) {
                for (String regex : regexes) {
                    if (!StringUtils.isBlank(regex) && url.matches(regex)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
