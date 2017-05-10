package com.cse40333.satchel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cse40333.satchel.firebaseNodes.Message;
import com.cse40333.satchel.firebaseNodes.User;
import com.cse40333.satchel.firebaseNodes.UserConversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Kris on 4/11/2017.
 */

public class ConversationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseUser mUser;
    private String convoId;
    private String name;
    private String text;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        convoId = getIntent().getStringExtra("convoId");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference("conversations").child(convoId).child("messages");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userConversations")
                .child(mUser.getUid()).child(convoId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserConversation userConversation = dataSnapshot.getValue(UserConversation.class);
                setTitle(userConversation.name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference("users")
                .child(mUser.getUid());
        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                name = user.displayName;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                LinearLayout messages = (LinearLayout) findViewById(R.id.conversation_messages);

                Message newMessage = dataSnapshot.getValue(Message.class);
                Log.d("User", newMessage.userId);
                Log.d("User", mUser.getUid());
                if (newMessage.userId.equals(mUser.getUid())) {
                    RelativeLayout message = (RelativeLayout) inflater.inflate(R.layout.outgoing_message, null);
                    ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    TextView messageText = (TextView) message.findViewById(R.id.message_text);
                    messageText.setText(newMessage.text);
                    messages.addView(message, lparams);
                } else {
                    LinearLayout message = (LinearLayout) inflater.inflate(R.layout.incoming_message, null);
                    ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    TextView messageText = (TextView) message.findViewById(R.id.message_text);
                    messageText.setText(newMessage.text);
                    messages.addView(message, lparams);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setSendOnClickListener();


    }

    private void setSendOnClickListener() {
        ImageButton sendButton = (ImageButton) findViewById(R.id.message_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText message = (EditText) findViewById(R.id.message_new_text);
                text = message.getText().toString();
                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();

                if (!text.equals("")) {
                    mRef.push().setValue(new Message(text, name, mUser.getUid(), ts));
                }
                message.setText("");

                time = getDate(tsLong);
                DatabaseReference convoRef = FirebaseDatabase.getInstance()
                        .getReference("userConversations").child(mUser.getUid())
                        .child(convoId).child("last_text");
                convoRef.setValue(text);

                convoRef = FirebaseDatabase.getInstance()
                        .getReference("userConversations").child(mUser.getUid())
                        .child(convoId).child("last_time");
                convoRef.setValue(time);

                DatabaseReference memberRef = FirebaseDatabase.getInstance()
                        .getReference("conversations").child(convoId);
                memberRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.child("members").getChildren()) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("userConversations").child(mUser.getUid())
                                    .child(convoId).child("last_text");
                            userRef.setValue(text);

                            userRef = FirebaseDatabase.getInstance()
                                    .getReference("userConversations").child(mUser.getUid())
                                    .child(convoId).child("last_time");
                            userRef.setValue(time);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
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
