package com.cse40333.satchel;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


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

        ListView messageListView = (ListView) rootView.findViewById(R.id.message_list_view);


        //Handler for New Message FAB
        View.OnClickListener newConvoClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ConversationActivity.class);
                startActivity(intent);
            }
        };
        FloatingActionButton newConvoFab = (FloatingActionButton) rootView.findViewById(R.id.add_conversation);
        newConvoFab.setOnClickListener(newConvoClick);


        // Inflate the layout for this fragment
        return rootView;
    }

}
