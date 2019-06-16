package app.cloudmusic.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import app.cloudmusic.R
import app.cloudmusic.utils.imageloader.ImageLoader
import app.cloudmusic.widget.PlayPauseView
import butterknife.ButterKnife
import butterknife.OnClick

class PlayControllFragment : Fragment() {

    private var cover: ImageView? = null
    private var title: TextView? = null
    private var artist: TextView? = null
    private var playList: ImageView? = null
    private var imageLoader: ImageLoader? = null
    private var playPauseView: PlayPauseView? = null
    private var baseUi: View? = null

    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val mHandler = Handler()
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private var mLastPlaybackState: PlaybackStateCompat? = null

    private val mUpdateProgressTask = Runnable { updateProgress() }

    private val mCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.w("AAA", "onPlaybackStateChanged")
            this@PlayControllFragment.onPlaybackStateChanged(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.w("AAA", "onMetadataChanged")
            this@PlayControllFragment.onMetadataChanged(metadata)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            super.onRepeatModeChanged(repeatMode)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_play_controll, container, false)
        cover = view.findViewById<View>(R.id.cover) as ImageView
        playPauseView = view.findViewById<View>(R.id.play) as PlayPauseView
        playList = view.findViewById<View>(R.id.play_list) as ImageView
        title = view.findViewById<View>(R.id.title) as TextView
        artist = view.findViewById<View>(R.id.artist) as TextView
        baseUi = view.findViewById(R.id.base_ui)
        title!!.isSelected = true
        imageLoader = ImageLoader(this!!.activity!!)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onStart() {
        super.onStart()
        val controllerCompat = MediaControllerCompat.getMediaController(activity!!)
        if (controllerCompat != null) {
            onConnected()
        }
    }

    fun onConnected() {
        val controller = MediaControllerCompat.getMediaController(activity!!)
        if (controller != null) {
            onMetadataChanged(controller.metadata)
            onPlaybackStateChanged(controller.playbackState)
            controller.registerCallback(mCallback)
        }
    }

    private fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        if (activity == null) {
            return
        }
        if (metadata == null) {
            return
        }
        imageLoader!!.DisplayImage(metadata.description.mediaUri!!.toString(), this!!.cover!!)
        title!!.text = metadata.description.title
        artist!!.text = metadata.description.description
        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
        playPauseView!!.setMax(duration)
    }

    private fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        if (activity == null) {
            return
        }
        if (state == null) {
            return
        }
        mLastPlaybackState = state
        when (state.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                playPauseView!!.setState(PlayPauseView.PLAY_STATE_PLAYING)
                scheduleSeekbarUpdate()
            }
            PlaybackStateCompat.STATE_BUFFERING -> playPauseView!!.setState(PlayPauseView.PLAY_STATE_PLAYING)
            PlaybackStateCompat.STATE_PAUSED -> playPauseView!!.setState(PlayPauseView.PLAY_STATE_PAUSE)
            PlaybackStateCompat.STATE_STOPPED -> playPauseView!!.setState(PlayPauseView.PLAY_STATE_PAUSE)
            PlaybackStateCompat.STATE_NONE -> playPauseView!!.setState(PlayPauseView.PLAY_STATE_PAUSE)
        }
    }

    @OnClick(R.id.play_list, R.id.play, R.id.card_view)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.play_list -> {
            }
            R.id.play -> {
                val controller = MediaControllerCompat.getMediaController(activity!!)
                if (controller == null) {
                    onConnected()
                    return
                }
                val state = controller.playbackState
                if (state != null) {
                    val controls = controller.transportControls
                    when (state.state) {
                        PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING -> {
                            controls.pause()
                            stopSeekbarUpdate()
                        }
                        PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                            controls.play()
                            scheduleSeekbarUpdate()
                        }
                        else -> Log.d("PlayControllFragment", "onClick with state " + state.state)
                    }
                }
            }
            R.id.card_view -> {
                val intent = Intent(activity, FullPlayActivity::class.java)
                startActivity(intent)
            }
        }
    }


    private fun scheduleSeekbarUpdate() {
        stopSeekbarUpdate()
        if (!mExecutorService.isShutdown) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    { mHandler.post(mUpdateProgressTask) }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS)
        }
    }

    private fun stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture!!.cancel(false)
        }
    }

    private fun updateProgress() {
        if (mLastPlaybackState == null) {
            return
        }
        var currentPosition = mLastPlaybackState!!.position
        if (mLastPlaybackState!!.state != PlaybackStateCompat.STATE_PAUSED) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            val timeDelta = SystemClock.elapsedRealtime() - mLastPlaybackState!!.lastPositionUpdateTime
            currentPosition += (timeDelta.toInt() * mLastPlaybackState!!.playbackSpeed).toLong()
        }
        playPauseView!!.setProgress(currentPosition.toInt())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSeekbarUpdate()
        mExecutorService.shutdown()
    }

    companion object {
        private val PROGRESS_UPDATE_INTERNAL: Long = 1000
        private val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 100
    }

}
