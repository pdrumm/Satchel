package com.cse40333.satchel;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.cse40333.satchel.firebaseNodes.UsersItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NewItemActivity extends AppCompatActivity {

    // Firebase references
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        mAuth = FirebaseAuth.getInstance();

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
                Map<String, String> itemInfo = new HashMap<String, String>();
                itemInfo.put("name", itemNameVal);
                itemInfo.put("owner", mAuth.getCurrentUser().getUid());
                itemsRef.setValue(itemInfo);
                // - userItems
                DatabaseReference userItemsRef = database.getReference("userItems").child(mAuth.getCurrentUser().getUid()).child(itemsRef.getKey());
                userItemsRef.setValue(new UsersItem(itemNameVal, mAuth.getCurrentUser().getDisplayName(), "path", false));
                // Return to Items list
                finish();
            }
        };
        FloatingActionButton submitItemFab = (FloatingActionButton) findViewById(R.id.submit_new_item);
        submitItemFab.setOnClickListener(submitItemClick);
    }
}
