package app.cloudmusic.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import app.cloudmusic.service.MusicService;

/**
 * Created by Administrator on 2018/3/31.
 */

public abstract class BaseMediaBrowserActivity extends AppCompatActivity{
    protected MediaBrowserCompat browserServiceCompat;
    protected MediaControllerCompat controller;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        browserServiceCompat = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        browserServiceCompat.connect();
    }

    protected abstract void onPlaybackStateChanged(PlaybackStateCompat state);
    protected abstract void onMetadataChanged(MediaMetadataCompat metadata);
    protected abstract void connectSuccess();
    public void onRepeatModeChanged(int repeatMode){}
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue){}
    public void onQueueTitleChanged(CharSequence title){}
    public void onShuffleModeChanged(int shuffleMode){}
    public void onExtrasChanged(Bundle extras){}

    private MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            BaseMediaBrowserActivity.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            BaseMediaBrowserActivity.this.onMetadataChanged(metadata);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            BaseMediaBrowserActivity.this.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            BaseMediaBrowserActivity.this.onQueueChanged(queue);
        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {
            BaseMediaBrowserActivity.this.onQueueTitleChanged(title);
        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            BaseMediaBrowserActivity.this.onShuffleModeChanged(shuffleMode);
        }

        @Override
        public void onExtrasChanged(Bundle extras) {
            BaseMediaBrowserActivity.this.onExtrasChanged(extras);
        }
    };

    //MediaBrowserCompat链接成功后调用该回调
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(browserServiceCompat.getSessionToken());
                    } catch (RemoteException e) {
                        finish();
                    }
                }
            };

    //连接媒体会话
    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        controller = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, controller);
        //媒体控制器注册回调
        controller.registerCallback(mMediaControllerCallback);
        onMetadataChanged(controller.getMetadata());
        onPlaybackStateChanged(controller.getPlaybackState());
        connectSuccess();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        browserServiceCompat.disconnect();
    }

}
