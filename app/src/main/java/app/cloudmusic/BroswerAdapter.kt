package app.cloudmusic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.support.v4.media.MediaBrowserCompat
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import app.cloudmusic.utils.imageloader.ImageLoader

/**
 * Created by Administrator on 2017/11/13.
 */

class BroswerAdapter(private val context: Context, private val arrayList: List<MediaBrowserCompat.MediaItem>) : RecyclerView.Adapter<BroswerAdapter.ViewHolder>() {
    private val imageLoader: ImageLoader
    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    init {
        imageLoader = ImageLoader(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_adapter_broswer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = arrayList[position]

        holder.title.text = item.description.title
        holder.description.text = item.description.subtitle.toString() + " - " + item.description.description
        imageLoader.DisplayImage(item.description.mediaUri!!.toString(), holder.cover)
        holder.itemView.setOnClickListener {
            if (onItemClickListener != null)
                onItemClickListener!!.onItemClick(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun getBitmap(url: String): Bitmap? {
        val time1 = System.currentTimeMillis()
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(url)
            var bitmap: Bitmap? = null

            val data = mmr.embeddedPicture

            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                val time2 = System.currentTimeMillis()
                return bitmap
            } else {
                return null
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }

        return null
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cover: ImageView
        var title: TextView
        var description: TextView

        init {
            cover = itemView.findViewById<View>(R.id.cover) as ImageView
            title = itemView.findViewById<View>(R.id.title) as TextView
            description = itemView.findViewById<View>(R.id.description) as TextView
        }
    }

    interface OnItemClickListener {
        fun onItemClick(holder: RecyclerView.ViewHolder, position: Int)
    }
}
