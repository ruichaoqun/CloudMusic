package app.cloudmusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

import app.cloudmusic.R
import app.cloudmusic.utils.imageloader.ImageLoader

/**
 * Created by Administrator on 2018/1/12.
 */

object RenderScriptUtil {

    fun rsBlur(context: Context, source: String, radius: Int): Drawable? {
        val bitmap = ImageLoader.getBitmapFromMediaData(source)
                ?: return ContextCompat.getDrawable(context, R.mipmap.image_bg_default)
        return createBlurredImageFromBitmap(context, bitmap, radius)
    }

    fun rsBlur(context: Context, source: Bitmap, radius: Int): Bitmap {

//(1)
        val renderScript = RenderScript.create(context)

        Log.i("RenderScriptUtil", "scale size:" + source.width + "*" + source.height)

        // Allocate memory for Renderscript to work with
        //(2)
        val input = Allocation.createFromBitmap(renderScript, source)
        val output = Allocation.createTyped(renderScript, input.type)
        //(3)
        // Load up an instance of the specific script that we want to use.
        val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        //(4)
        scriptIntrinsicBlur.setInput(input)
        //(5)
        // Set the blur radius
        scriptIntrinsicBlur.setRadius(radius.toFloat())
        //(6)
        // Start the ScriptIntrinisicBlur
        scriptIntrinsicBlur.forEach(output)
        //(7)
        // Copy the output to the blurred bitmap
        output.copyTo(source)
        //(8)
        renderScript.destroy()

        return source
    }

    fun createBlurredImageFromBitmap(context: Context, bitmap: Bitmap, inSampleSize: Int): Drawable {

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
        script.setRadius(25f)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(blurTemplate)

        return BitmapDrawable(context.resources, blurTemplate)
    }
}
