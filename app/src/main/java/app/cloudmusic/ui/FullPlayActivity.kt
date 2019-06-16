package app.cloudmusic.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import androidx.core.content.ContextCompat

import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView

import java.lang.reflect.Field
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import app.cloudmusic.R
import app.cloudmusic.utils.MediaSharePreference
import app.cloudmusic.utils.MediaUtils
import app.cloudmusic.utils.MyScroller
import app.cloudmusic.utils.RenderScriptUtil
import app.cloudmusic.utils.UIUtils
import app.cloudmusic.utils.broadnotify.BroadNotifyUtils
import app.cloudmusic.utils.broadnotify.NotifyContaces
import app.cloudmusic.utils.imageloader.ImageLoader
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

class FullPlayActivity : BaseMediaBrowserActivity() {

    @BindView(R.id.back_icon)
    internal var backIcon: ImageView? = null
    @BindView(R.id.title)
    internal var title: TextView? = null
    @BindView(R.id.content)
    internal var content: TextView? = null
    @BindView(R.id.share)
    internal var share: ImageView? = null
    @BindView(R.id.navigation_view)
    internal var navigationView: RelativeLayout? = null
    @BindView(R.id.line)
    internal var line: View? = null
    @BindView(R.id.needle)
    internal var needle: ImageView? = null
    @BindView(R.id.playing_fav)
    internal var playingFav: ImageView? = null
    @BindView(R.id.playing_down)
    internal var playingDown: ImageView? = null
    @BindView(R.id.playing_cmt)
    internal var playingCmt: ImageView? = null
    @BindView(R.id.playing_more)
    internal var playingMore: ImageView? = null
    @BindView(R.id.music_tool)
    internal var musicTool: LinearLayout? = null
    @BindView(R.id.seek_bar)
    internal var seekBar: SeekBar? = null
    @BindView(R.id.playing_mode)
    internal var playingMode: ImageView? = null
    @BindView(R.id.playing_pre)
    internal var playingPre: ImageView? = null
    @BindView(R.id.playing_play)
    internal var playingPlay: ImageView? = null
    @BindView(R.id.playing_next)
    internal var playingNext: ImageView? = null
    @BindView(R.id.playing_playlist)
    internal var playingPlaylist: ImageView? = null
    @BindView(R.id.content_controller)
    internal var contentController: LinearLayout? = null
    @BindView(R.id.image)
    internal var image: ImageView? = null
    @BindView(R.id.media_play_time)
    internal var mediaPlayTime: TextView? = null
    @BindView(R.id.media_total_time)
    internal var mediaTotalTime: TextView? = null
    @BindView(R.id.app_base_id)
    internal var appBaseId: RelativeLayout? = null
    @BindView(R.id.bg_gaosi)
    internal var bgGaosi: ImageView? = null
    @BindView(R.id.viewpager)
    internal var viewpager: ViewPager? = null

    private var imageLoader: ImageLoader? = null

    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val mHandler = Handler()
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private var mLastPlaybackState: PlaybackStateCompat? = null
    private val repeatMode = MediaSharePreference.instances.repeatMode
    private var ablumPagerAdapter: AblumPagerAdapter? = null

