package app.cloudmusic.ui

import android.content.Context

import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

import app.cloudmusic.R
import app.cloudmusic.utils.imageloader.ImageLoader
import app.cloudmusic.widget.CircleImageView

/**
 * Created by Administrator on 2018/1/22.
 */

class UltraPagerAdapter(context: Context, private val mediaUris: List<String>) : PagerAdapter() {
    private val imageLoader: ImageLoader

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getCount(): Int {
        return mediaUris.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val relativeLayout = LayoutInflater.from(container.context).inflate(R.layout.fragment_ablum, null) as RelativeLayout
        val circleImageView = relativeLayout.findViewById<CircleImageView>(R.id.imageView)
        container.addView(relativeLayout)
        imageLoader.DisplayImage(mediaUris[position], circleImageView)
        return relativeLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }
}