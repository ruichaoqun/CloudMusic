package app.cloudmusic.manager

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat

import java.util.ArrayList

import app.cloudmusic.utils.LogHelper

/**
 * Created by Administrator on 2017/11/6.
 */

class LocalMediaSource(private val mContext: Context) : MusicProviderSource {
    //projection：选择的列; where：过滤条件; sortOrder：排序。
    private val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE)
    private val where = "mime_type in ('audio/mpeg','audio/x-ms-wma','audio/mp4') and is_music > 0 "
    private val sortOrder = MediaStore.Audio.Media.DATA

    //获取本地歌曲
    private//get columns
    //add songs to list
    val localSongs: List<MediaMetadataCompat>
        get() {
            val musicResolver = mContext.contentResolver
            val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val musicCursor = musicResolver.query(musicUri, projection, where, null, sortOrder)
            val list = ArrayList<MediaMetadataCompat>()

            if (musicCursor != null && musicCursor.moveToFirst()) {
                val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val displayColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                do {
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    val thisAlbum = musicCursor.getString(albumColumn)
                    val path = musicCursor.getString(pathColumn)
                    val duration = musicCursor.getLong(durationColumn)
                    val displayTitle = musicCursor.getString(displayColumn)

                    val media = MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, thisId.toString())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, thisAlbum)
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, thisArtist)
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, thisTitle)
                            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                            .build()
                    list.add(media)
                } while (musicCursor.moveToNext())
            }
            return list
        }

    override fun iterator(): Iterator<MediaMetadataCompat> {
        return localSongs.iterator()
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(LocalMediaSource::class.java)
    }
}
