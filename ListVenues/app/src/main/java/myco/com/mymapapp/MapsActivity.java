package myco.com.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.locuslabs.sdk.configuration.LocusLabs;
import com.locuslabs.sdk.maps.model.VenueDatabase;
import com.locuslabs.sdk.maps.model.VenueInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;


public class MapsActivity extends Activity {

    // Static
    private static final int PERMISSIONS_REQUEST_CODE = 1000;

    private VenueDatabase               venueDatabase;
    private ArrayList<VenueInfo>        venues;
    private VenuesAdapter               venuesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        venues = new ArrayList<>();

        setContentView(R.layout.maps_activity);

        ListView venuesLV = findViewById(R.id.venuesLV);

        venuesAdapter = new VenuesAdapter(this);
        venuesLV.setAdapter(venuesAdapter);

        venuesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                VenueInfo venue = venues.get(position);

                Intent intent = new Intent(MapsActivity.this, MapActivity.class);
                intent.putExtra("VenueCode", venue.getVenueId());
                startActivity(intent);
            }
        });

        // Individual permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE);
        }
        // Global permissions (Android versions prior to m)
        else {

            initializeLocusLabsDatabase();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {

            // Assume all permissions were granted (in practice you would need to check each permission)
            if (grantResults.length > 0) {

                initializeLocusLabsDatabase();
            }
        }
    }

    private void initializeLocusLabsDatabase() {

        LocusLabs.registerOnReadyListener(new LocusLabs.OnReadyListener() {
            @Override
            public void onReady() {

                venueDatabase = new VenueDatabase();

                venueDatabase.listVenues(new VenueDatabase.OnListVenuesWithErrorListener() {
                    @Override
                    public void onListVenues(VenueInfo[] venueInfos, String s) {

                        for (VenueInfo venueInfo: venueInfos) {

                            venues.add(venueInfo);
                        }

                        Collections.sort(venues, new Comparator<VenueInfo>() {
                            @Override
                            public int compare(VenueInfo o1, VenueInfo o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });

                        venuesAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private class VenuesAdapter extends ArrayAdapter {

        private Context context;

        private class ViewHolder {

            TextView venueTV;
        }

        public VenuesAdapter(Context context) {

            super(context, R.layout.venue_row, venues);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.venue_row, parent, false);

                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);

                viewHolder.venueTV = convertView.findViewById(R.id.venueTV);
            }
            else {

                viewHolder = (ViewHolder) convertView.getTag();
            }

            VenueInfo venue = venues.get(position);
            viewHolder.venueTV.setText(venue.getName() +" (" +venue.getVenueId().toUpperCase() +")");

            return convertView;
        }
    }
}
