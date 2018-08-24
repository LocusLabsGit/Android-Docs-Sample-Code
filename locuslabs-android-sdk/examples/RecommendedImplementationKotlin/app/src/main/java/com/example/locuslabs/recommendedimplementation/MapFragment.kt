package com.example.locuslabs.recommendedimplementation

import android.app.Fragment
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.locuslabs.sdk.maps.model.*
import com.locuslabs.sdk.maps.model.Map
import com.locuslabs.sdk.maps.view.MapView

class MapFragment : Fragment() {

    private var button: Button? = null
    private var venueId: String? = null

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
    }

    override fun onDestroy() {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.map, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Create an VenueDatabase which allows venues to be loaded.
        venueDatabase = VenueDatabase()

        val receivedIntent = activity.intent
        this.venueId = receivedIntent.getStringExtra("venueId")

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 330, 0.1.toFloat())
            this.view!!.layoutParams = params
        }

        progressBar = view!!.findViewById(R.id.progress)
        progressBarMessage = view!!.findViewById(R.id.progressBarMessage)
        loadVenue(venueId)
    }

    private fun loadVenue(venueId: String?) {
        val listeners = VenueDatabase.OnLoadVenueAndMapListeners()
        listeners.loadedInitialViewListener = VenueDatabase.OnLoadedInitialViewListener { view ->
            if (null != view?.parent) {
                val parent = view.parent as ViewGroup
                parent.removeView(view)
            }

            view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            (getView() as ViewGroup).addView(view)
            venueDatabase!!.resumeLoadVenueAndMap()
        }
        listeners.loadCompletedListener = VenueDatabase.OnLoadCompletedListener { _venue, _map, _mapView, _floor, _marker ->
            venue = _venue
            map = _map
            mapView = _mapView
            floor = _floor
            marker = _marker

            mapView!!.hideAllWidgets()

            val viewGroup = view as ViewGroup
            viewGroup.removeAllViews()
            viewGroup.addView(mapView)

            setupButton(viewGroup, venueId)
        }

        listeners.loadProgressListener = VenueDatabase.OnLoadProgressListener { percentComplete ->
            progressBarMessage!!.text = MapActivity.PROGRESS_MESSAGE + percentComplete + "%"
            progressBar!!.progress = percentComplete!!
        }

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        venueDatabase!!.loadVenueAndMap(venueId, "gate:a5", listeners)
    }

    private fun setupButton(viewGroup: ViewGroup, venueId: String?) {
        if (this.button == null) {
            this.button = Button(this.activity.applicationContext)
            this.button!!.setBackgroundColor(Color.TRANSPARENT)
            this.button!!.setOnClickListener {
                val intent = Intent(this@MapFragment.activity.applicationContext, MapActivity::class.java)
                intent.putExtra("venueId", venueId)
                startActivity(intent)
            }
        }

        this.button!!.visibility = View.VISIBLE
        viewGroup.addView(this.button)
    }

    companion object {
        private val TAG = "MapFragment"
    }
}

