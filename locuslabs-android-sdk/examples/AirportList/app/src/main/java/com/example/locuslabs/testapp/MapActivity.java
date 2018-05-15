package com.example.locuslabs.testapp;

import android.app.Activity;
import android.content.Intent;
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

/**
 * Sample Activity that will render the specified venue.
 */
public class MapActivity extends Activity {

    private AirportDatabase airportDatabase;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent receivedIntent = getIntent();

        //Takes the String venueId to determine what venue needs to be used for loadMap
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
        final RelativeLayout rl = new RelativeLayout(this);

        AirportDatabase.OnLoadAirportAndMapListeners listeners = new AirportDatabase.OnLoadAirportAndMapListeners();
        listeners.loadedInitialViewListener = new AirportDatabase.OnLoadedInitialViewListener() {
            @Override
            public void onLoadedInitialView(View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }

                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                rl.addView(view);
                setContentView(rl);
                airportDatabase.resumeLoadAirportAndMap();
            }
        };
        listeners.loadCompletedListener = new AirportDatabase.OnLoadCompletedListener() {
            @Override
            public void onLoadCompleted(Airport _airport, Map _map, final MapView _mapView, Floor floor, Marker marker) {
                mapView = _mapView;
                mapView.setPositioningEnabled(_airport, true);
            }
        };

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        String initialSearch;
        if ("sea".equalsIgnoreCase(venueId))        initialSearch = "Gate A5";
        else if ("lax".equalsIgnoreCase(venueId))   initialSearch = "Gate 80";
        else                                        initialSearch = null;
        airportDatabase.loadAirportAndMap(venueId, initialSearch, listeners);
    }
}
