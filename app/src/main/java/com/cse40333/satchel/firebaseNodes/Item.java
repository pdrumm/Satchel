package com.cse40333.satchel.firebaseNodes;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Item {

    public String name;
    public String ownerId;
    public ArrayList<String> followers;
    public String thumbnailPath;
    public String locationType;
    public String locationValue;

    public Item() {}

    public Item(String name, String ownerId, String thumbnailPath, String locationType, String locationValue) {
        this.name = name;
        this.ownerId = ownerId;
        this.thumbnailPath = thumbnailPath;
        this.locationType = locationType;
        this.locationValue = locationValue;
    }

    public Item(String name, String ownerId, String thumbnailPath, String locationType, String locationValue, ArrayList<String> followers) {
        this.name = name;
        this.ownerId = ownerId;
        this.thumbnailPath = thumbnailPath;
        this.locationType = locationType;
        this.locationValue = locationValue;
        this.followers = followers;
    }
}
