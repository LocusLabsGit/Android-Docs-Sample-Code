package com.example.locuslabs.recommendedimplementation

import android.util.Log

import android.content.Context

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

object Utilities {
    private val TAG = "LocusLabsMapPack"

    fun getFileAsString(filename: String): String? {
        val file = File(filename)
        if (!file.exists()) {
            return null
        }

        var fis: FileInputStream? = null
        var bis: BufferedInputStream? = null
        var baos: ByteArrayOutputStream? = null
        try {
            fis = FileInputStream(file)
            bis = BufferedInputStream(fis)
            baos = ByteArrayOutputStream()

            val data = ByteArray(2048)
            var length: Int = bis.read(data)

            while (length != -1) {
                baos.write(data, 0, length)
                length = bis.read(data)
            }

            baos.flush()

            return baos.toString()
        } catch (exception: Exception) {
            Log.e(TAG, exception.message, exception)
            return null
        } finally {
            try {
                if (fis != null) fis.close()
            } catch (exception: Exception) {
            }

            try {
                if (bis != null) bis.close()
            } catch (exception: Exception) {
            }

            try {
                if (baos != null) baos.close()
            } catch (exception: Exception) {
            }

        }
    }

    @Throws(Exception::class)
    fun writeInputStreamToOutputStream(inputStream: InputStream, outputStream: OutputStream?) {
        var destination: BufferedOutputStream? = null
        try {
            val data = ByteArray(2048)
            var length: Int = inputStream.read(data)
            destination = BufferedOutputStream(outputStream!!)

            while (length != -1) {
                destination.write(data, 0, length)
                length = inputStream.read(data)
            }

            destination.flush()
        } finally {
            try {
                outputStream?.close()
            } catch (exception: Exception) {
            }

            try {
                if (destination != null) destination.close()
            } catch (exception: Exception) {
            }

        }
    }
}
