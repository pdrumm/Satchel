package com.cse40333.satchel;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cse40333.satchel.firebaseNodes.Item;
import com.cse40333.satchel.firebaseNodes.User;
import com.cse40333.satchel.firebaseNodes.UserItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemDetailActivity extends AppCompatActivity {

    // id of the firebase element being displayed
    String itemId;

    // Firebase references
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;

    final private String LOCATION_TEXT = "text";
    final private String LOCATION_IMAGE = "image";
    final private String LOCATION_MAP = "map";
    final private String LOCATION_CHECKED = "checked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Instantiate Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();

        // Retrieve bundle extras
        itemId = getIntent().getStringExtra("itemId");

        // Add Firebase Listeners
        addItemListener();
        addUserItemListener();

        // Update favorite field in FB when star is clicked
        addFavoriteClickListener();

        // Add click listener for check out button
        addCheckOutClickListener();
    }

    private void addItemListener() {
        DatabaseReference itemsRef = mDatabase.getReference("items").child(itemId);
        // Read from the database
        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Item value = dataSnapshot.getValue(Item.class);
                setItemImages(value.thumbnailPath, R.id.itemThumbnail, "thumbnail");
                setItemDetails(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Firebase Error", "Failed to read value.", error.toException());
            }
        });
    }

    private void addUserItemListener() {
        DatabaseReference userItemsRef = mDatabase.getReference("userItems").child(mAuth.getCurrentUser().getUid()).child(itemId);
        // Read from the database
        userItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                UserItem value = dataSnapshot.getValue(UserItem.class);
                setUserItemDetails(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Firebase Error", "Failed to read value.", error.toException());
            }
        });
    }

    private void addFollowersToList(ArrayList<String> followerIds) {
        if (followerIds == null) { return; }
        // Get list inflator
        final LinearLayout itemFollowers = (LinearLayout) findViewById(R.id.itemFollowersList);
        final LayoutInflater linf = LayoutInflater.from(ItemDetailActivity.this);
        // Get user names from FB
        DatabaseReference usersRef = mDatabase.getReference("users");
        for (String followerId : followerIds) {
            usersRef.child(followerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    View v = linf.inflate(android.R.layout.simple_list_item_1, null);
                    ((TextView) v.findViewById(android.R.id.text1)).setText(dataSnapshot.getValue(User.class).displayName);
                    itemFollowers.addView(v);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void setItemDetails(Item item) {
        Log.d("TESTMYTEST","What about here?");
        // get Layout views
        TextView itemNameView = (TextView) findViewById(R.id.itemName);
        RelativeLayout itemLocationView = (RelativeLayout) findViewById(R.id.item_location_container);
        // set item name
        itemNameView.setText(item.name);
        // set item location
        RelativeLayout locationText = (RelativeLayout) findViewById(R.id.item_location_type_text);
        RelativeLayout locationImage = (RelativeLayout) findViewById(R.id.item_location_type_image);
        RelativeLayout locationChecked = (RelativeLayout) findViewById(R.id.item_location_type_checked_out);
        switch (item.locationType) {
            case LOCATION_TEXT:
                locationText.setVisibility(View.VISIBLE);
                locationImage.setVisibility(View.GONE);
                locationChecked.setVisibility(View.GONE);
                TextView tv = (TextView) locationText.findViewById(R.id.item_location_text);
                tv.setText(item.locationValue);
                break;
            case LOCATION_IMAGE:
                locationText.setVisibility(View.GONE);
                locationImage.setVisibility(View.VISIBLE);
                locationChecked.setVisibility(View.GONE);
                setItemImages(item.locationValue, R.id.item_location_image, "location");
                break;
            case LOCATION_CHECKED:
                locationText.setVisibility(View.GONE);
                locationImage.setVisibility(View.GONE);
                locationChecked.setVisibility(View.VISIBLE);
                TextView tv2 = (TextView) locationChecked.findViewById(R.id.item_location_checked_out);
                tv2.setText(item.locationValue);
                //Log.d("CHECKED","Totally got checked out");
                break;
            default:
                locationText.setVisibility(View.GONE);
                locationImage.setVisibility(View.GONE);
                locationChecked.setVisibility(View.GONE);
        }
        // set followers list
        addFollowersToList(item.followers);
    }

    private void setUserItemDetails(UserItem item) {
        // get Layout views
        TextView itemOwnerView = (TextView) findViewById(R.id.itemOwner);
        CheckBox itemFavoriteView = (CheckBox) findViewById(R.id.itemFavorite);
        // set view values
        itemOwnerView.setText(item.ownerName);
        itemFavoriteView.setChecked(item.favorite);
    }

    private void setItemImages(String path, int imageViewId, String tempFileName) {
        Log.d("TESTZZ1", path);
        Log.d("TESTZZ2", String.valueOf(imageViewId));
        Log.d("TESTZZ3", tempFileName);
        final int imgViewId = imageViewId;
        try {
            StorageReference thumbnailRef = mStorageRef.child(path);
            final File localFile = File.createTempFile(tempFileName, "jpg");
            thumbnailRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Successfully downloaded data to local file
                    ImageView itemThumbnail = (ImageView) findViewById(imgViewId);
                    itemThumbnail.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                    Log.d("TESTZZ4", "here");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    ImageView itemThumbnail = (ImageView) findViewById(imgViewId);
                    itemThumbnail.setImageResource(R.drawable.ic_add_photo_light);
                }
            });
        } catch (IOException e) {
            // Failed to create new local file
            ImageView itemThumbnail = (ImageView) findViewById(imgViewId);
            itemThumbnail.setImageResource(R.drawable.ic_add_photo_light);
        }
    }

    private void addFavoriteClickListener() {
        CheckBox favStarCb = (CheckBox) findViewById(R.id.itemFavorite);
        favStarCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DatabaseReference userItemsRef = mDatabase.getReference("userItems").child(mAuth.getCurrentUser().getUid()).child(itemId);
                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("favorite", isChecked);
                userItemsRef.updateChildren(hm);
            }
        });
    }

    private void addCheckOutClickListener() {
        Button btn = (Button) findViewById(R.id.check_out_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CHANGE LOCATION_TYPE
                HashMap<String, Object> hm2 = new HashMap<String, Object>();
                hm2.put("locationType", "checked");
                mDatabase.getReference("items").child(itemId).updateChildren(hm2);
                Log.d("ONCLICK","Entered onclick");
                //DatabaseReference itemsRef = mDatabase.getReference("Items").child(itemId);
                DatabaseReference usersRef = mDatabase.getReference("users").child(mAuth.getCurrentUser().getUid());
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String data = dataSnapshot.getValue(User.class).displayName;
                        data = "Currently checked out by " + data;
                        HashMap<String, Object> hm = new HashMap<>();
                        hm.put("locationValue", data);
                        mDatabase.getReference("items").child(itemId).updateChildren(hm);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

}
