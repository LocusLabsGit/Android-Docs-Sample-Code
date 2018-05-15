package com.example.locuslabs.recommendedimplementation;

import android.app.Activity;
import android.os.Bundle;

public class FlightCardActivity extends Activity {
    private static final String TAG = "FlightCardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets the content view to match ./res/layout/flight_card.xml
        setContentView(R.layout.flight_card);
    }
}
