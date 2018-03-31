package app.cloudmusic.manager;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * Created by Administrator on 2017/11/16.
 */

public interface Playback {

    /**
     * Start/setup the playback.
     * Resources/listeners would be allocated by implementations.
     * 开始
     */
    void start();

    /**
     * Stop the playback. All resources can be de-allocated by implementations here.
     * @param notifyListeners if true and a callback has been set by setCallback,
     *                        callback.onPlaybackStatusChanged will be called after changing
     *                        the state.
     *                        停止
     */
    void stop(boolean notifyListeners);

    /**
     * Set the latest playback state as determined by the caller.
     * 设置播放状态
     */
    void setState(int state);

    /**
     * Get the current {@link android.media.session.PlaybackState#getState()}
     * 获取播放状态
     */
    int getState();

    /**
     * @return boolean that indicates that this is ready to be used.
     * 是否已连接
     */
    boolean isConnected();

    /**
     * @return boolean indicating whether the player is playing or is supposed to be
     * playing when we gain audio focus.
     * 是否正在播放
     */
    boolean isPlaying();

    /**
     * @return pos if currently playing an item
     * 获取当前播放位置
     */
    long getCurrentStreamPosition();

    /**
     * Set the current position. Typically used when switching players that are in
     * paused state.
     *  设置当前播放位置
     * @param pos position in the stream
     */
    void setCurrentStreamPosition(int pos);

    /**
     * Query the underlying stream and update the internal last known stream position.
     * 更新最后歌曲播放位置
     */
    void updateLastKnownStreamPosition();

    /**
     * @param item to play
     *             播放
     */
    void play(MediaSessionCompat.QueueItem item);

    /**
     * Pause the current playing item
     * 暂停
     */
    void pause();

    /**
     * Seek to the given position
     * 位移
     */
    void seekTo(long position);

    /**
     * Set the current mediaId. This is only used when switching from one
     * playback to another.
     * 设置当前歌曲的id
     *
     * @param mediaId to be set as the current.
     */
    void setCurrentMediaId(String mediaId);

    /**
     * 设置是否循环播放
     * @param repeatMode
     */
    void setRepeatMode(int repeatMode);

    /**
     *
     * @return the current media Id being processed in any state or null.
     * 获取当前歌曲的id
     */
    String getCurrentMediaId();

    interface Callback {
        /**
         * On current music completed.
         * 完成时调用
         */
        void onCompletion();
        /**
         * on Playback status changed
         * Implementations can use this callback to update
         * playback state on the media sessions.
         * 播放状态改变时调用
         */
        void onPlaybackStatusChanged(int state);

        /**
         * @param error to be added to the PlaybackState
         * 发生错误时调用
         */
        void onError(String error);

        /**
         * @param mediaId being currently played
         *  设置当前歌曲id
         */
        void setCurrentMediaId(String mediaId);
    }

    /**
     * @param callback to be called
     *                 设置回调
     */
    void setCallback(Callback callback);
}
