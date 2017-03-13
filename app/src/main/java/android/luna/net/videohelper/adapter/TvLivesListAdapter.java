package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.luna.net.videohelper.activity.VideoActivity;
import android.luna.net.videohelper.bean.TvLiveP;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelptools.R;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.luna.common.util.ListUtils;

import java.util.ArrayList;


/**
 * Created by bintou on 15/11/5.
 */
public class TvLivesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private ArrayList<TvLiveP> mTvLives;

    ImageLoader imageLoader;
    DisplayImageOptions options;
    private String mType;

    public TvLivesListAdapter(Context context, ArrayList<TvLiveP> tvLives, String type) {
        this.mContext = context;
        this.mTvLives = tvLives;
        this.mType = type;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.mipmap.zdy_img_default_210x140)
                .showImageOnLoading(R.mipmap.zdy_img_default_210x140)// default
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_tv_life, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            TvLiveP tvLive = mTvLives.get(position);
            if (tvLive != null) {
                ((VideoViewHolder) holder).bindData(tvLive);
                ((VideoViewHolder) holder).bindEvent(position, tvLive);
            }
        }
    }

    public void updateData(ArrayList<TvLiveP> tvLives) {
        mTvLives = tvLives;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(mTvLives);
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout layout;
        private ImageView cover;
        private TextView name;
        private TextView egps;

        public VideoViewHolder(View itemView) {
            super(itemView);
            layout = (RelativeLayout) itemView.findViewById(R.id.tv_item_layout);
            cover = (ImageView) itemView.findViewById(R.id.tv_icon);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            egps = (TextView) itemView.findViewById(R.id.tv_egps);
        }

        public void bindData(TvLiveP tvLive) {
            if ("PLACE".equals(mType)) {
                cover.setVisibility(View.GONE);
            } else {
                imageLoader.displayImage(tvLive.icon, cover, options);
            }
            name.setText(tvLive.channelName);
            egps.setText(mContext.getResources().getString(R.string.playing_tv) + tvLive.epgs);

        }

        public void bindEvent(int position, TvLiveP tvLive) {
            layout.setOnClickListener(new itemClickListener(tvLive));

        }
    }

    public class itemClickListener implements View.OnClickListener {

        TvLiveP tvLive;

        public itemClickListener(TvLiveP tvLive) {
            this.tvLive = tvLive;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, VideoActivity.class);
            intent.putExtra("name", tvLive.channelName);
            intent.putExtra("site", GlobalConstant.SITE_TV_LIVE);
            intent.putExtra("tvLive", tvLive);
            mContext.startActivity(intent);
            UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_TV_LIVE_NAME, tvLive.channelName);
        }
    }

}

