package com.cse40333.satchel;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    View rootView;
    private ArrayList<UserConversation> messages = new ArrayList<>();

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

        Query mRef = database.getReference("userConversations").child(mUser.getUid()).orderByChild("last_time");

        ListAdapter listAdapter = new FirebaseListAdapter<UserConversation>(getActivity(), UserConversation.class, R.layout.message_list_row, mRef) {
            protected void populateView(View view, UserConversation conversation, int position) {
                final View listView = view;

                Log.d("timeStamp", conversation.last_time);
                view.setTag(getRef(getCount() - 1 - position).getKey());
                TextView convoName = (TextView) view.findViewById(R.id.message_name);
                TextView lastMessage = (TextView) view.findViewById(R.id.message_last_text);
                TextView lastTime = (TextView) view.findViewById(R.id.message_time);

                convoName.setText(conversation.name);
                lastMessage.setText(conversation.last_text);
                lastTime.setText(getDate(Long.parseLong(conversation.last_time)));
            }

            @Override
            public UserConversation getItem(int pos) {
                return super.getItem(getCount() - 1 - pos);
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

    private String getDate(long timeStamp){
        try{
            // Get curr year
            DateFormat sdf = new SimpleDateFormat("yyyy");
            Date netDate = (new Date());
            String currYear = sdf.format(netDate);
            // Get year of timestamp
            sdf = new SimpleDateFormat("yyyy");
            netDate = (new Date(timeStamp));
            String tsYear = sdf.format(netDate);

            if (currYear.equals(tsYear)) {
                sdf = new SimpleDateFormat("MMM d - h:mm a");
            } else {
                sdf = new SimpleDateFormat("MMM d, yyyy - h:mm a");
            }
            netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

}
