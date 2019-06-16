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
import android.util.Log

import app.cloudmusic.R
import app.cloudmusic.service.MusicService

/**
 * Created by Administrator on 2018/1/6.
 * 包含媒体浏览器以及播放控制fragment的基类
 */

open class BaseMediaActivity : AppCompatActivity() {
    protected lateinit var mMediaBrowser: MediaBrowserCompat//媒体浏览器
    protected var mControlsFragment: PlayControllFragment? = null

    // Callback that ensures that we are showing the controls
    //媒体控制器控制播放过程中的回调接口
    protected val mMediaControllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            //播放状态发生改变时回调
            if (shouldShowControls()) {
                showPlaybackControls()
            } else {
                hidePlaybackControls()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            //播放的媒体数据发生变化时的回调
            if (shouldShowControls()) {
                showPlaybackControls()
            } else {
                hidePlaybackControls()
            }
        }
    }


    //MediaBrowserCompat链接成功后调用该回调
    private val mConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                connectToSession(mMediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                hidePlaybackControls()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMediaBrowser = MediaBrowserCompat(this,
                ComponentName(this, MusicService::class.java), mConnectionCallback, null)
    }

    override fun onStart() {
        super.onStart()

        mControlsFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_playback_controls) as PlayControllFragment?
        if (mControlsFragment == null) {
            throw IllegalStateException("Mising fragment with id 'controls'. Cannot continue.")
        }

        hidePlaybackControls()

        mMediaBrowser.connect()
    }

    override fun onStop() {
        super.onStop()

        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(mMediaControllerCallback)
        }
        mMediaBrowser.disconnect()
    }


    protected open fun onMediaControllerConnected() {
        // empty implementation, can be overridden by clients.
    }

    protected fun showPlaybackControls() {
        if (!mControlsFragment!!.isHidden)
            return
        supportFragmentManager.beginTransaction()
                //                .setCustomAnimations(
                //                        R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                //                        R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                .show(mControlsFragment!!)
                .commit()
    }

    protected fun hidePlaybackControls() {
        supportFragmentManager.beginTransaction()
                .hide(mControlsFragment!!)
                .commit()
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected fun shouldShowControls(): Boolean {
        val mediaController = MediaControllerCompat.getMediaController(this)
        Log.w("AAA", (mediaController == null || mediaController.metadata == null || mediaController.playbackState == null).toString() + "")
        if (mediaController == null ||
                mediaController.metadata == null ||
                mediaController.playbackState == null) {
            return false
        }

        when (mediaController.playbackState.state) {
            PlaybackStateCompat.STATE_ERROR, PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.STATE_STOPPED -> return false
            else -> return true
        }
    }

    //连接媒体会话
    @Throws(RemoteException::class)
    private fun connectToSession(token: MediaSessionCompat.Token) {
        val mediaController = MediaControllerCompat(this, token)
        MediaControllerCompat.setMediaController(this, mediaController)
        //媒体控制器注册回调
        mediaController.registerCallback(mMediaControllerCallback)
        if (shouldShowControls()) {
            showPlaybackControls()
        } else {
            hidePlaybackControls()
        }

        if (mControlsFragment != null) {
            mControlsFragment!!.onConnected()
        }

        onMediaControllerConnected()
    }
}
