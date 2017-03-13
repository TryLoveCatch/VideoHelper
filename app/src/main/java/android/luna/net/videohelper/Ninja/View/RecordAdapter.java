package android.luna.net.videohelper.Ninja.View;

import android.content.Context;
import android.luna.net.videohelptools.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import android.luna.net.videohelper.widget.RelativeTimeTextView;

import java.util.List;

public class RecordAdapter extends ArrayAdapter<android.luna.net.videohelper.Ninja.Database.Record> {
    private Context context;
    private int layoutResId;
    private List<android.luna.net.videohelper.Ninja.Database.Record> list;

    public RecordAdapter(Context context, int layoutResId, List<android.luna.net.videohelper.Ninja.Database.Record> list) {
        super(context, layoutResId, list);
        this.context = context;
        this.layoutResId = layoutResId;
        this.list = list;
    }

    private static class Holder {
        TextView title;
        RelativeTimeTextView time;
        TextView url;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
            holder = new Holder();
            holder.title = (TextView) view.findViewById(R.id.record_item_title);
            holder.time = (RelativeTimeTextView) view.findViewById(R.id.record_item_time);
            holder.url = (TextView) view.findViewById(R.id.record_item_url);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        android.luna.net.videohelper.Ninja.Database.Record record = list.get(position);
        holder.title.setText(record.getTitle());
        holder.time.setReferenceTime(record.getTime());
        holder.url.setText(record.getURL());

        return view;
    }
}