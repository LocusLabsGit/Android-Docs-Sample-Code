package myco.com.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.maps.model.Circle;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.MultiTermSearchResults;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.Search;
import com.locuslabs.sdk.maps.model.SearchResult;
import com.locuslabs.sdk.maps.model.SearchResults;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by juankruger on 13/03/18.
 */

public class MapActivity extends Activity {

    // Static
    private static final int PERMISSIONS_REQUEST_CODE = 1000;

    // Var
    private MapView         mapView;
    private Venue           venue;
    private VenueDatabase   venueDatabase;

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
            public void onLoadCompleted(Venue _venue, Map _map, final MapView _mapView, Floor floor, Marker marker) {

                venue = _venue;
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

                        ArrayList<String> searchTerms = new ArrayList<>();
                        searchTerms.add("Beer");
                        searchTerms.add("Burger");
                        performANDSearch(searchTerms);
                        //performORSearch(searchTerms);
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

    private void performANDSearch(ArrayList<String> searchTerms) {

        Search search = venue.search();

        Search.OnMultiTermSearchResultListener multiTermSearchResultListener = new Search.OnMultiTermSearchResultListener() {
            @Override
            public void onMultiTermSearchResults(MultiTermSearchResults multiTermSearchResults, String s) {

                drawSearchResultsOnMap(multiTermSearchResults.getResults(), Color.GREEN);
            }
        };

        // Perform a search for all POIs that match Beer AND Burger
        search.multiTermSearch(searchTerms, multiTermSearchResultListener);
    }

    private void performORSearch(ArrayList<String> searchTerms) {

        Search search = venue.search();

        Search.OnSearchWithTermsResultListener searchWithTermsResultListener = new Search.OnSearchWithTermsResultListener() {
            @Override
            public void onSearchWithTerms(SearchResults searchResults, String s) {

                drawSearchResultsOnMap(searchResults.getResults(), Color.YELLOW);
            }
        };

        // Perform a search for all POIs that match either Beer OR Burger
        search.searchWithTerms(searchTerms, searchWithTermsResultListener);
    }

    private void drawSearchResultsOnMap(List<SearchResult> searchResults, int color) {

        Position firstPosition = searchResults.get(0).getPosition();
        Position position = null;
        Map map = mapView.getMap();

        for (int i = 0 ; i < searchResults.size(); i++){

            position = searchResults.get(i).getPosition();
            map.addCircle(new Circle.Options()
                    .position(position)
                    .radius(10.0)
                    .fillOpacity(0.4)
                    .fillColor(color));
        }

        mapView.setCenterPosition(firstPosition);
        mapView.setRadius(150);
    }
}