package gp.sportsschedule;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dave on 2/12/2016.
 */
public class SimpleListAdapter extends BaseAdapter implements ListAdapter {

    public static interface ListItemAdapter<T> {
        public View getView(T item, Context context, View convertView, ViewGroup parent);
    }


    private final Context context;

    private final List<Schedule.Game> items;

    private final ListItemAdapter<Schedule.Game> itemAdapter;

    public SimpleListAdapter(Context context, List<Schedule.Game> items, ListItemAdapter<Schedule.Game> itemAdapter){
        if(context == null || items == null || itemAdapter == null) throw new IllegalArgumentException();

        this.context = context;
        this.items = new ArrayList<Schedule.Game>(items);
        this.itemAdapter = itemAdapter;
    }



    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Schedule.Game getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return itemAdapter.getView(items.get(position), context, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
