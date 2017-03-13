package android.luna.net.videohelper.connection;

import android.luna.net.videohelper.bean.VideoInfo;
import android.luna.net.videohelper.bean.VideoType;

import java.util.List;


/**
 * Created by bintou on 15/11/2.
 */
public interface VideoRequestListener {

    public void onRequestUpdate(VideoType videoType);

    public void onRequestAllData(List<VideoType> videoTypes);

    public void onGetFocuses(List<VideoInfo> focuses);

    public void onRequestFail();
}
