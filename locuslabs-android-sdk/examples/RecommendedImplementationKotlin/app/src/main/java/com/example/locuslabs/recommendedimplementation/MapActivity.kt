package com.example.locuslabs.recommendedimplementation

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.locuslabs.sdk.maps.model.*
import com.locuslabs.sdk.maps.model.Map
import com.locuslabs.sdk.maps.view.MapView

/**
 * Sample Activity that will render the specified venue.
 */
class MapActivity : Activity() {

    private var venue: Venue? = null
    private var map: Map? = null
    private var mapView: MapView? = null
    private var floor: Floor? = null
    private var marker: Marker? = null
    private var venueDatabase: VenueDatabase? = null
    private var progressBar: ProgressBar? = null
    private var progressBarMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receivedIntent = intent
        val venueId = receivedIntent.getStringExtra("venueId")

        //Create an VenueDatabase which allows venues to be loaded.
        venueDatabase = VenueDatabase()

        progressBar = findViewById(R.id.progressMap)
        progressBarMessage = findViewById(R.id.progressBarMessageMap)

        //Load the Venue specified by the venueId passed to the activity.
        loadVenue(venueId)
    }

    public override fun onDestroy() {
        super.onDestroy()

        //-----------------------------------
        // Be sure to close the mapView and
        // venueDatabase to release the memory
        // they consume.
        //-----------------------------------

        if (mapView != null) {
            mapView!!.close()
        }

        if (venueDatabase != null) {
            venueDatabase!!.close()
        }

        venueDatabase = null
        mapView = null
    }

    override fun onBackPressed() {
        if (mapView == null || !mapView!!.onBackPressed()) {
            super.onBackPressed()
        }
    }

    private fun loadVenue(venueId: String) {
        val rl = RelativeLayout(this)

        val listeners = VenueDatabase.OnLoadVenueAndMapListeners()
        listeners.loadedInitialViewListener = VenueDatabase.OnLoadedInitialViewListener { view ->
            if (null != view.parent) {
                val parent = view.parent as ViewGroup
                parent.removeView(view)
            }

            view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            rl.addView(view)
            setContentView(rl)
            venueDatabase!!.resumeLoadVenueAndMap()
        }
        listeners.loadCompletedListener = VenueDatabase.OnLoadCompletedListener { _venue, _map, _mapView, _floor, _marker ->
            venue = _venue
            map = _map
            mapView = _mapView
            floor = _floor
            marker = _marker

            // turn on positioning
            mapView!!.setPositioningEnabled(true)

            mapView!!.setOnSupplyCurrentActivityListener { this@MapActivity }
        }

        listeners.loadProgressListener = VenueDatabase.OnLoadProgressListener { percentComplete ->
            progressBarMessage!!.text = PROGRESS_MESSAGE + percentComplete + "%"
            progressBar!!.progress = percentComplete!!
        }

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        venueDatabase!!.loadVenueAndMap(venueId, "gate:a5", listeners)
    }

    companion object {
        val PROGRESS_MESSAGE = "Downloading map..."
    }
}
