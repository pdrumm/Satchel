package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Conversation {

    public ArrayList<String> members;

    public Conversation() {}

    public Conversation(ArrayList<String> members) {
        this.members = members;
    }
}
