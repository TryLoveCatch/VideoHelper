package org.kgmeng.dmlib;

import android.annotation.TargetApi;
import android.luna.net.videohelper.global.GlobalConstant;
import android.os.Build;
import android.util.Log;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * DownloadThread
 *
 * @author JF.Chang
 * @date 2015/8/28
 */
public class DownloadThread extends Thread implements Serializable {


    /**
     * 本地保存文件
     */
    private File saveFile;
    /**
     * 下载路径
     */
    private URL downUrl;
    /**
     * 该线程要下载的长度
     */
    private long block;
    /**
     * 起始位置
     */
    private long startIdx;
    /**
     * 结束位置
     */
    private long endIdx;
    /**
     * 该线程已经下载的长度
     */
    private long downLength;
    /**
     * 该线程ID
     */
    private int threadId;
    /**
     * 是否下载完成
     */
    private boolean finish = false;
    /**
     * 是否下载中断
     */
    private boolean interrupt = false;

    private IDownloadThreadListener downloadTaskListener;

//    private MultiTask multiTask;
//
//    public void setMultiTask(MultiTask task){
//        this.multiTask = task;
//    }

    /**
     * @param filePath             文件名,绝对路径
     * @param url                  下载地址
     * @param threadId             该线程ID
     * @param downloadTaskListener 回调接口
     */
    public DownloadThread(String filePath, URL url, int threadId, IDownloadThreadListener downloadTaskListener) throws IOException {
        this.saveFile = new File(filePath);
        this.downUrl = url;
        this.threadId = threadId;
        this.downloadTaskListener = downloadTaskListener;
        if (saveFile.exists()) {
            downLength = saveFile.length();
        } else {
            this.saveFile.createNewFile();
            downLength = 0;
        }
    }

//    /**
//     * 适应初始化block为0情况
//     * @param block
//     */
//    public void setDownBlockSize(long block) {
//        this.block = block;
//    }

    /**
     * 设置下载文件起始结束位置
     *
     * @param startIdx
     * @param endIdx
     */
    public void setDownloadPosistion(long startIdx, long endIdx) {
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.block = endIdx - startIdx;
    }

    public long getDownLength() {
        return downLength;
    }

    public boolean isFinish() {
        return finish;
    }

