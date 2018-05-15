
package com.example.locuslabs.recommendedimplementation;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.locuslabs.sdk.maps.model.Airport;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.List;

import static android.graphics.Color.parseColor;

public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";

    private Button button = null;
    private String venueId = null;

    private MapView mapView = null;
    private AirportDatabase airportDatabase = null;
    private ProgressBar progressBar;
    private TextView progresBarMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map, container, false);

        return view;
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Create an AirportDatabase which allows airports to be loaded.
        airportDatabase = new AirportDatabase();

        Intent receivedIntent = getActivity().getIntent();
        this.venueId = receivedIntent.getStringExtra("venueId");

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,330,(float)0.1);
           this.getView().setLayoutParams(params);
        }

        progressBar = getView().findViewById(R.id.progress);
        progresBarMessage = getView().findViewById(R.id.progressBarMessage);
        loadAirport(venueId);
    }

    private void loadAirport(final String venueId) {
        AirportDatabase.OnLoadAirportAndMapListeners listeners = new AirportDatabase.OnLoadAirportAndMapListeners();
        listeners.loadedInitialViewListener = new AirportDatabase.OnLoadedInitialViewListener() {
            @Override
            public void onLoadedInitialView(View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }

                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                ((ViewGroup)getView()).addView(view);
                airportDatabase.resumeLoadAirportAndMap();
            }
        };
        listeners.loadCompletedListener = new AirportDatabase.OnLoadCompletedListener() {
            @Override
            public void onLoadCompleted(Airport _airport, Map _map, final MapView _mapView, Floor floor, Marker marker) {
                mapView = _mapView;
                mapView.hideAllWidgets();

                ViewGroup viewGroup = (ViewGroup) getView();
                viewGroup.removeAllViews();
                viewGroup.addView(mapView);

                setupButton(viewGroup, venueId);
            }
        };

        listeners.loadProgressListener = new AirportDatabase.OnLoadProgressListener() {
            @Override
            public void onLoadProgress(Integer percentComplete) {
                progresBarMessage.setText(MapActivity.PROGRESS_MESSAGE+percentComplete+"%");
                progressBar.setProgress(percentComplete);
                progresBarMessage.invalidate();
                progressBar.invalidate();
            }
        };

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        airportDatabase.loadAirportAndMap(venueId, "gate:a5", listeners);
    }

    private void setupButton( ViewGroup viewGroup, final String venueId ) {
        if ( this.button == null ) {
            this.button = new Button(this.getActivity().getApplicationContext());
            this.button.setBackgroundColor(Color.TRANSPARENT);
            this.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MapFragment.this.getActivity().getApplicationContext(), MapActivity.class);
                    intent.putExtra("venueId", venueId);
                    startActivity(intent);
                }
            });
        }

        this.button.setVisibility(View.VISIBLE);
        viewGroup.addView(this.button);
    }

}

