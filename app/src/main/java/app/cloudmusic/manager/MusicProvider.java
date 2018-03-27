package app.cloudmusic.manager;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import app.cloudmusic.CloudApplication;
import app.cloudmusic.Contaces;
import app.cloudmusic.utils.LogHelper;

/**
 * Created by Administrator on 2017/11/6.
 * 媒体提供者API
 */

public class MusicProvider {
    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);


    private MusicProviderSource mSource;
    private ConcurrentMap<String, MediaMetadataCompat> mLocalMusicList;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider() {
        this(new LocalMediaSource(CloudApplication.getContext()));
    }

    public MusicProvider(MusicProviderSource source) {
        mSource = source;
        mLocalMusicList = new ConcurrentHashMap<>();
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     * 异步下载歌曲
     */
    public void retrieveMediaAsync(final Callback callback) {
        LogHelper.d(TAG, "retrieveMediaAsync called");
        //如果已经加载完毕
        if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                // Nothing to do, execute callback immediately
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        // 未加载，开启异步任务加载本地音乐到内存
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private void retrieveMedia() {
        if(mCurrentState == State.NON_INITIALIZED){
            mCurrentState = State.INITIALIZING;//切换标志到加载中

            Iterator<MediaMetadataCompat> tracks = mSource.iterator();
            while (tracks.hasNext()){
                MediaMetadataCompat item = tracks.next();
                String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                mLocalMusicList.put(musicId,item);
            }
            mCurrentState = State.INITIALIZED;
        }
    }

    public List<MediaBrowserCompat.MediaItem> getChild(String parentId ){
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        if(parentId == Contaces.SERVICE_ID_LOCALMUSIC){//返回本地所有音乐
            for (MediaMetadataCompat data:mLocalMusicList.values()) {
                mediaItems.add(new MediaBrowserCompat.MediaItem(data.getDescription(),MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
            }
        }
        if(parentId == Contaces.GET_PALYING_LIST){//返回本地所有音乐
            for (MediaMetadataCompat data:mLocalMusicList.values()) {
                mediaItems.add(new MediaBrowserCompat.MediaItem(data.getDescription(),MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
            }
        }
        return mediaItems;
    }

    public MediaMetadataCompat getMusicById(String musidId){
        return mLocalMusicList.get(musidId);
    }
}
