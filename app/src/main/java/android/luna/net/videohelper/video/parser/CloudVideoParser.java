package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Message;
import android.util.Base64;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.DigestUtils;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bintou on 16/3/24.
 */
public class CloudVideoParser {


    String VD_API = "http://api.vipkpsq.com/v1/parse_third?s=m3u8&v=[v]&t=[t]&sign=[sign]";

    String VD_API_IQY_VIP = "http://api.vipkpsq.com/v1/parse_third?s=sohuys&v=[v]&t=[t]&sign=[sign]";


    private Context mContext;
    private String backupUrl = "";
    private String mOriUrl;
    private String site = "";
    private int defini = 3;

    public CloudVideoParser(Context mContext, String url, String site, int defini) {
        this.mContext = mContext;
        this.site = site;
        this.defini = defini;
        mOriUrl = url;

    }

    private String generateUrl(String url) {
        try {
            if (!StringUtils.isBlank(url)) {
                long t = System.currentTimeMillis();
                String sign = t + url + GlobalConstant.REQUEST_CONSTANCT;

                sign = DigestUtils.md5(Base64.encodeToString(sign.getBytes(), Base64.NO_WRAP));
                LunaLog.d("sign: " + sign);
                url = Base64.encodeToString(url.getBytes(), Base64.NO_WRAP);

                return VD_API.replace("[v]", url).replace("[t]", t + "").replace("[sign]", sign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public Message run() {
        Message msg = new Message();
        msg.what = GlobalConstant.VIDEO_URL_RECEIVE;
        String targetUrl = null;
        try {
            if (mOriUrl.contains("www.iqiyi.com") && mOriUrl.contains("/?vid=")) {
                return ISMParser(msg);
            }

            if (site.equals(GlobalConstant.SITE_YOUKU) || site.equals(GlobalConstant.SITE_TUDOU)) {
                int end = mOriUrl.indexOf("==");
                if (end <= 0) {
                    end = mOriUrl.indexOf(".html");
                    mOriUrl = mOriUrl.substring(0, end + 4);
                } else {
                    mOriUrl = mOriUrl.substring(0, end + 1) + ".html";
                }
                String parserUrl = generateUrl(mOriUrl + "&hd=" + defini);
                LunaLog.d("parserUrl: " + parserUrl);
                return getMobileTargetUrl(parserUrl);
            }

            if (site.equals(GlobalConstant.SITE_LETV)) {
                mOriUrl = mOriUrl.replace("m.le", "www.le").replace("vplay_", "vplay/");
                String parserUrl = generateUrl(mOriUrl + "&hd=" + defini);
                LunaLog.d(parserUrl);
                return getMobileTargetUrl(parserUrl);
            }

            if (site.equals(GlobalConstant.SITE_YINYUETAI)) {
                mOriUrl = mOriUrl.replace("m.yinyuetai", "v.yinyuetai");
                String parserUrl = generateUrl(mOriUrl + "&hd=" + defini);
                return getMobileTargetUrl(parserUrl);
            }

            if (GlobalConstant.SITE_FUN.equals(site)) {
                String parserUrl = generateUrl(mOriUrl + "&hd=" + defini);
                return getMobileTargetUrl(parserUrl);
            }

            if (GlobalConstant.SITE_ACFUN.equals(site)) {
                mOriUrl = mOriUrl.replace("m.", "www.").replace("?ac=", "ac");
                String parserUrl = generateUrl(mOriUrl + "&hd=" + defini);
                return getMobileTargetUrl(parserUrl);
            }

            if (GlobalConstant.SITE_IQIYI.equals(site)) {
                mOriUrl = mOriUrl.replace("m.", "www.");
                String parserUrl = generateUrl(mOriUrl + "&hd=" + defini);

                return getMobileTargetUrl(parserUrl);
            }
            LunaLog.d("mOriUrl: " + mOriUrl);


            if (mOriUrl.contains("magnet:?xt")) {
                LunaLog.d("磁力链");
                String parserUrl = generateUrl(mOriUrl);
                LunaLog.d(parserUrl);
                String parserStr = HttpUtils.httpGetString(parserUrl);
                JSONObject jo = JSONUtils.toJsonObject(parserStr);
                JSONObject pc = JSONUtils.getJSONObject(jo, "result", null);
                JSONArray urls = JSONUtils.getJSONArray(pc, "filelist", null);
                if (urls != null && urls.length() > 0) {
                    JSONObject targetJo = urls.optJSONObject(0);
                    if (targetJo != null) {
                        targetUrl = targetJo.optString("url");
                    }
                }
            }
            if (!StringUtils.isBlank(targetUrl)) {
                LunaLog.d("cloud url: " + targetUrl);
                msg.obj = targetUrl;
                return msg;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        msg.obj = backupUrl;
        return msg;
    }

    public Message getMobileTargetUrl(String url) {
        LunaLog.d("cloudUrl:  " + url);
        Message msg = new Message();
        msg.what = GlobalConstant.VIDEO_URL_RECEIVE;
        String parserStr = HttpUtils.httpGetString(url);
        JSONObject jo = JSONUtils.toJsonObject(parserStr);
        JSONObject mobile = JSONUtils.getJSONObject(jo, "result", null);
        LunaLog.d(mobile.toString());
        String targetUrl = JSONUtils.getString(mobile, "files", "");
        if (!StringUtils.isBlank(targetUrl)) {
            msg.obj = targetUrl;

        }
        return msg;
    }

    public Message ISMParser(Message msg) {
        String phpStr = HttpUtils.httpGetString(mOriUrl);
        String insideVid = getQyPlayVid(phpStr);
        String playUrl = getQyPlayUrl(phpStr);
        if (playUrl.startsWith("http")) {
            playUrl = playUrl + "&" + insideVid;
        } else {
            playUrl = GlobalConstant.IQIYI_VIP_DOMAIN + playUrl + "&" + insideVid;
        }
        msg.obj = playUrl;
        return msg;
    }

    private String getQyPlayVid(String body) {
        String result = "";
        try {
            if (body != null) {
                body = body.replaceAll(" ", "");
//                String vidRegex = "vid:(\\d+)";
                String vidRegex = "playvid='(.*)'";
                Pattern p = Pattern.compile(vidRegex);
                Matcher m = p.matcher(body);
                if (m.find()) {
                    result = m.group(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getQyPlayUrl(String body) {
        String result = "";
        try {
            if (body != null) {
                body = body.replaceAll(" ", "");
                String vidRegex = "urlplay='(.*)'";
                Pattern p = Pattern.compile(vidRegex);
                Matcher m = p.matcher(body);
                if (m.find()) {
                    result = m.group(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getQyVipVid(String body) {
        String result = "";
        try {
            if (body != null) {
                body = body.replaceAll(" ", "");
                String vidRegex = "iqiyi\\.com/v_(.*)\\.html";
                Pattern p = Pattern.compile(vidRegex);
                Matcher m = p.matcher(body);
                if (m.find()) {
                    result = m.group(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getIqiyiUrl() {
        try {
            String vid = "";
            vid = getQyVipVid(mOriUrl);
            LunaLog.d("vid : " + vid);
            if (!StringUtils.isBlank(vid)) {
                long t = System.currentTimeMillis();
                String sign = t + vid + GlobalConstant.REQUEST_CONSTANCT;

                sign = DigestUtils.md5(Base64.encodeToString(sign.getBytes(), Base64.NO_WRAP));
                LunaLog.d("sign: " + sign);
                vid = Base64.encodeToString(vid.getBytes(), Base64.NO_WRAP);

                return VD_API_IQY_VIP.replace("[v]", vid).replace("[t]", t + "").replace("[sign]", sign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

}
