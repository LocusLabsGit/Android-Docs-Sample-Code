Start Here
==========

* [Quick Start Guide](#quick-start-guide)
* [API Reference](docs/index.html)
* Examples : examples/*
* [Release Notes](ReleaseNotes.txt)

Quick Start Guide
=================

Run Example Application
-----------------------

The directions below are for Android Studio 1.3 and later

1. Start Android Studio
2. Click "Open an Existing Android Studio Project"
3. Select the "RecommendedImplementation" folder
4. Press the "Play" button to run the sample application

Integrate into your Android project
---------------------------------

First, add the following to your module's build.gradle file should have the following:

~~~
    compile('com.locuslabs:sdk:+:release@aar') {
        transitive=true
    }
~~~

Then add an entry into project's build.gradle while which adds the SDK's maven repository:
~~~
    allprojects {
        repositories {
            jcenter()
            maven {
                url "${sdkRootDir}/maven/releases"
            }
        }
    }
~~~


Then add the needed permissions to your Manifest:
~~~
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
~~~

Finally, initialize the LocusLabs SDK with the following:
~~~
    LocusLabs.initialize(getApplicationContext(), "YOUR_ACCOUNT_ID_HERE", new LocusLabs.OnReadyListener() {
        public void onReady() {
            // Use LocusLabs SDK.....
        }
    });
~~~

Display a Venue Map
-------------------

~~~
    AirportDatabase airportDatabase = new AirportDatabase();
    
    AirportDatabase.OnLoadAirportAndMapListeners listeners = new AirportDatabase.OnLoadAirportAndMapListeners();
    listeners.loadedInitialViewListener = new AirportDatabase.OnLoadedInitialViewListener() {
        @Override
        public void onLoadedInitialView(View view) {
            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            setContentView(view);
        }
    };
    
    // The second parameter is an initial search option.
    // The map will zoom to the first matched POI.
    airportDatabase.loadAirportAndMap(venueId, "gate:a5", listeners);
~~~

Search for Points of Interest
-----------------------------

~~~
    Search search = airport.search();
    
    // Find all of the Starbucks
    search.search("starbucks",listener);
    
    // Find the stores which sell coffee
    search.search("keyword:coffee");
    
    // Find Gate D3
    search.search("gate:d3");
    
    // Find all of the bookstores near a position
    List<String> terms = new ArrayList<String>(1);
    terms.add("keyword:books");
    
    search.proximitySearchWithTerms(terms,position.getFloorId(),position.getLatLng().getLat(),position.getLatLng().getLng(),listener);
~~~

Indoor Positioning
------------------

Enable indoor positioning on a MapView.

~~~
    mapView.setPositioningEnabled(true);
~~~

Access positioning information programmatically.

~~~
    UserPositionManager userPositionManager = new UserPositionManager(context);
    userPositionManager.registerOnPositionChangedListener(listener);
    userPositionManager.registerVenue(airport);
~~~

Navigation
----------

Calculate travel time and distance between two POIs.

~~~
    airport.navigateFrom(poi1.position,poi2.position,listener);
~~~
