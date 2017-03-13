package org.kgmeng.dmlib.impl;

import android.os.Handler;

import org.kgmeng.dmlib.DownloadThread;
import org.kgmeng.dmlib.IDownloadBaseOption;
import org.kgmeng.dmlib.IDownloadStateListener;
import org.kgmeng.dmlib.IDownloadThreadListener;
import org.kgmeng.dmlib.config.DownloadConstants;
import org.kgmeng.dmlib.model.FileInfo;
import org.kgmeng.dmlib.status.DownloadStatus;
import org.kgmeng.dmlib.utils.FileUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * BaseTask
 *
 * @author JF.Chang
 * @date 2015/8/31
 */
public abstract class BaseTask implements IDownloadBaseOption, IDownloadThreadListener {

    protected final static int _INIT_ = 0x01;

    /**
     * 本地保存文件
     */
    protected String filePath;
    /**
     * 下载路径
     */
    protected final URL url;
    /**
     * 该文件的总长度
     */
    protected long block;
    /**
     * 已经下载的长度
     */
    protected long downLength;
    /**
     * 下载状态
     */
    protected IDownloadStateListener downloadStateListener;
    /**
     * 初始线程
     */
    protected Thread thread;
    /**  */
    protected Handler handler;
    /**
     * 下载对象
     */
    protected FileInfo entity;

    protected ExecutorService mExecutor = Executors.newCachedThreadPool();

    protected boolean hasCancel = false;

    /**
     * 增加下载对象方便上层使用
     *
     * @param fileName
     * @param url
     * @param entity
     */
    public BaseTask(String fileName, final URL url, final FileInfo entity) {
        this.entity = entity;
        FileUtils.createDir(DownloadConstants.FILE_BASE_PATH);
        //如:/mnt/sdcard/MMAssistant/apk/xxxx.apk
        this.filePath = DownloadConstants.FILE_BASE_PATH + "/" + fileName;
        this.url = url;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5 * 1000);
                    conn.setRequestMethod("GET");
                    int rspCode = conn.getResponseCode();
                    if (rspCode != 200) {
                        throw new IOException("response code:" + rspCode);
                    }
                    block = conn.getContentLength();
                    handler.sendEmptyMessage(_INIT_);
                } catch (Exception e) {
                    downloadStateListener.onFailed(entity, String.valueOf(e));
                }
            }
        });
    }

    protected abstract int getMaxThreadSize();

    /**
     * 构造新的下载任务
     *
     * @param isMulite 是否多线程
     * @param threadId 线程ID
     * @return
     * @throws IOException
     */
    protected DownloadThread buildDownloadTask(boolean isMulite, int threadId) throws IOException {
//        if (isMulite) {
//            return new DownloadThread(filePath + "_" + threadId, url, threadId, this);
//        } else {
//            return new DownloadThread(filePath+ "_single", url, threadId, this);
//        }
        entity.path = buildFileName(isMulite, threadId);

        return new DownloadThread(entity.path, url, threadId, this);
    }

    /**
     * 构造临时下载文件名
     *
     * @param isMulite
     * @param threadId
     * @return
     */
    public String buildFileName(boolean isMulite, int threadId) {
        if (isMulite) {
            return filePath + "_" + threadId;
        } else {
            return filePath + "_single";
        }
    }

    @Override
    public boolean equals(Object o) {
        super.equals(o);
        BaseTask strate = (BaseTask) o;
        if (url == null || strate.url == null) return false;
        return url.toString().equals(strate.url.toString()) || entity.FILENAME.equals(strate.entity.FILENAME);
    }

    /**
     * 获取应用
     *
     * @return
     */
    public FileInfo getEntity() {
        return entity;
    }

    public String getDestFileName() {
        return filePath + ".mp4";
    }

    /**
     * 合并文件
     *
     * @param outFile
     * @param files
     */
    public boolean mergeFiles(String outFile, String[] files) {
        FileChannel outChannel = null;
        System.out.println("Merge " + Arrays.toString(files) + " into " + outFile);
        boolean isSuccess = false;
        try {
            outChannel = new FileOutputStream(outFile).getChannel();
            for (String f : files) {
                FileChannel fc = new FileInputStream(f).getChannel();
                ByteBuffer bb = ByteBuffer.allocate(1024 * 8);
                while (fc.read(bb) != -1) {
                    bb.flip();
                    outChannel.write(bb);
                    bb.clear();
                }
                fc.close();
            }
            System.out.println("Merged!! ");
//            return true;
            isSuccess = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException ignore) {
            }
        }
        return isSuccess;
    }

    /**
     * 通知更新
     *
     * @param status
     * @param params
     */
    protected void notifyAllToUi(DownloadStatus status, Object... params) {
        if (!hasCancel) {
            if (downloadStateListener != null) {
                switch (status) {
                    case WAIT:
                        downloadStateListener.onPrepare((FileInfo) params[0], (Long) params[1]);
                        break;
                    case DLING:
                        downloadStateListener.onProcess((FileInfo) params[0], (Long) params[1], (Long) params[2], (int) params[3]);
                        break;
                    case ERROR:
                        downloadStateListener.onFailed((FileInfo) params[0], (String) params[1]);
                        break;
                    case PAUSE:
                        downloadStateListener.onPause((FileInfo) params[0], (Long) params[1]);
                        break;
                    case DONE:
                        downloadStateListener.onFinish((FileInfo) params[0], (String) params[1]);
                        break;
                    case NONE:
                        downloadStateListener.onCancel((FileInfo) params[0]);
                        break;
                }
            }
        }
    }

    public URL getUrl() {
        return url;
    }
}
