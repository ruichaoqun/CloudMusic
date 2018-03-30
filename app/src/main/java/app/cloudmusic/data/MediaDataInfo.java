package app.cloudmusic.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2018/3/30.
 */

public class MediaDataInfo implements Parcelable {
    private String mediaId;//歌曲id
    private String url;//歌曲地址
    private String title;//歌曲名
    private String artist;//歌手
    private String album;//专辑名
    private long size;//歌曲大小
    private long duration;//歌曲时长

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mediaId);
        dest.writeString(this.url);
        dest.writeString(this.title);
        dest.writeString(this.artist);
        dest.writeString(this.album);
        dest.writeLong(this.size);
        dest.writeLong(this.duration);
    }

    public MediaDataInfo() {
    }

    protected MediaDataInfo(Parcel in) {
        this.mediaId = in.readString();
        this.url = in.readString();
        this.title = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.size = in.readLong();
        this.duration = in.readLong();
    }

    public static final Creator<MediaDataInfo> CREATOR = new Creator<MediaDataInfo>() {
        @Override
        public MediaDataInfo createFromParcel(Parcel source) {
            return new MediaDataInfo(source);
        }

        @Override
        public MediaDataInfo[] newArray(int size) {
            return new MediaDataInfo[size];
        }
    };
}