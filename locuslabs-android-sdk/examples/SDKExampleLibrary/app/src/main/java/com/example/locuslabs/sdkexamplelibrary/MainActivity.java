package com.example.locuslabs.sdkexamplelibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.pm.ActivityInfo;

import com.locuslabs.sdk.configuration.LocusLabs;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    public static final String ACCOUNT_ID = "A11F4Y6SZRXH4X";
    private static final String VENUE_ID = "sea";
    private ListView listView;
    private List<String> activityList;
    private ActivityInfo[] list;

    // The Map Pack is expected to be a tar.gz file.
    // However, the name should be provided without the 'gz' extension.
    // The file must be placed in the Android assets/locuslabs directory.
    // IF the String MAP_PACK is null, the MapPackFinder will detect MapPacks based off the ACCOUNT_ID.
    private static final String MAP_PACK = null; //"android-A11F4Y6SZRXH4X-2015-09-04T22-02-48.tar";

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
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, MY_ID);
        }

        setContentView(R.layout.activity_main);
    }

    protected void onResume() {
        super.onResume();
        createActivitiesList();
    }

    protected void createActivitiesList() {
        LocusLabs.setLogLevel(LocusLabs.LogLevelDebug);
        // Initiate the Activity ExampleFlightStatus.
        // Display the given Map and show sample Flight status Markers.
        LocusLabs.initialize(MainActivity.this.getApplicationContext(), ACCOUNT_ID, new LocusLabs.OnReadyListener() {
            public void onReady() {

                listView = (ListView) findViewById(R.id.listView);

                try {
                    list = MainActivity.this.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities;
                    activityList = new ArrayList<String>();
                    for (int i = 1; i < list.length; i++) {
                        activityList.add((list[i].nonLocalizedLabel).toString());

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setClassName(getApplicationContext(), list[position + 1].name);
                                    intent.putExtra("venueId", VENUE_ID);
                                    startActivity(intent);
                                }catch(NullPointerException error){
                                    Log.e("Log",error.getMessage());
                                }
                            }
                        });
                    }
                } catch(PackageManager.NameNotFoundException error){
                    Log.i("NameNotFoundException",error.getMessage());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, activityList);

                listView.setAdapter(adapter);
            }
        });
    }

}
