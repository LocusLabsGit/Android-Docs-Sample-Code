package myco.com.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.javascriptintegration.JavaScriptEnvironment;
import com.locuslabs.sdk.javascriptintegration.JavaScriptProxyObject;
import com.locuslabs.sdk.maps.implementation.DefaultVenue;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.LatLng;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.ArrayList;

/**
 * Created by juankruger on 13/03/18.
 */

public class MapActivity extends Activity {

    // Static
    private static final int PERMISSIONS_REQUEST_CODE = 1000;

    // Var
    private VenueDatabase   venueDatabase;
    private MapView         mapView;

    // *************
    // LIFECYCLE
    // *************

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Individual permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSIONS_REQUEST_CODE);
        }
        // Global permissions (Android versions prior to m)
        else {

            initializeLocusLabsDatabase();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {

            // Assume all permissions were granted (in practice you would need to check each permission)
            if (grantResults.length > 0) {

                initializeLocusLabsDatabase();
            }
        }
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
                loadVenueAndMap("lax", "name of the venue you want to appear");
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
            public void onLoadCompleted(final Venue _venue, Map _map, final MapView _mapView, Floor floor, Marker marker) {

                mapView = _mapView;

                // Inform the SDK which activity will handle certain actions like showing error messages, opening pdfs etc. from selected POIs
                mapView.setOnSupplyCurrentActivityListener(new MapView.OnSupplyCurrentActivityListener() {
                    @Override
                    public Activity onSupplyCurrentActivity() {

                        return MapActivity.this;
                    }
                });

                // Simulate the user's position by setting some points on the map for the user's route
                mapView.setOnReadyListener(new MapView.OnReadyListener() {
                    @Override
                    public void onReady() {

                        // Center and zoom the map
                        LatLng newLatlng = new LatLng(33.941232, -118.401861);
                        Position newMapCenter = new Position(null, null, null, null, null, null, newLatlng, 0.0, 0.0);
                        mapView.setCenterPosition(newMapCenter);
                        mapView.setRadius(70.0);

                        final ArrayList<Position> positions = new ArrayList<>();
                        for (int i = 0; i < 10; i++) {

                            LatLng latLon = null;
                            if (i == 0) latLon = new LatLng(33.941232, -118.401861);
                            if (i == 1) latLon = new LatLng(33.941206, -118.401866);
                            if (i == 2) latLon = new LatLng(33.941132, -118.401884);
                            if (i == 3) latLon = new LatLng(33.941095, -118.401882);
                            if (i == 4) latLon = new LatLng(33.941086, -118.401876);
                            if (i == 5) latLon = new LatLng(33.940992, -118.401823);
                            if (i == 6) latLon = new LatLng(33.940971, -118.401815);
                            if (i == 7) latLon = new LatLng(33.940923, -118.401800);
                            if (i == 8) latLon = new LatLng(33.940904, -118.401794);
                            if (i == 9) latLon = new LatLng(33.940885, -118.401778);
                            Position newPosition = new Position(null,"lax","lax-south","lax-south-departures",null,0,latLon,0.0,0.0);
                            positions.add(newPosition);
                        }

                        AndroidUtils utils = new AndroidUtils(((DefaultVenue) _venue).getJavaScriptEnvironment());
                        utils.simulateWalking(mapView, positions, true);
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