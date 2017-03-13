package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.luna.net.videohelper.bean.HotWord;
import android.luna.net.videohelper.connection.VideoCatchManager;
import android.luna.net.videohelptools.R;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


/**
 * Created by bintou on 15/10/30.
 */
public class HotVideoGridAdapter extends BaseAdapter {

    private Context mContext;
    ImageLoader imageLoader;
    DisplayImageOptions options;

    private LayoutInflater inflater;
    private ArrayList<HotWord> mHotVideoList;

    private VideoCatchManager mVideoCatchManager;
    private boolean mLoading = false;
    private boolean mIsZh = false;
    private Handler mHandler;

    public HotVideoGridAdapter(Context context, ArrayList<HotWord> hotWords, Handler handler, boolean isZh) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mHotVideoList = hotWords;
        mHandler = handler;
        mVideoCatchManager = VideoCatchManager.getInstanct(context);
        imageLoader = ImageLoader.getInstance();
        mIsZh = isZh;
        options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.mipmap.home_img_default)
                .showImageOnLoading(R.mipmap.home_img_default)// default
                .cacheInMemory(true)
                .resetViewBeforeLoading(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
    }

    public void updateList(ArrayList<HotWord> hotWords) {
        if (hotWords != null) {
            mHotVideoList = hotWords;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mHotVideoList.size();
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
            convertView = inflater.inflate(R.layout.item_hotword, null);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.hotword_image);
            viewHolder.name = (TextView) convertView.findViewById(R.id.hotword_title);
            viewHolder.content = (TextView) convertView.findViewById(R.id.hotword_content);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HotWord hotWord = mHotVideoList.get(position);
        if (hotWord != null) {
            imageLoader.displayImage(hotWord.getImg(), viewHolder.img, options);
            viewHolder.img.setTag(hotWord.getImg());
            if (mIsZh) {
                viewHolder.name.setText(hotWord.getWord_zh());
                viewHolder.content.setText(hotWord.getContent_zh());
            } else {
                viewHolder.name.setText(hotWord.getWord());
                viewHolder.content.setText(hotWord.getContent());
            }
        }
        return convertView;
    }


    class ViewHolder {
        ImageView img;
        TextView name;
        TextView content;
    }

}

