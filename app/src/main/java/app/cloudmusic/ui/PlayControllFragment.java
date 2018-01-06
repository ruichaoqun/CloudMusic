package app.cloudmusic.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.cloudmusic.R;
import app.cloudmusic.utils.imageloader.ImageLoader;

public class PlayControllFragment extends Fragment implements View.OnClickListener {

    private ImageView cover;
    private TextView title,artist;
    private ImageView play,playList;
    private ImageLoader imageLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_controll, container, false);
        cover = (ImageView) view.findViewById(R.id.cover);
        play = (ImageView) view.findViewById(R.id.play);
        playList = (ImageView) view.findViewById(R.id.play_list);
        title = (TextView) view.findViewById(R.id.title);
        artist = (TextView) view.findViewById(R.id.artist);
        play.setOnClickListener(this);
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
    }

    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        if (getActivity() == null) {
            return;
        }
        if (state == null) {
            return;
        }
        switch (state.getState()){
            case PlaybackStateCompat.STATE_PLAYING:
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play:
                if(MediaControllerCompat.getMediaController(getActivity()) != null)
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().play();
                break;
        }
    }

    private MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            PlayControllFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            PlayControllFragment.this.onMetadataChanged(metadata);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
        }
    };


}
