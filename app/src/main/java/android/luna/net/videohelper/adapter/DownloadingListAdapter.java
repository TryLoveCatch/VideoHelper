package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.content.Intent;
import android.luna.net.videohelper.activity.VideoActivity;
import android.luna.net.videohelper.bean.VideoDownloadInfo;
import android.luna.net.videohelper.fragment.DownloadingFragment;
import android.luna.net.videohelper.widget.ResumeAndPauseBtn;
import android.luna.net.videohelptools.R;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.luna.common.util.ListUtils;
import net.luna.common.util.ToastUtils;
import net.luna.common.view.progress.ProgressBarDeterminate;
import net.luna.common.view.widget.Dialog;

import org.kgmeng.dmlib.VideoDownloadManager;
import org.kgmeng.dmlib.status.DownloadStatus;

import java.util.List;


/**
 * Created by bintou on 15/11/5.
 */
public class DownloadingListAdapter extends DownloadListAdapter {

    private Context mContext;

    private List<VideoDownloadInfo> videoFiles;

    ImageLoader imageLoader;
    private boolean isEditStatus = false;

    private DownloadingFragment mFragment;

    public DownloadingListAdapter(Context context, List<VideoDownloadInfo> videoFiles, DownloadingFragment fragment) {
        this.mContext = context;
        this.videoFiles = videoFiles;
        mFragment = fragment;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownloadHolder(LayoutInflater.from(mContext).inflate(R.layout.item_downloading, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DownloadHolder && ListUtils.getSize(videoFiles) > position) {
            VideoDownloadInfo videoFile = videoFiles.get(position);
            if (videoFile != null) {
                ((DownloadHolder) holder).bindData(videoFile);
                ((DownloadHolder) holder).bindEvent(position, videoFile);
            }
        }
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(videoFiles);
    }

    @Override
    public void changeEditStatus() {
        isEditStatus = !isEditStatus;
        if (!isEditStatus) {
            cleanDeleteStatus();
        }
        notifyDataSetChanged();
    }

    public void selectAllDownloaded(boolean isAllSelect) {
        if (ListUtils.getSize(videoFiles) > 0) {
            for (VideoDownloadInfo record : videoFiles) {
                record.needCancel = isAllSelect;
            }
            notifyDataSetChanged();
        }
    }


    public class DownloadHolder extends RecyclerView.ViewHolder {
        public TextView videoName;
        public TextView downloadStatus;
        public TextView downloadPercent;
        public ProgressBarDeterminate progressBar;
        public ResumeAndPauseBtn playBtn;
        public RelativeLayout layout;
        public CheckBox deleteCheckbox;


        public DownloadHolder(View itemView) {
            super(itemView);
            videoName = (TextView) itemView.findViewById(R.id.video_name);
            downloadStatus = (TextView) itemView.findViewById(R.id.download_status);
            downloadPercent = (TextView) itemView.findViewById(R.id.download_percent);
            playBtn = (ResumeAndPauseBtn) itemView.findViewById(R.id.btn_download_start);
            deleteCheckbox = (CheckBox) itemView.findViewById(R.id.checkbox_downloading);
            progressBar = (ProgressBarDeterminate) itemView.findViewById(R.id.download_progress_bar);
//            progressBar.setPressColor(pressColor);
//            progressBar.setBackgroundColor(progressColor);
            layout = (RelativeLayout) itemView.findViewById(R.id.downloading_layout);
        }

        public void bindData(VideoDownloadInfo videoFile) {
            if (videoFile != null) {
                videoName.setText(videoFile.FILENAME);
                progressBar.setProgress(videoFile.percent);
                if (videoFile.curStatus == null) {
                    videoFile.curStatus = DownloadStatus.WAIT;
                }
                switch (videoFile.curStatus) {
                    case WAIT:
                        downloadStatus.setText(mContext.getResources().getString(R.string.downloadWait));
                        downloadPercent.setText(videoFile.percent + "%");
                        break;
                    case DLING:
                        if (!playBtn.isPlaying()) {
                            playBtn.setPlayingStatus();
                        }
                        if (videoFile.percent > 0) {
                            downloadPercent.setText(videoFile.percent + "%");
                            progressBar.setProgress(videoFile.percent);
                        }
                        int contentSize = videoFile.currentMB;
                        int completeSize = videoFile.completeMB;
                        String text = "";
                        if (completeSize != 0) {
                            text = "(" + contentSize + "MB/" + completeSize + "MB)";
                        } else {
                            text = "(" + contentSize + "MB)";
                        }
                        downloadStatus.setText(text);
                        break;
                    case ERROR:
                        downloadStatus.setText(mContext.getResources().getString(R.string.downloadError));
                        break;
                    case PAUSE:
                        downloadStatus.setText(mContext.getResources().getString(R.string.downloadPause) + "(" + videoFile.currentMB + ")");
                        break;
                    case DONE:
                        downloadStatus.setText(mContext.getResources().getString(R.string.downloadComplete));
                        progressBar.setProgress(100);
                        break;
                }
                if (isEditStatus) {
                    deleteCheckbox.setVisibility(View.VISIBLE);
                    playBtn.setVisibility(View.INVISIBLE);
                    if (videoFile.needCancel) {
                        deleteCheckbox.setChecked(true);
                    }
                } else {
                    deleteCheckbox.setVisibility(View.GONE);
                    playBtn.setVisibility(View.VISIBLE);
                }
            }
        }

        public void bindEvent(int position, VideoDownloadInfo videoFile) {
            layout.setOnClickListener(new OnOperationClickListener(position, this));
            playBtn.setOnClickListener(new OnOperationClickListener(position, this));
            deleteCheckbox.setOnCheckedChangeListener(new OnDeleteCheckChangeListener(position));

            layout.setOnLongClickListener(new OnListLongClickListener(position));
        }
    }

    class OnListLongClickListener implements View.OnLongClickListener {

        private int position;

        public OnListLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View v) {
            isEditStatus = true;
            if (ListUtils.getSize(videoFiles) > position) {
                videoFiles.get(position).needCancel = true;
                if (mFragment != null) {
                    mFragment.showMenu();
                }
            }
            notifyDataSetChanged();
            return false;
        }
    }


