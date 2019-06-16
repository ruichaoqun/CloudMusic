package app.cloudmusic.ui

import android.content.ComponentName
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity

import app.cloudmusic.service.MusicService

/**
 * Created by Administrator on 2018/3/31.
 */

abstract class BaseMediaBrowserActivity : AppCompatActivity() {
    protected lateinit var browserServiceCompat: MediaBrowserCompat
    protected lateinit var controller: MediaControllerCompat

    private val mMediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            this@BaseMediaBrowserActivity.onPlaybackStateChanged(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            this@BaseMediaBrowserActivity.onMetadataChanged(metadata)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            this@BaseMediaBrowserActivity.onRepeatModeChanged(repeatMode)
        }

        override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>?) {
            this@BaseMediaBrowserActivity.onQueueChanged(queue)
        }

        override fun onQueueTitleChanged(title: CharSequence?) {
            this@BaseMediaBrowserActivity.onQueueTitleChanged(title)
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            this@BaseMediaBrowserActivity.onShuffleModeChanged(shuffleMode)
        }

        override fun onExtrasChanged(extras: Bundle?) {
            this@BaseMediaBrowserActivity.onExtrasChanged(extras)
        }
    }

    //MediaBrowserCompat链接成功后调用该回调
    private val mConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                connectToSession(browserServiceCompat.sessionToken)
            } catch (e: RemoteException) {
                finish()
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        browserServiceCompat = MediaBrowserCompat(this,
                ComponentName(this, MusicService::class.java), mConnectionCallback, null)
    }

    override fun onStart() {
        super.onStart()
        browserServiceCompat.connect()
    }

    protected abstract fun onPlaybackStateChanged(state: PlaybackStateCompat?)
    protected abstract fun onMetadataChanged(metadata: MediaMetadataCompat?)
    protected abstract fun connectSuccess()
    open fun onRepeatModeChanged(repeatMode: Int) {}
    open fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>?) {}
    open fun onQueueTitleChanged(title: CharSequence?) {}
    fun onShuffleModeChanged(shuffleMode: Int) {}
    fun onExtrasChanged(extras: Bundle?) {}

    //连接媒体会话
    @Throws(RemoteException::class)
    private fun connectToSession(token: MediaSessionCompat.Token) {
        controller = MediaControllerCompat(this, token)
        MediaControllerCompat.setMediaController(this, controller)
        //媒体控制器注册回调
        controller.registerCallback(mMediaControllerCallback)
        onMetadataChanged(controller.metadata)
        onPlaybackStateChanged(controller.playbackState)
        connectSuccess()
    }

    override fun onDestroy() {
        super.onDestroy()
        browserServiceCompat.disconnect()
    }

}
