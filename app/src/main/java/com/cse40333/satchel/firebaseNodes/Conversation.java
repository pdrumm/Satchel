package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Conversation {

    public String title;

    public Conversation(){}

    public Conversation(String title) {
        this.title = title;
    }
}
