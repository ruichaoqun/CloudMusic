package app.cloudmusic.utils

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils

import org.w3c.dom.Text

import java.util.ArrayList

import app.cloudmusic.data.MediaDataInfo

/**
 * Created by Administrator on 2018/1/11.
 */

object MediaUtils {
    //格式化歌曲时长以及播放时长
    fun makeTimeString(milliSecs: Long): String {
        val sb = StringBuffer()
        val m = milliSecs / (60 * 1000)
        sb.append(if (m < 10) "0$m" else m)
        sb.append(":")
        val s = milliSecs % (60 * 1000) / 1000
        sb.append(if (s < 10) "0$s" else s)
        return sb.toString()
    }

    /**
     * 将自定义的MediaDataInfo转换成QueueItem
     * @param infos
     * @return
     */
    fun transformTogetQueue(infos: List<MediaDataInfo>): List<MediaSessionCompat.QueueItem> {
        val list = ArrayList<MediaSessionCompat.QueueItem>()
        for (i in infos.indices) {
            val info = infos[i]
            val descriptionCompat = MediaDescriptionCompat.Builder()
                    .setMediaId(info.mediaId)
                    .setMediaUri(Uri.parse(info.url))
                    .setTitle(info.title)
                    .setSubtitle(info.artist)
                    .build()
            val item = MediaSessionCompat.QueueItem(descriptionCompat, i.toLong())
            list.add(item)
        }
        return list
    }

    fun tarsformToMediaDataInfo(medias: List<MediaBrowserCompat.MediaItem>): List<MediaDataInfo> {
        val list = ArrayList<MediaDataInfo>()
        for (i in medias.indices) {
            val data = medias[i]
            val dataInfo = MediaDataInfo()
            dataInfo.mediaId = data.mediaId
            dataInfo.url = data.description.mediaUri!!.toString()
            list.add(dataInfo)
        }
        return list
    }

    fun getMusicIndexOnQueue(queue: List<MediaSessionCompat.QueueItem>, mediaId: String): Int {
        var index = -1
        for (i in queue.indices) {
            val s = queue[i].description.mediaId
            if (TextUtils.equals(mediaId, s)) {
                index = i
                break
            }
        }
        return index
    }

    fun getMusicIndexOnQueue(queue: List<MediaSessionCompat.QueueItem>,
                             queueId: Long): Int {
        var index = 0
        for (item in queue) {
            if (queueId == item.queueId) {
                return index
            }
            index++
        }
        return -1
    }
}
