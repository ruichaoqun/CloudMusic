package app.cloudmusic.manager

import android.os.AsyncTask
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat

import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import app.cloudmusic.CloudApplication
import app.cloudmusic.Contaces
import app.cloudmusic.utils.LogHelper

/**
 * Created by Administrator on 2017/11/6.
 * 媒体提供者API
 */

class MusicProvider @JvmOverloads constructor(private val mSource: MusicProviderSource = LocalMediaSource(CloudApplication.context!!)) {
    private val mLocalMusicList: ConcurrentMap<String, MediaMetadataCompat>
    private var currentList: MutableList<MediaBrowserCompat.MediaItem>? = null

    @Volatile
    private var mCurrentState = State.NON_INITIALIZED

    val isInitialized: Boolean
        get() = mCurrentState == State.INITIALIZED

    internal enum class State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    interface Callback {
        fun onMusicCatalogReady(success: Boolean)
    }

    init {
        mLocalMusicList = ConcurrentHashMap()
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     * 异步下载歌曲
     */
    fun retrieveMediaAsync(callback: (() -> Unit)?) {
        LogHelper.d(TAG, "retrieveMediaAsync called")
        //如果已经加载完毕
        if (mCurrentState == State.INITIALIZED) {
            callback?.onMusicCatalogReady(true)
            return
        }

        // 未加载，开启异步任务加载本地音乐到内存
        object : AsyncTask<Void, Void, State>() {
            override fun doInBackground(vararg params: Void): State {
                retrieveMedia()
                return mCurrentState
            }

            override fun onPostExecute(current: State) {
                callback?.onMusicCatalogReady(current == State.INITIALIZED)
            }
        }.execute()
    }

    private fun retrieveMedia() {
        if (mCurrentState == State.NON_INITIALIZED) {
            mCurrentState = State.INITIALIZING//切换标志到加载中

            val tracks = mSource.iterator()
            while (tracks.hasNext()) {
                val item = tracks.next()
                val musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                mLocalMusicList[musicId] = item
            }
            mCurrentState = State.INITIALIZED
        }
    }

    fun getChild(parentId: String): List<MediaBrowserCompat.MediaItem> {
        if (currentList != null)
            return currentList as MutableList<MediaBrowserCompat.MediaItem>
        currentList = ArrayList()
        if (parentId === Contaces.SERVICE_ID_LOCALMUSIC) {//返回本地所有音乐
            for (data in mLocalMusicList.values) {
                currentList!!.add(MediaBrowserCompat.MediaItem(data.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
            }
        }
        if (parentId === Contaces.GET_PALYING_LIST) {//返回本地所有音乐
            for (data in mLocalMusicList.values) {
                currentList!!.add(MediaBrowserCompat.MediaItem(data.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
            }
        }
        return currentList as ArrayList<MediaBrowserCompat.MediaItem>
    }

    fun getMusicById(musidId: String): MediaMetadataCompat? {
        return mLocalMusicList[musidId]
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(MusicProvider::class.java)
    }
}
