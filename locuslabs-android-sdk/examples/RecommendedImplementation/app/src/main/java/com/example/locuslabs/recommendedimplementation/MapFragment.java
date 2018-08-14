
package com.example.locuslabs.recommendedimplementation;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Venue;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.view.MapView;

public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";

    private Button button = null;
    private String venueId = null;

    private MapView mapView = null;
    private VenueDatabase venueDatabase = null;
    private ProgressBar progressBar;
    private TextView progressBarMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();

        //-----------------------------------
        // Be sure to close the mapView and
        // venueDatabase to release the memory
        // they consume.
        //-----------------------------------

        if (mapView != null) {
            mapView.close();
        }

        if (venueDatabase != null) {
            venueDatabase.close();
        }

        venueDatabase = null;
        mapView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Create a VenueDatabase which allows venues to be loaded.
        venueDatabase = new VenueDatabase();

        Intent receivedIntent = getActivity().getIntent();
        this.venueId = receivedIntent.getStringExtra("venueId");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 330, (float) 0.1);
            this.getView().setLayoutParams(params);
        }

        progressBar = getView().findViewById(R.id.progress);
        progressBarMessage = getView().findViewById(R.id.progressBarMessage);
        loadVenue(venueId);
    }

    private void loadVenue(final String venueId) {
        VenueDatabase.OnLoadVenueAndMapListeners listeners = new VenueDatabase.OnLoadVenueAndMapListeners();
        listeners.loadedInitialViewListener = new VenueDatabase.OnLoadedInitialViewListener() {
            @Override
            public void onLoadedInitialView(View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }

                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                ((ViewGroup) getView()).addView(view);
                venueDatabase.resumeLoadVenueAndMap();
            }
        };
        listeners.loadCompletedListener = new VenueDatabase.OnLoadCompletedListener() {
            @Override
            public void onLoadCompleted(Venue _venue, Map _map, final MapView _mapView, Floor floor, Marker marker) {
                mapView = _mapView;
                mapView.hideAllWidgets();

                ViewGroup viewGroup = (ViewGroup) getView();
                viewGroup.removeAllViews();
                viewGroup.addView(mapView);

                setupButton(viewGroup, venueId);
            }
        };

        listeners.loadProgressListener = new VenueDatabase.OnLoadProgressListener() {
            @Override
            public void onLoadProgress(Integer percentComplete) {
                progressBarMessage.setText(MapActivity.PROGRESS_MESSAGE + percentComplete + "%");
                progressBar.setProgress(percentComplete);
                progressBarMessage.invalidate();
                progressBar.invalidate();
            }
        };

        // The second parameter is an initial search option.
        // The map will zoom to the first matched POI.
        venueDatabase.loadVenueAndMap(venueId, "gate:a5", listeners);
    }

    private void setupButton(ViewGroup viewGroup, final String venueId) {
        if (this.button == null) {
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