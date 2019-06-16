package app.cloudmusic.manager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log

import com.google.android.exoplayer2.Player

import java.util.ArrayList
import java.util.Random

import app.cloudmusic.Contaces
import app.cloudmusic.data.MediaDataInfo
import app.cloudmusic.utils.LogHelper
import app.cloudmusic.utils.MediaSharePreference


/**
 * Created by Administrator on 2017/11/13.
 */

class PlaybackManager(private val serviceCallback: PlaybackServiceCallback, private val musicProvider: MusicProvider, val playback: Playback?, private val queueManager: QueueManager) : Playback.Callback {

    val mediaSessionCallback: MediaSessionCompat.Callback
    private var mediaSessionCompat: MediaSessionCompat? = null
    private var repeatMode = MediaSharePreference.instances.repeatMode
    private val random = Random()

    private val availableActions: Long
        get() {
            var actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            if (playback!!.isPlaying) {
                actions = actions or PlaybackStateCompat.ACTION_PAUSE
            } else {
                actions = actions or PlaybackStateCompat.ACTION_PLAY
            }
            return actions
        }

    init {
        playback?.setCallback(this)
        mediaSessionCallback = MediaSessionCallback()
    }

    fun setMediaSessionCompat(mediaSessionCompat: MediaSessionCompat) {
        this.mediaSessionCompat = mediaSessionCompat
    }

    /**
     * Handle a request to play music
     */
    fun handlePlayRequest() {
        LogHelper.d(TAG, "handlePlayRequest: mState=" + playback!!.state)
        val item = queueManager.currentMusic
        if (item != null) {
            serviceCallback.onPlaybackStart()
            playback.play(item)
        }
    }

    fun handlePauseRequest() {
        if (playback!!.isPlaying) {
            playback.pause()
            serviceCallback.onPlaybackStop()
        }
    }

    fun handleStopRequest(withError: String) {
        playback!!.stop(true)
        serviceCallback.onPlaybackStop()
        updatePlaybackState(withError)
    }


    //Playback 回调
    /**
     * 播放器播放完当前音乐，跳转播放下一首
     */
    override fun onCompletion() {
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ONE ->
                //单曲循环
                mediaSessionCallback.onSeekTo(0)
            PlaybackStateCompat.REPEAT_MODE_ALL ->
                //列表循环
                mediaSessionCallback.onSkipToNext()
            PlaybackStateCompat.REPEAT_MODE_GROUP ->
                //重新定义为随机播放
                mediaSessionCallback.onSkipToNext()
        }
    }

    override fun onPlaybackStatusChanged(state: Int) {
        updatePlaybackState(null)
    }

    override fun onError(error: String) {

    }

    override fun setCurrentMediaId(mediaId: String) {

    }

    /**
     * 更新播放状态
     * @param error
     */
    fun updatePlaybackState(error: String?) {
        LogHelper.d(TAG, "updatePlaybackState, playback state=" + playback!!.state)
        var position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
        if (playback != null && playback.isConnected) {
            position = playback.currentStreamPosition
        }


        val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(availableActions)

        //设置自定义动作，例如设置喜欢与不喜欢，是否收藏等等的回调
        //setCustomAction(stateBuilder);
        var state = playback.state

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error)
            state = PlaybackStateCompat.STATE_ERROR
        }

        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime())

        // Set the activeQueueItemId if the current index is valid.
        //        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        //        if (currentMusic != null) {
        //            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        //        }

        //更新播放状态，不单单是播放状态，还可以自定义状态
        serviceCallback.onPlaybackStateUpdated(stateBuilder.build())

        //如果播放器进入暂停状态或者播放中状态，在通知栏显示
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            serviceCallback.onNotificationRequired()
        }
    }


    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            handlePlayRequest()
        }

        override fun onPause() {
            handlePauseRequest()
        }

        override fun onStop() {
            handleStopRequest("")
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            super.onAddQueueItem(description)
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
            super.onAddQueueItem(description, index)
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
        }

        override fun onFastForward() {
            super.onFastForward()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            extras!!.classLoader = javaClass.classLoader
            val list = extras.getParcelableArrayList<MediaDataInfo>("list")
            val title = extras.getString("title")
            queueManager.setCurrentQueue(list, title, mediaId)
            handlePlayRequest()
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            super.onRemoveQueueItem(description)
        }

        override fun onRemoveQueueItemAt(index: Int) {
            super.onRemoveQueueItemAt(index)
        }

        override fun onSeekTo(pos: Long) {
            playback!!.seekTo(pos.toInt().toLong())
        }

        override fun onSetRating(rating: RatingCompat?) {
            super.onSetRating(rating)
        }

        /**
         * 设置新的播放模式
         * @param repeatMode
         */
        override fun onSetRepeatMode(repeatMode: Int) {
            this@PlaybackManager.repeatMode = repeatMode
            queueManager.setRepeatMode(repeatMode)
            serviceCallback.onRepeatUpdated(repeatMode)
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            super.onSetShuffleMode(shuffleMode)
        }

        override fun onSkipToNext() {
            if (queueManager.skipQueuePosition(1)) {
                handlePlayRequest()
            } else {
                handleStopRequest("Cannot skip")
            }
            queueManager.updateMetadata()
        }

        override fun onSkipToPrevious() {
            if (queueManager.skipQueuePosition(-1)) {
                handlePlayRequest()
            } else {
                handleStopRequest("Cannot skip")
            }
            queueManager.updateMetadata()
        }

        override fun onSkipToQueueItem(id: Long) {
            if (queueManager.setCurrentQueueItem(id)) {
                handlePlayRequest()
            } else {
                handleStopRequest("Cannot skip")
            }
            queueManager.updateMetadata()
        }
    }

    interface PlaybackServiceCallback {
        fun onPlaybackStart()

        fun onNotificationRequired()

        fun onPlaybackStop()

        fun onPlaybackStateUpdated(newState: PlaybackStateCompat)

        fun onRepeatUpdated(repeatMode: Int)
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(PlaybackManager::class.java)
    }
}
