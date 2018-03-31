package app.cloudmusic.utils;

/**
 * Created by Administrator on 2018/3/31.
 */

public class MediaSharePreference extends BaseSPF {
    private static MediaSharePreference sharePreference = null;

    public MediaSharePreference() {
        super("AppBasicData");
    }

    public static MediaSharePreference getInstances(){
        if (sharePreference == null)
            sharePreference = new MediaSharePreference();
        return sharePreference;
    }

    public void setRepeatMode(int repeatMode){
        writeInt("repeatMode",repeatMode);
    }

    public int getRepeatMode(){
        return readInt("repeatMode",1);
    }
}
