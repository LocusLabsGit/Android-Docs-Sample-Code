package com.example.locuslabs.recommendedimplementation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.drm.DrmInfoRequest.ACCOUNT_ID
import android.os.Build
import android.os.Bundle
import android.util.Log

import com.locuslabs.sdk.configuration.LocusLabs

class MainActivity : Activity() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // MY_ID is an app-defined int constant. The callback method gets the result of the request.
            val MY_ID = 1

            /*
               See also app/src/main/AndroidManifest.xml for explanations of why each of these permissions is requested
             */

            requestPermissions(arrayOf(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), MY_ID)
        }

        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        // If the Map Pack name is null, it will automatically search
        // for a map pack in the assets/locuslabs directory.  The search will
        // look for tar files that begin with "android-<ACCOUNT_ID>-<DATE>.
        val onUnpackCallback = object: LocusLabsMapPack.OnUnpackCallback {
            override fun onUnpack(didInstall: Boolean, exception: Exception?) {
                Log.d(TAG, didInstall.toString())
                if (exception != null) {
                    Log.e(TAG, exception.message)
                }
                //After the Map Pack is unpacked, show flight card information with a LocusLabs Map below the flight card information.
                showFlightCard()
            }
        }
        installMapPack(ACCOUNT_ID, MAP_PACK, onUnpackCallback )

    }

    protected fun showFlightCard() {
        LocusLabs.setLogLevel(LocusLabs.LogLevelTrace)
        // Now that the map pack is installed load the map.
        LocusLabs.initialize(this@MainActivity.applicationContext, ACCOUNT_ID) {
            val intent = Intent(applicationContext, FlightCardActivity::class.java)
            intent.putExtra("venueId", VENUE_ID)

            startActivity(intent)
        }
    }

    private fun installMapPack(accountId: String, mapPackName: String?, callback: LocusLabsMapPack.OnUnpackCallback) {
        val finder = LocusLabsMapPackFinder.getMapPackFinder(this.applicationContext, accountId)
        try {
            val cache = LocusLabsCache.getDefaultCache(this.applicationContext)

            var pack: LocusLabsMapPack? = null
            if (mapPackName != null && mapPackName.length > 0) {
                Log.d("LocusLabsMapPack", "Installing Name Map Pack: $mapPackName")
                pack = LocusLabsMapPack(cache, mapPackName, finder.getAsMapPack(mapPackName))
            } else {
                Log.d("LocusLabsMapPack", "Looking for Map Pack: ")
                val newestMapPackName = finder.newestMapPackName
                if (null != newestMapPackName) {
                    pack = LocusLabsMapPack(cache, newestMapPackName, finder.newestMapPack)
                }
            }

            if (null != pack) {
                if (pack.needsInstall()) {
                    Log.d("LocusLabsMapPack", "Need installation for pack: " + pack.name)
                    pack.unpack(callback)
                } else {
                    Log.d("LocusLabsMapPack", "No installation needed for pack: " + pack.name)
                    callback.onUnpack(true, null)
                }
            } else {
                Log.e("LocusLabsMapPack", "Could not instantiate a LocusLabsMapPack")
            }
        } catch (exception: Exception) {
            Log.e("LocusLabsMapPack", exception.toString())
            callback.onUnpack(false, exception)
        }

    }

    companion object {
        private val TAG = "MainActivity"
        val ACCOUNT_ID = "A11F4Y6SZRXH4X"
        private val VENUE_ID = "sea"

        // The Map Pack is expected to be a tar.gz file.
        // However, the name should be provided without the 'gz' extension.
        // The file must be placed in the Android assets/locuslabs directory.
        // IF the String MAP_PACK is null, the MapPackFinder will detect MapPacks based off the ACCOUNT_ID.
        private val MAP_PACK: String? = null //"android-A11F4Y6SZRXH4X-2015-09-04T22-02-48.tar";
    }


}
