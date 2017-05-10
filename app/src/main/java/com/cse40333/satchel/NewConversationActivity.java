package com.cse40333.satchel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cse40333.satchel.firebaseNodes.Conversation;
import com.cse40333.satchel.firebaseNodes.UserConversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NewConversationActivity extends AppCompatActivity {

    //Firebase References
    private FirebaseAuth mAuth;

    NewFollowerAdapter newFollowerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);

        mAuth = FirebaseAuth.getInstance();

        addCreateConvoListener();

        initMemberAutoComplete();
    }

    private void addCreateConvoListener() {
        View.OnClickListener createConvoClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();


                //Conversations
                DatabaseReference convoRef = database.getReference("conversations").push();
                String newConvoKey = convoRef.getKey();
                convoRef.setValue(new Conversation(newFollowerAdapter.followerIds));

                EditText convoName = (EditText) findViewById(R.id.conversation_name);
                String name = convoName.getText().toString();

                //Users conversations
                DatabaseReference userConvosRef = database.getReference("userConversations").child(mAuth.getCurrentUser().getUid()).child(newConvoKey);
                userConvosRef.setValue(new UserConversation("", "", name));

                //Members conversations
                for (String userId : newFollowerAdapter.followerIds) {
                    DatabaseReference followersRef = database.getReference("userConversations").child(userId).child(newConvoKey);
                    followersRef.setValue(new UserConversation("", "", name));
                }

                Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                intent.putExtra("convoId", newConvoKey);
                startActivity(intent);
                finish();
            }
        };

        FloatingActionButton createConvoFab = (FloatingActionButton) findViewById(R.id.submit_new_convo);
        createConvoFab.setOnClickListener(createConvoClick);
    }

    private void initMemberAutoComplete() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final ArrayList<String[]> users = new ArrayList<>();

        database.child("users").orderByChild("displayName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()) {
                    String uid = suggestionSnapshot.getKey();
                    String displayName = suggestionSnapshot.child("displayName").getValue(String.class);
                    String email = suggestionSnapshot.child("email").getValue(String.class);
                    //Add the retrieved string to the list
                    String[] user = {uid, displayName, email};
                    users.add(user);
                }

                newFollowerAdapter = new NewFollowerAdapter(getApplicationContext(), android.R.layout.simple_list_item_2, users);
                AutoCompleteTextView ACTV = (AutoCompleteTextView) findViewById(R.id.new_convo_member_name);
                ACTV.setAdapter(newFollowerAdapter);

                ACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // add new follower to the maintained list of followers
                        String userId = ((TextView)view.findViewWithTag("userId")).getText().toString();
                        newFollowerAdapter.followerIds.add(userId);
                        // clear value from text field
                        AutoCompleteTextView followerView = (AutoCompleteTextView) findViewById(R.id.new_convo_member_name);
                        followerView.setText("");
                        /* add list element to gui */
                        // Get new follower's name
                        String followerName = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
                        // Inflate a new layout row
                        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View newFollowerRow = layoutInflater.inflate(R.layout.new_follower_list_row, null);
                        // fill in any details dynamically here
                        TextView textView = (TextView) newFollowerRow.findViewById(R.id.follower_name_row);
                        textView.setText(followerName);
                        TextView idTextView = (TextView) newFollowerRow.findViewById(R.id.follower_id);
                        idTextView.setText(userId);
                        idTextView.setVisibility(View.GONE);
                        // add listener for button to remove the follower
                        ImageButton removeFollowerBtn = (ImageButton) newFollowerRow.findViewById(R.id.remove_follower_row);
                        removeFollowerBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Remove follower from list
                                RelativeLayout removedRow = (RelativeLayout) v.getParent();
                                String removedFollowerId = ((TextView)removedRow.findViewById(R.id.follower_id)).getText().toString();
                                ((LinearLayout)removedRow.getParent()).removeView(removedRow);
                                // Remove follower from maintained array
                                newFollowerAdapter.followerIds.remove(removedFollowerId);
                            }
                        });
                        // insert into LinearLayout
                        ViewGroup followersList = (ViewGroup) findViewById(R.id.new_members_list);
                        ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        followersList.addView(newFollowerRow, 0, lparams);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
