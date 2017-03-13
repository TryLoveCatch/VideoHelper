package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.luna.net.videohelper.activity.VideoIntroduceActivity;
import android.luna.net.videohelper.bean.CloudVideo;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelptools.R;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import net.luna.common.util.ListUtils;
import net.luna.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by bintou on 15/11/16.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private ArrayList<VideoDetial> mVideoDetials;
    private List<CloudVideo> mCloudVideos;
    ImageLoader imageLoader;
    DisplayImageOptions options;

    public SearchResultAdapter(Context context, ArrayList<VideoDetial> videoDetials) {
        this.mContext = context;
        this.mVideoDetials = videoDetials != null ? videoDetials : new ArrayList<VideoDetial>();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.mipmap.search_img_default_210x280)
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            if (mVideoDetials != null && mVideoDetials.size() > 0) {
                VideoDetial videoDetial = mVideoDetials.get(position);
                CloudVideo cloudVideo = null;
                if (ListUtils.getSize(mCloudVideos) > position) {
                    cloudVideo = mCloudVideos.get(position);
                }
                if (videoDetial != null) {
                    ((VideoViewHolder) holder).bindData(videoDetial);
                    ((VideoViewHolder) holder).bindEvent(position, videoDetial, cloudVideo);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(mVideoDetials);
    }

    public void updateSearchResult(ArrayList<VideoDetial> videoDetials) {
        mVideoDetials = videoDetials;
        notifyDataSetChanged();
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder {
        private View contentView;
        private ImageView cover;
        private TextView videoName;
        private TextView actors;
        private TextView geners;
        private TextView area;

        public VideoViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            cover = (ImageView) itemView.findViewById(R.id.video_thumbnail);
            videoName = (TextView) itemView.findViewById(R.id.name);
            actors = (TextView) itemView.findViewById(R.id.cast);
            geners = (TextView) itemView.findViewById(R.id.genres);
            area = (TextView) itemView.findViewById(R.id.area);
        }

        public void bindData(VideoDetial videoDetial) {
            if (videoDetial != null) {
                imageLoader.displayImage(videoDetial.cover, cover, options);
                videoName.setText(videoDetial.name);
                videoDetial.actors = StringUtils.nullStrToEmpty(videoDetial.actors);
                actors.setText(mContext.getResources().getString(R.string.actor_open) + videoDetial.actors);
                geners.setText(mContext.getResources().getString(R.string.type_open) + videoDetial.genres);
                if (!StringUtils.isBlank(videoDetial.area)) {
                    area.setText(mContext.getResources().getString(R.string.area_open) + videoDetial.area);
                } else if (!StringUtils.isBlank(videoDetial.year)) {
                    area.setText(mContext.getResources().getString(R.string.year_open) + videoDetial.year);
                }
            }
        }

        public void bindEvent(int position, VideoDetial videoDetial, CloudVideo cloudVideo) {
            if (cloudVideo != null) {
                contentView.setOnClickListener(new OnSearchItemClickListener(videoDetial.vid, cloudVideo));
            } else {
                contentView.setOnClickListener(new OnSearchItemClickListener(videoDetial.vid));
            }
        }
    }


    private class OnSearchItemClickListener implements View.OnClickListener {
        String vid;
        CloudVideo cloudVideo;

        public OnSearchItemClickListener(String vid) {
            this.vid = vid;
        }

        public OnSearchItemClickListener(String vid, CloudVideo cloudVideo) {
            this.vid = vid;
            this.cloudVideo = cloudVideo;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, VideoIntroduceActivity.class);
            intent.putExtra("vid", vid);
            if (cloudVideo != null) {
                intent.putExtra("cloudVideo", cloudVideo);
            }
            mContext.startActivity(intent);
        }
    }

    public void setmCloudVideos(List<CloudVideo> mCloudVideos) {
        this.mCloudVideos = mCloudVideos;
    }
}
