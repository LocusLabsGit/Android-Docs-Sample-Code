package com.example.locuslabs.testapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.Logger;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Theme;
import com.locuslabs.sdk.maps.view.MapView;

/**
 * Sample Activity that will render the specified venue.
 */
public class MapActivity extends Activity {

    private VenueDatabase venueDatabase;
    private MapView mapView;

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

    private void loadVenue(final String venueId) {
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
                mapView.setPositioningEnabled(true);
                setMapViewTheme(_mapView);

                attachFunctionalityToMapView(_mapView, venueId);
            }
        };

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        String initialSearch;
        if ("lax".equalsIgnoreCase(venueId))   initialSearch = "Gate 80";
        else                                   initialSearch = null;
        venueDatabase.loadVenueAndMap(venueId, initialSearch, listeners);
    }



    private void attachFunctionalityToMapView(final MapView mapView, final String venueId) {

        mapView.setOnReadyListener(new MapView.OnReadyListener() {
            @Override
            public void onReady() {
                // The MapView callback onReady
                Logger.info("Ready");

                //  Default API Expected in Most Client Applications, expected in MapView's onReady callback.
                trySupplyCurrentActivity();

                // Let's take a look at our new Theme for the POI ViewGroup!
                if ("sea".equalsIgnoreCase(venueId)) {
                    mapView.showPoiPopup("126"); // Mountain Room Bar
                }
            }
        });

    }

    //====================================================//
    //  Default API Expected in Most Client Applications //
    //====================================================//

    //This allows activities to be started by providing the LocusLabs SDK with the current activity when a new activity is started.
    private void trySupplyCurrentActivity(){
        // Provides the current activity to the mMapView when a callback is requested.
        mapView.setOnSupplyCurrentActivityListener(new MapView.OnSupplyCurrentActivityListener() {
            @Override
            public Activity onSupplyCurrentActivity() {
                return MapActivity.this;
            }
        });
    }

    //=============//
    //  Theme APIs //
    //=============//

    private void setMapViewTheme(MapView mapView) {

        Typeface typeface_a = Typeface.create(Typeface.MONOSPACE,Typeface.BOLD_ITALIC);
        Typeface typeface_b = Typeface.create(Typeface.MONOSPACE,Typeface.NORMAL);
        Typeface typeface_c = Typeface.create(Typeface.SERIF,Typeface.BOLD_ITALIC);


        Theme theme = mapView
                .themeBuilder()
                .setProperty("view.poi.color.background", Color.DKGRAY)
                // POI Icon
                .setProperty("view.poi.icon.color.tint",Color.WHITE)
                .setProperty("view.poi.icon.color.background",Color.TRANSPARENT)
                // POI Header
                .setProperty("view.poi.header.color.background",Color.TRANSPARENT)
                // POI Header Icon
                .setProperty("view.poi.header.icon.color.background",Color.TRANSPARENT)
                // POI Header Title
                .setProperty("view.poi.header.title.color.text",Color.WHITE)
                .setProperty("view.poi.header.title.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.header.title.font.name", typeface_a)
                .setProperty("view.poi.header.title.font.size", 20.0)
                // POI Header Location
                .setProperty("view.poi.header.location.color.text",Color.WHITE)
                .setProperty("view.poi.header.location.color.tint",Color.WHITE)
                .setProperty("view.poi.header.location.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.header.location.font.name", typeface_b)
                .setProperty("view.poi.header.location.font.size", 16.0)
                // POI Header Security
                .setProperty("view.poi.header.security.color.text",Color.WHITE)
                .setProperty("view.poi.header.security.color.tint",Color.WHITE)
                .setProperty("view.poi.header.security.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.header.security.font.name", typeface_b)
                .setProperty("view.poi.header.security.font.size", 16.0)
                // POI Header Level
                .setProperty("view.poi.header.level.color.text",Color.WHITE)
                .setProperty("view.poi.header.level.color.tint",Color.WHITE)
                .setProperty("view.poi.header.level.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.header.level.font.name", typeface_b)
                .setProperty("view.poi.header.level.font.size", 16.0)
                // POI Carousel
                .setProperty("view.poi.carousel.color.background",Color.DKGRAY)
                .setProperty("view.poi.carousel.error.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.carousel.loader.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.carousel.image.color.background",Color.LTGRAY)
                // POI Carousel - Label
                .setProperty("view.poi.carousel.label.color.text",Color.BLACK)
                .setProperty("view.poi.carousel.label.color.background",Color.WHITE)
                .setProperty("view.poi.carousel.label.font.name", typeface_b)
                .setProperty("view.poi.carousel.label.font.size", 12.0)
                // POI Carousel - Loading
                .setProperty("view.poi.carousel.loading.color.background", Color.TRANSPARENT)
                .setProperty("view.poi.carousel.loading.text.color.text", Color.LTGRAY)
                .setProperty("view.poi.carousel.loading.text.color.background", Color.TRANSPARENT)
                .setProperty("view.poi.carousel.loading.text.font.name", typeface_b)
                .setProperty("view.poi.carousel.loading.text.font.size", 12.0)
                // POI Details Description String
                .setProperty("view.poi.detail.description.color.text",Color.WHITE)
                .setProperty("view.poi.detail.description.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.detail.description.font.name", typeface_b)
                .setProperty("view.poi.detail.description.font.size", 16.0)
                // POI Details Spannable Tag String
                .setProperty("view.poi.detail.tag.default.color.text",Color.BLACK)
                .setProperty("view.poi.detail.tag.default.color.background",Color.WHITE)
                // POI Contact - Website
                .setProperty("view.poi.contact.website.active.color.text",Color.YELLOW)
                .setProperty("view.poi.contact.website.active.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.contact.website.active.font.name", typeface_b)
                .setProperty("view.poi.contact.website.active.font.size", 18.0)
                .setProperty("view.poi.contact.website.default.color.text",Color.WHITE)
                .setProperty("view.poi.contact.website.default.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.contact.website.default.font.name", typeface_b)
                .setProperty("view.poi.contact.website.default.font.size", 16.0)
                // POI Contact - Phone
                .setProperty("view.poi.contact.phone.active.color.text",Color.YELLOW)
                .setProperty("view.poi.contact.phone.active.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.contact.phone.active.font.name", typeface_b)
                .setProperty("view.poi.contact.phone.active.font.size", 14.0)
                .setProperty("view.poi.contact.phone.default.color.text",Color.WHITE)
                .setProperty("view.poi.contact.phone.default.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.contact.phone.default.font.name", typeface_b)
                .setProperty("view.poi.contact.phone.default.font.size", 16.0)
                // POI Buttons
                .setProperty("view.poi.button.color.text",Color.WHITE)
                .setProperty("view.poi.button.color.tint",Color.WHITE)
                .setProperty("view.poi.button.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.button.font.name", typeface_b)
                .setProperty("view.poi.button.font.size", 13.0)
                // POI Operational Times - Day
                .setProperty("view.poi.time.day.color.text",Color.WHITE)
                .setProperty("view.poi.time.day.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.time.day.font.name", typeface_a)
                .setProperty("view.poi.time.day.font.size", 16.0)
                // POI Operational Times - Hour
                .setProperty("view.poi.time.hour.color.text",Color.LTGRAY)
                .setProperty("view.poi.time.hour.color.background",Color.TRANSPARENT)
                .setProperty("view.poi.time.hour.font.name", typeface_c)
                .setProperty("view.poi.time.hour.font.size", 18.0)
                // Create DefaultTheme
                .createTheme();

        mapView.setTheme(theme);
    }
}
