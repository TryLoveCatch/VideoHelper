package android.luna.net.videohelper.activity;

import android.content.Intent;
import android.luna.net.videohelper.bean.VideoDetial;
import android.luna.net.videohelper.download.VideosDownloadService;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelper.video.parser.UrlParseHelper;
import android.luna.net.videohelper.widget.DefinitionDialog;
import android.luna.net.videohelptools.R;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.dtr.zxing.activity.CaptureActivity;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.CompatUtils;
import net.luna.common.util.JSONUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ThreadUtils;
import net.luna.common.util.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by bintou on 15/11/5.
 */
public class EpisodeActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {


    VideoDetial mVideoDetial;
    String curSite;
    int action;

    JSONArray eplsodes;
    EpisodeViewPager episodeViewPager;

    int textColorSelected, textColorUnselected;

    public void changeSite(String site) {
        curSite = site;
        changeCurSource();
        episodeViewPager.notifyDataSetChanged();
    }

    private void changeCurSource() {
        if (mVideoDetial != null && mVideoDetial.episodes != null) {
            JSONObject episodesJo = JSONUtils.toJsonObject(mVideoDetial.episodes);
            if (episodesJo != null) {
                eplsodes = episodesJo.optJSONArray(curSite);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        textColorSelected = CompatUtils.getColor(mContext, R.color.text_white);
        textColorUnselected = CompatUtils.getColor(mContext, R.color.text_black_secondary);

        mVideoDetial = (VideoDetial) getIntent().getSerializableExtra("videoDetial");
        curSite = getIntent().getStringExtra("site");
        action = getIntent().getIntExtra("action", GlobalConstant.ACTION_DOWNLOAD);

        TextView title = (TextView) findViewById(R.id.title);
        if (action == GlobalConstant.ACTION_DOWNLOAD) {
            title.setText(getResources().getString(R.string.episode_selete));
            findViewById(R.id.btn_download_page).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_begin_download).setVisibility(View.VISIBLE);
        } else if (action == GlobalConstant.ACTION_CAPTURE) {
            title.setText(getResources().getString(R.string.episode_sycn_pc));
        }

        GridView gridView = (GridView) findViewById(R.id.gridview_episode);
        gridView.setOnItemClickListener(this);
        gridView.setDescendantFocusability(GridView.FOCUS_BLOCK_DESCENDANTS);
        changeCurSource();
        episodeViewPager = new EpisodeViewPager();
        gridView.setAdapter(episodeViewPager);

        gridView.setSelector(CompatUtils.getDrawable(mContext, R.drawable.grid_selector));
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            JSONObject eplsode = JSONUtils.getJsonObject(eplsodes, position, null);
            if (eplsode != null) {
                String url = eplsode.optString("url");
                String num = eplsode.optString("num");
                //判断是否是一个现成的视频地址
                boolean dog = eplsode.optBoolean("dog", false);
                String name = mVideoDetial.name + " 第" + num + "集";
                Intent intent;
                switch (action) {
                    case GlobalConstant.ACTION_PLAY_VIDEO:
                        intent = new Intent(this, VideoActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("name", name);
                        intent.putExtra("site", curSite);
                        if (dog) {
                            intent.putExtra("isFile", dog);
                        }
                        startActivity(intent);
                        break;
                    case GlobalConstant.ACTION_CAPTURE:
                        intent = new Intent(mContext, CaptureActivity.class);
                        intent.putExtra("webTitle", mVideoDetial.name);
                        if (dog) {
                            intent.putExtra("link", url);
                        } else {
                            intent.putExtra("webUrl", url);
                        }
                        intent.putExtra("site", curSite);

                        intent.putExtra("vid", mVideoDetial.vid);
                        startActivity(intent);
                        UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "自有视频扫码");
                        break;
                    case GlobalConstant.ACTION_DOWNLOAD:
                        boolean selectionFlag = eplsode.optBoolean("downloadFlag");
                        if (selectionFlag) {
                            eplsode.putOpt("downloadFlag", false);
                        } else {
                            eplsode.putOpt("downloadFlag", true);
                        }
                        eplsodes.put(position, eplsode);
                        episodeViewPager.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloads(int def) {
//        //正则表达式暂时不全，先用字符串判断
        int count = 0;
        try {
            for (int i = 0; i < eplsodes.length(); i++) {
                JSONObject eplsode = JSONUtils.getJsonObject(eplsodes, i, null);
                if (eplsode != null) {
                    if (eplsode.optBoolean("downloadFlag")) {
                        String url = eplsode.optString("url");
                        String num = eplsode.optString("num");
                        //判断是否是一个现成的视频地址
                        boolean dog = eplsode.optBoolean("dog", false);
                        String name = mVideoDetial.name + " 第" + num + "集";
                        Message msg = new Message();
                        if (dog) {
                            msg.what = GlobalConstant.VIDEO_URL_RECEIVE;
                            Bundle bundle = new Bundle();
                            bundle.putString("name", name);
                            msg.setData(bundle);
                            msg.obj = url;
                            downloadHandler.sendMessage(msg);
                        } else {
                            UrlParseHelper helper = new UrlParseHelper(mContext, url, curSite, mVideoDetial.vid, name, def, downloadHandler, true);
                            ThreadUtils.execute(helper);
                        }
                        count++;
                    }
                }
            }
            if (count > 0) {
                ToastUtils.show(mContext, "正在为您批量缓存");
                UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_ENTER, "自有视频下载");
                clearAllTag();
            } else {
                ToastUtils.show(mContext, "请先选择剧集");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAllTag() {
        try {
            if (eplsodes != null) {
                for (int i = 0; i < eplsodes.length(); i++) {
                    JSONObject eplsode = JSONUtils.getJsonObject(eplsodes, i, null);
                    if (eplsode != null) {
                        if (eplsode.optBoolean("downloadFlag")) {
                            eplsode.putOpt("downloadFlag", false);
                            eplsodes.put(i, eplsode);
                        }
                    }
                }
            }
            episodeViewPager.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler downloadHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == GlobalConstant.VIDEO_URL_RECEIVE) {
                String url = (String) msg.obj;
                if (StringUtils.isBlank(url)) {
                    if (msg.arg2 == 1003) {
                        ToastUtils.show(mContext, "此视频请在播放页中离线缓存。");
                    } else {
                        ToastUtils.show(mContext, "无法获取视频地址");
                    }
                    return;
                }
                String name = "";
                Bundle bundle = msg.getData();
                if (bundle != null) {
                    name = bundle.getString("name");
                    if (!StringUtils.isBlank(name)) {
                        LunaLog.d("name: " + name);
                    }

                }
                Intent i = new Intent(mContext, VideosDownloadService.class);
                i.setAction(Intent.ACTION_INSERT);
                Uri uri = Uri.parse(url);
                i.setData(uri);
                i.putExtra(Intent.EXTRA_TITLE, name);
                startService(i);


//                Intent i = new Intent(mContext, VideosDownloadService.class);
//                i.setAction(Intent.ACTION_INSERT);
//                Uri uri = Uri.parse(url);
//                i.setData(uri);
//                i.putExtra(Intent.EXTRA_TITLE, title);
//                i.putExtra(GlobalConstant.INTENT_FORBID_TOAST, true);
//                startService(i);
            }
        }
    };

    DefinitionDialog defDialog;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_download_page:
                Intent intent = new Intent(this, DownloadActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_begin_download:
                defDialog = new DefinitionDialog(mContext);
                defDialog.showDialog(new DefinitionDialog.OnDefinitionSelectedListener() {
                    @Override
                    public void onSelected(int def) {
                        downloads(def);
                    }
                });
                break;
            default:
                break;
        }
    }


    class EpisodeViewPager extends BaseAdapter {

        private LayoutInflater inflater;

        public EpisodeViewPager() {
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return eplsodes != null ? eplsodes.length() : 0;
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
                convertView = inflater.inflate(R.layout.item_episode, null);
                viewHolder.tv = (TextView) convertView.findViewById(R.id.number_episode);
                viewHolder.flag = convertView.findViewById(R.id.seletion_flag);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            JSONObject eplsode = JSONUtils.getJsonObject(eplsodes, position, null);
            if (eplsode != null) {
                String num = eplsode.optString("num");
                viewHolder.tv.setText(num + "集");
                boolean selectionFlag = eplsode.optBoolean("downloadFlag");

                if (selectionFlag) {
                    viewHolder.tv.setBackgroundResource(R.drawable.bg_episode_selected);
                    viewHolder.tv.setTextColor(textColorSelected);
                } else {
                    viewHolder.tv.setBackgroundResource(R.drawable.bg_episode);
                    viewHolder.tv.setTextColor(textColorUnselected);
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
