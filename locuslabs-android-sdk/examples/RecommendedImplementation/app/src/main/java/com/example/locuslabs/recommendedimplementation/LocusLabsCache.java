package com.example.locuslabs.recommendedimplementation;

import android.content.Context;

import com.locuslabs.sdk.configuration.Configuration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class LocusLabsCache {

    private static DateFormat dateFormat = null;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        dateFormat.setTimeZone( TimeZone.getTimeZone("UTC") );
    }

    private static final String TAG = "LocusLabsMapPack";

    public static final String LOCUSLABS = "locuslabs";

    private final File cacheDir;

    public static String getAccountId() {
        if ( null != Configuration.shared && null != Configuration.shared.getAccountId()) {
            return Configuration.shared.getAccountId();
        }
        else {
            return MainActivity.ACCOUNT_ID;
        }
    }

    public static LocusLabsCache getDefaultCache( Context context ) {
        File cache = context.getCacheDir();
        File defaultDir = new File( cache, File.separator + LOCUSLABS );
        return new LocusLabsCache( defaultDir );
    }

    protected LocusLabsCache( File directory ) {
        this.cacheDir = directory;
        if ( !this.cacheDir.exists() ) {
            this.cacheDir.mkdirs();
        }
    }

    public File getCacheDirectory() {
        return this.cacheDir;
    }

    public String pathForAsset( String asset ) {
        return this.cacheDir.getAbsolutePath() + File.separator + "assets.locuslabs.com" +  File.separator + asset;
    }

    public String venueListAsset() {
        return "accounts/" + getAccountId() + "/v4.js";
    }

    public String getLatestInstalledVersion() {
        File file = new File( this.pathForAsset( this.venueListAsset() ) );
        if ( file.exists() ) {
            return dateFormat.format( file.lastModified() );
        }
        else {
            return null;
        }
    }

}
