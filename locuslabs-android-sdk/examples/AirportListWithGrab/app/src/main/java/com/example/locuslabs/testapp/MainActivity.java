package com.example.locuslabs.testapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.cursus.sky.grabsdk.GrabStyles;
import com.locuslabs.sdk.configuration.Configuration;
import com.locuslabs.sdk.configuration.LocusLabs;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final String ACCOUNT_ID = "A11F4Y6SZRXH4X";
    public static final String GRAB_CUSTOMER_CODE = "abc2e5a1cdcebc486a6710b484aeaf9d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onResume() {
        super.onResume();
        showVenuesList();
    }

    protected void showVenuesList() {
        LocusLabs.setLogLevel(LocusLabs.LogLevelDebug);
        LocusLabs.initialize(MainActivity.this.getApplicationContext(), ACCOUNT_ID, new LocusLabs.OnReadyListener() {
            public void onReady() {
                // Call setGrabCustomerId in this onReady() before calling other LocusLabs APIs
                Configuration.shared.setGrabCustomerId(GRAB_CUSTOMER_CODE);

                // Set a custom GrabStyle (optional)
                //Configuration.shared.setGrabStyles(getCustomGrabStyles());

                Intent intent = new Intent(getApplicationContext(), VenueListActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Example of how to create a custom GrabStyles dictionary.
     *
     * See https://docs.poweredbygrab.com/docs/android-framework-style-tags
     *
     * @return a customer GrabStyles dictionary
     */
    private GrabStyles getCustomGrabStyles() {
        GrabStyles grabStyles = new GrabStyles();
        grabStyles.setActivityViewBackgroundColor(Color.rgb(30, 124, 196));
        grabStyles.setActivityViewTextColor(Color.rgb(10, 35, 100));
        grabStyles.setCartButtonBackgroundColor(Color.WHITE);
        grabStyles.setFoodRetailServicesButtonBackgroundColor(Color.WHITE);
        grabStyles.setFoodRetailServicesButtonTextColor(Color.rgb(30, 124, 196));
        grabStyles.setGrabContinueButtonBackgroundColor(Color.rgb(30, 124, 196));
        grabStyles.setGrabHighlightedTextColor(Color.rgb(92, 86, 116));
        grabStyles.setGrabWebServiceModalActivityViewBackgroundColor(Color.rgb(0, 70, 127));
        grabStyles.setGrabWebServiceModalActivityTextBackgroundColor(Color.WHITE);
        grabStyles.setMenuFeaturedItemBackgroundColor(Color.LTGRAY);
        grabStyles.setMenuOptionSelectionColor(Color.RED);
        grabStyles.setNavigationControllerBackgroundColor(Color.rgb(30, 124, 196));
        grabStyles.setNavigationControllerTextColor(Color.WHITE);
        grabStyles.setOrderSummaryTabLeftColor(Color.rgb(8, 122, 181));
        grabStyles.setOrderSummaryTabRightColor(Color.rgb(92, 86, 116));
        grabStyles.setStoreMenuCategoryButtonBackgroundColor(Color.rgb(30, 124, 196));
        grabStyles.setStoreMenuCategoryButtonBackgroundColorNonHighlighted(Color.GRAY);
        grabStyles.setStoreMenuInfoBackgroundColor(Color.rgb(30, 124, 196));
        grabStyles.setBottomNavigationButtonColor(Color.rgb(10, 20, 38));
        grabStyles.setBottomNavigationTextColor(Color.WHITE);
        return grabStyles;
    }
}
