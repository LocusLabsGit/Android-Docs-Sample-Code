package com.example.locuslabs.recommendedimplementation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.mappacks.LocusLabs_MapPack;
import com.locuslabs.sdk.mappacks.LocusLabs_MapPackFinder;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final String ACCOUNT_ID = "A11F4Y6SZRXH4X";
    private static final String VENUE_ID = "sea";
    private ProguardTestClass proguardTestClass;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // MY_ID is an app-defined int constant. The callback method gets the result of the request.
            int MY_ID = 1;

            /*
               See also app/src/main/AndroidManifest.xml for explanations of why each of these permissions is requested
             */

            requestPermissions(new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED
            }, MY_ID);
        }

        setContentView(R.layout.activity_main);

        LocusLabs_MapPackFinder.installMapPack(getApplicationContext(), ACCOUNT_ID, null, new LocusLabs_MapPack.OnUnpackCallback() {
            public void onUnpack(boolean didInstall, Exception exception) {
                Log.d(TAG, String.valueOf(didInstall));
                if (exception != null) {
                    Log.e(TAG, exception.getMessage());
                }
                //After the Map Pack is unpacked, show flight card information with a LocusLabs Map below the flight card information.
                showFlightCard();
            }
        });

        proguardTestClass = new ProguardTestClass(3);
        Log.d(TAG, "Proguard test: " + proguardTestClass.incrementValue());
    }

    protected void onResume() {
        super.onResume();
    }

    protected void showFlightCard() {
        LocusLabs.setLogLevel(LocusLabs.LogLevelTrace);
        // Now that the map pack is installed load the map.
        LocusLabs.initialize(MainActivity.this.getApplicationContext(), ACCOUNT_ID, new LocusLabs.OnReadyListener() {
            public void onReady() {
                Intent intent = new Intent(getApplicationContext(), FlightCardActivity.class);
                intent.putExtra("venueId", VENUE_ID);

                startActivity(intent);
            }
        });
    }
}
