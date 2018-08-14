package com.example.locuslabs.recommendedimplementation;

import android.app.Activity;
import android.app.ProgressDialog;
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

/**
 * Sample Activity that will render the specified venue.
 */
public class MapActivity extends Activity {

    private MapView mapView = null;
    private VenueDatabase venueDatabase = null;
    private ProgressDialog progressDialog;
    protected static final String PROGRESS_MESSAGE = "Downloading map...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent receivedIntent = getIntent();
        String venueId = receivedIntent.getStringExtra("venueId");

        // Create a VenueDatabase which allows venues to be loaded.
        venueDatabase = new VenueDatabase();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(PROGRESS_MESSAGE);
        progressDialog.setTitle(venueId.toUpperCase());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setProgress(0);
        progressDialog.show();

        //Load the Venue specified by the venueId passed to the activity.
        loadVenue(venueId);
    }

    @Override
    public void onDestroy() {
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

    @Override
    public void onBackPressed() {
        if ( mapView == null || !mapView.onBackPressed() ) {
            super.onBackPressed();
        }
    }

    private void loadVenue(final String venueId) {
        final RelativeLayout rl = new RelativeLayout(this);

        VenueDatabase.OnLoadVenueAndMapListeners listeners = new VenueDatabase.OnLoadVenueAndMapListeners();
        listeners.loadedInitialViewListener = new VenueDatabase.OnLoadedInitialViewListener() {
            @Override
            public void onLoadedInitialView(View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }

                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                rl.addView(view);
                setContentView(rl);
                venueDatabase.resumeLoadVenueAndMap();
            }
        };
        listeners.loadCompletedListener = new VenueDatabase.OnLoadCompletedListener() {
            @Override
            public void onLoadCompleted(Venue _venue, Map _map, final MapView _mapView, Floor floor, Marker marker) {
                progressDialog.dismiss();

                mapView = _mapView;

                // turn on positioning
                mapView.setPositioningEnabled(true);

                mapView.setOnSupplyCurrentActivityListener(new MapView.OnSupplyCurrentActivityListener() {
                    @Override
                    public Activity onSupplyCurrentActivity() {
                        return MapActivity.this;
                    }
                });

            }
        };

        listeners.loadProgressListener = new VenueDatabase.OnLoadProgressListener() {
            @Override
            public void onLoadProgress(Integer percentComplete) {
                progressDialog.setMessage(MapActivity.PROGRESS_MESSAGE + percentComplete + "%");
                progressDialog.setIndeterminate(false);
                progressDialog.setProgress(percentComplete);
            }
        };

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        venueDatabase.loadVenueAndMap(venueId, "gate:a5", listeners);
    }
}
