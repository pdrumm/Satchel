package com.cse40333.satchel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cse40333.satchel.firebaseNodes.UserItem;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class ItemsFragment extends Fragment {

    View rootView;

    // Firebase references
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    public ItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Instantiate Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_items, container, false);

        // Populate the Items list
        addPopulateListViewListener();

        // Add listeners to each item in list
        addItemListListener();

        // Handler for New Item FAB
        addNewItemListener();

        Log.d("USERZ", mAuth.getCurrentUser().getEmail());
//        Log.d("USERZ", mAuth.getCurrentUser().getDisplayName());

        // Inflate the layout for this fragment
        return rootView;
    }

    private void addPopulateListViewListener() {
        // Create an adapter for the list of items and attach it to the ListView
        ListView itemListView = (ListView) rootView.findViewById(R.id.item_list_list_view);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mRef = database.getReference("userItems").child(mAuth.getCurrentUser().getUid());
        ListAdapter listAdapter = new FirebaseListAdapter<UserItem>(getActivity(), UserItem.class, R.layout.item_list_row, mRef) {
            protected void populateView(View view, UserItem item, int position) {
                // Hide welcome message
                rootView.findViewById(R.id.item_welcome_text).setVisibility(View.GONE);
                // Get the thumbnail
                final View listView = view;
                try {
                    StorageReference thumbnailRef = mStorageRef.child(item.thumbnailPath);
                    final File localFile = File.createTempFile("thumbnail", "jpg");
                    thumbnailRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file
                            ImageView itemThumbnail = (ImageView) listView.findViewById(R.id.item_image);
                            itemThumbnail.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failed download
                            ImageView itemThumbnail = (ImageView) listView.findViewById(R.id.item_image);
                            itemThumbnail.setImageResource(R.drawable.ic_add_photo_light);
                        }
                    });
                } catch (IOException e) {
                    // Failed to create new local file
                    ImageView itemThumbnail = (ImageView) listView.findViewById(R.id.item_image);
                    itemThumbnail.setImageResource(R.drawable.ic_add_photo_light);
                }
                // Set hidden field
                view.setTag(getRef(position).getKey());
                // Set text fields
                TextView itemName = (TextView) view.findViewById(R.id.item_name);
                TextView itemOwner = (TextView) view.findViewById(R.id.item_owner);
                itemName.setText(item.name);
                itemOwner.setText(item.ownerName);
            }
        };
        itemListView.setAdapter(listAdapter);
    }

    private void addItemListListener() {
        // Create an event listener for each row in the schedule
        ListView itemListView = (ListView) rootView.findViewById(R.id.item_list_list_view);
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ItemDetailActivity.class);
                intent.putExtra("itemId", view.getTag().toString());
                startActivity(intent);
            }
        };
        itemListView.setOnItemClickListener(itemClickListener);
    }

    private void addNewItemListener() {
        View.OnClickListener newItemClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewItemActivity.class);
                startActivity(intent);
            }
        };
        FloatingActionButton newItemFab = (FloatingActionButton) rootView.findViewById(R.id.add_list_item);
        newItemFab.setOnClickListener(newItemClick);
    }
}
