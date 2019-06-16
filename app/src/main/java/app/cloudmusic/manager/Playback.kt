package app.cloudmusic.manager

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

/**
 * Created by Administrator on 2017/11/16.
 */

interface Playback {

    /**
     * Get the current [android.media.session.PlaybackState.getState]
     * 获取播放状态
     */
    /**
     * Set the latest playback state as determined by the caller.
     * 设置播放状态
     */
    var state: Int

    /**
     * @return boolean that indicates that this is ready to be used.
     * 是否已连接
     */
    val isConnected: Boolean

    /**
     * @return boolean indicating whether the player is playing or is supposed to be
     * playing when we gain audio focus.
     * 是否正在播放
     */
    val isPlaying: Boolean

    /**
     * @return pos if currently playing an item
     * 获取当前播放位置
     */
    val currentStreamPosition: Long

    /**
     *
     * @return the current media Id being processed in any state or null.
     * 获取当前歌曲的id
     */
    /**
     * Set the current mediaId. This is only used when switching from one
     * playback to another.
     * 设置当前歌曲的id
     *
     * @param mediaId to be set as the current.
     */
    var currentMediaId: String?

    /**
     * Start/setup the playback.
     * Resources/listeners would be allocated by implementations.
     * 开始
     */
    fun start()

    /**
     * Stop the playback. All resources can be de-allocated by implementations here.
     * @param notifyListeners if true and a callback has been set by setCallback,
     * callback.onPlaybackStatusChanged will be called after changing
     * the state.
     * 停止
     */
    fun stop(notifyListeners: Boolean)

    /**
     * Set the current position. Typically used when switching players that are in
     * paused state.
     * 设置当前播放位置
     * @param pos position in the stream
     */
    fun setCurrentStreamPosition(pos: Int)

    /**
     * Query the underlying stream and update the internal last known stream position.
     * 更新最后歌曲播放位置
     */
    fun updateLastKnownStreamPosition()

    /**
     * @param item to play
     * 播放
     */
    fun play(item: MediaSessionCompat.QueueItem)

    /**
     * Pause the current playing item
     * 暂停
     */
    fun pause()

    /**
     * Seek to the given position
     * 位移
     */
    fun seekTo(position: Long)

    /**
     * 设置是否循环播放
     * @param repeatMode
     */
    fun setRepeatMode(repeatMode: Int)

    interface Callback {
        /**
         * On current music completed.
         * 完成时调用
         */
        fun onCompletion()

        /**
         * on Playback status changed
         * Implementations can use this callback to update
         * playback state on the media sessions.
         * 播放状态改变时调用
         */
        fun onPlaybackStatusChanged(state: Int)

        /**
         * @param error to be added to the PlaybackState
         * 发生错误时调用
         */
        fun onError(error: String)

        /**
         * @param mediaId being currently played
         * 设置当前歌曲id
         */
        fun setCurrentMediaId(mediaId: String)
    }

    /**
     * @param callback to be called
     * 设置回调
     */
    fun setCallback(callback: Callback)
}
