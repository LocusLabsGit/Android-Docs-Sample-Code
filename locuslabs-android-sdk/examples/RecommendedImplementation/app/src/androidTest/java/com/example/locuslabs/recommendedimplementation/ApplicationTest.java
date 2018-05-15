package com.example.locuslabs.recommendedimplementation;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private static final String TAG = "ApplicationTest";

    private boolean ready = false;
    private LocusLabsCache cache = null;
    private LocusLabsMapPackFinder finder = null;
    private LocusLabsMapPack pack = null;

    // These test will only pass after running the app once.
    // They are meant to prove that the map pack installation worked correctly.
    public ApplicationTest() {
        super(Application.class);
    }

    public void setUp() {
        try {
            this.finder = LocusLabsMapPackFinder.getMapPackFinder(this.getContext(), "A11F4Y6SZRXH4X");
            this.cache = LocusLabsCache.getDefaultCache(this.getContext());
            this.pack = new LocusLabsMapPack(cache, this.finder.getNewestMapPackName(), this.finder.getNewestMapPack());
        }
        catch ( Exception exception ) {
            Log.e( TAG, exception.getMessage() );
        }
    }

    public void testLocusLabsCache() {
        String assetPath = this.cache.pathForAsset( "accounts/A11F4Y6SZRXH4X/lax/2015-06-26T16:50:24/v2/laxVenueData.json");
        assertEquals("/data/data/com.example.locuslabs.recommendedimplementation/cache/locuslabs/accounts-A11F4Y6SZRXH4X-lax-2015-06-26T16:50:24-v2-laxVenueData.json", assetPath);

     /*   assertFalse(this.cache.assetExists("accounts/A11F4Y6SZRXH4X/lax/2015-06-26T16:50:24/v2/BADVenueData.json"));
        assertTrue(this.cache.assetExists("accounts/A11F4Y6SZRXH4X/lax/2015-06-26T16:50:24/v2/laxVenueData.json"));

        assertEquals("accounts/A11F4Y6SZRXH4X/v2.json", this.cache.venueListAsset());

        JSONObject venueList = this.cache.loadVenueList();
        assertTrue( venueList.has( "lax" ) );
        assertTrue( venueList.has( "sea" ) );
        assertFalse(venueList.has("mia"));*/
    }

    public void testLocusLabsMapPackFinder() {
        List<String> packs = this.finder.getAllMapPacks();

        assertEquals(1, packs.size());
        assertEquals("A11F4Y6SZRXH4X-2015-06-26T16-50-24.tar", this.finder.getNewestMapPackName());
    }

    public void testLocusLabsMapPack() {
        assertEquals( "A11F4Y6SZRXH4X-2015-06-26T16-50-24.tar", this.pack.getName() );
        assertEquals("2015-06-26T16-50-24", this.pack.getVersion() );
        assertFalse( this.pack.needsInstall() );
    }
}