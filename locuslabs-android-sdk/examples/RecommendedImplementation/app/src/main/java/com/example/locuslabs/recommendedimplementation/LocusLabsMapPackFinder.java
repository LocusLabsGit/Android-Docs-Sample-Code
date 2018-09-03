package com.example.locuslabs.recommendedimplementation;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LocusLabsMapPackFinder {
    private static final String TAG = "LocusLabsMapPack";

    private final AssetManager assets;
    private final Pattern packPattern;

    public static LocusLabsMapPackFinder getMapPackFinder( Context context, String accountId ) {
        return new LocusLabsMapPackFinder( context.getAssets(), accountId );
    }

    protected LocusLabsMapPackFinder( AssetManager assets, String accountId ) {
        this.assets = assets;
        this.packPattern = Pattern.compile( "^android-" + accountId + "-([0-9A-Z\\\\-]+).tar.xz$" );
    }

    public List<String> getAllMapPacks() {
        List<String> mapPacks = new ArrayList<String>(1);
        try {
            String[] paths = this.assets.list( LocusLabsCache.LOCUSLABS );

            for (String path : paths) {
                Log.d(TAG, "Potential Pack : " + path );

                Matcher m = this.packPattern.matcher(path);
                if ( m.find() ) {
                    Log.d( TAG, "Matched as Map Pack : " + path );
                    mapPacks.add( path );
                }
            }
        }
        catch ( Exception exception ) {
            Log.e( TAG, exception.getMessage(), exception );
        }

        return mapPacks;
    }

    public String getNewestMapPackName() {
        String newestMapPack = null;
        for ( String pack : this.getAllMapPacks() ) {
            if ( newestMapPack == null ) {
                newestMapPack = pack;
            }
            else {
                if ( newestMapPack.compareTo( pack ) < 0 ) {
                    newestMapPack = pack;
                }
            }
        }

        return newestMapPack;
    }

    public InputStream getAsMapPack( String mapPackName ) throws Exception {
        return this.assets.open( LocusLabsCache.LOCUSLABS + File.separator + mapPackName );
    }
}
