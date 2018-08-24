package com.example.locuslabs.recommendedimplementation

import android.os.Handler
import android.util.Log

import org.kamranzafar.jtar.TarEntry
import org.kamranzafar.jtar.TarInputStream

import org.tukaani.xz.XZInputStream
import java.io.File
import java.io.InputStream
import java.io.FileOutputStream

class LocusLabsMapPack(private val cache: LocusLabsCache, val name: String, private val mapPack: InputStream) {
    private val handler: Handler
    val version: String

    interface OnUnpackCallback {
        fun onUnpack(didInstall: Boolean, exception: Exception?)
    }

    init {
        this.handler = Handler()
        this.version = getVersion(name)
    }

    fun needsInstall(): Boolean {
        return this.cache.latestInstalledVersion == null || this.cache.latestInstalledVersion!!.compareTo(this.version) < 0
    }

    fun unpack(callback: OnUnpackCallback) {
        this.handler.post {
            var tis: TarInputStream? = null
            try {
                tis = TarInputStream(XZInputStream(this@LocusLabsMapPack.mapPack))

                var entry: TarEntry? = tis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        Log.d(TAG, "Entry found in Map Pack : " + entry.name)

                        val file = File(this@LocusLabsMapPack.cache.cacheDirectory, entry.name)
                        File(file.parent).mkdirs()

                        Log.d(TAG, "Writing File To Cache Directory : $file")
                        Utilities.writeInputStreamToOutputStream(tis, FileOutputStream(file))
                    }
                    entry = tis.nextEntry
                }

                callback.onUnpack(true, null)
            } catch (exception: Exception) {
                Log.d(TAG, exception.toString(), exception)
                callback.onUnpack(false, exception)
            } finally {
                try {
                    if (tis != null) tis.close()
                } catch (exception: Exception) {
                }

            }
        }
    }

    protected fun getVersion(mapPackName: String): String {
        val firstIndex = mapPackName.indexOf("-") + 1
        val startIndex = mapPackName.indexOf("-", firstIndex) + 1
        val endIndex = mapPackName.indexOf(".tar")

        return mapPackName.substring(startIndex, endIndex)
    }

    companion object {
        private val TAG = "LocusLabsMapPack"
    }
}
