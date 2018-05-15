package com.example.locuslabs.testapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.VenueInfo;
import android.util.Log;

import java.util.ArrayList;

public class AirportListActivity extends Activity {
    private ListView listView;
    private Context context;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent receivedIntent = getIntent();
        final String venueListContents = receivedIntent.getStringExtra("venueListContents");

        setContentView(R.layout.activity_airport_list);
        listView = (ListView) findViewById(R.id.listView);
        context = this;

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
                //listAirports( venueListContents );
                listAirportsForLocale(venueListContents,"en");
            }
        });
    }

    private void listAirports( final String venueListContents ) {
        final AirportDatabase airportDatabase = new AirportDatabase();
        airportDatabase.listAirports(new AirportDatabase.OnListAirportsListener() {
            @Override
            public void onListAirports(final VenueInfo[] venueInfos) {
                updateListView(venueInfos);
                airportDatabase.close();
            }
        }, venueListContents );
    }

    private void listAirportsForLocale( final String venueListContents, final String locale ) {
        final AirportDatabase airportDatabase = new AirportDatabase();
        airportDatabase.listAirports(new AirportDatabase.OnListAirportsListener() {
            @Override
            public void onListAirports(final VenueInfo[] venueInfos) {
                ArrayList<VenueInfo> venueInfosFiltered = new ArrayList<VenueInfo>();
                for (int i =0; i < venueInfos.length;i++){
                    if(locale.equalsIgnoreCase(venueInfos[i].getLocale().toString())) venueInfosFiltered.add(venueInfos[i]);
                }
                updateListView(venueInfosFiltered.toArray(new VenueInfo[venueInfosFiltered.size()]));
                airportDatabase.close();
            }
        }, venueListContents );
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
