package com.example.locuslabs.sdkexamplelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.Logger;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.POI;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.ArrayList;
import java.util.List;

/*
* This activity loads a map then shows a POI with an Id of
* 126. After 5 seconds, the previous POI Card View is
* destroyed and 2 ExtraButtons are added to every POI
* then a new POI Card View with an Id of 100 is rendered.
* */
public class ExampleAddExtraPoiButtons extends Activity {
    private static final String TAG = "Ex. AddExtraPoiButtons";

    private VenueDatabase venueDatabase;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This activity takes a venueId parameter. The venueId represents the Venue to be loaded.
        Intent receivedIntent = getIntent();
        String venueId = receivedIntent.getStringExtra("venueId");

        // Create a VenueDatabase which allows venues to be loaded.
        venueDatabase = new VenueDatabase();

        // Load the Venue specified by the venueId passed to the activity.
        loadVenue(venueId);
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
        final RelativeLayout rl = new RelativeLayout( this );

        VenueDatabase.OnLoadVenueAndMapListeners listeners = new VenueDatabase.OnLoadVenueAndMapListeners();
        listeners.loadedInitialViewListener = new VenueDatabase.OnLoadedInitialViewListener() {
            @Override public void onLoadedInitialView(View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }

                view.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                rl.addView(view);
                setContentView(rl);
                venueDatabase.resumeLoadVenueAndMap();
            }
        };
        listeners.loadCompletedListener = new VenueDatabase.OnLoadCompletedListener() {

            @Override public void onLoadCompleted(Venue _venue, Map _map, final MapView _mapView,
                                                  Floor floor, Marker marker) {
                mapView = _mapView;

                // Note: ExtraButtons can be added to POI Views before the MapView is ready.
                setupExtraButtons(mapView);

                mapView.setOnReadyListener(new MapView.OnReadyListener() {
                    @Override
                    public void onReady() {
                        Logger.info("Ready");

                        // When the MapView callback onReady then the mapView will open the show the POI with an id of 126.
                        mapView.showPoiPopup("126");
                    }
                });


            }
        };

        venueDatabase.loadVenueAndMap(venueId, null, listeners);
    }

    // Adds a setter and handler. Ideally, this is added before any programmatic showPoiPopup API calls are made.
    private void setupExtraButtons(MapView mapView) {

        // Setter
        /* The Setter sets a listeners on the MapView, waiting for an extra button to be clicked on the Android WebView.
        * When the listeners is triggered by an extra button being clicked, the onExtraButtonForPoiPopupClickedListener callback
        * is called. In this example, the click Logs information about the click to the console.
        * */
        mapView.setOnExtraButtonForPoiPopupClickedListener(new MapView.OnExtraButtonForPoiPopupClickedListener() {
            @Override
            public void onExtraButtonForPoiPopupClicked(POI poi, MapView.ExtraButtonsForPoiPopupHandler.ButtonDefinition buttonDefinition) {
                Logger.debug("Extra button text was: " + buttonDefinition.text);
                String expected = poi.getName();
                if (!buttonDefinition.text.equalsIgnoreCase(expected)) {
                    Logger.error("Assertion: ButtonDefinition.text should be " + expected + " but was " + buttonDefinition.text);
                }
            }
        });

        // Handler
        /* The Handler assigns two buttons to every List of ButtonDefinitions.
        * Every POI will have one extra button with a subtitle of 'FakeName' and an online placeholder icon which looks like a red square.
        * Every POI will have a second extra button with a subtitle of 'TestName' and an online placeholder icon which looks like a blue square.
        * This List of ButtonDefinitions are returned to the MapView class.
        * */
        mapView.setExtraButtonsForPoiPopupHandler(new MapView.ExtraButtonsForPoiPopupHandler() {
            @Override
            public List<ButtonDefinition> extraButtonsForPoiPopup(POI poi) {
                List<ButtonDefinition> result = new ArrayList<>();

                result.add(new ButtonDefinition("FakeName", "https://placehold.it/24/ff0000/ffffff"));
                result.add(new ButtonDefinition("TestName", "https://placehold.it/24/0000ff/ffffff"));
                return result;
            }
        });

    }
}