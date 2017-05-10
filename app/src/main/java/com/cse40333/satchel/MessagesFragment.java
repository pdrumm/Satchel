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
import android.widget.TextView;

import com.cse40333.satchel.firebaseNodes.UserConversation;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    View rootView;

    //Firebase references
    private FirebaseAuth mAuth;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();

        rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        /*FirebaseDatabase databse = FirebaseDatabase.getInstance();
        DatabaseReference mRef = databse.getReference();

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
        messageListView.setOnItemClickListener(itemClickListener);*/

        addPopulateListViewListener();
        addMessageListener();
        addNewMessageListener();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void addPopulateListViewListener() {
        ListView messageListView = (ListView) rootView.findViewById(R.id.message_list_view);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        DatabaseReference mRef = database.getReference("userConversations").child(mUser.getUid());
        ListAdapter listAdapter = new FirebaseListAdapter<UserConversation>(getActivity(), UserConversation.class, R.layout.message_list_row, mRef) {
            protected void populateView(View view, UserConversation conversation, int position) {
                final View listView = view;

                view.setTag(getRef(position).getKey());
                TextView convoName = (TextView) view.findViewById(R.id.message_name);
                TextView lastMessage = (TextView) view.findViewById(R.id.message_last_text);
                TextView lastTime = (TextView) view.findViewById(R.id.message_time);

                convoName.setText(conversation.name);
                lastMessage.setText(conversation.last_text);
                lastTime.setText(conversation.last_time);
            }
        };

        messageListView.setAdapter(listAdapter);
    }

    private void addMessageListener() {
        ListView messageListView = (ListView) rootView.findViewById(R.id.message_list_view);
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), ConversationActivity.class);
                intent.putExtra("convoId", view.getTag().toString());
                startActivity(intent);
            }
        };

        messageListView.setOnItemClickListener(itemClickListener);
    }

    private void addNewMessageListener() {
        View.OnClickListener newConvoClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NewConversationActivity.class);
                startActivity(intent);
            }
        };
        FloatingActionButton newConvoFab = (FloatingActionButton) rootView.findViewById(R.id.add_conversation);
        newConvoFab.setOnClickListener(newConvoClick);
    }
}
