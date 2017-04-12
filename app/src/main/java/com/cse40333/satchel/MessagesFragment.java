package com.cse40333.satchel;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {


    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        ArrayList<String> messages = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            messages.add("");
        }
        ListView messageListView = (ListView) rootView.findViewById(R.id.message_list_view);
        MessagesAdapter messagesAdapter = new MessagesAdapter(getContext(), messages);
        messageListView.setAdapter(messagesAdapter);

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), ConversationActivity.class);
                startActivity(intent);
            }
        };
        messageListView.setOnItemClickListener(itemClickListener);

        //Handler for New Message FAB
        View.OnClickListener newConvoClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ConversationActivity.class);
                startActivity(intent);
            }
        };
        FloatingActionButton newConvoFab = (FloatingActionButton) rootView.findViewById(R.id.add_conversation);
//        newConvoFab.setOnClickListener(newConvoClick);


        // Inflate the layout for this fragment
        return rootView;
    }

}
