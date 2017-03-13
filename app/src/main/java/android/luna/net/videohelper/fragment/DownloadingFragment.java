package android.luna.net.videohelper.fragment;

import android.annotation.SuppressLint;
import android.luna.net.videohelper.adapter.DownloadingListAdapter;
import android.luna.net.videohelper.bean.VideoDownloadInfo;
import android.luna.net.videohelper.widget.DividerItemDecoration;
import android.luna.net.videohelper.widget.DownloadMenu;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.luna.common.debug.LunaLog;

import org.kgmeng.dmlib.IDownloadStateListener;
import org.kgmeng.dmlib.VideoDownloadManager;
import org.kgmeng.dmlib.model.FileInfo;
import org.kgmeng.dmlib.status.DownloadStatus;

import java.io.IOException;
import java.util.List;

/**
 * Created by bintou on 16/2/18.
 */

@SuppressLint("ValidFragment")
public class DownloadingFragment extends Fragment {

    public final int MSG_UPDATE_VIEW = 76;

    final long MB = 1024 * 1024;
    private DownloadingListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View mContainerView;
    private VideoDownloadManager videosDownloadManager;
    private List<VideoDownloadInfo> videoDownloadInfos;
    private RecyclerView recyclerView;
    private Handler downloadHandler;

    public DownloadingFragment() {
        super();
    }


    public DownloadingFragment(Handler handler) {
        super();
        downloadHandler = handler;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainerView = inflater.inflate(R.layout.fragment_downloading, container, false);
        recyclerView = (RecyclerView) mContainerView.findViewById(R.id.recycleview_downloading_list);

        mAdapter = new DownloadingListAdapter(getActivity(), videoDownloadInfos, this);
        recyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        initData();
        return mContainerView;
    }

    private void initData() {
        try {
            videosDownloadManager = VideoDownloadManager.getInstance(getActivity());
            videosDownloadManager.registerStateListener(fileDownloadListener);
            videoDownloadInfos = videosDownloadManager.getDownloadList();
            mAdapter.updateListandUi(videoDownloadInfos);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    IDownloadStateListener fileDownloadListener = new IDownloadStateListener() {


        @Override
        public void onPrepare(FileInfo entity, long size) {
            int index = indexOf((VideoDownloadInfo) entity);
            if (index >= 0) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                msg.what = MSG_UPDATE_VIEW;
                msg.arg1 = index;
                msg.obj = DownloadStatus.WAIT;
                bundle.putSerializable("entity", entity);
                bundle.putLong("size", size);
                msg.setData(bundle);
                handler.sendMessage(msg);
//                updateView(index, DownloadStatus.WAIT);
                LunaLog.d("WAIT");
            }

        }

        @Override
        public void onProcess(FileInfo entity, long contentLength, long completeLength, int percent) {
            int index = indexOf((VideoDownloadInfo) entity);
            if (index >= 0) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                msg.what = MSG_UPDATE_VIEW;
                msg.arg1 = index;
                msg.obj = DownloadStatus.DLING;
                bundle.putSerializable("entity", entity);
                bundle.putLong("contentLength", contentLength);
                bundle.putLong("completeLength", completeLength);
                bundle.putInt("percent", percent);
                msg.setData(bundle);
                handler.sendMessage(msg);
//                updateView(index, DownloadStatus.DLING, contentLength, completeLength, percent);
            }
        }

        @Override
        public void onFinish(FileInfo entity, String savePath) {
            int index = indexOf((VideoDownloadInfo) entity);
            if (index >= 0) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                msg.what = MSG_UPDATE_VIEW;
                msg.arg1 = index;
                msg.obj = DownloadStatus.DONE;
                bundle.putSerializable("entity", entity);
                bundle.putString("savePath", savePath);
                msg.setData(bundle);
                handler.sendMessage(msg);
//                updateView(index, DownloadStatus.DONE);
                LunaLog.d("FINISH");
            }
        }

        @Override
        public void onFailed(FileInfo entity, String msg) {
            int index = indexOf((VideoDownloadInfo) entity);
            LunaLog.d("FAIL");
            if (index >= 0) {
//                updateView(index, DownloadStatus.ERROR);
            }
        }

