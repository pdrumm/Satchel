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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    ArrayList<String> followerIds;
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

        // Add new follower
        View.OnClickListener newFollowerClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get new follower's name
                AutoCompleteTextView followerView = (AutoCompleteTextView) findViewById(R.id.new_follower_name);
                String followerName =  followerView.getText().toString();
                // Inflate a new layout row
                LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View newFollowerRow = layoutInflater.inflate(R.layout.new_follower_list_row, null);
                // fill in any details dynamically here
                TextView textView = (TextView) newFollowerRow.findViewById(R.id.follower_name_row);
                textView.setText(followerName);
                // insert into LinearLayout
                ViewGroup followersList = (ViewGroup) findViewById(R.id.new_followers_list);
                ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                followersList.addView(newFollowerRow, 0, lparams);
            }
        };
        Button newFollowerBtn = (Button) findViewById(R.id.add_new_follower);
        newFollowerBtn.setOnClickListener(newFollowerClick);

        // Autocomplete
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        //Create a new ArrayAdapter with your context and the simple layout for the dropdown menu provided by Android
        final ArrayList<String[]> users = new ArrayList<>();
        //Child the root before all the push() keys are found and add a ValueEventListener()
        database.child("users").orderByChild("displayName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Basically, this says "For each DataSnapshot *Data* in dataSnapshot, do what's inside the method.
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                    //Get the suggestion by childing the key of the string you want to get.
                    String uid = suggestionSnapshot.getKey();
                    String displayName = suggestionSnapshot.child("displayName").getValue(String.class);
                    String email = suggestionSnapshot.child("email").getValue(String.class);
                    //Add the retrieved string to the list
                    String[] user = {uid, displayName, email};
                    users.add(user);
                }
                newFollowerAdapter = new NewFollowerAdapter(getApplicationContext(), android.R.layout.simple_list_item_2, users);
                AutoCompleteTextView ACTV = (AutoCompleteTextView) findViewById(R.id.new_follower_name);
                ACTV.setAdapter(newFollowerAdapter);
                followerIds = new ArrayList<String>(); // instantiate
                ACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String userId = ((TextView)view.findViewWithTag("userId")).getText().toString();
                        followerIds.add(userId);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                itemsRef.setValue(new Item(itemNameVal, mAuth.getCurrentUser().getUid(), thumbnailPath, "[location of the item]", followerIds));
                // - userItems
                //   + curr user
                DatabaseReference userItemsRef = database.getReference("userItems").child(mAuth.getCurrentUser().getUid()).child(newItemKey);
                userItemsRef.setValue(new UserItem(itemNameVal, mAuth.getCurrentUser().getDisplayName(), thumbnailPath, false));
                //   + followers
                for (String userId : followerIds) {
                    DatabaseReference followerRef = database.getReference("userItems").child(userId).child(newItemKey);
                    followerRef.setValue(new UserItem(itemNameVal, mAuth.getCurrentUser().getDisplayName(), thumbnailPath, false));
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

}
