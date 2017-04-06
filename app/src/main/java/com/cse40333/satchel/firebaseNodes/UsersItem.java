package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UsersItem {

    public String name;
    public String ownerName;
    public String thumbnailPath;
    public boolean favorite;

    public UsersItem() {}

    public UsersItem(String name, String ownerName, String thumbnailPath, boolean favorite) {
        this.name = name;
        this.ownerName = ownerName;
        this.thumbnailPath = thumbnailPath;
        this.favorite = favorite;
    }

}
