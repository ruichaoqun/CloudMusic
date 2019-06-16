package app.cloudmusic.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils

import java.lang.ref.WeakReference

import app.cloudmusic.Contaces
import app.cloudmusic.manager.LocalPlayback
import app.cloudmusic.manager.MusicProvider
import app.cloudmusic.manager.PlaybackManager
import app.cloudmusic.manager.QueueManager
import app.cloudmusic.utils.LogHelper
import app.cloudmusic.utils.MediaSharePreference

import app.cloudmusic.Contaces.SERVICE_ID_LOCALMUSIC

/**
 * Created by Administrator on 2017/11/6.
 */

class MusicService : MediaBrowserServiceCompat(), PlaybackManager.PlaybackServiceCallback {

    private var musicProvider: MusicProvider? = null
    private var mediaSessionCompat: MediaSessionCompat? = null
    private var playbackManager: PlaybackManager? = null
    private val mDelayedStopHandler = DelayedStopHandler(this)
    private var queueManager: QueueManager? = null

    override fun onCreate() {
        super.onCreate()

        musicProvider = MusicProvider()
        musicProvider!!.retrieveMediaAsync(null)//获取本地所有音乐
        val playback = LocalPlayback(this, musicProvider!!)
        queueManager = QueueManager(musicProvider!!, MediaSharePreference.instances.repeatMode, object : QueueManager.MetaDataUpdateListener {
            override fun onMetaDataChanged(metadata: MediaMetadataCompat) {
                mediaSessionCompat!!.setMetadata(metadata)
            }

            override fun onError() {

            }

            override fun onCurrentQueueIndexUpdated(queueIndex: Int) {

            }

            override fun onQueueUpdated(title: String?, newQueue: List<MediaSessionCompat.QueueItem>?) {
                mediaSessionCompat!!.setQueue(newQueue)
                mediaSessionCompat!!.setQueueTitle(title)
            }
        })
        playbackManager = PlaybackManager(this, musicProvider!!, playback, queueManager!!)
        mediaSessionCompat = MediaSessionCompat(this, "MusicService")
        mediaSessionCompat!!.setCallback(playbackManager!!.mediaSessionCallback)
        mediaSessionCompat!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        playbackManager!!.setMediaSessionCompat(mediaSessionCompat!!)
        mediaSessionCompat!!.setRepeatMode(MediaSharePreference.instances.repeatMode)
        setSessionToken(mediaSessionCompat!!.sessionToken)
    }

    override fun onStartCommand(startIntent: Intent?, flags: Int, startId: Int): Int {
        if (startIntent != null) {
            val action = startIntent.action
            val command = startIntent.getStringExtra(CMD_NAME)
            if (ACTION_CMD == action) {
                if (CMD_PAUSE == command) {
                    playbackManager!!.handlePauseRequest()
                } else if (CMD_STOP_CASTING == command) {
                    //CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                MediaButtonReceiver.handleIntent(mediaSessionCompat, startIntent)
            }
        }
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null)
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY.toLong())
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(SERVICE_ID_LOCALMUSIC, null)
    }

    override fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        if (TextUtils.equals(Contaces.GET_PALYING_LIST, parentId)) {//订阅当前播放列表
            if (musicProvider!!.isInitialized) {//已加载完成，直接返回搜索结果
                result.sendResult(musicProvider!!.getChild(parentId))
            } else {
                result.detach()
                //异步加载
                musicProvider!!.retrieveMediaAsync { result.sendResult(musicProvider!!.getChild(parentId)) }
            }
        } else if (TextUtils.equals(Contaces.SERVICE_ID_LOCALMUSIC, parentId)) {//订阅本地音乐
            if (musicProvider!!.isInitialized) {//已加载完成，直接返回搜索结果
                result.sendResult(musicProvider!!.getChild(parentId))
            } else {
                result.detach()
                //异步加载
                musicProvider!!.retrieveMediaAsync { result.sendResult(musicProvider!!.getChild(parentId)) }
            }
        }
    }

    override fun onPlaybackStart() {
        mediaSessionCompat!!.isActive = true

        mDelayedStopHandler.removeCallbacksAndMessages(null)

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(Intent(applicationContext, MusicService::class.java))
    }

    override fun onNotificationRequired() {

    }

    override fun onPlaybackStop() {
        mediaSessionCompat!!.isActive = false
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        mDelayedStopHandler.removeCallbacksAndMessages(null)
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY.toLong())
        stopForeground(true)
    }

    override fun onPlaybackStateUpdated(newState: PlaybackStateCompat) {
        mediaSessionCompat!!.setPlaybackState(newState)
    }

    override fun onRepeatUpdated(repeatMode: Int) {
        mediaSessionCompat!!.setRepeatMode(repeatMode)
        MediaSharePreference.instances.repeatMode = repeatMode
    }


    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private class DelayedStopHandler (service: MusicService) : Handler() {
        private val mWeakReference: WeakReference<MusicService>

        init {
            mWeakReference = WeakReference(service)
        }

        override fun handleMessage(msg: Message) {
            val service = mWeakReference.get()
            if (service != null && service.playbackManager!!.playback != null) {
                if (service.playbackManager!!.playback!!.isPlaying) {
                    LogHelper.d(TAG, "Ignoring delayed stop since the media player is in use.")
                    return
                }
                LogHelper.d(TAG, "Stopping service with delay handler.")
                service.stopSelf()
            }
        }
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(MusicService::class.java)

        // Extra on MediaSession that contains the Cast device name currently connected to
        val EXTRA_CONNECTED_CAST = "com.example.android.uamp.CAST_NAME"
        // The action of the incoming Intent indicating that it contains a command
        // to be executed (see {@link #onStartCommand})
        val ACTION_CMD = "com.example.android.uamp.ACTION_CMD"
        // The key in the extras of the incoming Intent indicating the command that
        // should be executed (see {@link #onStartCommand})
        val CMD_NAME = "CMD_NAME"
        // A value of a CMD_NAME key in the extras of the incoming Intent that
        // indicates that the music playback should be paused (see {@link #onStartCommand})
        val CMD_PAUSE = "CMD_PAUSE"
        // A value of a CMD_NAME key that indicates that the music playback should switch
        // to local playback from cast playback.
        val CMD_STOP_CASTING = "CMD_STOP_CASTING"
        // Delay stopSelf by using a handler.
        private val STOP_DELAY = 30000
    }
}
