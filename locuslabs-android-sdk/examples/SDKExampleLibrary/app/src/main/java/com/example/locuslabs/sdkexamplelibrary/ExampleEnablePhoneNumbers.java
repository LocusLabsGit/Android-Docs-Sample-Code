package com.example.locuslabs.sdkexamplelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.maps.model.Airport;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.view.MapView;

/*
* This activity loads a map then shows a POI with an Id of
* 126. The user can than tap a phone number displayed at
* the bottom of the POI Card View to start an Intent.ACTION_DIAL Activity.
* */
public class ExampleEnablePhoneNumbers extends Activity {
    private static final String TAG = "ExampleEnablePhoneNumbers";

    private AirportDatabase airportDatabase;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This activity takes a venueId parameter. The venueId represents the Airport to be loaded.
        Intent receivedIntent = getIntent();
        String venueId = receivedIntent.getStringExtra("venueId");

        //Create an AirportDatabase which allows airports to be loaded.
        airportDatabase = new AirportDatabase();

        //Load the Airport specified by the venueId passed to the activity.
        loadAirport(venueId);
    }

    @Override
    public void onBackPressed() {
        if ( mapView == null || !mapView.onBackPressed() ) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //-----------------------------------
        // Be sure to close the mapView and
        // airportDatabase to release the memory
        // they consume.
        //-----------------------------------

        if ( mapView != null ) {
            mapView.close();
        }

        if ( airportDatabase != null ) {
            airportDatabase.close();
        }

        airportDatabase = null;
        mapView = null;
    }

    private void loadAirport(String venueId) {
        final RelativeLayout rl = new RelativeLayout( this );

        AirportDatabase.OnLoadAirportAndMapListeners listeners = new AirportDatabase.OnLoadAirportAndMapListeners();
        listeners.loadedInitialViewListener = new AirportDatabase.OnLoadedInitialViewListener() {
            @Override public void onLoadedInitialView(View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }

                view.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                rl.addView(view);
                setContentView(rl);
                airportDatabase.resumeLoadAirportAndMap();
            }
        };
        listeners.loadCompletedListener = new AirportDatabase.OnLoadCompletedListener() {

            @Override public void onLoadCompleted(Airport _airport, Map _map, final MapView _mapView,
                                                  Floor floor, Marker marker) {
                mapView = _mapView;
                //Set the MapView's OnPoiPhoneClickedListener before showing the POI
                setupPhoneNumberClickedListener();
                // Renders the POI Card View for a POI with a Id of 126.
                mapView.showPoiPopup("126");
            }
        };

        airportDatabase.loadAirportAndMap(venueId, null, listeners);
    }

    // Construct a listener with a callback. This callback starts a new Activity, setting up the user's phone to make a call or add the phone number to their contacts.
    private void setupPhoneNumberClickedListener() {
        mapView.setOnPoiPhoneClickedListener(new MapView.OnPoiPhoneClickedListener() {
            @Override
            public void onPoiPhoneClicked(String phone) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                ExampleEnablePhoneNumbers.this.startActivity(intent);
            }
        });
    }
}