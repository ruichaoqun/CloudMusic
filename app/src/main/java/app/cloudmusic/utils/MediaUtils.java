package app.cloudmusic.utils;

import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import app.cloudmusic.data.MediaDataInfo;

/**
 * Created by Administrator on 2018/1/11.
 */

public class MediaUtils {
    //格式化歌曲时长以及播放时长
    public static String makeTimeString(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }

    /**
     * 将自定义的MediaDataInfo转换成QueueItem
     * @param infos
     * @return
     */
    public static List<MediaSessionCompat.QueueItem> transformTogetQueue(List<MediaDataInfo> infos){
        List<MediaSessionCompat.QueueItem> list = new ArrayList<>();
        for (int i = 0; i < infos.size(); i++) {
            MediaDataInfo info = infos.get(i);
            MediaDescriptionCompat descriptionCompat =
                    new MediaDescriptionCompat.Builder()
                    .setMediaId(info.getMediaId())
                    .setMediaUri(Uri.parse(info.getUrl()))
                    .setTitle(info.getTitle())
                    .setSubtitle(info.getArtist())
                    .build();
            MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(descriptionCompat,i);
            list.add(item);
        }
        return list;
    }

    public static List<MediaDataInfo>  tarsformToMediaDataInfo(List<MediaBrowserCompat.MediaItem> medias){
        List<MediaDataInfo> list = new ArrayList<>();
        for (int i = 0; i < medias.size(); i++) {
            MediaBrowserCompat.MediaItem data = medias.get(i);
            MediaDataInfo dataInfo = new MediaDataInfo();
            dataInfo.setMediaId(data.getMediaId());
            dataInfo.setUrl(data.getDescription().getMediaUri().toString());
            list.add(dataInfo);
        }
        return list;
    }

    public static int getMusicIndexOnQueue(List<MediaSessionCompat.QueueItem> queue, String mediaId) {
        int index = -1;
        for (int i = 0; i < queue.size(); i++) {
            String s = queue.get(i).getDescription().getMediaId();
            if (TextUtils.equals(mediaId,s)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static int getMusicIndexOnQueue(List<MediaSessionCompat.QueueItem> queue,
                                           long queueId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
