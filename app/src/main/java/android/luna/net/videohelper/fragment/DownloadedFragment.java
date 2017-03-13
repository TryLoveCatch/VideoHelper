package android.luna.net.videohelper.fragment;

import android.annotation.SuppressLint;
import android.luna.net.videohelper.adapter.DownloadedListAdapter;
import android.luna.net.videohelper.bean.VideoDownloadInfo;
import android.luna.net.videohelper.widget.DividerItemDecoration;
import android.luna.net.videohelper.widget.DownloadMenu;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.luna.common.util.ThreadUtils;

import org.kgmeng.dmlib.config.DownloadConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bintou on 16/2/18.
 */
@SuppressLint("ValidFragment")
public class DownloadedFragment extends Fragment {

    private DownloadedListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View mContainerView;
    private View mTipsView;

    public DownloadedFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainerView = inflater.inflate(R.layout.fragment_downloaded, container, false);
        mTipsView = mContainerView.findViewById(R.id.download_no_file_layout);
        mTipsView.setVisibility(View.GONE);
        RecyclerView recyclerView = (RecyclerView) mContainerView.findViewById(R.id.recycleview_download_list);
        mAdapter = new DownloadedListAdapter(getActivity(), null, updateHandler, this);
        recyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        updateHandler.post(getDataRunnable);
        return mContainerView;
    }

    private Handler updateHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 1:
                    if (mTipsView != null && mTipsView.getVisibility() != View.VISIBLE) {
                        mTipsView.setVisibility(View.VISIBLE);
                    }
                    break;
                case 2:
                    if (mTipsView != null && mTipsView.getVisibility() != View.GONE) {
                        mTipsView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };


    public void updateList() {
        updateHandler.post(getDataRunnable);
    }

    Runnable getDataRunnable = new Runnable() {
        @Override
        public void run() {
            final List<VideoDownloadInfo> videoFiles = new ArrayList<>();
            try {
                File dir = new File(DownloadConstants.FILE_BASE_PATH + File.separator);
                dir.mkdirs();
                if (dir.exists() && dir.isDirectory()) {
                    for (File file : dir.listFiles()) {
                        if (file.isFile() && (file.getName().endsWith(".mts") || file.getName().endsWith(".mp4"))) {
                            VideoDownloadInfo videoFile = new VideoDownloadInfo("", "");
                            videoFile.FILENAME = file.getName();
                            videoFile.path = file.getPath();
                            videoFile.FILESIZE = (int) (file.length() / 1024 / 1024);
                            videoFiles.add(videoFile);
                        }
                    }
                }
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateList(videoFiles);
                        if (mContainerView != null) {
                            mContainerView.findViewById(R.id.progressbar).setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void destroy() {
    }

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


}
