package com.example.locuslabs.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.locuslabs.sdk.configuration.LocusLabs;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final String ACCOUNT_ID = "A11F4Y6SZRXH4X";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onResume() {
        super.onResume();

        showVenuesList();
    }

    protected void showVenuesList() {
        LocusLabs.setLogLevel(LocusLabs.LogLevelDebug);
        // Now that the map pack is installed load the map.
        LocusLabs.initialize(MainActivity.this.getApplicationContext(), ACCOUNT_ID, new LocusLabs.OnReadyListener() {
            public void onReady() {
                Intent intent = new Intent(getApplicationContext(), VenueListActivity.class);
                startActivity(intent);
            }
        });
    }
}
