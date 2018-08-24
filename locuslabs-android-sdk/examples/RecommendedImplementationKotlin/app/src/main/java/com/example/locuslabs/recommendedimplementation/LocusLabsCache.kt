package com.example.locuslabs.recommendedimplementation

import java.io.File
import java.text.DateFormat
import java.util.TimeZone
import java.text.SimpleDateFormat

import android.content.Context

import com.locuslabs.sdk.configuration.Configuration

class LocusLabsCache protected constructor(val cacheDirectory: File) {

    val venueListContents: String?
        get() = Utilities.getFileAsString(this.pathForAsset(this.venueListAsset()))

    val latestInstalledVersion: String?
        get() {
            val file = File(this.pathForAsset(this.venueListAsset()))
            return if (file.exists()) {
                dateFormat!!.format(file.lastModified())
            } else {
                null
            }
        }

    init {
        if (!this.cacheDirectory.exists()) {
            this.cacheDirectory.mkdirs()
        }
    }

    fun pathForAsset(asset: String): String {
        return this.cacheDirectory.absolutePath + File.separator + LocusLabsCache.filenameForAsset(asset)
    }

    fun venueListAsset(): String {
        return "accounts-$accountId-v3.js"
    }

    companion object {

        private var dateFormat: DateFormat? = null

        init {
            dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss")
            dateFormat!!.timeZone = TimeZone.getTimeZone("UTC")
        }

        private val TAG = "LocusLabsMapPack"

        val LOCUSLABS = "locuslabs"

        fun filenameForAsset(asset: String): String {
            return asset.replace("/".toRegex(), "-")
        }

        val accountId: String
            get() = if (null != Configuration.shared && null != Configuration.shared.accountId) {
                Configuration.shared.accountId
            } else {
                MainActivity.ACCOUNT_ID
            }

        fun getDefaultCache(context: Context): LocusLabsCache {
            val cache = context.cacheDir
            val defaultDir = File(cache, File.separator + LOCUSLABS)
            return LocusLabsCache(defaultDir)
        }

        fun getCustomCache(directory: File): LocusLabsCache {
            return LocusLabsCache(directory)
        }
    }

}
