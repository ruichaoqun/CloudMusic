package app.cloudmusic.utils

/**
 * Created by Administrator on 2018/3/31.
 */

class MediaSharePreference : BaseSPF("AppBasicData") {

    var repeatMode: Int
        get() = readInt("repeatMode", 1)
        set(repeatMode) = writeInt("repeatMode", repeatMode)

    companion object {
        private var sharePreference: MediaSharePreference? = null

        val instances: MediaSharePreference
            get() {
                if (sharePreference == null)
                    sharePreference = MediaSharePreference()
                return sharePreference!!
            }
    }
}
