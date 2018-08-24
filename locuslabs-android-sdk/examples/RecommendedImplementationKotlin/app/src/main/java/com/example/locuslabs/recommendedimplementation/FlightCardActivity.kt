package com.example.locuslabs.recommendedimplementation

import android.app.Activity
import android.os.Bundle

class FlightCardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets the content view to match ./res/layout/flight_card.xml
        setContentView(R.layout.flight_card)
    }

    companion object {
        private val TAG = "FlightCardActivity"
    }
}