    /**
     * 暂停/取消
     */
    public void cancel() {
        interrupt = true;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    @Override
    public void run() {
        super.run();

        try {
            HttpURLConnection http = (HttpURLConnection) downUrl
                    .openConnection();
            http.setConnectTimeout(5 * 1000);
            http.setRequestMethod("GET");
            http.setRequestProperty(
                    "Accept",
                    "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash,"
                            + " application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                            + "application/x-ms-application, application/vnd.ms-excel,"
                            + " application/vnd.ms-powerpoint, application/msword, */*");
            http.setRequestProperty("Accept-Language", "zh-CN");
            http.setRequestProperty("Referer", downUrl.toString());
            http.setRequestProperty("Charset", "UTF-8");
            // 该线程开始下载位置
            int startPos = (int) (startIdx + downLength);
            // 该线程下载结束位置
            int endPos = (int) (endIdx);
            Log.i("meng", "threadId=" + threadId + ",startPos=" + startPos + ",endPos=" + endPos);
                /*if (threadId != 1 && downLength == 0) {
                    // 设置获取实体数据的范围
                    http.setRequestProperty("Range", "bytes=" + (startPos + 1) + "-" + endPos);
                } else */
            {
                // 设置获取实体数据的范围
                http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            }
            http.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0;"
                            + " .NET CLR 1.1.4322; .NET CLR 2.0.50727; "
                            + ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            http.setRequestProperty("Connection", "Keep-Alive");
            System.out.println("DownloadThread http.getResponseCode():"
                    + http.getResponseCode());
            String contentType = http.getContentType();
            contentType = StringUtils.nullStrToEmpty(contentType);
            String finalUrl = http.getURL().toString();
            LunaLog.d("CONTENTTYPE: " + contentType + "   final url : " + finalUrl);
            if (contentType.contains("video/mp4") || (contentType.contains("application/octet-stream") && !finalUrl.contains(".m3u8"))) {
                downloadNormal(http);
            } else if (contentType.contains("application/vnd.apple.mpeg") || contentType.contains("application/x-mpeg")
                    || contentType.contains("audio/x-mpegurl") || contentType.contains("audio/mpegurl") || finalUrl.contains(".m3u8") || contentType.contains("text/html")) {
                downloadM3u8(finalUrl);
            }
        } catch (Exception e) {
            this.downLength = -1;
            Log.e("DownloadThread", "DownloadFailed--> Thread id " + this.threadId + ":" + e);
            e.printStackTrace();
            onFailed(threadId, String.valueOf(e));
        }
    }

    /**
     * m3u8专用参数
     */

    private RandomAccessFile m3u8AccessFile;
    private int mUrlCount;
    private int mDownloadedCount;
    private Queue<byte[]> mBuffers = new ConcurrentLinkedQueue<byte[]>();
    private volatile int mWriteIdx = 0;
    private long mDownloadedSize = 0;

    private void downloadM3u8(String mFinalUrl) {
        try {
            m3u8AccessFile = new RandomAccessFile(
                    this.saveFile, "rwd");

            List<String> urls = downloadM3u(mFinalUrl);

            mUrlCount = urls.size();
            LunaLog.d("size:" + urls.size());
            int idx = 0;
            for (final String url : urls) {
                if (interrupt) {
                    onCancel(threadId);
                    return;
                }
                final int fileIdx = idx++;
                String tsUrl;
                try {
                    tsUrl = new URI(mFinalUrl).resolve(url).toString();
//                                    LunaLog.d(+fileIdx + ": " + tsUrl);
                    downloadTsFile(fileIdx, tsUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailed(threadId, "download fail ,ts file field : " + url);
                    return;
                }
            }

            if (!interrupt) {
                onFinish(threadId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailed(threadId, "download ts fail ");
        } finally {
            if (m3u8AccessFile != null) {
                try {
                    m3u8AccessFile.close();
                    m3u8AccessFile = null;
                    mBuffers.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 下载单个ts文件
     *
     * @param index
     * @param url
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws InterruptedException
     */
    private int downloadTsFile(int index, String url) throws IOException, InterruptedException {
        InputStream is = null;
        HttpURLConnection con = null;
        int totalReadSize = 0;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestProperty("Accept-Encoding", "gzip,deflate");
            con.connect();
            is = con.getInputStream();
            long curLength = con.getContentLength();
            curLength = curLength > 0 ? curLength : 0;

            //暂停继续下载
            if (mDownloadedSize + curLength < downLength) {
                mDownloadedSize += curLength;
                mWriteIdx++;
                mDownloadedCount++;
                return totalReadSize;
            }
            byte[] buf = mBuffers.poll();
            if (buf == null) {
                buf = new byte[300 * 1024];
            }

            int pos = 0;
            int readSize;
            m3u8AccessFile.seek(downLength);
            while (!isInterrupted() && (readSize = is.read(buf, 0, buf.length)) >= 0) {
                pos += readSize;
                m3u8AccessFile.write(buf, 0, readSize);
            }

            mDownloadedCount++;

            writets(index, buf, pos);

            int precent = mDownloadedCount * 100 / mUrlCount;
            onProcess(threadId, downLength, 0, precent);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return totalReadSize;
    }

    void writets(int index, byte[] buf, int length) throws InterruptedException, IOException {
        downLength += length;
        mDownloadedSize = downLength;
        while (true) {
            if (mWriteIdx == index) {
                mWriteIdx++;
                mBuffers.add(buf);
                break;
            }
        }
    }

    /**
     * 将M3U8每个地址都获取出来
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    ArrayList<String> downloadM3u(String url) throws IOException, InterruptedException {
        boolean isMutiLevelLink = false;
        BufferedReader br;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            if (url.contains("http://cache.m.iqiyi.com/")) {
                con.setRequestProperty("User-Agent", GlobalConstant.USERAGENT_W);
//                System.setProperty("http.agent", USERAGENT_W);
            }
            br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            String line;

            ArrayList<String> urls = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                if (line.startsWith("#")) {
                    if (line.contains("#EXT-X-STREAM-INF")) {
                        //这是一个二级m3u8索引文件。
                        isMutiLevelLink = true;
                    }
                    continue;
                }
                if (line.length() > 0) {
                    if (!line.startsWith("http")) {
                        //不以http开头，需要拼接
                        int start = url.lastIndexOf("/");
                        if (!line.startsWith("/")) {
                            start += 1;
                        }
                        String m3u8_postfixname = url.substring(
                                start, url.length());
                        line = url.replace(m3u8_postfixname, line);
                        if (isMutiLevelLink) {
                            //如果是一个多级索引
                            return downloadM3u(line);
                        }
                    }
                    urls.add(line);
                }
            }
            return urls;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    private void downloadNormal(HttpURLConnection http) throws Exception {
        if (downLength < block) {// 未下载完成
            int rspCode = http.getResponseCode();
            if (rspCode == 206) {
                RandomAccessFile threadfile = null;
                FileChannel outFileChannel = null;
                InputStream inStream = null;
                try {
                    /***
                     * //获取输入流 InputStream inStream = http.getInputStream();
                     * byte[] buffer = new byte[1024]; int offset = 0;
                     * print("Thread " + this.threadId +
                     * " start download from position " + startPos);
                     *
                     * // rwd: 打开以便读取和写入，对于 "rw"，还要求对文件内容的每个更新都同步写入到基础存储设备。
                     * //对于Android移动设备一定要注意同步，否则当移动设备断电的话会丢失数据 RandomAccessFile
                     * threadfile = new RandomAccessFile( this.saveFile, "rwd");
                     * //直接移动到文件开始位置下载的 threadfile.seek(startPos); while
                     * (!downloader.getExit() && (offset = inStream.read(buffer,
                     * 0, 1024)) != -1) { threadfile.write(buffer, 0,
                     * offset);//开始写入数据到文件 downLength += offset; //该线程以及下载的长度增加
                     * downloader.update(this.threadId,
                     * downLength);//修改数据库中该线程已经下载的数据长度
                     * downloader.append(offset);//文件下载器已经下载的总长度增加 }
                     * threadfile.close();
                     *
                     * print("Thread " + this.threadId + " download finish");
                     * this.finish = true;
                     **/
                    // 获取输入流
                    inStream = http.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(inStream);
                    byte[] buffer = new byte[1024 * 300];
                    int offset = 0;
                    threadfile = new RandomAccessFile(
                            this.saveFile, "rwd");
//                    // 获取RandomAccessFile的FileChannel
                    outFileChannel = threadfile.getChannel();
//                    // 直接移动到文件开始位置下载的
                    outFileChannel.position(downLength);
                    // 分配缓冲区的大小
                    while (!interrupt
                            && (offset = bis.read(buffer)) > 0 - 1) {
                        outFileChannel.write(ByteBuffer.wrap(buffer, 0, offset));// 开始写入数据到文件
//                        threadfile.write(buffer,0,offset);
                        // 该线程以及下载的长度增加
                        downLength += offset;


                        int percent = 0;
                        try {
                            percent = (int) (downLength * 100 / block);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        onProcess(threadId, downLength, block, percent);
                    }
                    if (interrupt) {
                        System.out.println("Thread " + this.threadId + " download cancel");
                        onCancel(threadId);
//                            if(multiTask != null){
//                                multiTask.incrementPauseCount();
//                            }
                    } else {
                        System.out.println("Thread " + this.threadId + " download finish");
                        Log.d("DownloadThread", "Thread " + this.threadId + " download finish");
                        this.finish = true;
                        onFinish(threadId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outFileChannel != null) {
                            outFileChannel.close();
                        }
                        if (threadfile != null) {
                            threadfile.close();
                        }
                        if (inStream != null) {
                            inStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                throw new Exception("{code:" + rspCode + ",msg:下载失败!}");
            }
        } else {
            this.finish = true;
            onFinish(threadId);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public boolean equals(Object o) {
        super.equals(o);
        DownloadThread task = (DownloadThread) o;
        if (downUrl == null || task.downUrl == null) return false;
        return downUrl.getPath().equals(task.downUrl.getPath());
    }

    private long downloadUpdated = 0;

    public void onProcess(int threadId, long contentLength, long completeLength, int percent) {
        long now = System.currentTimeMillis();
        if (now - downloadUpdated > 1000) {
            downloadUpdated = now;
            if (downloadTaskListener != null)
                downloadTaskListener.onProcess(threadId, contentLength, completeLength, percent);
        }
    }

    public void onFinish(int threadId) {
        if (downloadTaskListener != null)
            downloadTaskListener.onFinish(threadId);
    }

    public void onFailed(int threadId, String msg) {
        if (downloadTaskListener != null)
            downloadTaskListener.onFailed(threadId, msg);
    }

    public void onCancel(int threadId) {
        Log.i("meng", "save file size=" + saveFile.length());
        if (downloadTaskListener != null)
            downloadTaskListener.onCancel(threadId);
    }

}
