package com.cse40333.satchel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

/*
In order for the ImageSelector to work properly, you must override the
onActivityResult and onRequestPermissionsResult of the current Activity
as follows:

    ImageSelector imageSelector;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        imageSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageSelector.onActivityResult(requestCode, resultCode, data);
    }
 */
public class ImageSelector {

    public int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private File OUTPUT;
    private String AUTHORITY = "com.cse40333.satchel.fileprovider";
    private String userChoosenTask;
    private Activity activity;
    private int imageViewId;
    public Uri imageUri;
    private String tempFileName = "default";

    /*
    Args:
     - activity: the current Activity (used for context)
     - imageViewId: the R.id of the ImageView to update with the new image
     */
    public ImageSelector(Activity activity, int imageViewId, String tempFileName) {
        this.activity = activity;
        this.imageViewId = imageViewId;
        this.tempFileName = tempFileName;
    }
    public ImageSelector(Activity activity, int imageViewId, String tempFileName, int request_camera, int select_file) {
        this.activity = activity;
        this.imageViewId = imageViewId;
        this.tempFileName = tempFileName;
        this.REQUEST_CAMERA = request_camera;
        this.SELECT_FILE = select_file;
    }

    // ~@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    // ~@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult();
            } else if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
        }
    }

    public void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(activity);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result){}
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result){}
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        String filename = this.tempFileName + ".jpg";
        File imagePath = new File(activity.getApplicationContext().getFilesDir(), "images");
        if (!imagePath.exists()) imagePath.mkdirs();
        OUTPUT = new File(imagePath, filename);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        final Uri outputUri = FileProvider.getUriForFile(activity.getApplicationContext(), AUTHORITY, OUTPUT);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        activity.getApplicationContext().grantUriPermission(
                "com.google.android.GoogleCamera",
                outputUri,
                FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_READ_URI_PERMISSION
        );
        cameraIntent.setFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void onCaptureImageResult() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        final Uri outputUri = FileProvider.getUriForFile(activity, AUTHORITY, OUTPUT);
        ImageView imgView = (ImageView) activity.findViewById(this.imageViewId);
        imgView.setImageURI(outputUri);
        this.imageUri = outputUri;
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(activity.getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ImageView imgView = (ImageView) activity.findViewById(this.imageViewId);
        imgView.setImageBitmap(bm);
        this.imageUri = data.getData();
    }
}
