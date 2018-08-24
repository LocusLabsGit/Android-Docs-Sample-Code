package com.example.locuslabs.recommendedimplementation

import android.content.Context
import android.content.res.AssetManager
import android.util.Log

import java.io.File
import java.io.InputStream

import java.util.ArrayList
import java.util.regex.Pattern
import java.util.regex.Matcher

class LocusLabsMapPackFinder protected constructor(private val assets: AssetManager, accountId: String) {
    private val packPattern: Pattern

    val allMapPacks: List<String>
        get() {
            val mapPacks = ArrayList<String>(1)
            try {
                val paths = this.assets.list(LocusLabsCache.LOCUSLABS)

                for (path in paths) {
                    Log.d(TAG, "Potential Pack : $path")

                    val m = this.packPattern.matcher(path)
                    if (m.find()) {
                        Log.d(TAG, "Matched as Map Pack : $path")
                        mapPacks.add(path)
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, exception.message, exception)
            }

            return mapPacks
        }

    val newestMapPackName: String?
        get() {
            var newestMapPack: String? = null
            for (pack in this.allMapPacks) {
                if (newestMapPack == null) {
                    newestMapPack = pack
                } else {
                    if (newestMapPack.compareTo(pack) < 0) {
                        newestMapPack = pack
                    }
                }
            }

            return newestMapPack
        }

    val newestMapPack: InputStream
        @Throws(Exception::class)
        get() = getAsMapPack(this.newestMapPackName)

    init {
        this.packPattern = Pattern.compile("^android-$accountId-([0-9A-Z\\\\-]+).tar.xz$")
    }

    @Throws(Exception::class)
    fun getAsMapPack(mapPackName: String?): InputStream {
        return this.assets.open(LocusLabsCache.LOCUSLABS + File.separator + mapPackName)
    }

    companion object {
        private val TAG = "LocusLabsMapPack"

        fun getMapPackFinder(context: Context, accountId: String): LocusLabsMapPackFinder {
            return LocusLabsMapPackFinder(context.assets, accountId)
        }
    }
}
