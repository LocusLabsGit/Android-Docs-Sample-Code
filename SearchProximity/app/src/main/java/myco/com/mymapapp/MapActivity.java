package myco.com.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.maps.model.Airport;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.Circle;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.Search;
import com.locuslabs.sdk.maps.model.SearchResult;
import com.locuslabs.sdk.maps.model.SearchResults;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by juankruger on 13/03/18.
 */

public class MapActivity extends AppCompatActivity {

    // Static
    private static final int PERMISSIONS_REQUEST_CODE = 1000;

    // Var
    private Airport         airport;
    private AirportDatabase airportDatabase;
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

            initializeLocusLabsDatabase();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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

        if (airportDatabase != null) {

            airportDatabase.close();
            airportDatabase = null;
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

                airportDatabase = new AirportDatabase();
                loadVenueAndMap("lax", "name of the venue you want to appear");
            }
        });
    }

    private void loadVenueAndMap(final String venueId, final String venueName) {

        final RelativeLayout rl = new RelativeLayout(this);

        AirportDatabase.OnLoadAirportAndMapListeners listeners = new AirportDatabase.OnLoadAirportAndMapListeners();
        listeners.loadedInitialViewListener = new AirportDatabase.OnLoadedInitialViewListener() {
            @Override
            public void onLoadedInitialView(View view) {

                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {

                    parent.removeView(view);
                }

                view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                rl.addView(view);
                setContentView(rl);
                airportDatabase.resumeLoadAirportAndMap();

            }
        };

        listeners.loadCompletedListener = new AirportDatabase.OnLoadCompletedListener() {
            @Override
            public void onLoadCompleted(Airport _airport, Map _map, final MapView _mapView, Floor floor, Marker marker) {

                airport = _airport;
                mapView = _mapView;
                mapView.setPositioningEnabled(true);

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

                        // Perform a proximity search for Starbucks close to gate 60 on the departures level
                        performProximitySearch("Starbucks", "lax-south-departures", 33.94221, -118.402057);
                    }
                });
            }
        };

        listeners.loadFailedListener = new AirportDatabase.OnLoadFailedListener() {
            @Override
            public void onLoadFailed(String s) {

                // Handle errors here
            }
        };

        // The second parameter is an optional initial search term
        airportDatabase.loadAirportAndMap(venueId, null, listeners);
    }

    private void performProximitySearch(String searchTerm, String floorID, double latitude, double longitude) {

        Search search = airport.search();

        Search.OnProximitySearchWithTermsResultsListener proximitySearchResultListener = new Search.OnProximitySearchWithTermsResultsListener() {
            @Override
            public void onProximitySearchWithTermsResults(SearchResults searchResults, String s) {

                drawSearchResultsOnMap(searchResults);
            }
        };

        search.proximitySearchWithTerms(Arrays.asList(searchTerm), floorID, latitude, longitude, proximitySearchResultListener);
    }

    private void drawSearchResultsOnMap(SearchResults searchResults) {

        List<SearchResult> resultList = searchResults.getResults();
        Position firstPosition = resultList.get(0).getPosition();
        Position position = null;
        Map map = mapView.getMap();

        for (int i = 0 ; i< resultList.size();i++){

            position = resultList.get(i).getPosition();
            map.addCircle(new Circle.Options()
                    .position(position)
                    .radius(10.0)
                    .fillOpacity(0.3)
                    .fillColor(Color.GREEN));
        }

        mapView.setCenterPosition(firstPosition);
        mapView.setRadius(150);
    }
}