package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.content.Intent;
import android.luna.net.videohelper.activity.VideoActivity;
import android.luna.net.videohelper.bean.VideoDownloadInfo;
import android.luna.net.videohelper.fragment.DownloadedFragment;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelptools.R;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.luna.common.util.FileUtils;
import net.luna.common.util.ListUtils;
import net.luna.common.util.ToastUtils;
import net.luna.common.view.widget.Dialog;

import java.util.List;


/**
 * Created by bintou on 15/11/5.
 */
public class DownloadedListAdapter extends DownloadListAdapter {

    private Context mContext;

    private List<VideoDownloadInfo> videoFiles;

    private ImageLoader imageLoader;
    private Handler handler;
    private boolean isEditStatus = false;
    private DownloadedFragment mFragment;

    public DownloadedListAdapter(Context context, List<VideoDownloadInfo> videoFiles, Handler handler, DownloadedFragment fragment) {
        this.mContext = context;
        this.videoFiles = videoFiles;
        this.handler = handler;
        isEditStatus = false;
        mFragment = fragment;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownloadHolder(LayoutInflater.from(mContext).inflate(R.layout.item_downloaded, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (videoFiles != null)
            if (holder instanceof DownloadHolder) {
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


    private class DownloadHolder extends RecyclerView.ViewHolder {
        private TextView webName;
        private TextView downloadInfo;
        private CheckBox checkBox;
        private RelativeLayout layout;

        public DownloadHolder(View itemView) {
            super(itemView);
            webName = (TextView) itemView.findViewById(R.id.video_name);
            downloadInfo = (TextView) itemView.findViewById(R.id.download_info);
            layout = (RelativeLayout) itemView.findViewById(R.id.download_layout);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_downloaded);
        }

        public void bindData(VideoDownloadInfo videoFile) {
            if (videoFile != null) {
                webName.setText(videoFile.FILENAME);
                downloadInfo.setText(videoFile.FILESIZE + "M");

                if (isEditStatus) {
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setChecked(videoFile.needDelete);
                } else {
                    checkBox.setVisibility(View.GONE);
                }
            }
        }

        public void bindEvent(int position, VideoDownloadInfo videoFile) {
            checkBox.setOnCheckedChangeListener(new DownloadedOncheckedChangeListener(position));
            layout.setOnClickListener(new OnOperationClickListener(position));
            layout.setOnLongClickListener(new DonwloadedOnLongClickListener(position));
        }

    }

    private class DonwloadedOnLongClickListener implements View.OnLongClickListener {

        private int position;

        public DonwloadedOnLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View v) {
            if (!isEditStatus) {
                isEditStatus = true;
                if (mFragment != null) {
                    mFragment.showMenu();
                }
                if (ListUtils.getSize(videoFiles) > position) {
                    videoFiles.get(position).needDelete = true;
                    notifyDataSetChanged();
                }
            }
            return false;
        }
    }


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
                record.needDelete = isAllSelect;
            }
            notifyDataSetChanged();
        }
    }


    class DownloadedOncheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        private int mPosition;

        public DownloadedOncheckedChangeListener(int mPosition) {
            this.mPosition = mPosition;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mPosition >= 0 && ListUtils.getSize(videoFiles) > mPosition) {
                videoFiles.get(mPosition).needDelete = isChecked;
            }
        }
    }

    private class OnOperationClickListener implements View.OnClickListener {

        private int position;

        public OnOperationClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            try {
                if (videoFiles != null && videoFiles.size() > position) {
                    if (!isEditStatus) {
                        VideoDownloadInfo videoFile = videoFiles.get(position);
                        Intent i = new Intent(mContext, VideoActivity.class);
                        i.putExtra("url", videoFile.path);
                        i.putExtra("name", videoFile.FILENAME);
                        i.putExtra("isFile", true);
                        mContext.startActivity(i);
                        UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "离线");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateList(List<VideoDownloadInfo> videoFiles) {
        this.videoFiles = videoFiles;
        notifyDataSetChanged();
        if (handler != null) {
            if (ListUtils.getSize(videoFiles) > 0) {
                handler.sendEmptyMessage(2);
            } else {
                handler.sendEmptyMessage(1);
            }
        }
    }

    private Dialog mDeleteDialog;

    public boolean showmDeleteDialog() {
        boolean hasNeedDelete = false;
        for (VideoDownloadInfo videoDownloadInfo : videoFiles) {
            if (videoDownloadInfo.needDelete == true) {
                hasNeedDelete = true;
                break;
            }
        }
        if (hasNeedDelete) {

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
                if (record.needDelete) {
                    String path = videoFiles.get(i).path;
                    String name = videoFiles.get(i).FILENAME;
                    videoFiles.remove(i);
                    FileUtils.deleteFile(path);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            ToastUtils.show(mContext, "删除失败，请退出重试");
            e.printStackTrace();
        }
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

}

