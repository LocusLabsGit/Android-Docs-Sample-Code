package myco.com.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.NavigationSegment;
import com.locuslabs.sdk.maps.model.POI;
import com.locuslabs.sdk.maps.model.POIDatabase;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.SecurityCategory;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.model.Waypoint;
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
    private Venue           venue;

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
            public void onLoadCompleted(Venue _venue, Map map, MapView _mapView, Floor floor, Marker marker) {

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

                        getDirectionsAndETA();
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

    private void getDirectionsAndETA() {

        // From: Blu20 Bar, Level 3, Terminal 6, ID 1025
        // To: Gate 73, Level 3, Terminal 7, ID 551
        venue.poiDatabase().loadPOI("1025", new POIDatabase.OnLoadPoiListener() {
            @Override
            public void onLoadPoi(POI poi) {

                final POI startPOI = poi;
                venue.poiDatabase().loadPOI("551", new POIDatabase.OnLoadPoiListener() {
                    @Override
                    public void onLoadPoi(POI poi) {

                        POI endPOI = poi;

                        Venue.OnGetNavigationSegmentsListener getNavigationSegmentsListener = new Venue.OnGetNavigationSegmentsListener() {
                            @Override
                            public void onGetNavigationSegments(NavigationSegment[] navigationSegments) {

                                Log.d("MapActivity", "Got Segments");
                                double eta = 0.0;

                                for (NavigationSegment navSegment : navigationSegments) {

                                    Log.d("MapActivity", "Segment: " + navSegment.getPrimaryText());
                                    eta += navSegment.getEstimatedTime();

                                    for (Waypoint waypoint: navSegment.getWaypoints()) {

                                        Log.d("MapActivity", "WayPoint");
                                        //Log.d("MapActivity", "ETA: " +String.valueOf(waypoint.getEta()));
                                        //Log.d("MapActivity", "Distance: " +String.valueOf(waypoint.getDistance()));
                                        Log.d("MapActivity", "Security Checkpoint?: " +String.valueOf(waypoint.securityCheckpoint()));
                                        Log.d("MapActivity", "\n");
                                    }
                                }

                                Log.d("MapActivity", "ETA: " + String.valueOf(eta));
                            }
                        };

                        Venue.OnGetNavigationSegmentsFailureListener getNavigationSegmentsFailureListener = new Venue.OnGetNavigationSegmentsFailureListener() {
                            @Override
                            public void onGetNavigationSegmentsFailure(String s) {

                                Log.d("MapActivity", "Failure getting navigation: " + s);
                            }
                        };

                        venue.getNavigationDirectSegments(mapView, startPOI.getPosition(), endPOI.getPosition(), new ArrayList<SecurityCategory>(), getNavigationSegmentsListener, getNavigationSegmentsFailureListener);
                    }
                });
            }
        });
    }
}