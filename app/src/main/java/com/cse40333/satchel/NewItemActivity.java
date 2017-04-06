package com.cse40333.satchel;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cse40333.satchel.firebaseNodes.Item;
import com.cse40333.satchel.firebaseNodes.UserItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class NewItemActivity extends AppCompatActivity {

    // Add photo
    private ImageSelector imageSelector;

    // Firebase references
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        // Instantiate Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Add item thumbnail
        View.OnClickListener addThumbnailClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelector = new ImageSelector(NewItemActivity.this, R.id.item_thumbnail);
                imageSelector.selectImage();
            }
        };
        Button addThumbnailBtn = (Button) findViewById(R.id.add_item_thumbnail);
        addThumbnailBtn.setOnClickListener(addThumbnailClick);

        // Submit new item
        View.OnClickListener submitItemClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve form data
                EditText itemName = (EditText) findViewById(R.id.item_name);
                String itemNameVal = itemName.getText().toString();

                // Submit new item data to Firebase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                // - items
                DatabaseReference itemsRef = database.getReference("items").push();
                String newItemKey = itemsRef.getKey();
                String thumbnailPath = (imageSelector.imageUri == null ? "default" : newItemKey) + "/thumbnail.jpg";
                itemsRef.setValue(new Item(itemNameVal, mAuth.getCurrentUser().getUid(), thumbnailPath, "[location of the item]"));
                // - userItems
                DatabaseReference userItemsRef = database.getReference("userItems").child(mAuth.getCurrentUser().getUid()).child(newItemKey);
                userItemsRef.setValue(new UserItem(itemNameVal, mAuth.getCurrentUser().getDisplayName(), thumbnailPath, false));
                // - Storage
                StorageReference thumbnailRef = mStorageRef.child(thumbnailPath);
                thumbnailRef.putFile(imageSelector.imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        // Return to Items list
                        finish();
                    }
                });
            }
        };
        FloatingActionButton submitItemFab = (FloatingActionButton) findViewById(R.id.submit_new_item);
        submitItemFab.setOnClickListener(submitItemClick);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        imageSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageSelector.onActivityResult(requestCode, resultCode, data);
    }
}
