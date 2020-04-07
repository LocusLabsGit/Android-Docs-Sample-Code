package myco.com.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

/**
 * Created by juankruger on 13/03/18.
 */

public class MapActivity extends Activity {

    // Var
    private VenueDatabase   venueDatabase;
    private MapView         mapView;
    private String          venueCode;

    // *************
    // LIFECYCLE
    // *************

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        venueCode = intent.getStringExtra("VenueCode");
        initializeLocusLabsDatabase();
    }

    @Override
    protected void onDestroy() {

        if (venueDatabase != null) {

            venueDatabase.close();
            venueDatabase = null;
        }

        if (mapView != null) {

            mapView.close();
            mapView = null;
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (mapView != null && !mapView.onBackPressed()) super.onBackPressed();
    }

    // *************
    // CUSTOM METHODS
    // *************

    private void initializeLocusLabsDatabase() {

        LocusLabs.registerOnReadyListener(new LocusLabs.OnReadyListener() {
            @Override
            public void onReady() {

                venueDatabase = new VenueDatabase();
                loadVenueAndMap(venueCode, "");
            }
        });
    }

    private void loadVenueAndMap(final String venueId, final String venueName) {

        final RelativeLayout rl = new RelativeLayout(this);

        VenueDatabase.OnLoadVenueAndMapListeners listeners = new VenueDatabase.OnLoadVenueAndMapListeners();
        listeners.loadedInitialViewListener = new VenueDatabase.OnLoadedInitialViewListener() {
            @Override
            public void onLoadedInitialView(View view) {

                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {

                    parent.removeView(view);
                }

                view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                rl.addView(view);
                setContentView(rl);
                venueDatabase.resumeLoadVenueAndMap();
            }
        };

        listeners.loadCompletedListener = new VenueDatabase.OnLoadCompletedListener() {
            @Override
            public void onLoadCompleted(Venue venue, Map map, MapView _mapView, Floor floor, Marker marker) {

                mapView = _mapView;
                mapView.setPositioningEnabled(true);

                // Inform the SDK which activity will handle certain actions like showing error messages, opening pdfs etc. from selected POIs
                mapView.setOnSupplyCurrentActivityListener(new MapView.OnSupplyCurrentActivityListener() {
                    @Override
                    public Activity onSupplyCurrentActivity() {

                        return MapActivity.this;
                    }
                });
            }
        };

        listeners.loadFailedListener = new VenueDatabase.OnLoadFailedListener() {
            @Override
            public void onLoadFailed(String s) {

                // Handle errors here
            }
        };

        // The second parameter is an optional initial search term
        venueDatabase.loadVenueAndMap(venueId, null, listeners);
    }
}