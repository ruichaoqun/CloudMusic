/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cloudmusic.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.PowerManager
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log


import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

import java.io.IOException

import app.cloudmusic.service.MusicService
import app.cloudmusic.utils.LogHelper

import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnSeekCompleteListener
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA

/**
 * A class that implements local media playback using [MediaPlayer]
 * 本地播放
 */
class LocalPlayback(context: Context, private val mMusicProvider: MusicProvider) : Playback {

    private var mContext: Context? = null
    private val mWifiLock: WifiManager.WifiLock
    private var mPlayOnFocusGain: Boolean = false
    private var mCallback: Playback.Callback? = null
    private var mAudioNoisyReceiverRegistered: Boolean = false
    override var currentMediaId: String? = null

    private var mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
    private val mAudioManager: AudioManager
    private var mExoPlayer: SimpleExoPlayer? = null
    private var repeatMode: Int = 0
    private val mEventListener = ExoPlayerEventListener()

    // Whether to return STATE_NONE or STATE_STOPPED when mExoPlayer is null;
    private var mExoPlayerNullIsStopped = false

    private val mAudioNoisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private val mAudioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                LogHelper.d(TAG, "Headphones disconnected.")
                if (isPlaying) {
                    val i = Intent(context, MusicService::class.java)
                    i.action = MusicService.ACTION_CMD
                    i.putExtra(MusicService.CMD_NAME, MusicService.CMD_PAUSE)
                    mContext?.startService(i)
                }
            }
        }
    }

    override// Nothing to do (mExoPlayer holds its own state).
    var state: Int
        get() {
            if (mExoPlayer == null) {
                return if (mExoPlayerNullIsStopped)
                    PlaybackStateCompat.STATE_STOPPED
                else
                    PlaybackStateCompat.STATE_NONE
            }
            when (mExoPlayer!!.playbackState) {
                ExoPlayer.STATE_IDLE -> return PlaybackStateCompat.STATE_PAUSED
                ExoPlayer.STATE_BUFFERING -> return PlaybackStateCompat.STATE_BUFFERING
                ExoPlayer.STATE_READY -> return if (mExoPlayer!!.playWhenReady)
                    PlaybackStateCompat.STATE_PLAYING
                else
                    PlaybackStateCompat.STATE_PAUSED
                ExoPlayer.STATE_ENDED -> return PlaybackStateCompat.STATE_PAUSED
                else -> return PlaybackStateCompat.STATE_NONE
            }
        }
        set(state) {}

    override val isConnected: Boolean
        get() = true

    override val isPlaying: Boolean
        get() = mPlayOnFocusGain || mExoPlayer != null && mExoPlayer!!.playWhenReady

    override val currentStreamPosition: Long
        get() = if (mExoPlayer != null) mExoPlayer!!.currentPosition else 0

    private val mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        LogHelper.d(TAG, "onAudioFocusChange. focusChange=", focusChange)
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> mCurrentAudioFocusState = AUDIO_FOCUSED
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost audio focus, but will gain it back (shortly), so note whether
                // playback should resume
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
                mPlayOnFocusGain = mExoPlayer != null && mExoPlayer!!.playWhenReady
            }
            AudioManager.AUDIOFOCUS_LOSS ->
                // Lost audio focus, probably "permanently"
                mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }

        if (mExoPlayer != null) {
            // Update the player state based on the change
            configurePlayerState()
        }
    }

    init {
        val applicationContext = context.applicationContext
        this.mContext = applicationContext

        this.mAudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        this.mWifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock")
    }

    override fun start() {
        // Nothing to do
    }

    override fun stop(notifyListeners: Boolean) {
        giveUpAudioFocus()
        unregisterAudioNoisyReceiver()
        releaseResources(true)
    }

    override fun setCurrentStreamPosition(pos: Int) {

    }

    override fun updateLastKnownStreamPosition() {
        // Nothing to do. Position maintained by ExoPlayer.
    }

    override fun play(item: MediaSessionCompat.QueueItem) {
        mPlayOnFocusGain = true
        tryToGetAudioFocus()
        registerAudioNoisyReceiver()
        val mediaId = item.description.mediaId
        val mediaHasChanged = !TextUtils.equals(mediaId, currentMediaId)
        if (mediaHasChanged) {
            currentMediaId = mediaId
        }

        if (mediaHasChanged || mExoPlayer == null) {
            releaseResources(false) // release everything except the player
            var source: String? = item.description.mediaUri!!.toString()
            if (source != null) {
                source = source.replace(" ".toRegex(), "%20") // Escape spaces for URLs
            }

            if (mExoPlayer == null) {
                mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                        mContext, DefaultTrackSelector(), DefaultLoadControl())
                mExoPlayer!!.addListener(mEventListener)
            }

            // Android "O" makes much greater use of AudioAttributes, especially
            // with regards to AudioFocus. All of UAMP's tracks are music, but
            // if your content includes spoken word such as audiobooks or podcasts
            // then the content type should be set to CONTENT_TYPE_SPEECH for those
            // tracks.
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(CONTENT_TYPE_MUSIC)
                    .setUsage(USAGE_MEDIA)
                    .build()
            mExoPlayer!!.audioAttributes = audioAttributes

            // Produces DataSource instances through which media data is loaded.
            val dataSourceFactory = DefaultDataSourceFactory(
                    mContext, Util.getUserAgent(mContext, "uamp"), null)
            // Produces Extractor instances for parsing the media data.
            val extractorsFactory = DefaultExtractorsFactory()
            // The MediaSource represents the media to be played.
            val mediaSource = ExtractorMediaSource(
                    Uri.parse(source), dataSourceFactory, extractorsFactory, null, null)

            // Prepares media to play (happens on background thread) and triggers
            // {@code onPlayerStateChanged} callback when the stream is ready to play.
            mExoPlayer!!.prepare(mediaSource)

            // If we are streaming from the internet, we want to hold a
            // Wifi lock, which prevents the Wifi radio from going to
            // sleep while the song is playing.
            mWifiLock.acquire()
        }

        configurePlayerState()
    }

    override fun pause() {
        // Pause player and cancel the 'foreground service' state.
        if (mExoPlayer != null) {
            mExoPlayer!!.playWhenReady = false
        }
        // While paused, retain the player instance, but give up audio focus.
        releaseResources(false)
        unregisterAudioNoisyReceiver()
    }

    override fun seekTo(position: Long) {
        LogHelper.d(TAG, "seekTo called with ", position)
        if (mExoPlayer != null) {
            registerAudioNoisyReceiver()
            mExoPlayer!!.seekTo(position)
        }
    }

    override fun setCallback(callback: Playback.Callback) {
        this.mCallback = callback
    }

    override fun setRepeatMode(repeatMode: Int) {
        this.repeatMode = repeatMode
        if (mExoPlayer != null) {
            mExoPlayer!!.repeatMode = repeatMode
        }
    }

    private fun tryToGetAudioFocus() {
        LogHelper.d(TAG, "tryToGetAudioFocus")
        val result = mAudioManager.requestAudioFocus(
                mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_FOCUSED
        } else {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    private fun giveUpAudioFocus() {
        LogHelper.d(TAG, "giveUpAudioFocus")
        if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    /**
     * Reconfigures the player according to audio focus settings and starts/restarts it. This method
     * starts/restarts the ExoPlayer instance respecting the current audio focus state. So if we
     * have focus, it will play normally; if we don't have focus, it will either leave the player
     * paused or set it to a low volume, depending on what is permitted by the current focus
     * settings.
     */
    private fun configurePlayerState() {
        LogHelper.d(TAG, "configurePlayerState. mCurrentAudioFocusState=", mCurrentAudioFocusState)
        if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            // We don't have audio focus and can't duck, so we have to pause
            pause()
        } else {
            registerAudioNoisyReceiver()

            if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                // We're permitted to play, but only if we 'duck', ie: play softly
                mExoPlayer!!.volume = VOLUME_DUCK
            } else {
                mExoPlayer!!.volume = VOLUME_NORMAL
            }

            // If we were playing when we lost focus, we need to resume playing.
            if (mPlayOnFocusGain) {
                mExoPlayer!!.playWhenReady = true
                mPlayOnFocusGain = false
            }
        }
    }

    /**
     * Releases resources used by the service for playback, which is mostly just the WiFi lock for
     * local playback. If requested, the ExoPlayer instance is also released.
     *
     * @param releasePlayer Indicates whether the player should also be released
     */
    private fun releaseResources(releasePlayer: Boolean) {
        LogHelper.d(TAG, "releaseResources. releasePlayer=", releasePlayer)

        // Stops and releases player (if requested and available).
        if (releasePlayer && mExoPlayer != null) {
            mExoPlayer!!.release()
            mExoPlayer!!.removeListener(mEventListener)
            mExoPlayer = null
            mExoPlayerNullIsStopped = true
            mPlayOnFocusGain = false
        }

        if (mWifiLock.isHeld) {
            mWifiLock.release()
        }
    }

    private fun registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext?.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter)
            mAudioNoisyReceiverRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext?.unregisterReceiver(mAudioNoisyReceiver)
            mAudioNoisyReceiverRegistered = false
        }
    }

    private inner class ExoPlayerEventListener : Player.EventListener {
        fun onTimelineChanged(timeline: Timeline, manifest: Any) {
            // Nothing to do.
        }

        override fun onTracksChanged(
                trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            // Nothing to do.
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            // Nothing to do.
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY -> if (mCallback != null) {
                    mCallback!!.onPlaybackStatusChanged(state)
                }
                Player.STATE_ENDED ->
                    // The media player finished playing the current song.
                    if (mCallback != null) {
                        mCallback!!.onCompletion()
                    }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            val what: String
            when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> what = error.sourceException.message!!
                ExoPlaybackException.TYPE_RENDERER -> what = error.rendererException.message!!
                ExoPlaybackException.TYPE_UNEXPECTED -> what = error.unexpectedException.message!!
                else -> what = "Unknown: $error"
            }

            LogHelper.e(TAG, "ExoPlayer error: what=$what")
            if (mCallback != null) {
                mCallback!!.onError("ExoPlayer error $what")
            }
        }

        fun onPositionDiscontinuity() {
            // Nothing to do.
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            // Nothing to do.
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            // Nothing to do.
        }
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(LocalPlayback::class.java)

        // The volume we set the media player to when we lose audio focus, but are
        // allowed to reduce the volume instead of stopping playback.
        val VOLUME_DUCK = 0.2f
        // The volume we set the media player when we have audio focus.
        val VOLUME_NORMAL = 1.0f

        // we don't have audio focus, and can't duck (play at a low volume)
        private val AUDIO_NO_FOCUS_NO_DUCK = 0
        // we don't have focus, but can duck (play at a low volume)
        private val AUDIO_NO_FOCUS_CAN_DUCK = 1
        // we have full audio focus
        private val AUDIO_FOCUSED = 2
    }
}
