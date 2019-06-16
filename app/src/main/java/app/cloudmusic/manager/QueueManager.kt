package app.cloudmusic.manager

import android.content.res.Resources
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils


import java.util.ArrayList
import java.util.Collections

import app.cloudmusic.data.MediaDataInfo
import app.cloudmusic.utils.MediaUtils

/**
 * Created by Administrator on 2017/11/15.
 */

class QueueManager(private val musicProvider: MusicProvider, private var repeatMode: Int, private val listener: MetaDataUpdateListener) {
    private var originalQueue: List<MediaSessionCompat.QueueItem>? = null
    private var mPlayingQueue: MutableList<MediaSessionCompat.QueueItem>? = null
    private var mCurrentIndex: Int = 0
    private var title: String? = null

    val currentMusic: MediaSessionCompat.QueueItem?
        get() = if (mPlayingQueue != null && mCurrentIndex >= 0 && mCurrentIndex < mPlayingQueue!!.size) {
            mPlayingQueue!![mCurrentIndex]
        } else null

    val isLastMusic: Boolean
        get() = if (mCurrentIndex == mPlayingQueue!!.size - 1) true else false

    val queueSize: Int
        get() = mPlayingQueue!!.size


    fun setCurrentQueueIndex(index: Int) {
        if (index >= 0 && index < mPlayingQueue!!.size) {
            mCurrentIndex = index
            listener.onCurrentQueueIndexUpdated(index)
        }
    }

    fun skipQueuePosition(amount: Int): Boolean {
        if (mPlayingQueue!!.size == 0)
            return false
        var index = mCurrentIndex + amount
        if (index < 0) {
            index = mPlayingQueue!!.size - 1
        } else {
            index %= mPlayingQueue!!.size
        }
        mCurrentIndex = index
        return true
    }


    fun setCurrentQueue(list: List<MediaDataInfo>, title: String, mediaId: String?) {
        var canReuseQueue = false
        if (TextUtils.equals(this.title, title)) {
            canReuseQueue = setCurrentQueueItem(mediaId)
        }

        if (!canReuseQueue) {
            originalQueue = MediaUtils.transformTogetQueue(list)
            mPlayingQueue = ArrayList()
            for (i in originalQueue!!.indices) {
                mPlayingQueue!!.add(originalQueue!![i])
            }
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_GROUP) {
                Collections.shuffle(mPlayingQueue!!)
            }
            this.title = title
            var index = 0
            if (mediaId != null) {
                index = MediaUtils.getMusicIndexOnQueue(mPlayingQueue!!, mediaId)
            }
            mCurrentIndex = Math.max(index, 0)
            listener.onQueueUpdated(title, mPlayingQueue)
        }
        updateMetadata()

    }

    fun setCurrentQueueItem(mediaId: String?): Boolean {
        val index = MediaUtils.getMusicIndexOnQueue(mPlayingQueue!!, mediaId!!)
        setCurrentQueueIndex(index)
        return index >= 0
    }

    fun setCurrentQueueItem(queueId: Long): Boolean {
        val index = MediaUtils.getMusicIndexOnQueue(mPlayingQueue!!, queueId)
        setCurrentQueueIndex(index)
        return index >= 0
    }

    fun setRepeatMode(repeatMode: Int) {
        this.repeatMode = repeatMode
        if (mPlayingQueue != null && mPlayingQueue!!.size > 0) {
            val mediaId = mPlayingQueue!![mCurrentIndex].description.mediaId
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_GROUP) {
                Collections.shuffle(mPlayingQueue!!)
                mCurrentIndex = MediaUtils.getMusicIndexOnQueue(mPlayingQueue!!, mediaId!!)
            } else {
                mPlayingQueue!!.clear()
                for (i in originalQueue!!.indices) {
                    mPlayingQueue!!.add(originalQueue!![i])
                }
                mCurrentIndex = MediaUtils.getMusicIndexOnQueue(mPlayingQueue!!, mediaId!!)
            }
        }
        listener.onQueueUpdated(title, mPlayingQueue)
    }

    fun updateMetadata() {
        val mediaId = currentMusic!!.description.mediaId
        val metadataCompat = musicProvider.getMusicById(mediaId!!)
                ?: throw IllegalArgumentException("无效Id " + mediaId!!)
        listener.onMetaDataChanged(metadataCompat)
    }


    interface MetaDataUpdateListener {
        fun onMetaDataChanged(metadata: MediaMetadataCompat)
        fun onError()
        fun onCurrentQueueIndexUpdated(queueIndex: Int)
        fun onQueueUpdated(title: String?, newQueue: List<MediaSessionCompat.QueueItem>?)
    }
}
