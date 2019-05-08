package com.example.locuslabs.sdkexamplelibrary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
* This activity loads a map then shows a POI with an Id of 1.
* The user can than tap the URL address displayed at the bottom of
* the POI Card View to initiate a open their web browser to the URL
* address.
* */
public class ExampleEnableUrls extends Activity {
    private static final String TAG = "ExampleEnableUrls";

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
                        return ExampleEnableUrls.this;
                    }
                });

                // Set the MapView's OnPoiUrlClickedListener before showing the POI
                setupUrlClickedListener();
                // Renders the POI Card View for a POI with a particular poiID.
                mapView.showPoiPopup("15"); // Starbucks "Satellite N - Gate N9" on level "Level 5 - Concourse (Main Terminal)"
            }
        };

        venueDatabase.loadVenueAndMap(venueId, null, listeners);
    }

    // Construct a listener from the MapView class with a callback. This callback starts a new Activity, opening the URL Address in the native browser.
    private void setupUrlClickedListener() {
        mapView.setOnPoiUrlClickedListener(new MapView.OnPoiUrlClickedListener() {
            @Override
            public void onPoiUrlClicked(String url) {
                // String url could be something like -> "http://www.google.com"
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                ExampleEnableUrls.this.startActivity(intent);
            }
        });
    }
}