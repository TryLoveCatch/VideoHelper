package android.luna.net.videohelper.video.parser;

import android.content.Context;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Message;
import android.util.Base64;

import net.luna.common.basic.GlobalCharsets;
import net.luna.common.debug.LunaLog;
import net.luna.common.entity.HttpResponse;
import net.luna.common.service.HttpCache;
import net.luna.common.util.HttpUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.UrilUtil;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by bintou on 15/10/27.
 */
public class ShuoshuParser {

    private static final char[] DIGITS_LOWER;

    public static final String HIGH_DEFINITION = "&format=high";
    public static final String SUPER_DEFINITION = "&format=super";
    public static final String NORMAL_DEFINITION = "";

    private static final String[] defArray = {HIGH_DEFINITION, NORMAL_DEFINITION, SUPER_DEFINITION};

    static {
        DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    }

    private String mOriUrl;
    private String webUrl;
    private Context mContext;
    private String mDefinition;
    private String mSite;
    private int defini;

    /**
     * 硕鼠视频解析接口
     *
     * @param context
     * @param url        视频网页地址
     * @param definition 优先请求的清晰度
     *                   $ {ShuoshuParser.HIGH_DEFINITION}
     */
    public ShuoshuParser(Context context, String url, int definition, String site) {
        mOriUrl = url;
        webUrl = url;
        defini = definition;
        if (webUrl.contains("c-h5.youku.com/co_show/h5")) {
            String vid = UrilUtil.getQueryString(webUrl, "x#vid");
            webUrl = "http://v.youku.com/v_show/id_" + vid + ".html";
        }
        mContext = context;
        mDefinition = defArray[definition];
        mSite = site;
    }

