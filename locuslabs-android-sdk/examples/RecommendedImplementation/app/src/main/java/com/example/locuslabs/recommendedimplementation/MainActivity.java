package com.example.locuslabs.recommendedimplementation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.locuslabs.sdk.configuration.LocusLabs;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final String ACCOUNT_ID = "A11F4Y6SZRXH4X";
    private static final String VENUE_ID = "sea";

    // The Map Pack is expected to be a tar.gz file.
    // However, the name should be provided without the 'gz' extension.
    // The file must be placed in the Android assets/locuslabs directory.
    // IF the String MAP_PACK is null, the MapPackFinder will detect MapPacks based off the ACCOUNT_ID.
    private static final String MAP_PACK = null; //"android-A11F4Y6SZRXH4X-2015-09-04T22-02-48.tar";

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // MY_ID is an app-defined int constant. The callback method gets the result of the request.
            int MY_ID = 1;

            /*
               See also app/src/main/AndroidManifest.xml for explanations of why each of these permissions is requested
             */

            requestPermissions(new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, MY_ID);
        }

        setContentView(R.layout.activity_main);
    }

    protected void onResume() {
        super.onResume();

        // If the Map Pack name is null, it will automatically search
        // for a map pack in the assets/locuslabs directory.  The search will
        // look for tar files that begin with "android-<ACCOUNT_ID>-<DATE>.
        installMapPack(ACCOUNT_ID, MAP_PACK, new LocusLabsMapPack.OnUnpackCallback() {
            public void onUnpack(boolean didInstall, Exception exception) {
                Log.d(TAG, String.valueOf(didInstall));
                if (exception != null) {
                    Log.e(TAG, exception.getMessage());
                }
                //After the Map Pack is unpacked, show flight card information with a LocusLabs Map below the flight card information.
                showFlightCard();
            }
        });
    }

    protected void showFlightCard() {
        LocusLabs.setLogLevel(LocusLabs.LogLevelDebug);
        // Now that the map pack is installed load the map.
        LocusLabs.initialize(MainActivity.this.getApplicationContext(), ACCOUNT_ID, new LocusLabs.OnReadyListener() {
            public void onReady() {
                Intent intent = new Intent(getApplicationContext(), FlightCardActivity.class);
                intent.putExtra("venueId", VENUE_ID);

                startActivity(intent);
            }
        });
    }

    private void installMapPack( String accountId, String mapPackName, LocusLabsMapPack.OnUnpackCallback callback ) {
        LocusLabsMapPackFinder finder = LocusLabsMapPackFinder.getMapPackFinder( this.getApplicationContext(), accountId );
        try {
            LocusLabsCache cache = LocusLabsCache.getDefaultCache(this.getApplicationContext());

            LocusLabsMapPack pack = null;
            if ( mapPackName != null && mapPackName.length() > 0 ) {
                Log.d("LocusLabsMapPack", "Installing Name Map Pack: " + mapPackName );
                pack = new LocusLabsMapPack( cache, mapPackName, finder.getAsMapPack( mapPackName ) );
            }
            else {
                Log.d("LocusLabsMapPack", "Looking for Map Pack: " );
                pack = new LocusLabsMapPack( cache, finder.getNewestMapPackName(), finder.getNewestMapPack() );
            }

            if ( pack.needsInstall() ) {
                Log.d("LocusLabsMapPack", "Need installation for pack: " + pack.getName());
                pack.unpack( callback );
            }
            else {
                Log.d("LocusLabsMapPack", "No installation needed for pack: " + pack.getName());
                callback.onUnpack(true, null);
            }
        }
        catch ( Exception exception ) {
            Log.e( "LocusLabsMapPack", exception.toString() );
            callback.onUnpack(false, exception);
        }
    }


}
