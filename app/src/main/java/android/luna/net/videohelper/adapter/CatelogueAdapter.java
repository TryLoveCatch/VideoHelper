package android.luna.net.videohelper.adapter;

import android.content.Context;
import android.luna.net.videohelper.anim.AnimationCollector;
import android.luna.net.videohelper.bean.Catelogue;
import android.luna.net.videohelper.global.GlobalConstant;
import android.luna.net.videohelptools.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.luna.common.util.ListUtils;
import net.luna.common.util.PreferencesUtils;

import java.util.List;


/**
 * Created by bintou on 15/10/30.
 */
public class CatelogueAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater inflater;
    private List<Catelogue> mCatelogueList;

    private boolean isEditMode = false;

    public CatelogueAdapter(Context context, List<Catelogue> catelogues) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mCatelogueList = catelogues;
    }

    public void updateList(List<Catelogue> catelogues) {
        if (catelogues != null) {
            mCatelogueList = catelogues;
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mCatelogueList.size();
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
            convertView = inflater.inflate(R.layout.item_catelogue, null);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.img_catelogue);
            viewHolder.name = (TextView) convertView.findViewById(R.id.catelogue_name);
            viewHolder.point = (ImageView) convertView.findViewById(R.id.point);
            viewHolder.selectedTag = (ImageView) convertView.findViewById(R.id.img_select_tag);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Catelogue catelogue = mCatelogueList.get(position);
        if (catelogue != null) {
            viewHolder.icon.setImageResource(catelogue.icon);
            viewHolder.name.setText(catelogue.title);
            if (catelogue.isSelected && isEditMode) {
                viewHolder.selectedTag.setVisibility(View.VISIBLE);
            } else {
                viewHolder.selectedTag.setVisibility(View.GONE);
            }

            if (catelogue.type == 1) {
                viewHolder.point.setVisibility(View.GONE);
            } else {
                if (catelogue.showPoint) {
                    if (viewHolder.point.getVisibility() != View.VISIBLE) {
                        viewHolder.point.setVisibility(View.VISIBLE);
                        AnimationCollector collector = new AnimationCollector(mContext);
                        collector.pointVisiable(viewHolder.point);
                    } else {
                        viewHolder.point.setVisibility(View.GONE);
                    }
                } else {
                    viewHolder.point.setVisibility(View.GONE);
                }
            }
        }
        return convertView;
    }


    class ViewHolder {
        ImageView icon;
        TextView name;
        ImageView point;
        ImageView selectedTag;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        if (isEditMode) {
            String[] validTitles = PreferencesUtils.getString(mContext, GlobalConstant.SP_CATELOGUE_TITLES, "").split("\\;");
            int index = 0;
            if (validTitles != null && validTitles.length > 3) {
                for (Catelogue catelogue : mCatelogueList) {
                    if (index < validTitles.length && catelogue.title.equals(validTitles[index])) {
                        catelogue.isSelected = true;
                        index++;
                    }
                }
            }
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void changeCatelogueStatus(int position) {
        if (ListUtils.getSize(mCatelogueList) > position) {
            mCatelogueList.get(position).isSelected = !mCatelogueList.get(position).isSelected;
        }
        notifyDataSetChanged();
    }

    public void saveCatelogueStatus() {
        if (ListUtils.getSize(mCatelogueList) > 0) {
            String titles = "";
            for (Catelogue catelogue : mCatelogueList) {
                if (catelogue.isSelected) {
                    titles = titles.concat(catelogue.title + ";");
                }
            }
            PreferencesUtils.putString(mContext, GlobalConstant.SP_CATELOGUE_TITLES, titles);
            notifyDataSetChanged();
        }
    }
}