    private class OnDeleteCheckChangeListener implements CompoundButton.OnCheckedChangeListener {

        private int position;

        public OnDeleteCheckChangeListener(int position) {
            this.position = position;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                if (ListUtils.getSize(videoFiles) > position) {
                    videoFiles.get(position).needCancel = isChecked;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class OnOperationClickListener implements View.OnClickListener {

        private int position;
        private DownloadHolder holder;

        public OnOperationClickListener(int position, DownloadHolder holder) {
            this.position = position;
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            try {
                if (v.getId() == R.id.downloading_layout) {
                    VideoDownloadInfo videoFile = videoFiles.get(position);
                    Intent i = new Intent(mContext, VideoActivity.class);
                    i.putExtra("url", videoFile.path);
                    i.putExtra("name", videoFile.FILENAME);
                    i.putExtra("isFile", true);
                    mContext.startActivity(i);
                } else {
                    if (isEditStatus) {
                        return;
                    }
                    if (videoFiles != null && videoFiles.size() > position) {
                        switch (v.getId()) {
                            case R.id.btn_download_start:
                                if (holder != null && holder.playBtn != null) {
                                    if (holder.playBtn.isPlaying()) {
                                        VideoDownloadManager.getInstance(mContext).onPause(videoFiles.get(position));
                                        holder.playBtn.setPauseStatus();
                                    } else {
                                        VideoDownloadManager.getInstance(mContext).onContinue(videoFiles.get(position));
                                        holder.playBtn.setPlayingStatus();
                                    }
                                }
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateListandUi(List<VideoDownloadInfo> videoFiles) {
        this.videoFiles = videoFiles;
        notifyDataSetChanged();
    }

    public void updateList(List<VideoDownloadInfo> videoFiles) {
        this.videoFiles = videoFiles;
    }

    public void setIsLongClickStatus(boolean isLongClickStatus) {
        this.isEditStatus = isLongClickStatus;
    }

    public void cleanDeleteStatus() {
        try {
            if (ListUtils.getSize(videoFiles) > 0) {
                for (int i = 0; i < videoFiles.size(); i++) {
                    videoFiles.get(i).needCancel = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<VideoDownloadInfo> getVideoFileList() {
        return videoFiles;
    }

    private Dialog mDeleteDialog;

    public boolean showmDeleteDialog() {
        boolean hasneedCancel = false;
        for (VideoDownloadInfo videoDownloadInfo : videoFiles) {
            if (videoDownloadInfo.needCancel == true) {
                hasneedCancel = true;
                break;
            }
        }
        if (hasneedCancel) {

            if (mDeleteDialog == null) {
                mDeleteDialog = new Dialog(mContext, "删除文件", "是否确认删除此文件");
                mDeleteDialog.addCancelButton("取消");
            }
            mDeleteDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ListUtils.getSize(videoFiles) > 0) {
                        deleteDownload();
                        mDeleteDialog.dismiss();
                    }
                }
            });
            mDeleteDialog.show();
            return true;
        } else {
            ToastUtils.show(mContext, "没有需要删除的问题");
            return false;
        }
    }

    public void deleteDownload() {
        try {
            for (int i = videoFiles.size() - 1; i >= 0; i--) {
                VideoDownloadInfo record = videoFiles.get(i);
                if (record.needCancel) {
                    mFragment.cancelTast(record);
                }
            }
            mDeleteDialog.dismiss();
        } catch (Exception e) {
            ToastUtils.show(mContext, "删除失败，请退出重试");
            e.printStackTrace();
        }
    }
}

