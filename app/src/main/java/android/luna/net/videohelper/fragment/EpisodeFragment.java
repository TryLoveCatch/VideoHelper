package android.luna.net.videohelper.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.luna.net.videohelper.activity.VideoActivity;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelptools.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.CompatUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by bintou on 15/11/5.
 */
@SuppressLint("ValidFragment")
public class EpisodeFragment extends Fragment implements AdapterView.OnItemClickListener {

    private VideoDetial mVideoDetial;
    private String curSite;
    private String siteName;
    private int lastVistPositon;

    private JSONArray episodes;
    private EpisodeAdapter episodeViewPager;


    public EpisodeFragment(VideoDetial videoDetial, String site, String siteName) {
        super();
        mVideoDetial = videoDetial;
        curSite = site;
        this.siteName = siteName;
        changeCurSource();
    }

    public EpisodeFragment() {
        super();
    }

    public void changeSite(String site, String siteName) {
        curSite = site;
        this.siteName = siteName;
        changeCurSource();
        if (episodeViewPager != null) {
            episodeViewPager.notifyDataSetChanged();
        }
    }

    private void changeCurSource() {
        if (mVideoDetial != null && mVideoDetial.episodes != null) {
            JSONObject episodesJo = JSONUtils.toJsonObject(mVideoDetial.episodes);
            if (episodesJo != null) {
                episodes = episodesJo.optJSONArray(curSite);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_episode, container, false);
        GridView gridView = (GridView) v.findViewById(R.id.gridview_episode);
        gridView.setOnItemClickListener(this);
        gridView.setDescendantFocusability(GridView.FOCUS_BLOCK_DESCENDANTS);
        episodeViewPager = new EpisodeAdapter();
        gridView.setAdapter(episodeViewPager);
        gridView.setSelector(CompatUtils.getDrawable(getActivity(), R.drawable.grid_selector));
        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            JSONObject eplsode = JSONUtils.getJsonObject(episodes, position, null);
            if (eplsode != null) {
                String url = eplsode.optString("url");
                String num = eplsode.optString("num");
                LunaLog.d(eplsode.toString());
                boolean dog = eplsode.optBoolean("dog", false);
                Intent intent = new Intent(getActivity(), VideoActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("name", mVideoDetial.name + " 第" + num + "集");
                intent.putExtra("position", position);
                if (episodes != null) {
                    intent.putExtra("episodes", episodes.toString());
                }
                LunaLog.d("site: " + curSite);
                intent.putExtra("site", curSite);
                if (dog) {
                    intent.putExtra("isFile", dog);
                }
                try {
                    intent.putExtra("sitename", siteName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PreferencesUtils.putInt(getContext(), mVideoDetial.vid, position);
                startActivityForResult(intent, GlobalConstant.RESULT_VISIT_BEGIN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getLastVistPositon() {
        return lastVistPositon;
    }

    @Override
    public void onResume() {
        try {
            if (mVideoDetial != null) {
                lastVistPositon = PreferencesUtils.getInt(getContext(), mVideoDetial.vid);
            }
            if (episodeViewPager != null) {
                episodeViewPager.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    class EpisodeAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public EpisodeAdapter() {
            inflater = LayoutInflater.from(getContext());
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
                convertView = inflater.inflate(R.layout.item_episode, null);
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
                    if (position == lastVistPositon) {
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
    }

}
