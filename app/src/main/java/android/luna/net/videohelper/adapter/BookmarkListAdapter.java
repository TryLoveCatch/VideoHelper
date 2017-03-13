package android.luna.net.videohelper.adapter;

import android.content.Intent;
import android.luna.net.videohelper.Ninja.Database.Record;
import android.luna.net.videohelper.Ninja.Database.RecordAction;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelper.Ninja.Unit.IntentUnit;
import android.luna.net.videohelper.activity.BookmarkActivity;
import android.luna.net.videohelper.activity.VideoIntroduceActivity;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelper.global.UploadEventRecord;
import android.luna.net.videohelper.widget.RelativeTimeTextView;
import android.luna.net.videohelptools.R;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.luna.common.util.ListUtils;
import net.luna.common.util.StringUtils;
import net.luna.common.util.ToastUtils;

import java.util.List;
import java.util.Locale;


/**
 * Created by bintou on 15/11/5.
 */
public class BookmarkListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BookmarkActivity mContext;

    private List<Record> mBookmarks;

    private RecordAction mRecordAction;

    private boolean isEditStatus = false;

    private ImageLoader imageLoader;

    private int mActivityType;

    private final int[] iconArray = {R.mipmap.web_ic_mango, R.mipmap.web_ic_aiqiyi, R.mipmap.web_ic_letv, R.mipmap.web_ic_tudou,
            R.mipmap.web_ic_youku, R.mipmap.web_ic_vqq, R.mipmap.web_ic_souhu, R.mipmap.web_ic_bilibili, R.mipmap.web_ic_acfun, R.mipmap.web_ic_earth};
    private final String[] siteArrays = {"m.mgtv.", "m.iqiyi.", "m.le.", "tudou.", "youku.", "v.qq.", "m.tv.sohu", "bilibili.", "acfun."};

    public BookmarkListAdapter(BookmarkActivity activity, List<Record> bookmarks, RecordAction recordAction, int type) {
        this.mContext = activity;
        this.mBookmarks = bookmarks;
        imageLoader = ImageLoader.getInstance();
        isEditStatus = false;
        mRecordAction = recordAction;
        mActivityType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BookmarkHolder(LayoutInflater.from(mContext).inflate(R.layout.item_bookmark, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BookmarkHolder) {
            Record videoDetial = mBookmarks.get(position);
            if (videoDetial != null) {
                ((BookmarkHolder) holder).bindData(videoDetial);
                ((BookmarkHolder) holder).bindEvent(position, videoDetial);
            }
        }
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(mBookmarks);
    }


    public void changeEditStatus() {
        isEditStatus = !isEditStatus;
        notifyDataSetChanged();
    }


    public void selectAllBookMark(boolean bool) {
        if (ListUtils.getSize(mBookmarks) > 0) {
            for (Record record : mBookmarks) {
                record.setShouldDelete(bool);
            }
            notifyDataSetChanged();
        }
    }

    private class BookmarkHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView webName;
        private TextView webUrl;
        protected CheckBox checkBox;
        private RelativeTimeTextView timeTextView;
        private RelativeLayout layoutBookmark;

        public BookmarkHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon_bookmark);
            webName = (TextView) itemView.findViewById(R.id.web_name);
            webUrl = (TextView) itemView.findViewById(R.id.web_url);
            layoutBookmark = (RelativeLayout) itemView.findViewById(R.id.layout_bookmark);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_bookmark);
            timeTextView = (RelativeTimeTextView) itemView.findViewById(R.id.time_text);
        }

        public void bindData(Record record) {
            if (record != null) {
                if (isEditStatus) {
                    if (checkBox.getVisibility() != View.VISIBLE)
                        checkBox.setVisibility(View.VISIBLE);
                } else {
                    if (checkBox.getVisibility() != View.GONE)
                        checkBox.setVisibility(View.GONE);
                }

                String url = record.getURL();
                icon.setImageResource(R.mipmap.web_ic_earth);
                if (!StringUtils.isBlank(url)) {

                    if (mActivityType == GlobalConstant.ACTIVITY_TYPE_BOOKMARK) {
                        timeTextView.setVisibility(View.GONE);
                        if (BrowserUnit.isURL(url)) {
                            webName.setText(record.getTitle());
                            webUrl.setText(record.getURL());
                            for (int i = 0; i < siteArrays.length; i++) {
                                if (url.contains(siteArrays[i])) {
                                    icon.setImageResource(iconArray[i]);
                                }
                            }
                        } else {
                            try {
                                String[] titleStrs = record.getTitle().split("iandi");
                                webName.setText(titleStrs[0]);
                                webUrl.setText(titleStrs[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                webName.setText(record.getTitle());
                                webUrl.setText(record.getURL());
                            }
                            icon.setImageResource(R.mipmap.download_ic_video);
                        }
                    } else {
                        timeTextView.setVisibility(View.VISIBLE);
                        timeTextView.setReferenceTime(record.getTime());
                        webName.setText(record.getTitle());
                        String timeStr = getTimeFormat(record.getPlayTime());
                        webUrl.setText(mContext.getResources().getString(R.string.has_played_at) + timeStr);
                        if (BrowserUnit.isURL(url)) {
                            for (int i = 0; i < siteArrays.length; i++) {
                                if (url.contains(siteArrays[i])) {
                                    icon.setImageResource(iconArray[i]);
                                }
                            }
                        } else {
                            icon.setImageResource(R.mipmap.download_ic_video);
                        }
                    }
                }
                checkBox.setChecked(record.isShouldDelete());
            }
        }

        public void bindEvent(int position, Record record) {
            layoutBookmark.setOnClickListener(new BookmarkOnClickListener(position, record));
            checkBox.setOnCheckedChangeListener(new BookmarkOncheckedChangeListener(position));
            layoutBookmark.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isEditStatus) {
                        changeEditStatus();
                        checkBox.setChecked(true);
                        mContext.showMenu();
                    }
                    return false;
                }
            });
        }

    }

    public void deleteBookmark() {
        try {
            for (int i = 0; i < mBookmarks.size(); i++) {
                Record record = mBookmarks.get(i);
                if (mRecordAction != null && record != null && record.isShouldDelete()) {
                    if (mActivityType == GlobalConstant.ACTIVITY_TYPE_BOOKMARK) {
                        mRecordAction.deleteBookmark(record);
                    } else {
                        mRecordAction.deletePlayRecord(record);
                    }
                    mBookmarks.remove(record);
                    i--;
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            ToastUtils.show(mContext, "删除失败，请退出重试");
            e.printStackTrace();
        }
    }

    class BookmarkOnClickListener implements View.OnClickListener {


        private Record mRecord;
        private int mPosition;

        public BookmarkOnClickListener(int position, Record mRecord) {
            this.mRecord = mRecord;
            this.mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if (!isEditStatus) {
                if (BrowserUnit.isURL(mRecord.getURL())) {
                    UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_SEARCH, "书签页");
                    Intent intent = new Intent();
                    intent.putExtra(IntentUnit.URL, mRecord.getURL());
                    mContext.setResult(GlobalConstant.INDEX_BOOKMARK_RETURN, intent);
                    mContext.finish();
                } else {
                    Intent intent = new Intent(mContext, VideoIntroduceActivity.class);
                    intent.putExtra("vid", mRecord.getURL());
                    UploadEventRecord.recordEvent(mContext, GlobalConstant.VIDEO_ENTER_EVENT, GlobalConstant.P_VIDEO_NAME, mRecord.getTitle());
                    UploadEventRecord.recordEventinternal(mContext, GlobalConstant.P_VIDEO_NAME, mRecord.getTitle());
                    mContext.startActivity(intent);
                }
            } else {
//                mBookmarks.get(mPosition).setShouldDelete(!mBookmarks.get(mPosition).isShouldDelete());
//                notifyDataSetChanged();
            }
        }
    }

    class BookmarkOncheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        private int mPosition;

        public BookmarkOncheckedChangeListener(int mPosition) {
            this.mPosition = mPosition;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mBookmarks != null && mBookmarks.size() > mPosition && mPosition >= 0) {
                mBookmarks.get(mPosition).setShouldDelete(isChecked);
            }
        }
    }

    public String getTimeFormat(long playtime) {
        playtime = playtime / 1000;
        int min = (int) (playtime / 60);
        int second = (int) (playtime % 60);
        String strTime = String.format(Locale.CHINA, "%02d:%02d",
                min, second);
        return strTime;

    }

}

