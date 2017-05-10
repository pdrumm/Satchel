package com.cse40333.satchel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cse40333.satchel.firebaseNodes.Feed;
import com.cse40333.satchel.firebaseNodes.Item;
import com.cse40333.satchel.firebaseNodes.User;
import com.cse40333.satchel.firebaseNodes.UserItem;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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


public class NewItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Add photo
    private ImageSelector imageSelector;
    private ImageSelector locationImageSelector;

    // Firebase references
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    // Show progress bar
    private Progress progress;

    // Keep track of the followers added
    NewFollowerAdapter newFollowerAdapter;

    // User info
    private String userDisplayName = "";

    // Google Maps
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location myLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);

        // Instantiate Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Add item thumbnail
        addNewImageListener();

        // Add item location
        initLocationSpinner();
        addLocationListener();

        // Submit new item
        addSubmitItemListener();

        // Instantiate Progress bar
        View mNewItemFormView = findViewById(R.id.new_item_form_scroll);
        View mProgressView = findViewById(R.id.new_item_progress);
        progress = new Progress(getApplicationContext(), mNewItemFormView, mProgressView);

        // Initialize Autocomplete field and add listener to it
        initFollowerAutocomplete();

        // Get username from database
        getUserDisplayName();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        imageSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == imageSelector.SELECT_FILE || requestCode == imageSelector.REQUEST_CAMERA)
            imageSelector.onActivityResult(requestCode, resultCode, data);
        else if (requestCode == locationImageSelector.SELECT_FILE || requestCode == locationImageSelector.REQUEST_CAMERA)
            locationImageSelector.onActivityResult(requestCode, resultCode, data);
    }

    private void addSubmitItemListener() {
        View.OnClickListener submitItemClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve form data
                EditText itemNameView = (EditText) findViewById(R.id.item_name);
                String itemNameVal = itemNameView.getText().toString();

                // Check for errors in form
                if (TextUtils.isEmpty(itemNameVal)) {
                    itemNameView.setError("Item name is required");
                    itemNameView.requestFocus();
                    return;
                }

                progress.showProgress(true);

                // Submit new item data to Firebase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                // - items
                DatabaseReference itemsRef = database.getReference("items").push();
                String newItemKey = itemsRef.getKey();
                String thumbnailPath = (imageSelector.imageUri == null ? "default" : newItemKey) + "/thumbnail.jpg";
                String locationType = getLocationType();
                String locationValue = getLocationValue(locationType, newItemKey);
                itemsRef.setValue(new Item(itemNameVal, mAuth.getCurrentUser().getUid(), thumbnailPath, locationType, locationValue, newFollowerAdapter.followerIds));
                // Current user's userItem & feed
                DatabaseReference userItemsRef = database.getReference("userItems").child(mAuth.getCurrentUser().getUid()).child(newItemKey);
                userItemsRef.setValue(new UserItem(itemNameVal, userDisplayName, thumbnailPath, false));
                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();
                DatabaseReference feedRef = database.getReference("feed").child(mAuth.getCurrentUser().getUid()).push();
                feedRef.setValue(new Feed(newItemKey, itemNameVal, thumbnailPath, userDisplayName, ts, "Created"));
                // - Followers userItems & feed
                for (String userId : newFollowerAdapter.followerIds) {
                    // userItems
                    DatabaseReference followerRef = database.getReference("userItems").child(userId).child(newItemKey);
                    followerRef.setValue(new UserItem(itemNameVal, userDisplayName, thumbnailPath, false));
                    // - feed
                    feedRef = database.getReference("feed").child(userId).push();
                    feedRef.setValue(new Feed(newItemKey, itemNameVal, thumbnailPath, userDisplayName, ts, "Shared with you"));
                }
                // - Storage
                //   + location
                if ( locationType.equals(LOCATION_IMAGE) ) {
                    StorageReference locationRef = mStorageRef.child(locationValue);
                    locationRef.putFile(locationImageSelector.imageUri);
                }
                //   + thumbnail
                StorageReference thumbnailRef = mStorageRef.child(thumbnailPath);
                if (imageSelector.imageUri != null) {
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
                } else {
                    // if the user did not add an image thumbnail, then immediately return to prev activity
                    finish();
                }

            }
        };
        // Get FAB and add listener
        FloatingActionButton submitItemFab = (FloatingActionButton) findViewById(R.id.submit_new_item);
        submitItemFab.setOnClickListener(submitItemClick);
    }

    private void addNewImageListener() {
        imageSelector = new ImageSelector(NewItemActivity.this, R.id.item_thumbnail, "thumbnail");
        View.OnClickListener addThumbnailClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelector.selectImage();
            }
        };
        Button addThumbnailBtn = (Button) findViewById(R.id.add_item_thumbnail);
        addThumbnailBtn.setOnClickListener(addThumbnailClick);
    }

    private void initLocationSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.location_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.location_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        // Add onclick listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get different location types
                String[] location_types = getResources().getStringArray(R.array.location_types);
                // Get the selected location type
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                String selected_location = tv.getText().toString();
                // Get the different location input views from the layout
                RelativeLayout location_text = (RelativeLayout) findViewById(R.id.location_type_text);
                RelativeLayout location_image = (RelativeLayout) findViewById(R.id.location_type_image);
                RelativeLayout location_map = (RelativeLayout) findViewById(R.id.location_type_gps);
                // Show/hide appropriate views
                if (selected_location.equals(location_types[0])) {
                    // textual description
                    location_text.setVisibility(View.VISIBLE);
                    location_image.setVisibility(View.GONE);
                    location_map.setVisibility(View.GONE);
                } else if (selected_location.equals(location_types[1])) {
                    // image
                    location_text.setVisibility(View.GONE);
                    location_image.setVisibility(View.VISIBLE);
                    location_map.setVisibility(View.GONE);
                }  else if (selected_location.equals(location_types[2])) {
                    // map
                    location_text.setVisibility(View.GONE);
                    location_image.setVisibility(View.GONE);
                    location_map.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addLocationListener() {
        locationImageSelector = new ImageSelector(NewItemActivity.this, R.id.item_location_image, "location", 3, 4);
        View.OnClickListener addThumbnailClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationImageSelector.selectImage();
            }
        };
        Button addThumbnailBtn = (Button) findViewById(R.id.add_item_location_image);
        addThumbnailBtn.setOnClickListener(addThumbnailClick);
    }

    final private String LOCATION_TEXT = "text";
    final private String LOCATION_IMAGE = "image";
    final private String LOCATION_MAP = "map";

    private String getLocationType() {
        // Get possible location views
        RelativeLayout location_text = (RelativeLayout) findViewById(R.id.location_type_text);
        RelativeLayout location_image = (RelativeLayout) findViewById(R.id.location_type_image);
        RelativeLayout location_map = (RelativeLayout) findViewById(R.id.location_type_gps);
        // determine which view is visible
        if (location_text.getVisibility() == View.VISIBLE) {
            return LOCATION_TEXT;
        } else if (location_image.getVisibility() == View.VISIBLE) {
            return LOCATION_IMAGE;
        } else if (location_map.getVisibility() == View.VISIBLE) {
            return LOCATION_MAP;
        }
        return "none";
    }

    private String getLocationValue(String locationType, String newItemKey) {
        RelativeLayout locationLayout;
        String locationVal = "none";
        switch (locationType) {
            case LOCATION_TEXT:
                locationLayout = (RelativeLayout) findViewById(R.id.location_type_text);
                TextView tv = (TextView) locationLayout.findViewById(R.id.item_location_text);
                locationVal = tv.getText().toString();
                break;
            case LOCATION_IMAGE:
                locationLayout = (RelativeLayout) findViewById(R.id.location_type_image);
                locationVal = (locationImageSelector.imageUri == null ? "default" : newItemKey) + "/location.jpg";
                break;
            case LOCATION_MAP:
                locationLayout = (RelativeLayout) findViewById(R.id.location_type_gps);
                break;
        }
        return locationVal;
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

    private void getUserDisplayName() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference usersRef = database.getReference("users").child(mAuth.getCurrentUser().getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 userDisplayName = dataSnapshot.getValue(User.class).displayName;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Google Maps
    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;

//        enableMyLocation();
//        getMyLocation();
        LatLng nd = new LatLng(41.703119, -86.238992); //Dome Coords
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nd,13.0f));
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission to access the location is missing
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
/*            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();*/
            mGoogleApiClient.connect();
//            mMap.setMyLocationEnabled(true);
        }
    }

    public void getMyLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
//            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //TODO Ask Kris what this line is for
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
        }
    }

}
