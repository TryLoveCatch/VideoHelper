package android.luna.net.videohelper.applicaiton;

import android.content.Intent;
import android.luna.net.videohelper.download.VideosDownloadService;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Handler;

import net.luna.common.BaseApplication;
import net.luna.common.util.PreferencesUtils;
import net.luna.common.wechat.WeChatSDKManager;

import org.kgmeng.dmlib.VideoDownloadManager;
import org.kgmeng.dmlib.config.DownloadConstants;

import java.io.IOException;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import io.vov.vitamio.Vitamio;

/**
 * Created by bintou on 15/10/29.
 */
public class VideoHelperApplication extends BaseApplication {

    private Handler webHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        DownloadConstants.FILE_BASE_PATH = PreferencesUtils.getString(this, GlobalConstant.SP_FILE_BASE_PATH, DownloadConstants.FILE_BASE_PATH);

        BmobConfig config = new BmobConfig.Builder()
                //请求超时时间（单位为秒）：默认15s
                .setConnectTimeout(10)
                //文件分片上传时每片的大小（单位字节），默认512*1024
                .setBlockSize(500 * 1024)
                .build();
        Bmob.getInstance().initConfig(config);

        Bmob.initialize(this, "42b61326ece0b7dc938d32261813d06e");
        WeChatSDKManager.getInstanct(this).init("wx4d91d77c6218583b");
        try {
            VideoDownloadManager downloadManager = VideoDownloadManager.getInstance(getApplicationContext());
            downloadManager.onStart();
            startService(new Intent(this, VideosDownloadService.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Vitamio.isInitialized(this);
    }


    public Handler getWebHandler() {
        return webHandler;
    }

    public void setWebHandler(Handler webHandler) {
        this.webHandler = webHandler;
    }

}
