package app.cloudmusic.manager;

import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;


import java.util.List;

import app.cloudmusic.data.MediaDataInfo;
import app.cloudmusic.utils.MediaUtils;

/**
 * Created by Administrator on 2017/11/15.
 */

public class QueueManager {
    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private int mCurrentIndex;

    private MusicProvider musicProvider;
    private Resources resource;
    private MetaDataUpdateListener listener;
    private String title;

    public QueueManager(MusicProvider musicProvider, Resources resource, MetaDataUpdateListener listener) {
        this.musicProvider = musicProvider;
        this.resource = resource;
        this.listener = listener;
    }


    public void setCurrentQueueIndex(int index){
        if(index >= 0 && index < mPlayingQueue.size()){
            mCurrentIndex = index;
            listener.onCurrentQueueIndexUpdated(index);
        }
    }

    public boolean skipQueuePosition(int amount){
        if(mPlayingQueue.size() == 0)
            return false;
        int index = mCurrentIndex + amount;
        if(index < 0){
            index = mPlayingQueue.size() - 1;
        }else{
            index %= mPlayingQueue.size();
        }
        mCurrentIndex = index;
        return true;
    }


    public void setCurrentQueue(List<MediaDataInfo> list,String title,String mediaId){
        boolean canReuseQueue = false;
        if(TextUtils.equals(this.title,title)){
            canReuseQueue = setCurrentQueueItem(mediaId);
        }

        if(!canReuseQueue){
            mPlayingQueue = MediaUtils.transformTogetQueue(list);
            this.title = title;
            int index = 0;
            if(mediaId != null){
                index = MediaUtils.getMusicIndexOnQueue(mPlayingQueue,mediaId);
            }
            mCurrentIndex = Math.max(index,0);
            listener.onQueueUpdated(title,mPlayingQueue);
        }
        updateMetadata();

    }

    public boolean setCurrentQueueItem(String mediaId){
        int index = MediaUtils.getMusicIndexOnQueue(mPlayingQueue, mediaId);
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    public boolean setCurrentQueueItem(long queueId){
        int index = MediaUtils.getMusicIndexOnQueue(mPlayingQueue,queueId);
        setCurrentQueueIndex(index);
        return index > 0;
    }

    public MediaSessionCompat.QueueItem getCurrentMusic() {
        if(mPlayingQueue != null && mCurrentIndex >= 0 && mCurrentIndex < mPlayingQueue.size()){
            return mPlayingQueue.get(mCurrentIndex);
        }
        return null;
    }

    public boolean isLastMusic() {
        if(mCurrentIndex == mPlayingQueue.size() - 1)
            return true;
        return false;
    }

    public int getQueueSize() {
        return mPlayingQueue.size();
    }

    public void updateMetadata() {
        String mediaId = getCurrentMusic().getDescription().getMediaId();
        MediaMetadataCompat metadataCompat = musicProvider.getMusicById(mediaId);
        if (metadataCompat == null) {
            throw new IllegalArgumentException("无效Id " + mediaId);
        }
        listener.onMetaDataChanged(metadataCompat);
    }


    public interface MetaDataUpdateListener{
        void onMetaDataChanged(MediaMetadataCompat metadata);
        void onError();
        void onCurrentQueueIndexUpdated(int queueIndex);
        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }
}
