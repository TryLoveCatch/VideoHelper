package android.luna.net.videohelper.download;

import android.app.Service;
import android.content.Intent;
import android.luna.net.videohelper.bean.VideoDownloadInfo;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.IBinder;
import android.text.TextUtils;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.NetWorkUtil;

import org.kgmeng.dmlib.VideoDownloadManager;

import java.io.IOException;
import java.net.URLEncoder;


public class VideosDownloadService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        LunaLog.d("onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            boolean forbidToast = intent.getBooleanExtra(GlobalConstant.INTENT_FORBID_TOAST, false);
            final String downloadUrl = intent.getDataString();
//            LunaLog.d("m3u8: " + m3u8);
            if (Intent.ACTION_INSERT.equals(action)) {
                String title = intent.getStringExtra(Intent.EXTRA_TITLE);
                if (TextUtils.isEmpty(title)) {
                    title = downloadUrl;
                    int question = title.indexOf('?');
                    if (question != -1) {
                        int start = title.substring(0, question).lastIndexOf('/');
                        if (start != -1) {
                            title = title.substring(start);
                            title = URLEncoder.encode(title);
                        }
                    } else {
                        int start = title.lastIndexOf('/');
                        if (start != -1) {
                            title = title.substring(start);
                            title = URLEncoder.encode(title);
                        }
                    }
                }
                title = title.replace(" ", "_");
                title = title.trim();
                //防止文件名过长
                if (title.length() > 200) {
                    title = title.substring(0, 200);
                }

                VideoDownloadInfo videoDownloadInfo = new VideoDownloadInfo(downloadUrl, title);
                try {
                    VideoDownloadManager.getInstance(getApplicationContext()).onOffer(videoDownloadInfo,true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                VideoDownloadManager videoDownloadManager = VideoDownloadManager.getInstance(getApplicationContext());
                if (!(NetWorkUtil.isWifiAvailable() && videoDownloadManager.isRunning() && videoDownloadManager.getTotalTaskCount() > 0)) {
                    stopSelf();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

}
