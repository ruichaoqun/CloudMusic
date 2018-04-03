package app.cloudmusic.ui;

import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import app.cloudmusic.R;
import app.cloudmusic.service.MusicService;
import app.cloudmusic.utils.MediaSharePreference;
import app.cloudmusic.utils.MediaUtils;
import app.cloudmusic.utils.MyScroller;
import app.cloudmusic.utils.RenderScriptUtil;
import app.cloudmusic.utils.UIUtils;
import app.cloudmusic.utils.broadnotify.BroadNotifyUtils;
import app.cloudmusic.utils.broadnotify.NotifyContaces;
import app.cloudmusic.utils.imageloader.ImageLoader;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FullPlayActivity extends BaseMediaBrowserActivity{
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

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private final Handler mHandler = new Handler();
    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;
    private int repeatMode = MediaSharePreference.getInstances().getRepeatMode();
    private AblumPagerAdapter ablumPagerAdapter;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private List<MediaSessionCompat.QueueItem> queueItemList;
    private int pageScrollState = 0;



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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mediaPlayTime.setText(DateUtils.formatElapsedTime(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControllerCompat.getMediaController(FullPlayActivity.this).getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();
            }
        });
    }

    @OnClick({R.id.back_icon, R.id.share, R.id.playing_fav, R.id.playing_down, R.id.playing_cmt, R.id.playing_more, R.id.playing_mode, R.id.playing_pre, R.id.playing_play, R.id.playing_next, R.id.playing_playlist})
    public void onViewClicked(View view) {
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
            case R.id.playing_mode://播放模式
                int repeatMode = controller.getRepeatMode();
                int newRepeatMode = repeatMode%3 + 1;//新的播放模式
                controller.getTransportControls().setRepeatMode(newRepeatMode);//申请更新播放模式
                break;
            case R.id.playing_pre://上一首
                if (state != null) {
                    controller.getTransportControls().skipToPrevious();
                }
                break;
            case R.id.playing_play://播放 暂停
                if (state != null) {
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING:
                        case PlaybackStateCompat.STATE_BUFFERING:
                            controller.getTransportControls().pause();
                            stopSeekbarUpdate();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                        case PlaybackStateCompat.STATE_STOPPED:
                            controller.getTransportControls().play();
                            scheduleSeekbarUpdate();
                            break;
                        default:
                            Log.d("PlayControllFragment", "onClick with state " + state.getState());
                    }
                }
                break;
            case R.id.playing_next: //下一首
                if (state != null) {
                    controller.getTransportControls().skipToNext();
                }
                break;
            case R.id.playing_playlist: //播放列表

                break;
        }
    }

    //当前播放状态改变时
    protected void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        if (playbackState == null) {
            return;
        }
        mLastPlaybackState = playbackState;
        switch (playbackState.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                playingPlay.setImageResource(R.mipmap.play_rdi_btn_pause);
                String mediaUri = controller.getMetadata().getDescription().getMediaUri().toString();
                Bundle bundle = new Bundle();
                bundle.putString("mediaUri",mediaUri);
                bundle.putBoolean("isRunning",true);
                BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR,bundle);
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                playingPlay.setImageResource(R.mipmap.play_rdi_btn_play);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                playingPlay.setImageResource(R.mipmap.play_rdi_btn_play);
                String mediaUri1 = controller.getMetadata().getDescription().getMediaUri().toString();
                Bundle bundle1 = new Bundle();
                bundle1.putString("mediaUri",mediaUri1);
                bundle1.putBoolean("isRunning",false);
                BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR,bundle1);
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
    protected void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null)
            return;
        title.setText(metadata.getDescription().getTitle());
        content.setText(metadata.getDescription().getDescription());
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        seekBar.setMax(duration);
        mediaTotalTime.setText(MediaUtils.makeTimeString(duration));

        Drawable drawable = bgGaosi.getDrawable();
        if(drawable == null)
            drawable = ContextCompat.getDrawable(this, R.mipmap.login_bg_night);;
        Drawable result = RenderScriptUtil.rsBlur(this, metadata.getDescription().getMediaUri().toString(), 10);
        bgGaosi.setImageDrawable(result);
        final TransitionDrawable td =
                new TransitionDrawable(new Drawable[]{drawable, result});
        bgGaosi.setImageDrawable(td);
        //去除过度绘制
        td.setCrossFadeEnabled(true);
        td.startTransition(500);

        String id = metadata.getDescription().getMediaId();
        int index = MediaUtils.getMusicIndexOnQueue(controller.getQueue(),id);
        viewpager.setCurrentItem(index+1);
    }

    @Override
    protected void connectSuccess() {
        initFragment();
        if(controller != null){
            //设置播放模式
            setRepeatMode(controller.getRepeatMode());
        }
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
        seekBar.setProgress((int) currentPosition%seekBar.getMax());
        mediaPlayTime.setText(MediaUtils.makeTimeString((int) currentPosition%seekBar.getMax()));
    }

    private void initFragment() {
        if(controller != null){
            queueItemList = new ArrayList<>();
            List<MediaSessionCompat.QueueItem> queue = controller.getQueue();
            for (int i = 0; i < queue.size(); i++) {
                queueItemList.add(queue.get(i));
            }
            if(queue != null){
                ablumPagerAdapter = new AblumPagerAdapter(getSupportFragmentManager(),queueItemList);
                viewpager.setAdapter(ablumPagerAdapter);
                int index = MediaUtils.getMusicIndexOnQueue(queue, controller.getMetadata().getDescription().getMediaId());
                ablumPagerAdapter.setCurrentItem(index+1);
                viewpager.setCurrentItem(index+1,false);
                viewpager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
                    @Override
                    public void onPageScrollStateChanged(int state) {
                        super.onPageScrollStateChanged(state);
                        pageScrollState = state;
                        if(state == ViewPager.SCROLL_STATE_IDLE){
                            if(controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
                                String mediaUri = controller.getMetadata().getDescription().getMediaUri().toString();
                                Bundle bundle = new Bundle();
                                bundle.putString("mediaUri",mediaUri);
                                bundle.putBoolean("isRunning",true);
                                BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR,bundle);
                            }

                            int position = viewpager.getCurrentItem();
                            ablumPagerAdapter.setCurrentItem(position);
                            int truePosition = position-1;
                            final List<MediaSessionCompat.QueueItem> list = controller.getQueue();
                            if(position == 0){
                                truePosition = list.size() - 1;
                                ablumPagerAdapter.setCurrentItem(list.size());
                                viewpager.setCurrentItem(list.size(),false);
                            }
                            if(position == list.size() + 1){
                                truePosition = 0;
                                ablumPagerAdapter.setCurrentItem(1);
                                viewpager.setCurrentItem(1,false);
                            }
                            controller.getTransportControls().skipToQueueItem(list.get(truePosition).getQueueId());

                        }else{
                            String mediaUri = controller.getMetadata().getDescription().getMediaUri().toString();
                            Bundle bundle = new Bundle();
                            bundle.putString("mediaUri",mediaUri);
                            bundle.putBoolean("isRunning",false);
                            BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR,bundle);
                        }
                    }
                });


                if(controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
                    String mediaUri = controller.getMetadata().getDescription().getMediaUri().toString();
                    Bundle bundle = new Bundle();
                    bundle.putString("mediaUri",mediaUri);
                    bundle.putBoolean("isRunning",true);
                    BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR,bundle);
                }
                // 改变viewpager动画时间
                try {
                    Field mField = ViewPager.class.getDeclaredField("mScroller");
                    mField.setAccessible(true);
                    MyScroller mScroller = new MyScroller(viewpager.getContext().getApplicationContext(), new LinearInterpolator());
                    mField.set(viewpager, mScroller);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        setRepeatMode(repeatMode);
    }

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
        List<MediaSessionCompat.QueueItem> queu1 = controller.getQueue();
        String id = controller.getMetadata().getDescription().getMediaId();
        int index = MediaUtils.getMusicIndexOnQueue(queue, id);
        queueItemList.clear();
        for (int i = 0; i < queue.size(); i++) {
            queueItemList.add(queue.get(i));
        }
        ablumPagerAdapter.setCurrentItem(index+1);
        viewpager.setCurrentItem(index+1,false);
//        ablumPagerAdapter = new AblumPagerAdapter(getSupportFragmentManager(),queue);
//        viewpager.setAdapter(ablumPagerAdapter);
//
    }

    @Override
    public void onQueueTitleChanged(CharSequence title) {
        super.onQueueTitleChanged(title);
    }

    /**
     * 更新播放模式
     */
    private void setRepeatMode(int repeatMode) {
        switch (repeatMode){
            case PlaybackStateCompat.REPEAT_MODE_ONE://单曲模式
                playingMode.setImageResource(R.mipmap.icon_playmode_single);
                break;
            case PlaybackStateCompat.REPEAT_MODE_ALL://循环播放模式
                playingMode.setImageResource(R.mipmap.icon_playmode_cycle);
                break;
            case PlaybackStateCompat.REPEAT_MODE_GROUP://随机播放模式
                playingMode.setImageResource(R.mipmap.icon_playmode_shuffle);
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }
}
