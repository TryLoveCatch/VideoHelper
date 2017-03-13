package io.vov.vitamio.widget;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.R;

/**
 * Created by bintou on 16/3/16.
 */
public class PopupAdapter extends BaseAdapter {

    private List<String> itemList;
    private PopupAdapter self;

    protected PopupAdapter() {
        self = this;
        if (this.itemList == null) {
            this.itemList = new ArrayList();
        }
    }

    public void addItem(String str) {
        if (itemList != null)
            itemList.add(str);
        update();
    }

    public void addList(List<String> list) {
        if (itemList != null)
            itemList.addAll(list);
        update();
    }

    public void cleanList() {
        itemList.clear();
        update();
    }

    public void update() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                self.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return this.itemList == null ? 0 : this.itemList.size();
    }

    @Override
    public String getItem(int position) {
        return this.itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        public TextView tv;
    }

    public final View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.venvy_video_item_quality_sdk, null);
            (holder = new ViewHolder()).tv = (TextView) convertView.findViewById(R.id.quality_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv.setText(this.itemList.get(position));
        return convertView;
    }


}
