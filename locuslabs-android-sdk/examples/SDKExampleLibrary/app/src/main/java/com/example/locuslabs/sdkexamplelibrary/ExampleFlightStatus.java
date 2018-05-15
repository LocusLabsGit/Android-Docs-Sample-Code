package com.example.locuslabs.sdkexamplelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.maps.model.Airline;
import com.locuslabs.sdk.maps.model.Airport;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.Flight;
import com.locuslabs.sdk.maps.model.FlightCode;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
* This activity loads a map and adds a Flight departing from SEA Airport then arriving in
* LAX Airport and another flight departing from LAX Airport then arriving in SEA Airport.
* The user will see two clickable flight status markers on the map.
* */
public class ExampleFlightStatus extends Activity {
    private static final String TAG = "ExampleFlightStatus";

    private AirportDatabase airportDatabase;
    private MapView mapView;
    private Map map;

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
                mapView = _mapView;
                map = _map;
                addTwoFlightStatusMarkers();
            }
        };

        airportDatabase.loadAirportAndMap(venueId, null, listeners);
    }

//    //Constructs a flight from LAX Airport to SEA Airport
    private Flight createLaxToSeaFlight() {
        Airline oceanic = Airline.Oceanic();
        FlightCode oceanicFlightCode = new FlightCode(oceanic,"815");

        //Construct the Flight's departure gate.
        Flight.AirportGate departingGate = new Flight.AirportGate();
        departingGate.setAirportCode("lax");
        departingGate.setAirportName("Los Angeles");
        departingGate.setGate("130");

        //Construct the Flight's arrival gate.
        Flight.AirportGate arrivalGate = new Flight.AirportGate();
        arrivalGate.setAirportCode("sea");
        arrivalGate.setAirportName("Seattle Tacoma International Airport");
        arrivalGate.setGate("a1");
        arrivalGate.setBaggageClaim("3");

        Date departureDate = new Date();
        Date arrivalDate = new Date();

        // The arrival flight arrives two hours after departure for our sample.
        arrivalDate.setHours( arrivalDate.getHours() + 2 );

        //Setup the Flight's departure time.
        Flight.Times departureTimes = new Flight.Times();
        departureTimes.setActual(departureDate);
        departureTimes.setEstimated(departureDate);
        departureTimes.setScheduled(departureDate);

        //Setup the Flight's arrival time.
        Flight.Times arrivalTimes = new Flight.Times();
        arrivalTimes.setActual(arrivalDate);
        arrivalTimes.setEstimated(arrivalDate);
        arrivalTimes.setScheduled(arrivalDate);

        //Setup the flight status Marker
        Flight oceanicFlight815 = new Flight();
        oceanicFlight815.setOperatingFlightCode(oceanicFlightCode);
        oceanicFlight815.setDepartureGate(departingGate);
        oceanicFlight815.setArrivalGate(arrivalGate);
        oceanicFlight815.setDepartureTimes(departureTimes);
        oceanicFlight815.setArrivalTimes(arrivalTimes);

        return oceanicFlight815;
    }

    //Constructs a flight from SEA Airport to LAX Airport
    private Flight createSeaToLaxFlight() {
        Airline oceanic = Airline.Oceanic();
        FlightCode oceanicFlightCode = new FlightCode(oceanic,"3313");

        //Construct the Flight's departure gate.
        Flight.AirportGate departingGate = new Flight.AirportGate();
        departingGate.setAirportCode("sea");
        departingGate.setAirportName("Seattle Tacoma International Airport");
        departingGate.setGate("d1");

        //Construct the Flight's arrival gate.
        Flight.AirportGate arrivingGate = new Flight.AirportGate();
        arrivingGate.setAirportCode("lax");
        arrivingGate.setAirportName("Los Angeles");
        arrivingGate.setGate("35");
        arrivingGate.setBaggageClaim("3");

        Date arrivalDate = new Date();
        Date departureDate = new Date();

        // The arrival flight arrives two hours after departure for our sample.
        arrivalDate.setHours( arrivalDate.getHours() + 2 );

        //Setup the Flight's departure time.
        Flight.Times departureTimes = new Flight.Times();
        departureTimes.setActual(departureDate);
        departureTimes.setEstimated(departureDate);
        departureTimes.setScheduled(departureDate);

        //Setup the Flight's arrival time.
        Flight.Times arrivalTimes = new Flight.Times();
        arrivalTimes.setActual(arrivalDate);
        arrivalTimes.setEstimated(arrivalDate);
        arrivalTimes.setScheduled(arrivalDate);
        //Setup the flight status Marker
        Flight oceanicFlight3313 = new Flight();
        oceanicFlight3313.setOperatingFlightCode(oceanicFlightCode);
        oceanicFlight3313.setArrivalGate(arrivingGate);
        oceanicFlight3313.setDepartureGate(departingGate);
        oceanicFlight3313.setArrivalTimes(arrivalTimes);
        oceanicFlight3313.setDepartureTimes(departureTimes);

        return oceanicFlight3313;
    }

    // Creating a List of Flights to add to the Map.
    private void addTwoFlightStatusMarkers() {
        List<Flight> flights = new ArrayList<Flight>();

        flights.add(createLaxToSeaFlight());
        flights.add(createSeaToLaxFlight());

        map.setFlights(flights);
    }


}