    /**
     *
     */
    private Message parserUrl() {
        if (!StringUtils.isBlank(webUrl)) {
            try {
                mOriUrl = URLEncoder.encode(webUrl, GlobalCharsets.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mDefinition = StringUtils.nullStrToEmpty(mDefinition);
            String flvUrl = "http://m.flvcd.com/parse_m3u8.php?url=" + mOriUrl + mDefinition;
            LunaLog.d(flvUrl);
            HttpCache httpClient = new HttpCache(mContext);
            HttpResponse httpResponse = httpClient.httpGet(flvUrl);
            if (httpResponse != null) {
                String encryptStr = httpResponse.getResponseBody();
                String url;
                try {
                    String webStr = decryptFlvcd(encryptStr);
                    LunaLog.e(webStr);
                    url = substringBetween(webStr, "<U><![CDATA[", "]]></U>");
                    String des = substringBetween(webStr, "<description>", "</description>");
                    if (des != null && (des.contains("该视频为付费视频") || des.contains("解析失败"))) {
                        LunaLog.d(mSite + "");
//                        if (mSite.equals(GlobalConstant.SITE_YOUKU) || mSite.equals(GlobalConstant.SITE_TUDOU) || mSite.equals("")) {
//                            CloudVideoParser cloudVideoParser = new CloudVideoParser(mContext, webUrl, mSite, 3);
//                            Message msg = cloudVideoParser.run();
//                            msg.arg1 = GlobalConstant.ARG_VIP_SITE;
//                            return msg;
//                        }
                        Message msg = new Message();
                        msg.arg1 = GlobalConstant.ARG_VIP_SITE;
                        msg.obj = "";
                        return msg;
                    }

                    StringUtils.nullStrToEmpty(url);
                    if (mSite.equals(GlobalConstant.SITE_LETV) && !StringUtils.isBlank(url)) {
                        HttpResponse response = HttpUtils.httpGet(url);
                        if (response != null) {
                            String mainStr = response.getResponseBody();
                            JSONObject mainJo = JSONUtils.toJsonObject(mainStr);
                            url = JSONUtils.getString(mainJo, "location", "");
                        }
                    }
                    Message msg = new Message();
                    msg.what = GlobalConstant.VIDEO_URL_RECEIVE;
                    msg.obj = url;
                    msg.arg1 = 1;
                    return msg;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        CloudVideoParser cloudVideoParser = new CloudVideoParser(mContext, webUrl, mSite, 2);
        return cloudVideoParser.run();
    }

    private String substringBetween(String oriStr, String str1, String str2) {
        if ((oriStr == null) || (str1 == null) || (str2 == null))
            return "";
        int i = oriStr.indexOf(str1);
        if (i != -1) {
            int j = oriStr.indexOf(str2, str1.length() + i);
            if (j != -1)
                return oriStr.substring(str1.length() + i, j);
        }
        return "";
    }


    private String decryptFlvcd(String paramString)
            throws Exception {
        char[] arrayOfChar = paramString.toCharArray();
        int k = paramString.length();
        if ((k % 2) == 1) {
            k--;
        }
        byte[] paramsByte = new byte[k >> 1];
        int i = 0;
        int j = 0;
        while (true) {
            if (j >= k) {
                return new String(Base64.decode(transformFlvcd(paramsByte), 0), "utf-8");
            }
            int l = Character.digit(arrayOfChar[j], 16) << 4;
            j += 1;
            int l1 = Character.digit(arrayOfChar[j], 16);
            j += 1;
            paramsByte[i] = (byte) ((l | l1) & 0xFF);
            i += 1;
        }
    }

    private String encryptFlvcd(String paramString)
            throws Exception {
        byte[] paramsByte = transformFlvcd(Base64.encode(paramString.getBytes("utf-8"), 0));
        StringBuilder localStringBuilder = new StringBuilder();
        int i = 0;
        while (true) {
            if (i >= paramsByte.length) {
                return localStringBuilder.toString();
            }
            localStringBuilder.append(DIGITS_LOWER[((paramsByte[i] & 0xF0) >>> 4)]);
            localStringBuilder.append(DIGITS_LOWER[(paramsByte[i] & 0xF)]);
            i += 1;
        }
    }

    public Message run() {
        return parserUrl();
    }

    private static byte[] transformFlvcd(byte[] paramArrayOfByte)
            throws Exception {
        byte[] arrayOfByte1 = new byte[256];
        arrayOfByte1[0] = 63;
        arrayOfByte1[1] = 121;
        arrayOfByte1[2] = -44;
        arrayOfByte1[3] = 54;
        arrayOfByte1[4] = 86;
        arrayOfByte1[5] = -68;
        arrayOfByte1[6] = 114;
        arrayOfByte1[7] = 15;
        arrayOfByte1[8] = 108;
        arrayOfByte1[9] = 94;
        arrayOfByte1[10] = 77;
        arrayOfByte1[11] = -15;
        arrayOfByte1[12] = 89;
        arrayOfByte1[13] = 46;
        arrayOfByte1[14] = -81;
        arrayOfByte1[15] = 4;
        arrayOfByte1[16] = -114;
        arrayOfByte1[17] = 69;
        arrayOfByte1[18] = -88;
        arrayOfByte1[19] = -79;
        arrayOfByte1[20] = -26;
        arrayOfByte1[21] = 91;
        arrayOfByte1[22] = 50;
        arrayOfByte1[23] = -19;
        arrayOfByte1[24] = -37;
        arrayOfByte1[25] = 38;
        arrayOfByte1[26] = 27;
        arrayOfByte1[27] = -80;
        arrayOfByte1[28] = 7;
        arrayOfByte1[29] = 32;
        arrayOfByte1[30] = -64;
        arrayOfByte1[31] = 127;
        arrayOfByte1[32] = -41;
        arrayOfByte1[33] = 27;
        arrayOfByte1[34] = -49;
        arrayOfByte1[35] = -89;
        arrayOfByte1[36] = 3;
        arrayOfByte1[37] = 42;
        arrayOfByte1[38] = 52;
        arrayOfByte1[39] = 29;
        arrayOfByte1[40] = 86;
        arrayOfByte1[41] = 122;
        arrayOfByte1[42] = 6;
        arrayOfByte1[43] = -35;
        arrayOfByte1[44] = -110;
        arrayOfByte1[45] = -1;
        arrayOfByte1[46] = -57;
        arrayOfByte1[47] = 41;
        arrayOfByte1[48] = 52;
        arrayOfByte1[49] = -13;
        arrayOfByte1[50] = -73;
        arrayOfByte1[51] = 10;
        arrayOfByte1[52] = 48;
        arrayOfByte1[53] = 49;
        arrayOfByte1[54] = 92;
        arrayOfByte1[55] = 117;
        arrayOfByte1[56] = 67;
        arrayOfByte1[57] = 72;
        arrayOfByte1[58] = 45;
        arrayOfByte1[59] = 121;
        arrayOfByte1[60] = 93;
        arrayOfByte1[61] = -63;
        arrayOfByte1[62] = 101;
        arrayOfByte1[63] = -90;
        arrayOfByte1[64] = 73;
        arrayOfByte1[65] = 108;
        arrayOfByte1[66] = -29;
        arrayOfByte1[67] = -91;
        arrayOfByte1[68] = 7;
        arrayOfByte1[69] = 46;
        arrayOfByte1[70] = -110;
        arrayOfByte1[71] = 85;
        arrayOfByte1[73] = 81;
        arrayOfByte1[74] = 67;
        arrayOfByte1[75] = 83;
        arrayOfByte1[76] = 113;
        arrayOfByte1[77] = 67;
        arrayOfByte1[78] = 9;
        arrayOfByte1[79] = -57;
        arrayOfByte1[80] = 116;
        arrayOfByte1[81] = -102;
        arrayOfByte1[82] = -26;
        arrayOfByte1[83] = 15;
        arrayOfByte1[84] = 92;
        arrayOfByte1[85] = -14;
        arrayOfByte1[86] = -91;
        arrayOfByte1[87] = 90;
        arrayOfByte1[88] = 56;
        arrayOfByte1[89] = -76;
        arrayOfByte1[90] = 18;
        arrayOfByte1[91] = 1;
        arrayOfByte1[92] = 57;
        arrayOfByte1[93] = 95;
        arrayOfByte1[94] = -1;
        arrayOfByte1[95] = 83;
        arrayOfByte1[96] = 67;
        arrayOfByte1[97] = -84;
        arrayOfByte1[98] = 52;
        arrayOfByte1[99] = 117;
        arrayOfByte1[100] = -93;
        arrayOfByte1[101] = 86;
        arrayOfByte1[102] = 116;
        arrayOfByte1[103] = -58;
        arrayOfByte1[104] = 120;
        arrayOfByte1[105] = -112;
        arrayOfByte1[106] = 70;
        arrayOfByte1[107] = -88;
        arrayOfByte1[108] = -123;
        arrayOfByte1[109] = -45;
        arrayOfByte1[110] = -122;
        arrayOfByte1[111] = 10;
        arrayOfByte1[112] = 38;
        arrayOfByte1[113] = 39;
        arrayOfByte1[114] = -10;
        arrayOfByte1[115] = -60;
        arrayOfByte1[116] = -114;
        arrayOfByte1[117] = 93;
        arrayOfByte1[118] = 31;
        arrayOfByte1[119] = 25;
        arrayOfByte1[120] = 1;
        arrayOfByte1[121] = -120;
        arrayOfByte1[122] = -121;
        arrayOfByte1[123] = -66;
        arrayOfByte1[124] = -40;
        arrayOfByte1[125] = 74;
        arrayOfByte1[126] = -69;
        arrayOfByte1[127] = 83;
        arrayOfByte1[128] = 101;
        arrayOfByte1[129] = -86;
        arrayOfByte1[130] = 107;
        arrayOfByte1[131] = 121;
        arrayOfByte1[132] = -6;
        arrayOfByte1[133] = 109;
        arrayOfByte1[134] = 50;
        arrayOfByte1[135] = 111;
        arrayOfByte1[136] = -33;
        arrayOfByte1[137] = 62;
        arrayOfByte1[138] = 27;
        arrayOfByte1[139] = -63;
        arrayOfByte1[140] = -33;
        arrayOfByte1[141] = 1;
        arrayOfByte1[142] = 52;
        arrayOfByte1[143] = 81;
        arrayOfByte1[144] = 83;
        arrayOfByte1[145] = 109;
        arrayOfByte1[146] = -59;
        arrayOfByte1[147] = 122;
        arrayOfByte1[148] = 11;
        arrayOfByte1[149] = -57;
        arrayOfByte1[150] = -75;
        arrayOfByte1[151] = 34;
        arrayOfByte1[152] = 58;
        arrayOfByte1[153] = 38;
        arrayOfByte1[154] = -75;
        arrayOfByte1[155] = -115;
        arrayOfByte1[156] = 62;
        arrayOfByte1[157] = -46;
        arrayOfByte1[158] = 7;
        arrayOfByte1[159] = -114;
        arrayOfByte1[160] = -60;
        arrayOfByte1[161] = -20;
        arrayOfByte1[162] = 55;
        arrayOfByte1[163] = 4;
        arrayOfByte1[164] = -107;
        arrayOfByte1[165] = -110;
        arrayOfByte1[166] = -62;
        arrayOfByte1[167] = 103;
        arrayOfByte1[168] = -21;
        arrayOfByte1[169] = 40;
        arrayOfByte1[170] = 56;
        arrayOfByte1[171] = -62;
        arrayOfByte1[172] = -110;
        arrayOfByte1[173] = -91;
        arrayOfByte1[174] = -64;
        arrayOfByte1[175] = 53;
        arrayOfByte1[176] = -69;
        arrayOfByte1[177] = 123;
        arrayOfByte1[178] = -87;
        arrayOfByte1[179] = 66;
        arrayOfByte1[180] = -67;
        arrayOfByte1[181] = 57;
        arrayOfByte1[182] = 91;
        arrayOfByte1[183] = 74;
        arrayOfByte1[184] = 82;
        arrayOfByte1[185] = 13;
        arrayOfByte1[186] = 14;
        arrayOfByte1[187] = 109;
        arrayOfByte1[188] = -77;
        arrayOfByte1[189] = -108;
        arrayOfByte1[190] = -28;
        arrayOfByte1[191] = -78;
        arrayOfByte1[192] = 103;
        arrayOfByte1[193] = -85;
        arrayOfByte1[194] = -37;
        arrayOfByte1[195] = -47;
        arrayOfByte1[196] = -33;
        arrayOfByte1[197] = -33;
        arrayOfByte1[198] = 97;
        arrayOfByte1[199] = -103;
        arrayOfByte1[200] = 102;
        arrayOfByte1[201] = -96;
        arrayOfByte1[202] = -78;
        arrayOfByte1[203] = -116;
        arrayOfByte1[204] = 57;
        arrayOfByte1[205] = 55;
        arrayOfByte1[206] = 91;
        arrayOfByte1[207] = 20;
        arrayOfByte1[208] = 80;
        arrayOfByte1[209] = -66;
        arrayOfByte1[210] = -82;
        arrayOfByte1[211] = -77;
        arrayOfByte1[212] = -78;
        arrayOfByte1[213] = 39;
        arrayOfByte1[214] = -63;
        arrayOfByte1[215] = 19;
        arrayOfByte1[216] = 12;
        arrayOfByte1[217] = -2;
        arrayOfByte1[218] = 93;
        arrayOfByte1[219] = -32;
        arrayOfByte1[220] = 65;
        arrayOfByte1[221] = -25;
        arrayOfByte1[222] = 89;
        arrayOfByte1[223] = 104;
        arrayOfByte1[224] = -51;
        arrayOfByte1[225] = -102;
        arrayOfByte1[226] = 76;
        arrayOfByte1[227] = -68;
        arrayOfByte1[228] = -86;
        arrayOfByte1[229] = -90;
        arrayOfByte1[230] = 121;
        arrayOfByte1[231] = 39;
        arrayOfByte1[232] = -83;
        arrayOfByte1[233] = -118;
        arrayOfByte1[234] = -102;
        arrayOfByte1[235] = 110;
        arrayOfByte1[236] = 113;
        arrayOfByte1[237] = -3;
        arrayOfByte1[238] = -23;
        arrayOfByte1[239] = 52;
        arrayOfByte1[240] = -71;
        arrayOfByte1[241] = -16;
        arrayOfByte1[242] = -21;
        arrayOfByte1[243] = 72;
        arrayOfByte1[244] = -99;
        arrayOfByte1[245] = -86;
        arrayOfByte1[246] = -120;
        arrayOfByte1[247] = -16;
        arrayOfByte1[248] = 2;
        arrayOfByte1[249] = 114;
        arrayOfByte1[250] = 72;
        arrayOfByte1[251] = -50;
        arrayOfByte1[252] = 56;
        arrayOfByte1[253] = 73;
        arrayOfByte1[254] = -56;
        arrayOfByte1[255] = 117;
        byte[] arrayOfByte2 = new byte[paramArrayOfByte.length];
        int i = 0;
        while (true) {
            if (i >= paramArrayOfByte.length)
                return arrayOfByte2;
            int j = arrayOfByte1[(i % 256)];
            arrayOfByte2[i] = (byte) (paramArrayOfByte[i] ^ j);
            i += 1;
        }
    }

}
