package com.example.locuslabs.sdkexamplelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.maps.model.Airport;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.POI;
import com.locuslabs.sdk.maps.model.POIDatabase;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.view.MapView;

/*
* This activity loads a map then sets Navigation Mode to TRUE
* and starts a navigation between a POI with an Id of "10", ending
* at a POI with an Id of "100".
* */
public class ExampleShowNavigation extends Activity {
    private static final String TAG = "ExampleShowNavigation";

    private AirportDatabase airportDatabase;
    private MapView mapView;
    private Airport airport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This activity takes a venueId parameter. The venueId represents the Airport to be loaded.
        Intent receivedIntent = getIntent();
        String venueId = receivedIntent.getStringExtra("venueId");

        //Create an AirportDatabase which allows airports to be loaded.
        airportDatabase = new AirportDatabase();

        //Load the Airport specified by the venueId passed to the activity.
        loadAirport(venueId);
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
        // airportDatabase to release the memory
        // they consume.
        //-----------------------------------

        if ( mapView != null ) {
            mapView.close();
        }

        if ( airportDatabase != null ) {
            airportDatabase.close();
        }

        airportDatabase = null;
        mapView = null;
    }

    private void loadAirport(String venueId) {
        final RelativeLayout rl = new RelativeLayout( this );

        AirportDatabase.OnLoadAirportAndMapListeners listeners = new AirportDatabase.OnLoadAirportAndMapListeners();
        listeners.loadedInitialViewListener = new AirportDatabase.OnLoadedInitialViewListener() {
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
                airportDatabase.resumeLoadAirportAndMap();
            }
        };
        listeners.loadCompletedListener = new AirportDatabase.OnLoadCompletedListener() {

            @Override public void onLoadCompleted(Airport _airport, Map _map, final MapView _mapView,
                                                  Floor floor, Marker marker) {
                airport = _airport;
                mapView = _mapView;
                showNavigation();
            }
        };

        airportDatabase.loadAirportAndMap(venueId, null, listeners);
    }

    //To show a navigation you need two position. In the example below retrieve two POIs for establishing a starting Poistion and an ending Position
    private void showNavigation() {
        final POIDatabase poiDatabase = airport.poiDatabase();

        poiDatabase.loadPOI("15", new POIDatabase.OnLoadPoiListener() {
            @Override
            public void onLoadPoi(final POI startPOI) {
                poiDatabase.loadPOI("126", new POIDatabase.OnLoadPoiListener() {
                    @Override
                    public void onLoadPoi(final POI endPOI) {

                        //Use the Position.Builder to create a new Position based off the startPOIs Position.
                        Position startPosition = new Position.Builder(startPOI.getPosition())
                        //Assign a name to the Position startPosition by getting the POIs name.
                        .name(startPOI.getName())
                        //Create the Position startPosition, so it can be called from MapView > showNavigation
                        .createPosition();

                        //Use the Position.Builder to create a new Position based off the endPOIs Position.
                        Position endPosition = new Position.Builder(endPOI.getPosition())
                        //Assign a name to the Position endPosition by getting the POIs name.
                        .name(endPOI.getName())
                        //Create the Position endPosition, so it can be called from MapView > showNavigation
                        .createPosition();

                        mapView.showNavigation(startPosition,endPosition);
                    }
                });
            }
        });
    }
}