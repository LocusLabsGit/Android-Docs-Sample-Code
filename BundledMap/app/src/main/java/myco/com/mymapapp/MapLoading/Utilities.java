package myco.com.mymapapp.MapLoading;

import android.util.Log;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Utilities {
    private static final String TAG = "LocusLabsMapPack";

    public static String getFileAsString( String filename ) {
        File file = new File( filename );
        if ( !file.exists() ) {
            return null;
        }

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            baos = new ByteArrayOutputStream();

            int length;
            byte data[] = new byte[2048];

            while ((length = bis.read(data)) != -1) {
                baos.write(data, 0, length);
            }

            baos.flush();

            return baos.toString();
        }
        catch ( Exception exception ) {
            Log.e(TAG, exception.getMessage(), exception);
            return null;
        }
        finally {
            try { if ( fis != null ) fis.close(); } catch ( Exception exception ) {}
            try { if ( bis != null ) bis.close(); } catch ( Exception exception ) {}
            try { if ( baos != null ) baos.close(); } catch ( Exception exception ) {}
        }
    }

    public static void writeInputStreamToOutputStream( InputStream inputStream, OutputStream outputStream  ) throws Exception {
        BufferedOutputStream destination = null;
        try {
            int length;
            byte data[] = new byte[2048];
            destination = new BufferedOutputStream(outputStream);

            while ((length = inputStream.read(data)) != -1) {
                destination.write(data, 0, length);
            }

            destination.flush();
        }
        finally {
            try { if ( outputStream != null ) outputStream.close(); } catch ( Exception exception ) {}
            try { if ( destination != null ) destination.close(); } catch ( Exception exception ) {}
        }
    }
}
