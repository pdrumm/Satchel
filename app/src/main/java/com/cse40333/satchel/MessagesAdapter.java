package com.cse40333.satchel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Kris on 4/11/2017.
 */

public class MessagesAdapter extends ArrayAdapter<String> {
    MessagesAdapter (Context context, ArrayList<String> conversations) {
        super(context, R.layout.message_list_row, conversations);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater messageInflater = LayoutInflater.from(getContext());
        View messageView = messageInflater.inflate(R.layout.message_list_row, parent, false);

        return messageView;
    }
}
