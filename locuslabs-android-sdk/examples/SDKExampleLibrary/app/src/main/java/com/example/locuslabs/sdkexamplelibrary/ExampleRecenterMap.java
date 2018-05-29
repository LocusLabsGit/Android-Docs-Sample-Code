package com.example.locuslabs.sdkexamplelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.TimerTask;

/*
* This activity loads a map and adds starts a timer that has a callback after 10 seconds
* which renders the MapView to the given venue's default radius and position. The venueCenter
* and venueRadius are both static values persisted in the venue's Venue Data JSON file.
* */
public class ExampleRecenterMap extends Activity {
    private static final String TAG = "ExampleFlightStatus";

    private VenueDatabase venueDatabase;
    private MapView mapView;
    private Venue venue;
    private TimerTask resetMapPeriodically;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This activity takes a venueId parameter. The venueId represents the Venue to be loaded.
        Intent receivedIntent = getIntent();
        String venueId = receivedIntent.getStringExtra("venueId");

        // Create an VenueDatabase which allows venues to be loaded.
        venueDatabase = new VenueDatabase();

        // Load the Venue specified by the venueId passed to the activity.
        loadVenue(venueId);
    }

    @Override
    public void onBackPressed() {
        stopResettingMapPeriodically();
        if ( mapView == null || !mapView.onBackPressed() ) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //-----------------------------------
        // Be sure to close the mapView and
        // venueDatabase to release the memory
        // they consume.
        //-----------------------------------

        if ( mapView != null ) {
            mapView.close();
        }

        if ( venueDatabase != null ) {
            venueDatabase.close();
        }

        venueDatabase = null;
        mapView = null;
    }

    private void loadVenue(String venueId) {
        final RelativeLayout rl = new RelativeLayout( this );

        VenueDatabase.OnLoadVenueAndMapListeners listeners = new VenueDatabase.OnLoadVenueAndMapListeners();
        listeners.loadedInitialViewListener = new VenueDatabase.OnLoadedInitialViewListener() {
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
                venueDatabase.resumeLoadVenueAndMap();
            }
        };
        listeners.loadCompletedListener = new VenueDatabase.OnLoadCompletedListener() {

            @Override public void onLoadCompleted(Venue _venue, Map _map, final MapView _mapView,
                                                  Floor floor, Marker marker) {
                mapView = _mapView;
                venue = _venue;
                startResettingMapPeriodically();
            }
        };

        venueDatabase.loadVenueAndMap(venueId, null, listeners);
    }

    /* After the Java Utilities Timer counts 10,000 milliseconds, the Venue's center Position and the Venue's radius
    *  are used to set the Map's setRadius and Map's setCenterPosition.
    * */
    private void startResettingMapPeriodically() {
        long tenSeconds = 10000;
        resetMapPeriodically = new java.util.TimerTask() {
            public void run() {
                try {
                    Position currentVenueCenter = venue.getVenueCenter();
                    mapView.setCenterPosition(currentVenueCenter);

                    double currentVenueRadius = venue.getVenueRadius();
                    mapView.setRadius(currentVenueRadius);
                }
                catch (Exception e) {
                    Log.e("ExampleRecenterMap", "Error resetting map periodically", e);
                    stopResettingMapPeriodically();
                }
            }
        };
        new java.util.Timer().scheduleAtFixedRate(resetMapPeriodically, tenSeconds, tenSeconds);
    }

    private void stopResettingMapPeriodically() {
        if (null != resetMapPeriodically) {
            resetMapPeriodically.cancel();
            resetMapPeriodically = null;
        }
    }
}
