package com.example.locuslabs.recommendedimplementation;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.locuslabs.sdk.maps.model.Airport;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.Beacon;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.UserPositionManager;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.List;

/**
 * Sample Activity that will render the specified venue.
 */
public class MapActivity extends Activity {

    private Airport airport = null;
    private MapView mapView = null;
    private AirportDatabase airportDatabase = null;
    private ProgressDialog progressDialog;
    protected static final String PROGRESS_MESSAGE = "Downloading map...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent receivedIntent = getIntent();
        String venueId = receivedIntent.getStringExtra("venueId");

        //Create an AirportDatabase which allows airports to be loaded.
        airportDatabase = new AirportDatabase();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(PROGRESS_MESSAGE);
        progressDialog.setTitle(venueId.toUpperCase());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setProgress(0);
        progressDialog.show();

        //Load the Airport specified by the venueId passed to the activity.
        loadAirport(venueId);
    }

    @Override
    public void onDestroy() {
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

    @Override
    public void onBackPressed() {
        if ( mapView == null || !mapView.onBackPressed() ) {
            super.onBackPressed();
        }
    }

    private void loadAirport(final String venueId) {
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
                progressDialog.dismiss();

                airport = _airport;
                mapView = _mapView;

                // turn on positioning
                mapView.setPositioningEnabled(airport, true);

                mapView.setOnSupplyCurrentActivityListener(new MapView.OnSupplyCurrentActivityListener() {
                    @Override
                    public Activity onSupplyCurrentActivity() {
                        return MapActivity.this;
                    }
                });

            }
        };

        listeners.loadProgressListener = new AirportDatabase.OnLoadProgressListener() {
            @Override
            public void onLoadProgress(Integer percentComplete) {
                progressDialog.setMessage(MapActivity.PROGRESS_MESSAGE + percentComplete + "%");
                progressDialog.setIndeterminate(false);
                progressDialog.setProgress(percentComplete);
            }
        };

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        airportDatabase.loadAirportAndMap(venueId, "gate:a5", listeners);
    }
}
