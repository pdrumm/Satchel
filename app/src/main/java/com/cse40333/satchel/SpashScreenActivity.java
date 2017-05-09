package com.cse40333.satchel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SpashScreenActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();

    // System preferences
    final String PREFS_NAME = "MyPrefsFile";

    // Firebase references
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private boolean isLoggedIn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);

        setupFirebase();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);

                    Intent intent;
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    if (settings.getBoolean("my_first_time", true)) {
                        intent = new Intent(getApplicationContext(), SignUpActivity.class);
                    } else if (isLoggedIn) {
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                    } else {
                        intent = new Intent(getApplicationContext(), LoginActivity.class);
                    }
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    isLoggedIn = true;

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    if (settings.getBoolean("my_first_time", true)) {
                        // record the fact that the app has been started at least once
                        settings.edit().putBoolean("my_first_time", false).commit();
                    }
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
