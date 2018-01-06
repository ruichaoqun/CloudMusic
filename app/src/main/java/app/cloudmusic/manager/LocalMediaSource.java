package app.cloudmusic.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.cloudmusic.utils.LogHelper;

/**
 * Created by Administrator on 2017/11/6.
 */

public class LocalMediaSource implements MusicProviderSource{
    private static final String TAG = LogHelper.makeLogTag(LocalMediaSource.class);
    private Context mContext;
    //projection：选择的列; where：过滤条件; sortOrder：排序。
    private String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
    };
    private String where =  "mime_type in ('audio/mpeg','audio/x-ms-wma') and is_music > 0 " ;
    private String sortOrder = MediaStore.Audio.Media.DATA;

    public LocalMediaSource(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        return getLocalSongs().iterator();
    }

    //获取本地歌曲
    private List<MediaMetadataCompat> getLocalSongs() {
        ContentResolver musicResolver = mContext.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, projection, where, null, sortOrder);
        List<MediaMetadataCompat> list = new ArrayList<>();

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int durationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            int displayColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String path = musicCursor.getString(pathColumn);
                long duration = musicCursor.getLong(durationColumn);
                String displayTitle = musicCursor.getString(displayColumn);

                MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(thisId))
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, thisAlbum)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, thisArtist)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, thisTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,path)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .build();
                list.add(media);
            }
            while (musicCursor.moveToNext());
        }
        return list;
    }
}
