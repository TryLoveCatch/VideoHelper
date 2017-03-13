package org.kgmeng.dmlib.model;

import org.kgmeng.dmlib.Type;
import org.kgmeng.dmlib.status.DownloadStatus;

import java.io.Serializable;


/**
 * AppInfo
 *
 * @author JF Zhang
 * @date 2015/8/21
 */
public class FileInfo implements Serializable {
    public static final long serialVersionUID = 1L;

    public String FILENAME;//名称
    public String DOWNLOADURL;//下载地址

    //总长度
    public long FILESIZE = 0;

    public int HASHVALUE;
    public DownloadStatus curStatus;
    //下载的长度
    public long cur_size = 0;

    public int percent = 0;
    public Type downloadType;

    public String path;

    public void setCurStatus(int statusValue) {
        switch (statusValue) {
            case 0:
                curStatus = DownloadStatus.NONE;
                break;
            case 0x21:
                curStatus = DownloadStatus.WAIT;
                break;
            case 0x22:
                curStatus = DownloadStatus.DLING;
                break;
            case 0x23:
                curStatus = DownloadStatus.PAUSE;
                break;
            case 0x25:
                curStatus = DownloadStatus.DONE;
                break;
            case 0x26:
                curStatus = DownloadStatus.ERROR;
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo appInfo = (FileInfo) o;

        if (DOWNLOADURL != null ? !DOWNLOADURL.equals(appInfo.DOWNLOADURL) : appInfo.DOWNLOADURL != null)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "APPNAME='" + FILENAME + '\'' +
                ", DOWNLOADURL='" + DOWNLOADURL + '\'' +
                ", APPSIZE=" + FILESIZE +
                ", HASHVALUE='" + HASHVALUE + '\'' +
                ", curStatus=" + curStatus +
                ", cur_size=" + cur_size +
                ", FILESEZIE=" + FILESIZE +
                ", percent=" + percent +
                ", downloadType=" + downloadType +
                '}';
    }
}
