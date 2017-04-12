package com.cse40333.satchel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Kris on 4/11/2017.
 */

public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        setTitle("Conversation Name");

        LinearLayout messages = (LinearLayout) findViewById(R.id.conversation_messages);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        Random r = new Random();
        for (int i = 0; i < 20; i++) {

            int temp = r.nextInt();
            if (temp%2 == 0) {
                LinearLayout message = (LinearLayout) inflater.inflate(R.layout.incoming_message, null);
                ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );
                messages.addView(message, lparams);
            } else {
                RelativeLayout message = (RelativeLayout) inflater.inflate(R.layout.outgoing_message, null);
                ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );
                messages.addView(message, lparams);
            }

        }
    }
}
