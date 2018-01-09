package app.cloudmusic.manager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import app.cloudmusic.Contaces;
import app.cloudmusic.utils.LogHelper;

import static app.cloudmusic.manager.PlaybackManager.PlayMode.ORDER_CYCLE;

/**
 * Created by Administrator on 2017/11/13.
 */

public class PlaybackManager implements Playback.Callback {
    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class);

    private MediaSessionCompat.Callback mMediaSessionCallback;
    private List<MediaBrowserCompat.MediaItem> currentQueue = new ArrayList<>();
    private int currentPosition = 0;
    private MediaSessionCompat mediaSessionCompat;
    private MusicProvider musicProvider;
    private Playback playback;
    private PlaybackServiceCallback serviceCallback;
    //设置播放模式

    /**
     * ORDER:顺序
     * RANDOM:随机
     * SINGLE：单曲
     * SINGLE_CYCLE：单曲循环
     * ORDER_CYCLE：顺序循环
     */
    public enum PlayMode{
        ORDER,RANDOM,SINGLE_CYCLE,SINGLE,ORDER_CYCLE
    }

    private PlayMode playMode = ORDER_CYCLE;

    public PlaybackManager(PlaybackServiceCallback serviceCallback,MusicProvider musicProvider,Playback playback) {
        this.serviceCallback = serviceCallback;
        this.musicProvider = musicProvider;
        this.playback = playback;
        playback.setCallback(this);
        mMediaSessionCallback = new MediaSessionCallback();
    }

    public Playback getPlayback() {
        return playback;
    }

    public void setMediaSessionCompat(MediaSessionCompat mediaSessionCompat) {
        this.mediaSessionCompat = mediaSessionCompat;
    }

    public MediaSessionCompat.Callback  getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    /**
     * Handle a request to play music
     */
    public void handlePlayRequest() {
        LogHelper.d(TAG, "handlePlayRequest: mState=" + playback.getState());
        MediaBrowserCompat.MediaItem currentMusic = currentQueue.get(currentPosition);
        if (currentMusic != null) {
            serviceCallback.onPlaybackStart();
            playback.play(currentMusic);
        }
    }

    public void handlePauseRequest(){
        if(playback.isPlaying()){
            playback.pause();
            serviceCallback.onPlaybackStop();
        }
    }

    public void handleStopRequest(String withError){
        playback.stop(true);
        serviceCallback.onPlaybackStop();
        //updatePlaybackState(withError);
    }


    //Playback 回调
    /**
     * 播放器播放完当前音乐，跳转播放下一首
     */
    @Override
    public void onCompletion() {
        switch (playMode){
            case ORDER_CYCLE:
                if(currentPosition == currentQueue.size() - 1)
                    currentPosition = 0;
                else
                    currentPosition++;
                break;
            case ORDER:
                if(currentPosition == currentQueue.size() - 1)
                    currentPosition = -1;
                else
                    currentPosition++;
                break;
            case SINGLE:
                currentPosition = -1;
                break;
            case SINGLE_CYCLE:
                break;
            case RANDOM:
                //TODO 暂时未找到算法
                break;
        }

        if(currentPosition >= 0) {
            handlePlayRequest();
            mediaSessionCompat.setMetadata(musicProvider.getMusicById(currentQueue.get(currentPosition).getMediaId()));
        }else{
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void setCurrentMediaId(String mediaId) {

    }

    /**
     * 更新播放状态
     * @param error
     */
    public void updatePlaybackState(String error) {
        LogHelper.d(TAG, "updatePlaybackState, playback state=" + playback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (playback != null && playback.isConnected()) {
            position = playback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        //设置自定义动作，例如设置喜欢与不喜欢，是否收藏等等的回调
        //setCustomAction(stateBuilder);
        int state = playback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
//        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
//        if (currentMusic != null) {
//            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
//        }

        //更新播放状态，不单单是播放状态，还可以自定义状态
        serviceCallback.onPlaybackStateUpdated(stateBuilder.build());

        //如果播放器进入暂停状态或者播放中状态，在通知栏显示
        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            serviceCallback.onNotificationRequired();
        }
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (playback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }


    private class MediaSessionCallback extends MediaSessionCompat.Callback{
        @Override
        public void onPlay() {
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            super.onAddQueueItem(description);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
            super.onAddQueueItem(description, index);
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            currentQueue.clear();
            currentQueue.addAll(musicProvider.getChild(Contaces.SERVICE_ID_LOCALMUSIC));
            for (int i = 0; i < currentQueue.size(); i++) {
                if(TextUtils.equals(currentQueue.get(i).getMediaId(),mediaId)){
                    currentPosition = i;
                    break;
                }
            }
            mediaSessionCompat.setMetadata(musicProvider.getMusicById(mediaId));
            handlePlayRequest();
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
        }

        @Override
        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            super.onPrepareFromMediaId(mediaId, extras);
        }

        @Override
        public void onPrepareFromSearch(String query, Bundle extras) {
            super.onPrepareFromSearch(query, extras);
        }

        @Override
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            super.onPrepareFromUri(uri, extras);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            super.onRemoveQueueItem(description);
        }

        @Override
        public void onRemoveQueueItemAt(int index) {
            super.onRemoveQueueItemAt(index);
        }

        @Override
        public void onRewind() {
            super.onRewind();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            super.onSetRating(rating);
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
        }

        @Override
        public void onSetShuffleModeEnabled(boolean enabled) {
            super.onSetShuffleModeEnabled(enabled);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
        }
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
