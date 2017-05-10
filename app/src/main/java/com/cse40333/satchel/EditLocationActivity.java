package com.cse40333.satchel;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cse40333.satchel.firebaseNodes.Feed;
import com.cse40333.satchel.firebaseNodes.Item;
import com.cse40333.satchel.firebaseNodes.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditLocationActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ImageSelector locationImageSelector;
    String itemId;
    private Long ts;
    private String userDisplayName;

    // Show progress bar
    private Progress progress;

    // Google Maps
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 900;
    private Location myLocation;
    private LocationRequest mLocationRequest;
    private boolean mPermissionDenied = false;
    private LatLng itemMapLocation;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        mAuth = FirebaseAuth.getInstance();
        itemId = getIntent().getStringExtra("itemId");
        getUserDisplayName();

        // Initialize location spinner
        initLocationSpinner();

        // Initialize location listener
        addLocationImageListener();

        // Instantiate Progress bar
        View mNewItemFormView = findViewById(R.id.add_new_location_linear_layout);
        View mProgressView = findViewById(R.id.new_item_progress);
        progress = new Progress(getApplicationContext(), mNewItemFormView, mProgressView);

        // Start Google maps
        enableMyLocation();

        // add submit listener
        addSubmitLocationListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationImageSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == locationImageSelector.SELECT_FILE || requestCode == locationImageSelector.REQUEST_CAMERA)
            locationImageSelector.onActivityResult(requestCode, resultCode, data);
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

    private void addLocationImageListener() {
        locationImageSelector = new ImageSelector(EditLocationActivity.this, R.id.item_location_image, "location", 3, 4);
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
                if (itemMapLocation == null) {
                    locationVal = null;
                } else {
                    locationVal = itemMapLocation.latitude + "," + itemMapLocation.longitude;
                }
                break;
        }
        return locationVal;
    }

    private void addSubmitLocationListener() {

        View.OnClickListener submitItemClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve form data
                progress.showProgress(true);

                // hashmap to push
                HashMap<String, Object> hm = new HashMap<>();

                // Submit new item data to Firebase
                database = FirebaseDatabase.getInstance();
                // - items
                DatabaseReference itemsRef = database.getReference("items").child(itemId);
                String locationType = getLocationType();
                String locationValue = getLocationValue(locationType, itemId);
                hm.put("locationType",locationType);
                hm.put("locationValue",locationValue);
                itemsRef.updateChildren(hm);

                //Add Feed Item
                DatabaseReference followerRef = database.getReference("items").child(itemId);
                ts = System.currentTimeMillis();
                followerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Item item = dataSnapshot.getValue(Item.class);

                        if(item.followers != null) {
                            for (String follower : item.followers) {
                                DatabaseReference feedRef = database.getReference("feed")
                                        .child(follower).push();
                                feedRef.setValue(new Feed(itemId, item.name, item.thumbnailPath,
                                        userDisplayName, ts.toString(), "Location update by"));
                            }
                        }

                        DatabaseReference feedRef = database.getReference("feed")
                                .child(item.ownerId).push();
                        feedRef.setValue(new Feed(itemId, item.name, item.thumbnailPath,
                                userDisplayName, ts.toString(), "Location update by"));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // return to previous activity
                finish();
                // - Storage
                //   + location
                if ( locationType.equals(LOCATION_IMAGE) && locationImageSelector.imageUri != null ) {
                    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference locationRef = mStorageRef.child(locationValue);
                    Log.d("LOCZ", locationValue);
                    Log.d("LOCZ", locationType);
                    locationRef.putFile(locationImageSelector.imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            finish();
                        }
                    });
                } else {
                    // return to previous activity
                    finish();
                }
            }
        };
        // Get FAB and add listener
        FloatingActionButton submitItemFab = (FloatingActionButton) findViewById(R.id.submit_edit_loc);
        submitItemFab.setOnClickListener(submitItemClick);
    }

    /*
     * Google Maps
     */
    // Activity onCreate calls enableMyLocation
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission to access the location is missing
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
//        } else if (mMap != null) {
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            Log.d("MAPZ", "called connect");
        }
    }

    @Override
    protected void onStart() {
        Log.d("MAPZ", "onStart");
        super.onStart();
        enableMyLocation();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("MAPZ", "onConnected?");
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        Log.d("MAPZ", "onConnected");
        getMyLocation();
//        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("TAG", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i("Tag", "Connection Failed");
    }

    public void getMyLocation(){
        Log.d("MAPZ", "gettin my location");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (myLocation != null) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map_frag);
                mapFragment.getMapAsync(this);
                Log.d("MAPZ", "yay");
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        Log.d("MAPZ", "in onMapReady");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        LatLng curCoord = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curCoord, 15.0f));
        setMapListener();
    }

    private void setMapListener() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                itemMapLocation = point;
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));
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

}
