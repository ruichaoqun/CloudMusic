package app.cloudmusic.utils.imageloader

/**
 * Created by Harjot on 06-Apr-16.
 */

import android.content.Context

import java.io.File

class FileCache(context: Context) {

    private var cacheDir: File? = null

    init {
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED)
            cacheDir = File(android.os.Environment.getExternalStorageDirectory(), "TTImages_cache")
        else
            cacheDir = context.cacheDir
        if (!cacheDir!!.exists())
            cacheDir!!.mkdirs()
    }

    fun getFile(url: String): File {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        val filename = url.hashCode().toString()
        //Another possible solution (thanks to grantland)
        //String filename = URLEncoder.encode(url);
        return File(cacheDir, filename)

    }

    fun clear() {
        val files = cacheDir!!.listFiles() ?: return
        for (f in files)
            f.delete()
    }

}
