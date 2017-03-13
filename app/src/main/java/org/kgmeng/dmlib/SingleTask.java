package org.kgmeng.dmlib;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import net.luna.common.util.FileUtils;

import org.kgmeng.dmlib.impl.BaseTask;
import org.kgmeng.dmlib.model.FileInfo;
import org.kgmeng.dmlib.status.DownloadStatus;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SingleTask extends BaseTask implements IDownloadBaseOption {

    /**
     * 下载任务
     */
    private DownloadThread downloadThread;

    public SingleTask(String fileName, final URL url, final FileInfo entity, IDownloadStateListener downloadStateListeners) {
        super(fileName, url, entity);
        this.downloadStateListener = downloadStateListeners;

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case _INIT_:
                        if (downloadThread != null) {
                            downloadThread.setDownloadPosistion(0, block);
                            mExecutor.execute(downloadThread);
                            entity.curStatus = DownloadStatus.DLING;
                            onProcess(1, entity.cur_size, entity.FILESIZE, entity.percent);
                        }
                        break;
                }
            }
        };
        try {
            downloadThread = buildDownloadTask(false, 1);
            downLength = downloadThread.getDownLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyAllToUi(DownloadStatus.WAIT, this.entity, this.downLength);
    }

    @Override
    protected int getMaxThreadSize() {
        return 1;
    }

    @Override
    public void onPrepareOption() {
        notifyAllToUi(DownloadStatus.WAIT, this.entity, downLength);
    }

    @Override
    public void onStartOption() {
        if (!thread.isAlive())
            mExecutor.execute(thread);
        hasCancel = false;
    }


    @Override
    public void onPauseOption() {
        if (downloadThread != null)
            downloadThread.cancel();
        notifyAllToUi(DownloadStatus.PAUSE, this.entity, this.downLength);
    }

    @Override
    public void onStopOption() {
        if (downloadThread != null)
            downloadThread.cancel();
        notifyAllToUi(DownloadStatus.PAUSE, this.entity, this.downLength);
    }

    @Override
    public void onCancelOption() {
        if (downloadThread != null)
            downloadThread.cancel();
        downloadThread.interrupt();
        notifyAllToUi(DownloadStatus.NONE, this.entity);
        hasCancel = true;
        FileUtils.deleteFile(filePath + "_single");
    }

    @Override
    public void onProcess(int threadId, long contentLength, long completeLength, int percent) {
        this.downLength = contentLength;
        entity.cur_size = contentLength;
        entity.FILESIZE = completeLength;
        entity.percent = percent;
        entity.curStatus = DownloadStatus.DLING;
        if (downloadThread != null && !downloadThread.isInterrupt()) {
            notifyAllToUi(DownloadStatus.DLING, this.entity, downLength, completeLength, percent);
        }
    }

    @Override
    public void onFinish(int threadId) {
        new File(buildFileName(false, 1)).renameTo(new File(filePath + ".mp4"));
        notifyAllToUi(DownloadStatus.DONE, this.entity, filePath);
    }

    @Override
    public void onFailed(int threadId, String msg) {
        notifyAllToUi(DownloadStatus.ERROR, this.entity, msg);
    }

    @Override
    public void onCancel(int threadId) {
        notifyAllToUi(DownloadStatus.NONE, this.entity);
    }
}
