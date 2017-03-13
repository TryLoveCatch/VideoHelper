package org.kgmeng.dmlib;

import org.kgmeng.dmlib.model.FileInfo;

/**
 * DownloadStateListener
 *
 * @author JF.Chang
 * @date 2015/8/31
 */
public interface IDownloadStateListener {

    void onPrepare(FileInfo entity, long size);

    void onProcess(FileInfo entity, long contentLength, long completeLength, int percent);

    void onFinish(FileInfo entity, String savePath);

    void onFailed(FileInfo entity, String msg);

    void onPause(FileInfo entity, long size);

    void onCancel(FileInfo entity);
}
