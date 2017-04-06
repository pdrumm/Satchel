package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String email;
    public String displayName;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
    }
}
