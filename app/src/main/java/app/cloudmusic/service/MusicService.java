package app.cloudmusic.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import androidx.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import androidx.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import app.cloudmusic.Contaces;
import app.cloudmusic.manager.LocalPlayback;
import app.cloudmusic.manager.MusicProvider;
import app.cloudmusic.manager.Playback;
import app.cloudmusic.manager.PlaybackManager;
import app.cloudmusic.manager.QueueManager;
import app.cloudmusic.utils.LogHelper;
import app.cloudmusic.utils.MediaSharePreference;

import static app.cloudmusic.Contaces.SERVICE_ID_LOCALMUSIC;

/**
 * Created by Administrator on 2017/11/6.
 */

public class MusicService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback {
    private static final String TAG = LogHelper.makeLogTag(MusicService.class);

    // Extra on MediaSession that contains the Cast device name currently connected to
    public static final String EXTRA_CONNECTED_CAST = "com.example.android.uamp.CAST_NAME";
    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.example.android.uamp.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";
    // A value of a CMD_NAME key that indicates that the music playback should switch
    // to local playback from cast playback.
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    private MusicProvider musicProvider;
    private MediaSessionCompat mediaSessionCompat;
    private PlaybackManager  playbackManager;
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private QueueManager queueManager;

    @Override
    public void onCreate() {
        super.onCreate();

        musicProvider = new MusicProvider();
        musicProvider.retrieveMediaAsync(null);//获取本地所有音乐
        Playback playback = new LocalPlayback(this,musicProvider);
        queueManager = new QueueManager(musicProvider, MediaSharePreference.getInstances().getRepeatMode(), new QueueManager.MetaDataUpdateListener() {
            @Override
            public void onMetaDataChanged(MediaMetadataCompat metadata) {
                mediaSessionCompat.setMetadata(metadata);
            }

            @Override
            public void onError() {

            }

            @Override
            public void onCurrentQueueIndexUpdated(int queueIndex) {

            }

            @Override
            public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                mediaSessionCompat.setQueue(newQueue);
                mediaSessionCompat.setQueueTitle(title);
            }
        });
        playbackManager = new PlaybackManager(this,musicProvider,playback,queueManager);
        mediaSessionCompat = new MediaSessionCompat(this,"MusicService");
        mediaSessionCompat.setCallback(playbackManager.getMediaSessionCallback());
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        playbackManager.setMediaSessionCompat(mediaSessionCompat);
        mediaSessionCompat.setRepeatMode(MediaSharePreference.getInstances().getRepeatMode());
        setSessionToken(mediaSessionCompat.getSessionToken());
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    playbackManager.handlePauseRequest();
                } else if (CMD_STOP_CASTING.equals(command)) {
                    //CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                MediaButtonReceiver.handleIntent(mediaSessionCompat, startIntent);
            }
        }
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(SERVICE_ID_LOCALMUSIC,null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        if(TextUtils.equals(Contaces.GET_PALYING_LIST,parentId)){//订阅当前播放列表
            if(musicProvider.isInitialized()){//已加载完成，直接返回搜索结果
                result.sendResult(musicProvider.getChild(parentId));
            }else{
                result.detach();
                //异步加载
                musicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                    @Override
                    public void onMusicCatalogReady(boolean success) {
                        result.sendResult(musicProvider.getChild(parentId));
                    }
                });
            }
        }else if(TextUtils.equals(Contaces.SERVICE_ID_LOCALMUSIC,parentId)){//订阅本地音乐
            if(musicProvider.isInitialized()){//已加载完成，直接返回搜索结果
                result.sendResult(musicProvider.getChild(parentId));
            }else{
                result.detach();
                //异步加载
                musicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                    @Override
                    public void onMusicCatalogReady(boolean success) {
                        result.sendResult(musicProvider.getChild(parentId));
                    }
                });
            }
        }
    }

    @Override
    public void onPlaybackStart() {
        mediaSessionCompat.setActive(true);

        mDelayedStopHandler.removeCallbacksAndMessages(null);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    public void onNotificationRequired() {

    }

    @Override
    public void onPlaybackStop() {
        mediaSessionCompat.setActive(false);
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mediaSessionCompat.setPlaybackState(newState);
    }

    @Override
    public void onRepeatUpdated(int repeatMode) {
        mediaSessionCompat.setRepeatMode(repeatMode);
        MediaSharePreference.getInstances().setRepeatMode(repeatMode);
    }


    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.playbackManager.getPlayback() != null) {
                if (service.playbackManager.getPlayback().isPlaying()) {
                    LogHelper.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                LogHelper.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }
}