    private val mUpdateProgressTask = Runnable { updateProgress() }
    private var queueItemList: MutableList<MediaSessionCompat.QueueItem>? = null
    private var pageScrollState = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_full_play)
        ButterKnife.bind(this)
        val params = navigationView!!.layoutParams as RelativeLayout.LayoutParams
        params.topMargin = UIUtils.getStatuBarHeight(this)
        navigationView!!.layoutParams = params
        imageLoader = ImageLoader(this)
        title!!.isSelected = true

        seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mediaPlayTime!!.text = DateUtils.formatElapsedTime((progress / 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                stopSeekbarUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                MediaControllerCompat.getMediaController(this@FullPlayActivity).transportControls.seekTo(seekBar.progress.toLong())
                scheduleSeekbarUpdate()
            }
        })
    }

    @OnClick(R.id.back_icon, R.id.share, R.id.playing_fav, R.id.playing_down, R.id.playing_cmt, R.id.playing_more, R.id.playing_mode, R.id.playing_pre, R.id.playing_play, R.id.playing_next, R.id.playing_playlist)
    fun onViewClicked(view: View) {
        if (controller == null) {
            browserServiceCompat.connect()
            return
        }
        val state = controller.playbackState
        when (view.id) {
            R.id.back_icon -> onBackPressed()
            R.id.share -> {
            }
            R.id.playing_fav -> {
            }
            R.id.playing_down -> {
            }
            R.id.playing_cmt -> {
            }
            R.id.playing_more -> {
            }
            R.id.playing_mode//播放模式
            -> {
                val repeatMode = controller.repeatMode
                val newRepeatMode = repeatMode % 3 + 1//新的播放模式
                controller.transportControls.setRepeatMode(newRepeatMode)//申请更新播放模式
            }
            R.id.playing_pre//上一首
            -> if (state != null) {
                controller.transportControls.skipToPrevious()
            }
            R.id.playing_play//播放 暂停
            -> if (state != null) {
                when (state.state) {
                    PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING -> {
                        controller.transportControls.pause()
                        stopSeekbarUpdate()
                    }
                    PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                        controller.transportControls.play()
                        scheduleSeekbarUpdate()
                    }
                    else -> Log.d("PlayControllFragment", "onClick with state " + state.state)
                }
            }
            R.id.playing_next //下一首
            -> if (state != null) {
                controller.transportControls.skipToNext()
            }
            R.id.playing_playlist //播放列表
            -> {
            }
        }//分享
        //喜欢
        //下载
        //评论
        //更多
    }

    //当前播放状态改变时
    override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat?) {
        if (playbackState == null) {
            return
        }
        mLastPlaybackState = playbackState
        when (playbackState.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                playingPlay!!.setImageResource(R.mipmap.play_rdi_btn_pause)
                val mediaUri = controller.metadata.description.mediaUri!!.toString()
                val bundle = Bundle()
                bundle.putString("mediaUri", mediaUri)
                bundle.putBoolean("isRunning", true)
                BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR, bundle)
                scheduleSeekbarUpdate()
            }
            PlaybackStateCompat.STATE_BUFFERING -> playingPlay!!.setImageResource(R.mipmap.play_rdi_btn_play)
            PlaybackStateCompat.STATE_PAUSED -> {
                playingPlay!!.setImageResource(R.mipmap.play_rdi_btn_play)
                val mediaUri1 = controller.metadata.description.mediaUri!!.toString()
                val bundle1 = Bundle()
                bundle1.putString("mediaUri", mediaUri1)
                bundle1.putBoolean("isRunning", false)
                BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR, bundle1)
            }
            PlaybackStateCompat.STATE_STOPPED -> playingPlay!!.setImageResource(R.mipmap.play_rdi_btn_play)
            PlaybackStateCompat.STATE_NONE -> playingPlay!!.setImageResource(R.mipmap.play_rdi_btn_play)
        }
    }

    //当前播放歌曲改变时
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        if (metadata == null)
            return
        title!!.text = metadata.description.title
        content!!.text = metadata.description.description
        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
        seekBar!!.max = duration
        mediaTotalTime!!.text = MediaUtils.makeTimeString(duration.toLong())

        var drawable: Drawable? = bgGaosi!!.drawable
        if (drawable == null)
            drawable = ContextCompat.getDrawable(this, R.mipmap.login_bg_night)
        val result = RenderScriptUtil.rsBlur(this, metadata.description.mediaUri!!.toString(), 10)
        bgGaosi!!.setImageDrawable(result)
        val td = TransitionDrawable(arrayOf<Drawable>(drawable!!, result!!))
        bgGaosi!!.setImageDrawable(td)
        //去除过度绘制
        td.isCrossFadeEnabled = true
        td.startTransition(500)

        val id = metadata.description.mediaId
        val index = MediaUtils.getMusicIndexOnQueue(controller.queue, id!!)
        viewpager!!.currentItem = index + 1
    }

    override fun connectSuccess() {
        initFragment()
        if (controller != null) {
            //设置播放模式
            setRepeatMode(controller.repeatMode)
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
        seekBar!!.progress = currentPosition.toInt() % seekBar!!.max
        mediaPlayTime!!.text = MediaUtils.makeTimeString((currentPosition.toInt() % seekBar!!.max).toLong())
    }

    private fun initFragment() {
        if (controller != null) {
            queueItemList = ArrayList()
            val queue = controller.queue
            for (i in queue!!.indices) {
                queueItemList!!.add(queue[i])
            }
            if (queue != null) {
                ablumPagerAdapter = AblumPagerAdapter(supportFragmentManager, queueItemList as ArrayList<MediaSessionCompat.QueueItem>)
                viewpager!!.adapter = ablumPagerAdapter
                val index = MediaUtils.getMusicIndexOnQueue(queue, controller.metadata.description.mediaId!!)
                ablumPagerAdapter!!.setCurrentItem(index + 1)
                viewpager!!.setCurrentItem(index + 1, false)
                viewpager!!.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        pageScrollState = state
                        if (state == ViewPager.SCROLL_STATE_IDLE) {
                            if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                                val mediaUri = controller.metadata.description.mediaUri!!.toString()
                                val bundle = Bundle()
                                bundle.putString("mediaUri", mediaUri)
                                bundle.putBoolean("isRunning", true)
                                BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR, bundle)
                            }

                            val position = viewpager!!.currentItem
                            ablumPagerAdapter!!.setCurrentItem(position)
                            var truePosition = position - 1
                            val list = controller.queue
                            if (position == 0) {
                                truePosition = list.size - 1
                                ablumPagerAdapter!!.setCurrentItem(list.size)
                                viewpager!!.setCurrentItem(list.size, false)
                            }
                            if (position == list.size + 1) {
                                truePosition = 0
                                ablumPagerAdapter!!.setCurrentItem(1)
                                viewpager!!.setCurrentItem(1, false)
                            }
                            controller.transportControls.skipToQueueItem(list[truePosition].queueId)

                        } else {
                            val mediaUri = controller.metadata.description.mediaUri!!.toString()
                            val bundle = Bundle()
                            bundle.putString("mediaUri", mediaUri)
                            bundle.putBoolean("isRunning", false)
                            BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR, bundle)
                        }
                    }
                })


                if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                    val mediaUri = controller.metadata.description.mediaUri!!.toString()
                    val bundle = Bundle()
                    bundle.putString("mediaUri", mediaUri)
                    bundle.putBoolean("isRunning", true)
                    BroadNotifyUtils.sendReceiver(NotifyContaces.UPDATE_ABLUM_ANIMATOR, bundle)
                }
                // 改变viewpager动画时间
                try {
                    val mField = ViewPager::class.java.getDeclaredField("mScroller")
                    mField.isAccessible = true
                    val mScroller = MyScroller(viewpager!!.context.applicationContext, LinearInterpolator())
                    mField.set(viewpager, mScroller)
                } catch (e: NoSuchFieldException) {
                    e.printStackTrace()
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        setRepeatMode(repeatMode)
    }

    override fun onQueueChanged(queue: List<MediaSessionCompat.QueueItem>?) {
        val queu1 = controller.queue
        val id = controller.metadata.description.mediaId
        val index = MediaUtils.getMusicIndexOnQueue(queue!!, id!!)
        queueItemList!!.clear()
        for (i in queue.indices) {
            queueItemList!!.add(queue[i])
        }
        ablumPagerAdapter!!.setCurrentItem(index + 1)
        viewpager!!.setCurrentItem(index + 1, false)
        //        ablumPagerAdapter = new AblumPagerAdapter(getSupportFragmentManager(),queue);
        //        viewpager.setAdapter(ablumPagerAdapter);
        //
    }

    override fun onQueueTitleChanged(title: CharSequence?) {
        super.onQueueTitleChanged(title)
    }

    /**
     * 更新播放模式
     */
    private fun setRepeatMode(repeatMode: Int) {
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ONE//单曲模式
            -> playingMode!!.setImageResource(R.mipmap.icon_playmode_single)
            PlaybackStateCompat.REPEAT_MODE_ALL//循环播放模式
            -> playingMode!!.setImageResource(R.mipmap.icon_playmode_cycle)
            PlaybackStateCompat.REPEAT_MODE_GROUP//随机播放模式
            -> playingMode!!.setImageResource(R.mipmap.icon_playmode_shuffle)
        }
    }


    public override fun onDestroy() {
        super.onDestroy()
        stopSeekbarUpdate()
        mExecutorService.shutdown()
    }

    companion object {
        private val PROGRESS_UPDATE_INTERNAL: Long = 1000
        private val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 100
    }
}
