package com.example.locuslabs.sdkexamplelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

/*
* This Activity loads a Map of the given Venue then
* sets SearchMode to true and displays red Markers
* for every Starbucks Coffee Shop found in the given
* Venue.
* */
public class ExampleSearchResultsOnLoad extends Activity {
    private static final String TAG = "ExampleFlightStatus";

    private VenueDatabase venueDatabase;
    private MapView mapView;

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

                mapView.setOnSupplyCurrentActivityListener(new MapView.OnSupplyCurrentActivityListener() {
                    @Override
                    public Activity onSupplyCurrentActivity() {
                        return ExampleSearchResultsOnLoad.this;
                    }
                });

                showSearchResults();
            }
        };

        venueDatabase.loadVenueAndMap(venueId, null, listeners);
    }

    private void showSearchResults() {
        mapView.showSearchResults("Starbucks");
    }

}
