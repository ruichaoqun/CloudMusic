package app.cloudmusic.ui;

import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import app.cloudmusic.R;
import app.cloudmusic.service.MusicService;
import app.cloudmusic.utils.MediaUtils;
import app.cloudmusic.utils.RenderScriptUtil;
import app.cloudmusic.utils.UIUtils;
import app.cloudmusic.utils.imageloader.ImageLoader;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FullPlayActivity extends AppCompatActivity {
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    @BindView(R.id.back_icon)
    ImageView backIcon;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.share)
    ImageView share;
    @BindView(R.id.navigation_view)
    RelativeLayout navigationView;
    @BindView(R.id.line)
    View line;
    @BindView(R.id.needle)
    ImageView needle;
    @BindView(R.id.playing_fav)
    ImageView playingFav;
    @BindView(R.id.playing_down)
    ImageView playingDown;
    @BindView(R.id.playing_cmt)
    ImageView playingCmt;
    @BindView(R.id.playing_more)
    ImageView playingMore;
    @BindView(R.id.music_tool)
    LinearLayout musicTool;
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.playing_mode)
    ImageView playingMode;
    @BindView(R.id.playing_pre)
    ImageView playingPre;
    @BindView(R.id.playing_play)
    ImageView playingPlay;
    @BindView(R.id.playing_next)
    ImageView playingNext;
    @BindView(R.id.playing_playlist)
    ImageView playingPlaylist;
    @BindView(R.id.content_controller)
    LinearLayout contentController;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.media_play_time)
    TextView mediaPlayTime;
    @BindView(R.id.media_total_time)
    TextView mediaTotalTime;
    @BindView(R.id.app_base_id)
    RelativeLayout appBaseId;
    @BindView(R.id.bg_gaosi)
    ImageView bgGaosi;
    @BindView(R.id.viewpager)
    ViewPager viewpager;

    private ImageLoader imageLoader;
    private MediaBrowserCompat browserServiceCompat;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_full_play);
        ButterKnife.bind(this);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) navigationView.getLayoutParams();
        params.topMargin = UIUtils.getStatuBarHeight(this);
        navigationView.setLayoutParams(params);
        imageLoader = new ImageLoader(this);
        title.setSelected(true);
        browserServiceCompat = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);

        setupViewPager();
    }

    private void setupViewPager() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        browserServiceCompat.connect();
    }

    @OnClick({R.id.back_icon, R.id.share, R.id.playing_fav, R.id.playing_down, R.id.playing_cmt, R.id.playing_more, R.id.playing_mode, R.id.playing_pre, R.id.playing_play, R.id.playing_next, R.id.playing_playlist})
    public void onViewClicked(View view) {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller == null) {
            browserServiceCompat.connect();
            return;
        }
        PlaybackStateCompat state = controller.getPlaybackState();
        switch (view.getId()) {
            case R.id.back_icon:
                onBackPressed();
                break;
            case R.id.share:
                //分享
                break;
            case R.id.playing_fav:
                //喜欢
                break;
            case R.id.playing_down:
                //下载
                break;
            case R.id.playing_cmt:
                //评论
                break;
            case R.id.playing_more:
                //更多
                break;
            case R.id.playing_mode:  //播放模式
                break;
            case R.id.playing_pre://上一首
                if (state != null) {
                    MediaControllerCompat.TransportControls controls =
                            controller.getTransportControls();
                    controls.skipToPrevious();
                }
                break;
            case R.id.playing_play://播放 暂停
                if (state != null) {
                    MediaControllerCompat.TransportControls controls =
                            controller.getTransportControls();
                    switch (state.getState()) {
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
                            Log.d("PlayControllFragment", "onClick with state " + state.getState());
                    }
                }
                break;
            case R.id.playing_next: //下一首
                if (state != null) {
                    MediaControllerCompat.TransportControls controls =
                            controller.getTransportControls();
                    controls.skipToNext();
                }
                break;
            case R.id.playing_playlist: //播放列表

                break;
        }
    }

    //当前播放状态改变时
    private void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        if (playbackState == null) {
            return;
        }
        mLastPlaybackState = playbackState;
        switch (playbackState.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                playingPlay.setImageResource(R.mipmap.play_rdi_btn_pause);
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                playingPlay.setImageResource(R.mipmap.play_rdi_btn_play);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                playingPlay.setImageResource(R.mipmap.play_rdi_btn_play);
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                playingPlay.setImageResource(R.mipmap.play_rdi_btn_play);
                break;
            case PlaybackStateCompat.STATE_NONE:
                playingPlay.setImageResource(R.mipmap.play_rdi_btn_play);
                break;
        }
    }

    //当前播放歌曲改变时
    private void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null)
            return;
        title.setText(metadata.getDescription().getTitle());
        content.setText(metadata.getDescription().getDescription());
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        seekBar.setMax(duration);
        mediaTotalTime.setText(MediaUtils.makeTimeString(duration));


        Drawable result = RenderScriptUtil.rsBlur(this, metadata.getDescription().getMediaUri().toString(), 10);
        bgGaosi.setImageDrawable(result);
        Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.login_bg_night);
        final TransitionDrawable td =
                new TransitionDrawable(new Drawable[]{drawable, result});
        bgGaosi.setImageDrawable(td);
        //去除过度绘制
        td.setCrossFadeEnabled(true);
        td.startTransition(200);

    }

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
        seekBar.setProgress((int) currentPosition);
        mediaPlayTime.setText(MediaUtils.makeTimeString(currentPosition));
    }

    //连接媒体会话
    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
        //媒体控制器注册回调
        mediaController.registerCallback(mMediaControllerCallback);
        onMetadataChanged(mediaController.getMetadata());
        onPlaybackStateChanged(mediaController.getPlaybackState());
    }

    private MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.w("AAA", "onPlaybackStateChanged");
            FullPlayActivity.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.w("AAA", "onMetadataChanged");
            FullPlayActivity.this.onMetadataChanged(metadata);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }
}
