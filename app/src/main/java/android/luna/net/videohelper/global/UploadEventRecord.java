package android.luna.net.videohelper.global;

import android.content.Context;
import android.luna.net.videohelper.connection.VideoCatchManager;

import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import net.luna.common.debug.LunaLog;

import java.util.HashMap;
import java.util.Properties;

/**
 * Created by bintou on 15/12/30.
 */
public class UploadEventRecord {

    static HashMap<String, String> eventMap = new HashMap<String, String>();
    static Properties prop = new Properties();

    public static void recordEvent(Context context, String id, String key, String value) {

        if (!LunaLog.isDebug && key != null && value != null) {
            //友盟
            eventMap.clear();
            eventMap.put(key, value);
            MobclickAgent.onEvent(context, id, eventMap);
            //腾讯云
//            prop.clear();
//            prop.setProperty(key, value);
//            StatService.trackCustomKVEvent(context, id, prop);
        }
    }

    public static void recordEventinternal(Context context, String action, String content) {
//        VideoCatchManager.getInstanct(context).uploadRecord(action, content);
    }
}
