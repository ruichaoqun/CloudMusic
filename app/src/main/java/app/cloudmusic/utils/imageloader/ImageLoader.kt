package app.cloudmusic.utils.imageloader

/**
 * Created by Harjot on 06-Apr-16.
 */

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.widget.ImageView


import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import java.util.Random
import java.util.WeakHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import app.cloudmusic.R

//图片管理
class ImageLoader(internal var ctx: Context) {

    internal var memoryCache = MemoryCache()
    internal var fileCache: FileCache
    private val imageViews = Collections.synchronizedMap(WeakHashMap<ImageView, String>())
    internal var executorService: ExecutorService
    var type = "none"
    internal var random: Random

    internal val stub_id = R.drawable.cover_default

    init {
        fileCache = FileCache(ctx)
        executorService = Executors.newFixedThreadPool(5)
        random = Random()
    }

    fun DisplayImage(url: String, imageView: ImageView) {
        imageViews[imageView] = url
        var bitmap: Bitmap? = null
        if (type == "none")
            bitmap = memoryCache.get(url)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            queuePhoto(url, imageView)
            if (type == "none")
                imageView.setImageResource(stub_id)
        }
    }

    private fun queuePhoto(url: String, imageView: ImageView) {
        val p = PhotoToLoad(url, imageView)
        executorService.submit(PhotosLoader(p))
    }

    private fun getBitmap(url: String?): Bitmap? {
        if (url == null) {
            return BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.cover_default)
        } else if (url.contains("all_playlist") || url.contains("folder")) {
            return null
        } else if (url.contains("https")) {
            val f = fileCache.getFile(url)

            //from SD cache
            val b = decodeFile(f)
            if (b != null)
                return b

            //from web
            try {
                var bitmap: Bitmap? = null
                val imageUrl = URL(url)
                val conn = imageUrl.openConnection() as HttpURLConnection
                conn.connectTimeout = 30000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                val `is` = conn.inputStream
                val os = FileOutputStream(f)
                Utils.CopyStream(`is`, os)
                os.close()
                bitmap = decodeFile(f)
                return bitmap
            } catch (ex: Throwable) {
                ex.printStackTrace()
                if (ex is OutOfMemoryError)
                    memoryCache.clear()
                return null
            }

        } else {

            val f = fileCache.getFile(url)
            val b = decodeFile(f)
            if (b != null)
                return b

            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(url)
                var bitmap: Bitmap? = null

                val data = mmr.embeddedPicture

                if (data != null) {
                    val os = FileOutputStream(f)
                    os.write(data)
                    os.close()
                    //                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    bitmap = decodeFile(f)
                    return bitmap
                } else {
                    return null
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
                if (ex is OutOfMemoryError)
                    memoryCache.clear()
                return null
            }

        }
    }

    //decodes image and scales it to reduce memory consumption
    private fun decodeFile(f: File): Bitmap? {
        try {
            //decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(f), null, o)

            //Find the correct scale value. It should be the power of 2.
            val REQUIRED_SIZE = 128
            var width_tmp = o.outWidth
            var height_tmp = o.outHeight
            var scale = 1
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break
                width_tmp /= 2
                height_tmp /= 2
                scale *= 2
            }

            //decode with inSampleSize
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            return BitmapFactory.decodeStream(FileInputStream(f), null, o2)
        } catch (e: FileNotFoundException) {
        }

        return null
    }

    //Task for the queue
    inner class PhotoToLoad(var url: String?, var imageView: ImageView)

    internal inner class PhotosLoader(var photoToLoad: PhotoToLoad) : Runnable {

        override fun run() {
            if (imageViewReused(photoToLoad))
                return
            val bmp = getBitmap(photoToLoad.url)
            if (type.contains("none") && photoToLoad.url != null)
                memoryCache.put(photoToLoad.url!!, bmp!!)
            if (imageViewReused(photoToLoad)) {
                return
            }
            val a = ctx as Activity
            a.runOnUiThread(Runnable {
                if (imageViewReused(photoToLoad)) {
                    return@Runnable
                }
                if (bmp != null) {
                    photoToLoad.imageView.setImageBitmap(bmp)
                } else {
                    if (type == "none")
                        photoToLoad.imageView.setImageResource(stub_id)
                    else {
                        val r = random.nextInt(210) + 45
                        val g = random.nextInt(210) + 45
                        val b = random.nextInt(210) + 45
                        photoToLoad.imageView.setImageResource(R.mipmap.ic_record_3)
                        photoToLoad.imageView.scaleType = ImageView.ScaleType.FIT_XY
                        photoToLoad.imageView.imageTintList = ColorStateList.valueOf(Color.rgb(r, g, b))
                    }
                }
            })
        }
    }

    internal fun imageViewReused(photoToLoad: PhotoToLoad): Boolean {
        val tag = imageViews[photoToLoad.imageView]
        return if (tag == null || tag != photoToLoad.url) true else false
    }

    fun clearCache() {
        memoryCache.clear()
        fileCache.clear()
    }

    companion object {

        fun getBitmapFromMediaData(url: String): Bitmap? {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(url)
                var bitmap: Bitmap? = null

                val data = mmr.embeddedPicture

                if (data != null) {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    return bitmap
                } else {
                    return null
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
                return null
            }

        }
    }

}
