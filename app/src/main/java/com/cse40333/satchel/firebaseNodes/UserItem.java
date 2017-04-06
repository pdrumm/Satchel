package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserItem {

    public String name;
    public String ownerName;
    public String thumbnailPath;
    public boolean favorite;

    public UserItem() {}

    public UserItem(String name, String ownerName, String thumbnailPath, boolean favorite) {
        this.name = name;
        this.ownerName = ownerName;
        this.thumbnailPath = thumbnailPath;
        this.favorite = favorite;
    }

}
