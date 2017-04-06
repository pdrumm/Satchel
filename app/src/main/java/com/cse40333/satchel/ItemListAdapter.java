package com.cse40333.satchel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemListAdapter extends ArrayAdapter<String[]> {
    ItemListAdapter(Context context, ArrayList<String[]> itemList) {
        super(context, R.layout.item_list_row, itemList);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate item row
        LayoutInflater itemListInflater = LayoutInflater.from(getContext());
        View itemListView = itemListInflater.inflate(R.layout.item_list_row, parent, false);

        // Retrieve item data
        String[] itemData = getItem(position);
        Log.d("itemlist", itemData[0] + ", " + itemData[1]);

        // Retrieve View elements
        TextView itemName = (TextView) itemListView.findViewById(R.id.item_name);
        TextView itemOwner = (TextView) itemListView.findViewById(R.id.item_owner);

        // Set img/txt properties of View elements
        itemName.setText(itemData[0]);
        itemOwner.setText(itemData[1]);

        return itemListView;
    }
}
