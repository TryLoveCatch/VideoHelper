package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.luna.net.videohelptools.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.luna.common.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by bintou on 16/4/26.
 */
public class VideoEpisodeAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private JSONArray episodes;
    private Context mContext;
    private int mCurEpisode;

    public VideoEpisodeAdapter(Context mContext, JSONArray episodes) {
        this.episodes = episodes;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return episodes != null ? episodes.length() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_episode_mediaplay, null);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.number_episode);
            viewHolder.flag = convertView.findViewById(R.id.seletion_flag);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (episodes != null && episodes.length() > position) {
            JSONObject eplsode = JSONUtils.getJsonObject(episodes, position, null);
            if (eplsode != null) {
                String num = eplsode.optString("num");
                viewHolder.tv.setText(num + "集");
            }
            if (viewHolder.flag != null) {
                if (position == mCurEpisode) {
                    viewHolder.flag.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.flag.setVisibility(View.GONE);
                }
            }
        }

        return convertView;
    }


    class ViewHolder {
        TextView tv;
        View flag;
    }

    public int getCurEpisode() {
        return mCurEpisode;
    }

    public void setCurEpisode(int mCurEpisode) {
        this.mCurEpisode = mCurEpisode;

    }
}
