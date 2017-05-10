package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserConversation {

    public String last_text;
    public String last_time;
    public String name;

    public UserConversation() {}

    public UserConversation(String last_text, String last_time, String name) {
        this.last_text = last_text;
        this.last_time = last_time;
        this.name = name;
    }
}
