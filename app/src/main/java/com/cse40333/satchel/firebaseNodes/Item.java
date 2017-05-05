package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Item {

    public String name;
    public String ownerId;
    public ArrayList<String> followers;
    public String thumbnailPath;
    public String location;

    public Item() {}

    public Item(String name, String ownerId, String thumbnailPath, String location) {
        this.name = name;
        this.ownerId = ownerId;
        this.thumbnailPath = thumbnailPath;
        this.location = location;
    }

    public Item(String name, String ownerId, String thumbnailPath, String location, ArrayList<String> followers) {
        this.name = name;
        this.ownerId = ownerId;
        this.thumbnailPath = thumbnailPath;
        this.location = location;
        this.followers = followers;
    }
}
