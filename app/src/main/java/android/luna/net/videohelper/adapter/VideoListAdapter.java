package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelptools.R;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.luna.common.util.ListUtils;

import java.util.ArrayList;


/**
 * Created by bintou on 15/11/5.
 */
public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private ArrayList<VideoDetial> mVideoDetials;

    ImageLoader imageLoader;

    public VideoListAdapter(Context context, ArrayList<VideoDetial> videoDetials) {
        this.mContext = context;
        this.mVideoDetials = videoDetials;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_video_type, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            VideoDetial videoDetial = mVideoDetials.get(position);
            if (videoDetial != null) {
                ((VideoViewHolder) holder).bindData(videoDetial);
                ((VideoViewHolder) holder).bindEvent(position, videoDetial);
            }
        }
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(mVideoDetials);
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder {
        private ImageView cover;
        private TextView videoName;
        private TextView score;

        public VideoViewHolder(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.video_cover);
            videoName = (TextView) itemView.findViewById(R.id.video_name);
            score = (TextView) itemView.findViewById(R.id.video_score);
        }

        public void bindData(VideoDetial videoDetial) {
            imageLoader.displayImage(videoDetial.cover, cover);
            videoName.setText(videoDetial.name);
            score.setText(videoDetial.rating);
        }

        public void bindEvent(int position, VideoDetial videoDetial) {

        }

    }

}

