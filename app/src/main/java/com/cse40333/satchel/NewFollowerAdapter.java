package com.cse40333.satchel;

import android.content.Context;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;

public class NewFollowerAdapter extends ArrayAdapter<String[]> {

    private ArrayList<String[]> items;
    private ArrayList<String[]> itemsAll;
    private ArrayList<String[]> suggestions;
    private int viewResourceId;
    private ArrayList<String> userIds;

    NewFollowerAdapter(Context context, int viewResourceId, ArrayList<String[]> followers) {
        super(context, viewResourceId, followers);
        this.items = followers;
        this.itemsAll = (ArrayList<String[]>) items.clone();
        this.suggestions = new ArrayList<String[]>();
        this.viewResourceId = viewResourceId;
    }

    public View getView (int position, View convertView, ViewGroup parent) {
        // Inflate the list item
        LayoutInflater scheduleInflater = LayoutInflater.from(getContext());
        ViewGroup newFollowerView = (ViewGroup) scheduleInflater.inflate(viewResourceId, parent, false);
        // Retrieve the adapter data
        String[] followerData = getItem(position);
        // Get the view elements and set their text properties
        TextView tv1 = (TextView) newFollowerView.findViewById(android.R.id.text1);
        TextView tv2 = (TextView) newFollowerView.findViewById(android.R.id.text2);
        tv1.setText(followerData[1]);
        tv2.setText(followerData[2]);
        // Create a hidden view for the user id
        TextView tv3 = new TextView(getContext());
        tv3.setTag("userId");
        tv3.setText(followerData[0]);
        tv3.setVisibility(View.GONE);
        newFollowerView.addView(tv3);

        return newFollowerView;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        public String convertResultToString(Object resultValue) {
            String str = ((String[]) (resultValue))[1];
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (String[] user : itemsAll) {
                    if (user[1].toLowerCase()
                            .startsWith(constraint.toString().toLowerCase())) {
                        suggestions.add(user);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            @SuppressWarnings("unchecked")
            ArrayList<String[]> filteredList = (ArrayList<String[]>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (String[] c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}
