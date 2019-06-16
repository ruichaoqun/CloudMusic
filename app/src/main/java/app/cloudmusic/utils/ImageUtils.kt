/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package app.cloudmusic.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore

import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object ImageUtils {

    private val sBitmapOptionsCache = BitmapFactory.Options()
    private val sBitmapOptions = BitmapFactory.Options()
    private val sArtworkUri = Uri
            .parse("content://media/external/audio/albumart")


    /*
    * private static class FastBitmapDrawable extends Drawable { private Bitmap
    * mBitmap; public FastBitmapDrawable(Bitmap b) { mBitmap = b; }
    *
    * @Override public void draw(Canvas canvas) { canvas.drawBitmap(mBitmap, 0,
    * 0, null); }
    *
    * @Override public int getOpacity() { return PixelFormat.OPAQUE; }
    *
    * @Override public void setAlpha(int alpha) { }
    *
    * @Override public void setColorFilter(ColorFilter cf) { } }
    */
    private val proj_album = arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.NUMBER_OF_SONGS, MediaStore.Audio.Albums.ARTIST)

    init {
        // for the cache,
        // 565 is faster to decode and display
        // and we don't want to dither here because the image will be scaled
        // down later
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565
        sBitmapOptionsCache.inDither = false

        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565
        sBitmapOptions.inDither = false
    }

    fun createBlurredImageFromBitmap(bitmap: Bitmap, context: Context, inSampleSize: Int): Drawable {

        val rs = RenderScript.create(context)
        val options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageInByte = stream.toByteArray()
        val bis = ByteArrayInputStream(imageInByte)
        val blurTemplate = BitmapFactory.decodeStream(bis, null, options)

        val input = Allocation.createFromBitmap(rs, blurTemplate)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(8f)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(blurTemplate)

        return BitmapDrawable(context.resources, blurTemplate)
    }

    fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        val COLORDRAWABLE_DIMENSION = 2

        if (drawable == null) {
            return null
        }

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        try {
            val bitmap: Bitmap

            if (drawable is ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG)
            } else {
                bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, BITMAP_CONFIG)
            }

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: OutOfMemoryError) {
            return null
        }

    }

    fun getArtworkQuick(file: File, w: Int,
                        h: Int): Bitmap? {
        var w = w
        // NOTE: There is in fact a 1 pixel border on the right side in the
        // ImageView
        // used to display this drawable. Take it into account now, so we don't
        // have to
        // scale later.
        w -= 1
        try {
            var sampleSize = 1

            // Compute the closest power-of-two scale factor
            // and pass that to sBitmapOptionsCache.inSampleSize, which will
            // result in faster decoding and better quality
            sBitmapOptionsCache.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, sBitmapOptionsCache)
            var nextWidth = sBitmapOptionsCache.outWidth shr 1
            var nextHeight = sBitmapOptionsCache.outHeight shr 1
            while (nextWidth > w && nextHeight > h) {
                sampleSize = sampleSize shl 1
                nextWidth = nextWidth shr 1
                nextHeight = nextHeight shr 1
            }

            sBitmapOptionsCache.inSampleSize = sampleSize
            sBitmapOptionsCache.inJustDecodeBounds = false
            var b: Bitmap? = BitmapFactory.decodeFile(file.absolutePath, sBitmapOptionsCache)

            if (b != null) {
                // finally rescale to exactly the size we need
                if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                    val tmp = Bitmap.createScaledBitmap(b, w, h, true)
                    // Bitmap.createScaledBitmap() can return the same
                    // bitmap
                    if (tmp != b)
                        b.recycle()
                    b = tmp
                }
            }

            return b
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun isAlbumUri(context: Context, uri: Uri?): Boolean {
        // NOTE: There is in fact a 1 pixel border on the right side in the
        // ImageView
        // used to display this drawable. Take it into account now, so we don't
        // have to
        // scale later.
        val res = context.contentResolver
        //        Cursor cursor = res.query(uri, new String[]{}, null, null, null);
        //        if(cursor == null){
        //            return null;
        //        }else {

        if (uri != null) {
            var fd: ParcelFileDescriptor? = null
            try {
                fd = res.openFileDescriptor(uri, "r")
                if (fd == null) {
                    return false
                }

                Log.e("album", "is true")
                return true
            } catch (e: FileNotFoundException) {
            } finally {
                if (fd == null) {
                    return false
                }
                try {
                    fd?.close()
                } catch (e: IOException) {
                }

            }
        }
        return false

    }

    fun getArtworkQuick(context: Context, uri: Uri?, w: Int,
                        h: Int): Bitmap? {
        var w = w
        // NOTE: There is in fact a 1 pixel border on the right side in the
        // ImageView
        // used to display this drawable. Take it into account now, so we don't
        // have to
        // scale later.
        w -= 1
        val res = context.contentResolver
        //        Cursor cursor = res.query(uri, new String[]{}, null, null, null);
        //        if(cursor == null){
        //            return null;
        //        }else {

        if (uri != null) {
            var fd: ParcelFileDescriptor? = null
            try {
                fd = res.openFileDescriptor(uri, "r")
                if (fd == null) {
                    return null
                }
                var sampleSize = 1

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true
                BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, sBitmapOptionsCache)
                var nextWidth = sBitmapOptionsCache.outWidth shr 1
                var nextHeight = sBitmapOptionsCache.outHeight shr 1
                while (nextWidth > w && nextHeight > h) {
                    sampleSize = sampleSize shl 1
                    nextWidth = nextWidth shr 1
                    nextHeight = nextHeight shr 1
                }

                sBitmapOptionsCache.inSampleSize = sampleSize
                sBitmapOptionsCache.inJustDecodeBounds = false
                var b: Bitmap? = BitmapFactory.decodeFileDescriptor(
                        fd.fileDescriptor, null, sBitmapOptionsCache)

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        val tmp = Bitmap.createScaledBitmap(b, w, h, true)
                        // Bitmap.createScaledBitmap() can return the same
                        // bitmap
                        if (tmp != b)
                            b.recycle()
                        b = tmp
                    }
                }

                return b
            } catch (e: FileNotFoundException) {
            } finally {
                try {
                    fd?.close()
                } catch (e: IOException) {
                }

            }
        }
        return null
    }

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    fun getArtworkQuick(context: Context, album_id: Long, w: Int,
                        h: Int): Bitmap? {
        var w = w
        // NOTE: There is in fact a 1 pixel border on the right side in the
        // ImageView
        // used to display this drawable. Take it into account now, so we don't
        // have to
        // scale later.
        w -= 1
        val res = context.contentResolver
        val uri = ContentUris.withAppendedId(sArtworkUri, album_id)
        //        Cursor cursor = res.query(uri, new String[]{}, null, null, null);
        //        if(cursor == null){
        //            return null;
        //        }else {

        if (uri != null) {
            var fd: ParcelFileDescriptor? = null
            try {
                fd = res.openFileDescriptor(uri, "r")
                if (fd == null) {
                    return null
                }
                var sampleSize = 1

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true
                BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, sBitmapOptionsCache)
                var nextWidth = sBitmapOptionsCache.outWidth shr 1
                var nextHeight = sBitmapOptionsCache.outHeight shr 1
                while (nextWidth > w && nextHeight > h) {
                    sampleSize = sampleSize shl 1
                    nextWidth = nextWidth shr 1
                    nextHeight = nextHeight shr 1
                }

                sBitmapOptionsCache.inSampleSize = sampleSize
                sBitmapOptionsCache.inJustDecodeBounds = false
                var b: Bitmap? = BitmapFactory.decodeFileDescriptor(
                        fd.fileDescriptor, null, sBitmapOptionsCache)

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        val tmp = Bitmap.createScaledBitmap(b, w, h, true)
                        // Bitmap.createScaledBitmap() can return the same
                        // bitmap
                        if (tmp != b)
                            b.recycle()
                        b = tmp
                    }
                }

                return b
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fd?.close()
                } catch (e: IOException) {
                }

            }
        }
        return null
    }

}
