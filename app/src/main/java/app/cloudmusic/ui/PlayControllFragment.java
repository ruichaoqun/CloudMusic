package app.cloudmusic.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import app.cloudmusic.R;
import app.cloudmusic.utils.imageloader.ImageLoader;
import app.cloudmusic.widget.PlayPauseView;

public class PlayControllFragment extends Fragment implements View.OnClickListener {
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    private ImageView cover;
    private TextView title,artist;
    private ImageView playList;
    private ImageLoader imageLoader;
    private PlayPauseView playPauseView;

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private final Handler mHandler = new Handler();
    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_controll, container, false);
        cover = (ImageView) view.findViewById(R.id.cover);
        playPauseView = (PlayPauseView) view.findViewById(R.id.play);
        playList = (ImageView) view.findViewById(R.id.play_list);
        title = (TextView) view.findViewById(R.id.title);
        artist = (TextView) view.findViewById(R.id.artist);
        title.setSelected(true);
        playPauseView.setOnClickListener(this);
        playList.setOnClickListener(this);
        imageLoader = new ImageLoader(getActivity());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(getActivity());
        if(controllerCompat != null){
            onConnected();
        }
    }

    public void onConnected() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mCallback);
        }
    }

    private void onMetadataChanged(MediaMetadataCompat metadata) {
        if (getActivity() == null) {
            return;
        }
        if (metadata == null) {
            return;
        }
        imageLoader.DisplayImage(metadata.getDescription().getMediaUri().toString(),cover);
        title.setText(metadata.getDescription().getTitle());
        artist.setText(metadata.getDescription().getDescription());
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        playPauseView.setMax(duration);
    }

    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        if (getActivity() == null) {
            return;
        }
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;
        switch (state.getState()){
            case PlaybackStateCompat.STATE_PLAYING:
                playPauseView.setState(PlayPauseView.PLAY_STATE_PLAYING);
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                playPauseView.setState(PlayPauseView.PLAY_STATE_PLAYING);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                playPauseView.setState(PlayPauseView.PLAY_STATE_PAUSE);
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                playPauseView.setState(PlayPauseView.PLAY_STATE_PAUSE);
                break;
            case PlaybackStateCompat.STATE_NONE:
                playPauseView.setState(PlayPauseView.PLAY_STATE_PAUSE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play:
                MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
                if(controller == null){
                    onConnected();
                    return;
                }
                PlaybackStateCompat state = controller.getPlaybackState();
                if(state != null){
                    MediaControllerCompat.TransportControls controls =
                            controller.getTransportControls();
                    switch (state.getState()){
                        case PlaybackStateCompat.STATE_PLAYING:
                        case PlaybackStateCompat.STATE_BUFFERING:
                            controls.pause();
                            stopSeekbarUpdate();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                        case PlaybackStateCompat.STATE_STOPPED:
                            controls.play();
                            scheduleSeekbarUpdate();
                            break;
                        default:
                            Log.d("PlayControllFragment", "onClick with state "+ state.getState());
                    }
                }
                break;
        }
    }

    private MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.w("AAA","onPlaybackStateChanged");
            PlayControllFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.w("AAA","onMetadataChanged");
            PlayControllFragment.this.onMetadataChanged(metadata);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
        }
    };


    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_PAUSED) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        playPauseView.setProgress((int) currentPosition);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }
}
