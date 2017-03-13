package android.luna.net.videohelper.Ninja.View;

import android.content.Context;
import android.luna.net.videohelper.Ninja.Database.Record;
import android.luna.net.videohelper.Ninja.Unit.BrowserUnit;
import android.luna.net.videohelptools.R;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import net.luna.common.util.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CompleteAdapter extends BaseAdapter implements Filterable {
    private class CompleteFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            if (prefix == null) {
                return new FilterResults();
            }

            resultList.clear();
            for (CompleteItem item : originalList) {
                if (item.getTitle().contains(prefix) || item.getURL().contains(prefix)) {
                    if (item.getTitle().contains(prefix)) {
                        item.setIndex(item.getTitle().indexOf(prefix.toString()));
                    } else if (item.getURL().contains(prefix)) {
                        item.setIndex(item.getURL().indexOf(prefix.toString()));
                    }
                    resultList.add(item);
                    if (resultList.size() > 20) {
                        break;
                    }
                }
            }

            Collections.sort(resultList, new Comparator<CompleteItem>() {
                @Override
                public int compare(CompleteItem first, CompleteItem second) {
                    if (first.getIndex() < second.getIndex()) {
                        return -1;
                    } else if (first.getIndex() > second.getIndex()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            FilterResults results = new FilterResults();
            results.values = resultList;
            results.count = resultList.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (Looper.myLooper() == Looper.getMainLooper())
            super.notifyDataSetChanged();
    }

    private class CompleteItem {
        private String title;

        protected String getTitle() {
            return title;
        }

        private String url;

        protected String getURL() {
            return url;
        }

        private int index = Integer.MAX_VALUE;

        protected int getIndex() {
            return index;
        }

        protected void setIndex(int index) {
            this.index = index;
        }

        protected CompleteItem(String title, String url) {
            this.title = title;
            this.url = url;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof CompleteItem)) {
                return false;
            }

            CompleteItem item = (CompleteItem) object;
            return item.getTitle().equals(title) && item.getURL().equals(url);
        }

        @Override
        public int hashCode() {
            if (title == null || url == null) {
                return 0;
            }

            return title.hashCode() & url.hashCode();
        }
    }

    private static class Holder {
        protected TextView titleView;
        protected TextView urlView;
    }

    private Context context;
    private int layoutResId;
    private List<CompleteItem> originalList;
    private List<CompleteItem> resultList;
    private CompleteFilter filter = new CompleteFilter();

    public CompleteAdapter(Context context, int layoutResId, List<Record> recordList, boolean needDedup) {
        this.context = context;
        this.layoutResId = layoutResId;
        this.originalList = new ArrayList<>();
        this.resultList = new ArrayList<>();
        if (needDedup) {
            dedup(recordList);
        } else {
            switchRecord(recordList);
        }
    }

    public void switchRecord(List<Record> recordList) {
        for (Record record : recordList) {
            if (record.getTitle() != null) {
                CompleteItem completeItem = new CompleteItem(record.getTitle(), record.getURL());
                resultList.add(completeItem);
            }
        }
    }

    private void dedup(List<Record> recordList) {
        for (Record record : recordList) {
            if (record.getTitle() != null
                    && !record.getTitle().isEmpty()
                    && record.getURL() != null
                    && !record.getURL().isEmpty()) {
                CompleteItem completeItem = new CompleteItem(record.getTitle(), record.getURL());
                if (!originalList.contains(completeItem)) {
                    originalList.add(completeItem);
                }
            }
        }

//        Set<CompleteItem> set = new HashSet<>(originalList);
//        originalList.clear();
//        originalList.addAll(set);
    }

    @Override
    public int getCount() {
        return ListUtils.getSize(resultList);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public Object getItem(int position) {
        try {
            if (ListUtils.getSize(resultList) > position) {
                return resultList.get(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Holder holder;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, null, false);
            holder = new Holder();
            holder.titleView = (TextView) view.findViewById(R.id.complete_item_title);
            holder.urlView = (TextView) view.findViewById(R.id.complete_item_url);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        try {
            if (ListUtils.getSize(resultList) > position) {
                CompleteItem item = resultList.get(position);
                holder.titleView.setText(item.getTitle());
                if (item.getURL() != null && BrowserUnit.isURL(item.getURL())) {
                    holder.urlView.setVisibility(View.VISIBLE);
                    if (item.getURL() != null) {
                        holder.urlView.setText(Html.fromHtml(BrowserUnit.urlWrapper(item.getURL())), TextView.BufferType.SPANNABLE);
                    } else {
                        holder.urlView.setText(item.getURL());
                    }
                } else {
                    holder.urlView.setText(item.getURL());
                    holder.urlView.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
}
