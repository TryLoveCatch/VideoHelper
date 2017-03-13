package android.luna.net.videohelper.Ninja.View;

import android.content.Context;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelper.Ninja.Unit.ViewUnit;
import android.luna.net.videohelptools.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.askerov.dynamicgrid.BaseDynamicGridAdapter;

import java.util.List;

public class GridAdapter extends BaseDynamicGridAdapter {
    private static class Holder {
        TextView title;
        ImageView cover;
    }

    private List<android.luna.net.videohelper.Ninja.View.GridItem> list;

    public List<android.luna.net.videohelper.Ninja.View.GridItem> getList() {
        return list;
    }

    private Context context;

    public GridAdapter(Context context, List<android.luna.net.videohelper.Ninja.View.GridItem> list, int columnCount) {
        super(context, list, columnCount);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
            holder = new Holder();
            holder.title = (TextView) view.findViewById(R.id.grid_item_title);
            holder.cover = (ImageView) view.findViewById(R.id.grid_item_cover);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        GridItem item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.cover.setImageBitmap(BrowserUnit.file2Bitmap(context, item.getFilename()));
        ViewUnit.setElevation(view, context.getResources().getDimensionPixelSize(R.dimen.elevation_1dp));

        return view;
    }
}
