package com.cse40333.satchel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.cse40333.satchel.firebaseNodes.Feed;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    View rootView;

    // Firebase references
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;

    public FeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Instantiate Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_feed, container, false);

        // Populate the Feed list
        DatabaseReference ref = mDatabase.getReference("feed").child(mAuth.getCurrentUser().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    addPopulateListViewListener();
                } else {
                    rootView.findViewById(R.id.feed_welcome_text).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Add listeners to each item in list
        addFeedListListener();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void addPopulateListViewListener() {
        // Create an adapter for the list of items and attach it to the ListView
        ListView itemListView = (ListView) rootView.findViewById(R.id.feed_list_list_view);
        Query mRef = mDatabase.getReference("feed").child(mAuth.getCurrentUser().getUid()).orderByChild("timeStamp");
        ListAdapter listAdapter = new FirebaseListAdapter<Feed>(getActivity(), Feed.class, R.layout.feed_list_row, mRef) {
            protected void populateView(View view, Feed feed, int position) {
                // Hide welcome message
//                rootView.findViewById(R.id.feed_welcome_text).setVisibility(View.GONE);
                // Get the thumbnail
                final View listView = view;
                try {
                    StorageReference thumbnailRef = mStorageRef.child(feed.itemThumbnailPath);
                    final File localFile = File.createTempFile("thumbnail", "jpg");
                    thumbnailRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file
                            ImageView itemThumbnail = (ImageView) listView.findViewById(R.id.item_image);
                            itemThumbnail.setImageBitmap(decodeFile(localFile, 50, 50));
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
                view.setTag(feed.itemId);
                // Set text fields
                TextView itemName = (TextView) view.findViewById(R.id.item_name);
                TextView feedTimestamp = (TextView) view.findViewById(R.id.feed_timestamp);
                TextView feedMessage = (TextView) view.findViewById(R.id.feed_message);
                TextView feedUser = (TextView) view.findViewById(R.id.feed_user);
                itemName.setText(feed.itemName);
                feedTimestamp.setText(getDate(Long.parseLong(feed.timestamp)));
                feedMessage.setText(feed.message);
                feedUser.setText(feed.userName);
            }

            @Override
            public Feed getItem(int pos) {
                return super.getItem(getCount() - 1 - pos);
            }
        };
        itemListView.setAdapter(listAdapter);
    }

    private String getDate(long timeStamp){
        try{
            // Get curr year
            DateFormat sdf = new SimpleDateFormat("yyyy");
            Date netDate = (new Date());
            String currYear = sdf.format(netDate);
            // Get year of timestamp
            sdf = new SimpleDateFormat("yyyy");
            netDate = (new Date(timeStamp));
            String tsYear = sdf.format(netDate);

            if (currYear.equals(tsYear)) {
                sdf = new SimpleDateFormat("MMM d - h:mm a");
            } else {
                sdf = new SimpleDateFormat("MMM d, yyyy - h:mm a");
            }
            netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    private void addFeedListListener() {
        // Create an event listener for each row in the schedule
        ListView feedListView = (ListView) rootView.findViewById(R.id.feed_list_list_view);
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ItemDetailActivity.class);
                intent.putExtra("itemId", view.getTag().toString());
                startActivity(intent);
            }
        };
        feedListView.setOnItemClickListener(itemClickListener);
    }

    public static Bitmap decodeFile(File f, int WIDTH, int HIGHT){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_WIDTH=WIDTH;
            final int REQUIRED_HIGHT=HIGHT;
            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_WIDTH && o.outHeight/scale/2>=REQUIRED_HIGHT)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

}
