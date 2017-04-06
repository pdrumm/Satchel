package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Item {

    public String name;
    public String ownerId;
    public String[] followers;
    public String thumbnailPath;
    public String location;

    public Item() {}

    public Item(String name, String ownerId, String thumbnailPath, String location) {
        this.name = name;
        this.ownerId = ownerId;
        this.thumbnailPath = thumbnailPath;
        this.location = location;
    }
}