        @Override
        public void onPause(FileInfo entity, long size) {
            int index = indexOf((VideoDownloadInfo) entity);
            if (index >= 0) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                msg.what = MSG_UPDATE_VIEW;
                msg.arg1 = index;
                msg.obj = DownloadStatus.PAUSE;
                bundle.putSerializable("entity", entity);
                bundle.putLong("size", size);
                msg.setData(bundle);
                handler.sendMessage(msg);
                LunaLog.d("PAUSE");
            }
        }

        @Override
        public void onCancel(FileInfo entity) {
            LunaLog.d("CANCEL");
        }
    };

    public void destroy() {
        try {
            videosDownloadManager.unRegisterStateListener(fileDownloadListener);
        } catch (Exception e) {

        }
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private int indexOf(VideoDownloadInfo videoDownloadInfo) {
        int hashCode = videoDownloadInfo.HASHVALUE;
        for (VideoDownloadInfo info : videoDownloadInfos) {
            if (info.HASHVALUE == hashCode) {
                return videoDownloadInfos.indexOf(info);
            }
        }
        videoDownloadInfos.add(videoDownloadInfo);
        mAdapter.updateListandUi(videoDownloadInfos);
        return videoDownloadInfos.indexOf(videoDownloadInfo);
    }


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == MSG_UPDATE_VIEW) {
                try {
                    if (recyclerView != null) {
                        int index = msg.arg1;
                        DownloadStatus status = (DownloadStatus) msg.obj;
                        Bundle bundle = msg.getData();
                        if (index >= 0 && videoDownloadInfos.size() > index) {
                            VideoDownloadInfo videoDownloadInfo = videoDownloadInfos.get(index);
                            videoDownloadInfo.curStatus = status;
                            View view = recyclerView.getChildAt(index);
                            DownloadingListAdapter.DownloadHolder vh = (DownloadingListAdapter.DownloadHolder) recyclerView.getChildViewHolder(view);
                            switch (status) {
                                case WAIT:
                                    videoDownloadInfo.currentMB = (int) (bundle.getLong("size") / MB);
                                    vh.downloadStatus.setText(getActivity().getResources().getString(R.string.downloadWait) + "(" + videoDownloadInfo.currentMB + "MB)");
                                    break;
                                case DLING:
                                    int percent = bundle.getInt("percent");
                                    long contentLength = bundle.getLong("contentLength");
                                    long completeLength = bundle.getLong("completeLength");
                                    vh.downloadPercent.setText(percent + "%");
                                    vh.progressBar.setProgress(percent);
                                    int contentSize = (int) (contentLength / MB);
                                    videoDownloadInfo.currentMB = contentSize;
                                    String text = "";
                                    if (completeLength != 0) {
                                        int completeSize = (int) (completeLength / MB);
                                        videoDownloadInfo.completeMB = completeSize;
                                        text = "(" + contentSize + "MB/" + completeSize + "MB)";
                                    } else {
                                        text = "(" + contentSize + "MB)";
                                    }
                                    videoDownloadInfo.percent = bundle.getInt("percent");
                                    vh.downloadStatus.setText(text);
                                    vh.playBtn.setPlayingStatus();
                                    break;
                                case ERROR:
                                    vh.downloadStatus.setText(getActivity().getResources().getString(R.string.downloadError));
                                    vh.playBtn.setPauseStatus();
                                    break;
                                case PAUSE:
                                    videoDownloadInfo.currentMB = (int) (bundle.getLong("size") / MB);
                                    vh.downloadStatus.setText(getActivity().getResources().getString(R.string.downloadPause) + "(" + videoDownloadInfo.currentMB + "MB)");
                                    vh.playBtn.setPauseStatus();
                                    break;
                                case DONE:
                                    vh.downloadStatus.setText(getActivity().getResources().getString(R.string.downloadComplete));
                                    vh.progressBar.setProgress(100);
                                    vh.downloadStatus.setVisibility(View.GONE);
                                    if (downloadHandler != null) {
                                        downloadHandler.sendEmptyMessage(1001);
                                    }
                                    videoDownloadInfos.remove(videoDownloadInfo);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };


    private DownloadMenu mDownloadMenu;

    public void showMenu() {
        if (mAdapter.getItemCount() > 0) {

            if (mDownloadMenu == null) {
                mDownloadMenu = new DownloadMenu(getActivity(), mAdapter);
            }
            if (!mDownloadMenu.isShowing()) {
                mAdapter.changeEditStatus();
                mDownloadMenu.show();
            } else {
                mDownloadMenu.dismiss();
            }
        }
    }

    public void selectAll() {
        if (mDownloadMenu.isHasSelectAll()) {
            mAdapter.selectAllDownloaded(false);
        } else {
            mAdapter.selectAllDownloaded(true);
        }
        mDownloadMenu.changeSelectedStatus();
    }

    public void delete() {
        if (mAdapter.showmDeleteDialog()) {
            mDownloadMenu.dismiss();
        }
    }

    public void dismissMenu() {
        if (mDownloadMenu != null && mDownloadMenu.isShowing()) {
            mDownloadMenu.dismiss();
        }
    }

    public void cancelTast(VideoDownloadInfo videoDownloadInfo) {
        videosDownloadManager.onCanceled(videoDownloadInfo);
        videoDownloadInfos.remove(videoDownloadInfo);
        mAdapter.updateList(videoDownloadInfos);
    }
}
