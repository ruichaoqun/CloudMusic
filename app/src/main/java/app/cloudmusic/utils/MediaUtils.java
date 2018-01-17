package app.cloudmusic.utils;

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
}
