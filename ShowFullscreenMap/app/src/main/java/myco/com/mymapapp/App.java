package myco.com.mymapapp;

import android.app.Application;
import com.locuslabs.sdk.configuration.LocusLabs;


/**
 * Created by juankruger on 13/03/18.
 */

public class App extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        LocusLabs.initialize(getApplicationContext(),"A11F4Y6SZRXH4X");
    }
}
