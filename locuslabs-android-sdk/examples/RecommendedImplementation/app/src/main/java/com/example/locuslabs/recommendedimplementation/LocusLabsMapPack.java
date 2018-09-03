package com.example.locuslabs.recommendedimplementation;

import android.os.Handler;
import android.util.Log;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;

import org.tukaani.xz.XZInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

public class LocusLabsMapPack {
    private static final String TAG = "LocusLabsMapPack";

    private final LocusLabsCache cache;
    private final InputStream mapPack;
    private final Handler handler;
    private final String version;
    private final String mapPackName;

    public interface OnUnpackCallback { void onUnpack( boolean didInstall, Exception exception ); }

    public LocusLabsMapPack( LocusLabsCache cache, String mapPackName, InputStream mapPack ) {
        this.handler = new Handler();
        this.cache = cache;
        this.mapPack = mapPack;
        this.mapPackName = mapPackName;
        this.version = getVersion( mapPackName );
    }

    public String getVersion() {
        return this.version;
    }

    public String getName() {
        return this.mapPackName;
    }

    public boolean needsInstall() {
        String latestInstalledVersion = this.cache.getLatestInstalledVersion();
        String thisVersion = this.getVersion();
        boolean needsInstall = ( latestInstalledVersion == null || latestInstalledVersion.compareTo( thisVersion ) < 0 );

        // Print a log message
        String logMessage = "MapPack [" + mapPackName + "] does ";
        String logMessageReason;
        if (needsInstall) {
            logMessageReason = "null or older than ";
        }
        else {
            logMessage += "not ";
            logMessageReason = "newer than or same as ";
        }
        logMessageReason += "the MapPack's version [" + thisVersion + "]";
        logMessage += "need to be installed because latest installed version [" + latestInstalledVersion + "] is " + logMessageReason;
        Log.d(TAG, logMessage);

        return needsInstall;
    }

    public void unpack( final OnUnpackCallback callback ) {
        this.handler.post( new Runnable() {
            @Override
            public void run() {
                TarInputStream tis = null;
                try {
                    tis = new TarInputStream(new XZInputStream(LocusLabsMapPack.this.mapPack));

                    TarEntry entry;
                    while ((entry = tis.getNextEntry()) != null) {
                        if ( !entry.isDirectory()) {
                            Log.d(TAG, "Entry found in Map Pack : " + entry.getName());

                            File file = new File(LocusLabsMapPack.this.cache.getCacheDirectory(), entry.getName());
                            new File( file.getParent() ).mkdirs();

                            Log.d(TAG, "Writing File To Cache Directory : " + file);
                            Utilities.writeInputStreamToOutputStream( tis, new FileOutputStream(file) );
                        }
                    }

                    callback.onUnpack(true, null);
                }
                catch ( Exception exception ) {
                    Log.d( TAG, exception.toString(), exception );
                    callback.onUnpack( false, exception );
                }
                finally {
                    try { if ( tis != null ) tis.close(); } catch ( Exception exception ) {}
                }
            }
        });
    }

    protected String getVersion( String mapPackName ) {
        int firstIndex = mapPackName.indexOf( "-" ) + 1;
        int startIndex = mapPackName.indexOf( "-", firstIndex ) + 1;
        int endIndex = mapPackName.indexOf( ".tar" );

        return mapPackName.substring( startIndex, endIndex );
    }
}
