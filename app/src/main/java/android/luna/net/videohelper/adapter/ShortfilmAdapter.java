package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelper.activity.ShortfilmActivity;
import android.luna.net.videohelper.activity.VideoActivity;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelper.widget.ShareDialog;
import android.luna.net.videohelptools.R;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;

import net.luna.common.util.ListUtils;

import java.util.ArrayList;


/**
 * Created by bintou on 15/11/16.
 */
public class ShortfilmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private ArrayList<VideoDetial> mVideoDetials;
    ImageLoader imageLoader;
    DisplayImageOptions options, options2;
    private boolean inAuthorList = false;

    public ShortfilmAdapter(Context context, ArrayList<VideoDetial> videoDetials) {
        this.mContext = context;
        this.mVideoDetials = videoDetials != null ? videoDetials : new ArrayList<VideoDetial>();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.mipmap.home_img_default)
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        options2 = new DisplayImageOptions.Builder().showImageOnLoading(R.mipmap.shortvideo_img_preson)
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .displayer(new CircleBitmapDisplayer()).build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_shortfilm, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            if (mVideoDetials != null && mVideoDetials.size() > 0) {
                VideoDetial videoDetial = mVideoDetials.get(position);
                if (videoDetial != null) {
                    ((VideoViewHolder) holder).bindData(videoDetial);
                    ((VideoViewHolder) holder).bindEvent(position, videoDetial);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(mVideoDetials);
    }

    public void updateResult(ArrayList<VideoDetial> videoDetials) {
        mVideoDetials = videoDetials;
        notifyDataSetChanged();
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder {
        private View contentView;
        private ImageView cover;
        private TextView videoName;
        private TextView author;
        private ImageButton authorImg;
        private ImageButton playBtn;
        private ImageButton shareBtn;

        public VideoViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            cover = (ImageView) itemView.findViewById(R.id.cover);
            videoName = (TextView) itemView.findViewById(R.id.shortfilm_title);
            author = (TextView) itemView.findViewById(R.id.author_name);
            authorImg = (ImageButton) itemView.findViewById(R.id.author_img);
            playBtn = (ImageButton) itemView.findViewById(R.id.btn_play);
            shareBtn = (ImageButton) itemView.findViewById(R.id.btn_share);
        }

        public void bindData(VideoDetial videoDetial) {
            if (videoDetial != null) {
                imageLoader.displayImage(videoDetial.cover, cover, options);
                imageLoader.displayImage(videoDetial.icon, authorImg, options2);
                videoName.setText(videoDetial.name);
                author.setText(videoDetial.directors);
            }
        }

        public void bindEvent(int position, VideoDetial videoDetial) {
            playBtn.setOnClickListener(new OnShortFilmClickListener(videoDetial));
            shareBtn.setOnClickListener(new OnShortFilmClickListener(videoDetial));
            authorImg.setOnClickListener(new OnShortFilmClickListener(videoDetial));
        }
    }


    private class OnShortFilmClickListener implements View.OnClickListener {
        VideoDetial videoDetial;

        public OnShortFilmClickListener(VideoDetial videoDetial) {
            this.videoDetial = videoDetial;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_play) {
                String site = BrowserUnit.checkSite(videoDetial.videoUrl);
                Intent intent = new Intent(mContext, VideoActivity.class);
                intent.putExtra("site", site);
                intent.putExtra("url", videoDetial.videoUrl);
                intent.putExtra("name", videoDetial.name);
                UploadEventRecord.recordEventinternal(mContext, GlobalConstant.P_VIDEO_NAME, videoDetial.name);
                mContext.startActivity(intent);

            } else if (v.getId() == R.id.author_img) {
                if (!inAuthorList) {
                    Intent intent = new Intent(mContext, ShortfilmActivity.class);
                    intent.putExtra("authorName", videoDetial.directors);
                    mContext.startActivity(intent);
                }
            } else {
                ShareDialog shareDialog = new ShareDialog(mContext);
                shareDialog.setUrl(videoDetial.videoUrl);
                shareDialog.showShareDialog(videoDetial.name + "- 微趣");
            }
        }
    }


    public void setInAuthorList(boolean inAuthorList) {
        this.inAuthorList = inAuthorList;
    }
}
