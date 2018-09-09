package myco.com.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import myco.com.mymapapp.MapLoading.LocusLabsCache;
import myco.com.mymapapp.MapLoading.LocusLabsMapPack;
import myco.com.mymapapp.MapLoading.LocusLabsMapPackFinder;

/**
 * Created by juankruger on 13/03/18.
 */

public class MapActivity extends AppCompatActivity {

    // Static
    public static final String ACCOUNT_ID =             "A11F4Y6SZRXH4X";
    private static final int PERMISSIONS_REQUEST_CODE = 1000;

    // Var
    private VenueDatabase   venueDatabase;
    private MapView         mapView;

    // *************
    // LIFECYCLE
    // *************

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Individual permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions(this,
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

            // If the Map Pack name is null, it will automatically search
            // for a map pack in the assets/locuslabs directory.  The search will
            // look for tar files that begin with "android-<ACCOUNT_ID>-<DATE>.
            installMapPack(ACCOUNT_ID, null, new LocusLabsMapPack.OnUnpackCallback() {
                public void onUnpack(String venueListContents, Exception exception) {

                    if (exception != null) {Log.d("MapActivity", exception.getMessage());}

                    // After the Map Pack is unpacked, load the map
                    initializeLocusLabsDatabase();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {

            // Assume all permissions were granted (in practice you would need to check each permission)
            if (grantResults.length > 0) {

                // If the Map Pack name is null, it will automatically search
                // for a map pack in the assets/locuslabs directory.  The search will
                // look for tar files that begin with "android-<ACCOUNT_ID>-<DATE>.
                installMapPack(ACCOUNT_ID, null, new LocusLabsMapPack.OnUnpackCallback() {
                    public void onUnpack(String venueListContents, Exception exception) {

                        if (exception != null) {Log.d("MapActivity", exception.getMessage());}

                        // After the Map Pack is unpacked, load the map
                        initializeLocusLabsDatabase();
                    }
                });
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

    private void installMapPack( String accountId, String mapPackName, LocusLabsMapPack.OnUnpackCallback callback ) {

        LocusLabsMapPackFinder finder = LocusLabsMapPackFinder.getMapPackFinder( this.getApplicationContext(), accountId );

        try {
            LocusLabsCache cache = LocusLabsCache.getDefaultCache(this.getApplicationContext());

            LocusLabsMapPack pack = null;
            if ( mapPackName != null && mapPackName.length() > 0 ) {
                Log.d("LocusLabsMapPack", "Installing Name Map Pack: " + mapPackName );
                pack = new LocusLabsMapPack( cache, mapPackName, finder.getAsMapPack( mapPackName ) );
            }
            else {
                Log.d("LocusLabsMapPack", "Looking for Map Pack: " );
                pack = new LocusLabsMapPack( cache, finder.getNewestMapPackName(), finder.getNewestMapPack() );
            }

            if ( pack.needsInstall() ) {
                Log.d("LocusLabsMapPack", "Need installation for pack: " + pack.getName());
                pack.unpack( callback );
            }
            else {
                Log.d("LocusLabsMapPack", "No installation needed for pack: " + pack.getName());
                callback.onUnpack(null, null);
            }
        }
        catch ( Exception exception ) {
            Log.e( "LocusLabsMapPack", exception.toString() );
            callback.onUnpack(null, exception);
        }
    }
}