package com.cse40333.satchel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cse40333.satchel.firebaseNodes.Feed;
import com.cse40333.satchel.firebaseNodes.Item;
import com.cse40333.satchel.firebaseNodes.UserItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;


public class NewItemActivity extends AppCompatActivity {

    // Add photo
    private ImageSelector imageSelector;

    // Firebase references
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    // Show progress bar
    private Progress progress;

    // Keep track of the followers added
    NewFollowerAdapter newFollowerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        // Instantiate Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Add item thumbnail
        addNewImageListener();

        // Submit new item
        addSubmitItemListener();

        // Instantiate Progress bar
        View mNewItemFormView = findViewById(R.id.new_item_form_scroll);
        View mProgressView = findViewById(R.id.new_item_progress);
        progress = new Progress(getApplicationContext(), mNewItemFormView, mProgressView);

        // Initialize Autocomplete field and add listener to it
        initFollowerAutocomplete();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        imageSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageSelector.onActivityResult(requestCode, resultCode, data);
    }

    private void addSubmitItemListener() {
        View.OnClickListener submitItemClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve form data
                EditText itemName = (EditText) findViewById(R.id.item_name);
                String itemNameVal = itemName.getText().toString();

                progress.showProgress(true);

                // Submit new item data to Firebase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                // - items
                DatabaseReference itemsRef = database.getReference("items").push();
                String newItemKey = itemsRef.getKey();
                String thumbnailPath = (imageSelector.imageUri == null ? "default" : newItemKey) + "/thumbnail.jpg";
                itemsRef.setValue(new Item(itemNameVal, mAuth.getCurrentUser().getUid(), thumbnailPath, "[location of the item]", newFollowerAdapter.followerIds));
                // Current user's userItem & feed
                DatabaseReference userItemsRef = database.getReference("userItems").child(mAuth.getCurrentUser().getUid()).child(newItemKey);
                userItemsRef.setValue(new UserItem(itemNameVal, mAuth.getCurrentUser().getDisplayName(), thumbnailPath, false));
                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();
                DatabaseReference feedRef = database.getReference("feed").child(mAuth.getCurrentUser().getUid()).push();
                feedRef.setValue(new Feed(newItemKey, itemNameVal, thumbnailPath, mAuth.getCurrentUser().getDisplayName(), ts, "Created"));
                // - Followers userItems & feed
                for (String userId : newFollowerAdapter.followerIds) {
                    // userItems
                    DatabaseReference followerRef = database.getReference("userItems").child(userId).child(newItemKey);
                    followerRef.setValue(new UserItem(itemNameVal, mAuth.getCurrentUser().getDisplayName(), thumbnailPath, false));
                    // - feed
                    feedRef = database.getReference("feed").child(userId).push();
                    feedRef.setValue(new Feed(newItemKey, itemNameVal, thumbnailPath, mAuth.getCurrentUser().getDisplayName(), ts, "Shared with you"));
                }
                // - Storage
                StorageReference thumbnailRef = mStorageRef.child(thumbnailPath);
                thumbnailRef.putFile(imageSelector.imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        // Return to Items list
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        progress.showProgress(false);
                        Toast.makeText(getApplicationContext(), "Failed to upload image to Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        // Get FAB and add listener
        FloatingActionButton submitItemFab = (FloatingActionButton) findViewById(R.id.submit_new_item);
        submitItemFab.setOnClickListener(submitItemClick);
    }

    private void addNewImageListener() {
        View.OnClickListener addThumbnailClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelector = new ImageSelector(NewItemActivity.this, R.id.item_thumbnail);
                imageSelector.selectImage();
            }
        };
        Button addThumbnailBtn = (Button) findViewById(R.id.add_item_thumbnail);
        addThumbnailBtn.setOnClickListener(addThumbnailClick);
    }

    private void initFollowerAutocomplete() {

        /*
         * Init Autocomplete Field
         */
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final ArrayList<String[]> users = new ArrayList<>();
        // Query the list of all users and add them to an array.
        // This array will be the base set for the autocomplete field of followers.
        database.child("users").orderByChild("displayName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // build an array of all users' info
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                    //Get the suggestion by childing the key of the string you want to get.
                    String uid = suggestionSnapshot.getKey();
                    String displayName = suggestionSnapshot.child("displayName").getValue(String.class);
                    String email = suggestionSnapshot.child("email").getValue(String.class);
                    //Add the retrieved string to the list
                    String[] user = {uid, displayName, email};
                    users.add(user);
                }
                // Set the autocomplete values
                newFollowerAdapter = new NewFollowerAdapter(getApplicationContext(), android.R.layout.simple_list_item_2, users);
                AutoCompleteTextView ACTV = (AutoCompleteTextView) findViewById(R.id.new_follower_name);
                ACTV.setAdapter(newFollowerAdapter);

                /*
                 * When the user selects a follower, add them to the list
                 */
                ACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // add new follower to the maintained list of followers
                        String userId = ((TextView)view.findViewWithTag("userId")).getText().toString();
                        newFollowerAdapter.followerIds.add(userId);
                        // clear value from text field
                        AutoCompleteTextView followerView = (AutoCompleteTextView) findViewById(R.id.new_follower_name);
                        followerView.setText("");
                        /* add list element to gui */
                        // Get new follower's name
                        String followerName = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
                        // Inflate a new layout row
                        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View newFollowerRow = layoutInflater.inflate(R.layout.new_follower_list_row, null);
                        // fill in any details dynamically here
                        TextView textView = (TextView) newFollowerRow.findViewById(R.id.follower_name_row);
                        textView.setText(followerName);
                        TextView idTextView = (TextView) newFollowerRow.findViewById(R.id.follower_id);
                        idTextView.setText(userId);
                        idTextView.setVisibility(View.GONE);
                        // add listener for button to remove the follower
                        ImageButton removeFollowerBtn = (ImageButton) newFollowerRow.findViewById(R.id.remove_follower_row);
                        removeFollowerBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Remove follower from list
                                RelativeLayout removedRow = (RelativeLayout) v.getParent();
                                String removedFollowerId = ((TextView)removedRow.findViewById(R.id.follower_id)).getText().toString();
                                ((LinearLayout)removedRow.getParent()).removeView(removedRow);
                                // Remove follower from maintained array
                                newFollowerAdapter.followerIds.remove(removedFollowerId);
                            }
                        });
                        // insert into LinearLayout
                        ViewGroup followersList = (ViewGroup) findViewById(R.id.new_followers_list);
                        ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        followersList.addView(newFollowerRow, 0, lparams);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
