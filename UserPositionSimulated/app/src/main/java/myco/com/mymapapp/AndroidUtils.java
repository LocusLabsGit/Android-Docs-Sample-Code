package myco.com.mymapapp;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.view.MapView;

import com.locuslabs.sdk.javascriptintegration.JavaScriptEnvironment;
import com.locuslabs.sdk.javascriptintegration.JavaScriptProxyObject;

import java.util.List;

import android.util.Log;

public class AndroidUtils {
    private static final String TAG = "AndroidUtils";

    private static final String JAVA_SCRIPT_CLASS = "locuslabs.maps.AndroidUtils";

    private transient JavaScriptProxyObject javaScriptProxyObject;
    private transient JavaScriptEnvironment javaScriptEnvironment;

    public AndroidUtils( JavaScriptEnvironment javaScriptEnvironment ) {
        this.javaScriptEnvironment = javaScriptEnvironment;
    }

    public void simulateWalking( final MapView mapView, final List<Position> points, final boolean shouldUpdateMapViewOnUserPositionChange) {
        Log.d( TAG, "simulateWalking: " + mapView.getUuid() );
        javaScriptEnvironment.registerOnReadyListener(new JavaScriptEnvironment.OnReadyListener() {
            @Override
            public void onReady() {
                if (javaScriptProxyObject == null) {
                    javaScriptProxyObject = new JavaScriptProxyObject(javaScriptEnvironment, this, JAVA_SCRIPT_CLASS);
                }
                javaScriptProxyObject.callJavaScriptMethod("simulateWalking", "uuid:" + mapView.getUuid(), points, shouldUpdateMapViewOnUserPositionChange);
            }
        });

    }

}
