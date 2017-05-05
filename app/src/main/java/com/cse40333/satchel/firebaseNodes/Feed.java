package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Feed {

    public String itemId;
    public String itemName;
    public String itemThumbnailPath;
    public String userName;
    public String timestamp;
    public String message;

    public Feed() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Feed(String itemId, String itemName, String itemThumbnailPath, String userName, String timestamp, String message) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemThumbnailPath = itemThumbnailPath;
        this.userName = userName;
        this.timestamp = timestamp;
        this.message = message;
    }
}
