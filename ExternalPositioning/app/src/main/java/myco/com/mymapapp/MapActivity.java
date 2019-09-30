package myco.com.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.LatLng;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.PositioningSensorAlgorithm;
import com.locuslabs.sdk.maps.model.UserPositionManager;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

/**
 * Created by juankruger on 13/03/18.
 */

public class MapActivity extends Activity {

    // Static
    private static final int PERMISSIONS_REQUEST_CODE = 1000;

    // Var
    private VenueDatabase               venueDatabase;
    private MapView                     mapView;
    private PositioningSensorAlgorithm  positioningSensorAlgorithm;
    private Venue                       venue;

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

    private void hideBlueDot() {

        venue.clearPosition();
    }

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
            public void onLoadCompleted(final Venue _venue, Map map, MapView _mapView, Floor floor, Marker marker) {

                mapView = _mapView;
                venue = _venue;

                // Inform the SDK which activity will handle certain actions like showing error messages, opening pdfs etc. from selected POIs
                mapView.setOnSupplyCurrentActivityListener(new MapView.OnSupplyCurrentActivityListener() {
                    @Override
                    public Activity onSupplyCurrentActivity() {

                        return MapActivity.this;
                    }
                });

                mapView.setOnReadyListener(new MapView.OnReadyListener() {
                    @Override
                    public void onReady() {

                        // Configure external positioning algorithm
                        positioningSensorAlgorithm = new PositioningSensorAlgorithm.External(venue);
                        UserPositionManager userPositionManager = new UserPositionManager(MapActivity.this);
                        userPositionManager.registerPositioningSensorAlgorithmForVenue(positioningSensorAlgorithm);

                        mockExternalLocationData();
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

    private void externalLocationUpdateReceived(double lat, double lon, String floorID, double accuracy, double heading) {

        LatLng latLng = new LatLng(lat, lon);
        String locusLabsFloorID = locusLabsFloorIDForExternalFloorID(floorID);

        ((PositioningSensorAlgorithm.External) positioningSensorAlgorithm).recordPositionSensorReading(latLng, accuracy, locusLabsFloorID, heading);
    }

    private String locusLabsFloorIDForExternalFloorID(String floorID) {

        // If you are not able to compile this mapping table yourself, please send us a list (help@locuslabs.com) of
        // the building names and associated floor ids as provided by your external mapping provider and we will compile the mapping table

        String locusLabsFloorID = null;

        if (floorID.equals("T48L3")) locusLabsFloorID = "lax-south-departures";
        else if (floorID.equals("???")) locusLabsFloorID = "???";

        return (locusLabsFloorID != null ? locusLabsFloorID : "");
    }

    private void mockExternalLocationData() {

        // Position 1 (Initial - DFS Duty Free)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                externalLocationUpdateReceived(33.941485, -118.40195, "T48L3", 0, 0);
            }
        }, 1000);

        // Position 2 (2 secs later)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                externalLocationUpdateReceived(33.941398, -118.401916, "T48L3", 0, 0);
            }
        }, 3000);

        // Position 3 (4 secs later)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                externalLocationUpdateReceived(33.941283, -118.401863, "T48L3", 0, 0);
            }
        }, 5000);

        // Position 4 (6 secs later)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                externalLocationUpdateReceived(33.941102, -118.401902, "T48L3", 0, 0);
            }
        }, 7000);

        // Position 5 (8 secs later - Destination - Gate 64B)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                externalLocationUpdateReceived(33.940908, -118.40177, "T48L3", 0, 0);
            }
        }, 9000);
    }
}