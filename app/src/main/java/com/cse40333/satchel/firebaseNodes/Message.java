package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {

    public String text;
    public String name;
    public String userId;
    public String timeStamp;

    public Message(){}

    public Message(String text, String name, String userId, String timeStamp) {
        this.text = text;
        this.name = name;
        this.userId = userId;
        this.timeStamp = timeStamp;
    }
}
