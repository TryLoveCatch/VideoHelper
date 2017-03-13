package android.luna.net.videohelper.bean;

import net.luna.common.util.SizeUtils;

import org.kgmeng.dmlib.model.FileInfo;

/**
 * Created by bintou on 15/12/30.
 */
public class VideoDownloadInfo extends FileInfo {

    public boolean isDownloading;
    public int currentMB = 0;
    public int completeMB = 0;
    public boolean needCancel = false;
    public boolean needDelete= false;

    public VideoDownloadInfo(String rawUrl, String name) {
        DOWNLOADURL = rawUrl;
        FILENAME = name;
        HASHVALUE = rawUrl.hashCode();
    }

    public VideoDownloadInfo(FileInfo fileInfo) {
        DOWNLOADURL = fileInfo.DOWNLOADURL;
        FILENAME = fileInfo.FILENAME;
        HASHVALUE = fileInfo.DOWNLOADURL.hashCode();
        curStatus = fileInfo.curStatus;
        cur_size = fileInfo.cur_size;
        downloadType = fileInfo.downloadType;
        FILESIZE = fileInfo.FILESIZE;
        percent = fileInfo.percent;
        if (cur_size > 0) {
            currentMB = (int) (cur_size / SizeUtils.MB_2_BYTE);
        }
        if (FILESIZE > 0) {
            completeMB = (int) (FILESIZE / SizeUtils.MB_2_BYTE);
        }

        if (currentMB > 0 && completeMB > 0 && percent == 0) {
            percent = currentMB * 100 / completeMB;
        }
    }
}
