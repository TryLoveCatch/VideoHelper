package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.luna.net.videohelper.bean.VideoInfo;
import android.luna.net.videohelptools.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import net.luna.common.util.ListUtils;
import net.luna.common.util.StringUtils;

import java.util.ArrayList;


/**
 * Created by bintou on 15/11/12.
 */
public class VideoGridAdapter extends BaseAdapter {
    Context mContext;

    private LayoutInflater inflater;
    private ArrayList<VideoInfo> mVideoInfos;
    ImageLoader imageLoader;
    DisplayImageOptions options;


    public VideoGridAdapter(Context context, ArrayList<VideoInfo> videoInfos) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mVideoInfos = videoInfos;
        imageLoader = ImageLoader.getInstance();

        options = new DisplayImageOptions.Builder().showImageOnLoading(R.mipmap.zdy_img_default_200x280)
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
    }

    public void notifyDataSetChanged(ArrayList<VideoInfo> videoInfos) {
        mVideoInfos = videoInfos;
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return ListUtils.getSize(mVideoInfos);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_video_grid, null);
            viewHolder.cover = (ImageView) convertView.findViewById(R.id.video_cover);
            viewHolder.name = (TextView) convertView.findViewById(R.id.video_name);
            viewHolder.score = (TextView) convertView.findViewById(R.id.video_score);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        VideoInfo videoInfo = mVideoInfos.get(position);
        if (videoInfo != null) {
            imageLoader.displayImage(videoInfo.cover, viewHolder.cover, options);
            viewHolder.cover.setTag(videoInfo.cover);
            if (StringUtils.isBlank(videoInfo.rating) || videoInfo.rating.equals("0")) {
                viewHolder.score.setText("");
            }else {
                viewHolder.score.setText(videoInfo.rating + "分");
            }

            viewHolder.name.setText(videoInfo.name);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView cover;
        TextView score;
        TextView name;
    }
}
