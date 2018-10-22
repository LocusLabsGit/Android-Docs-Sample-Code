package com.example.locuslabs.testapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.configuration.Logger;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.model.VenueInfo;

import java.util.ArrayList;

public class VenueListActivity extends Activity {
    private static final String TAG = "VenueListActivity";

    private ListView listView;
    private Context context;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_venue_list);
        listView = findViewById(R.id.listView);
        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // MY_ID is an app-defined int constant. The callback method gets the result of the request.
            int MY_ID = 1;

            /*
               See also app/src/main/AndroidManifest.xml for explanations of why each of these permissions is requested

               Some of these needed for IndoorAtlas (see https://docs.indooratlas.com/develop/android/sdk-setup/)
             */

            requestPermissions(new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
            }, MY_ID);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VenueInfo venueInfo = (VenueInfo) parent.getItemAtPosition(position);

                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("venueId", venueInfo.getVenueId());
                startActivity(intent);
            }
        });

        LocusLabs.registerOnReadyListener(new LocusLabs.OnReadyListener() {
            @Override
            public void onReady() {
                // Two API Choices are illustrated below, the second uses locale
                //listVenues();
                listVenuesForLocale("en");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Handle if any of the permissions are denied, in grantResults
        for (int grantResult : grantResults) {
            if (PackageManager.PERMISSION_GRANTED != grantResult) {
                Logger.warning(TAG, "Positioning may not work because user did not grant all needed permissions");
            }
        }
    }

    private void listVenues() {
        final VenueDatabase venueDatabase = new VenueDatabase();
        venueDatabase.listVenues(new VenueDatabase.OnListVenuesWithErrorListener() {

            @Override
            public void onListVenues(final VenueInfo[] venueInfos, final String errorMessage) {
                if ( errorMessage != null ) {
                    Toast.makeText( VenueListActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
                else {
                    updateListView(venueInfos);
                    venueDatabase.close();
                }
            }
        });
    }

    private void listVenuesForLocale(final String locale) {
        final VenueDatabase venueDatabase = new VenueDatabase();
        venueDatabase.listVenues(new VenueDatabase.OnListVenuesWithErrorListener() {

            @Override
            public void onListVenues(final VenueInfo[] venueInfos, final String errorMessage) {
                if ( errorMessage != null ) {
                    Toast.makeText( VenueListActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
                else {
                    ArrayList<VenueInfo> venueInfosFiltered = new ArrayList<>();
                    for (int i =0; i < venueInfos.length;i++){
                        if(locale.equalsIgnoreCase(venueInfos[i].getLocale().toString())) venueInfosFiltered.add(venueInfos[i]);
                    }
                    updateListView(venueInfosFiltered.toArray(new VenueInfo[venueInfosFiltered.size()]));
                    venueDatabase.close();
                }
            }
        });
    }

    private void updateListView(final VenueInfo[] venueInfos) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<VenueInfo> adapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_list_item_1, android.R.id.text1, venueInfos);
                listView.setAdapter(adapter);
            }
        });
    }

}
