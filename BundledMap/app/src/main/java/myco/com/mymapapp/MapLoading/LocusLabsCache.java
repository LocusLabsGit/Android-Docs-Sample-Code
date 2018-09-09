package myco.com.mymapapp.MapLoading;

import java.io.File;
import java.text.DateFormat;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import com.locuslabs.sdk.configuration.Configuration;

import android.content.Context;

import myco.com.mymapapp.MapActivity;

public class LocusLabsCache {

    private static DateFormat dateFormat = null;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        dateFormat.setTimeZone( TimeZone.getTimeZone("UTC") );
    }

    private static final String TAG = "LocusLabsMapPack";

    public static final String LOCUSLABS = "locuslabs";

    private final File cacheDir;

    public static String filenameForAsset( String asset ) {
        return asset.replaceAll( "/", "-" );
    }
    public static String getAccountId() {
        if ( null != Configuration.shared && null != Configuration.shared.getAccountId()) {
            return Configuration.shared.getAccountId();
        }
        else {
            return MapActivity.ACCOUNT_ID;
        }
    }

    public static LocusLabsCache getDefaultCache( Context context ) {
        File cache = context.getCacheDir();
        File defaultDir = new File( cache, File.separator + LOCUSLABS );
        return new LocusLabsCache( defaultDir );
    }

    public static LocusLabsCache getCustomCache( File directory ) {
        return new LocusLabsCache( directory );
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
        return this.cacheDir.getAbsolutePath() + File.separator + LocusLabsCache.filenameForAsset( asset );
    }

    public String venueListAsset() {
        return "accounts-" + getAccountId() + "-v3.js";
    }

    public String getVenueListContents() {
        return Utilities.getFileAsString( this.pathForAsset( this.venueListAsset() ) );
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
