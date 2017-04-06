package com.cse40333.satchel;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cse40333.satchel.firebaseNodes.Item;
import com.cse40333.satchel.firebaseNodes.UserItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class NewItemActivity extends AppCompatActivity {

    // Add photo
    private ImageSelector imageSelector;

    // Firebase references
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        mAuth = FirebaseAuth.getInstance();

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
                itemsRef.setValue(new Item(itemNameVal, mAuth.getCurrentUser().getUid(), "[path/to/file]", "[location of the item]"));
                // - userItems
                DatabaseReference userItemsRef = database.getReference("userItems").child(mAuth.getCurrentUser().getUid()).child(itemsRef.getKey());
                userItemsRef.setValue(new UserItem(itemNameVal, mAuth.getCurrentUser().getDisplayName(), "[path/to/file]", false));
                // Return to Items list
                finish();
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
