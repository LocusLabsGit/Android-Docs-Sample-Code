package com.example.locuslabs.testapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

import androidx.appcompat.app.AlertDialog;

/**
 * Sample Activity that will render the specified venue.
 */
public class MapActivity extends Activity {
    private static final String TAG = "MapActivity";

    private VenueDatabase venueDatabase;
    private MapView mapView;
    private BroadcastReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent receivedIntent = getIntent();

        //Takes the String venueId to determine what venue needs to be used for loadMap
        String venueId = receivedIntent.getStringExtra("venueId");

        //Create an VenueDatabase which allows venues to be loaded.
        venueDatabase = new VenueDatabase();

        //Load the Venue specified by the venueId passed to the activity.
        loadVenue(venueId);

        // Warn user if network dropped because it's a necessity for IndoorAtlas 2.8.2
        networkChangeReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

                if (null == activeNetworkInfo || !activeNetworkInfo.isConnected()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    builder.setMessage("Lost Internet needed for IndoorAtlas").setTitle("Internet Status").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do nothing, just an info message
                        }
                    });
                    builder.create();
                    builder.show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    builder.setMessage("Successfully obtained Internet needed for IndoorAtlas").setTitle("Internet Status").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do nothing, just an info message
                        }
                    });
                    builder.create();
                    builder.show();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, intentFilter);
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
                mapView = _mapView;

                // Calling setPositioningEnabled(true) automatically turns on IndoorAtlas,
                // as long as the venue you're looking at has an IndoorAtlas survey
                mapView.setPositioningEnabled(true);
//                testIndoorAtlasDirectly();
            }
        };

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        String initialSearch;
        if ("sea".equalsIgnoreCase(venueId))        initialSearch = "Gate A5";
        else if ("lax".equalsIgnoreCase(venueId))   initialSearch = "Gate 80";
        else                                        initialSearch = null;
        venueDatabase.loadVenueAndMap(venueId, initialSearch, listeners);
    }

    /**
     * Tests calling IndoorAtlas directly, that is to say, not through reflection
     *
     * Follows https://docs.indooratlas.com/develop/android/
     */
    private void testIndoorAtlasDirectly() {
        IALocationManager iaLocationManager = IALocationManager.create(this);
        iaLocationManager.requestLocationUpdates(IALocationRequest.create(), new IALocationListener() {

            // Called when the location has changed.
            @Override
            public void onLocationChanged(IALocation location) {
                Log.d(TAG, "onLocationChanged Latitude: " + location.getLatitude());
                Log.d(TAG, "onLocationChanged Longitude: " + location.getLongitude());
                Log.d(TAG, "onLocationChanged Floor number: " + location.getFloorLevel());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d(TAG, "onStatusChanged s: " + s);
                Log.d(TAG, "onStatusChanged i: " + i);
            }
        });

        iaLocationManager.registerRegionListener(new IARegion.Listener() {
            IARegion mCurrentFloorPlan = null;

            @Override
            public void onEnterRegion(IARegion region) {
                if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                    Log.d(TAG, "Entered " + region.getName());
                    Log.d(TAG, "floor plan ID: " + region.getId());
                    mCurrentFloorPlan = region;
                }
            }

            @Override
            public void onExitRegion(IARegion region) {
                Log.d(TAG, "Exited " + region);
            }
        });
    }
}
