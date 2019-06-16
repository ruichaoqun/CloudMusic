package app.cloudmusic.data

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Administrator on 2018/3/30.
 */

@Parcelize
class MediaDataInfo(
        var mediaId: String? = null,//歌曲id
        var url: String? = null,//歌曲地址
        var title: String? = null,//歌曲名
        var artist: String? = null,//歌手
        var album: String? = null,//专辑名
        var size: Long = 0,//歌曲大小
        var duration: Long = 0//歌曲时长
) : Parcelable