package app.cloudmusic.utils;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by Administrator on 2018/4/3.
 */

public class MyScroller extends Scroller {
    private static final int VIEWPAGER_SCROLL_TIME = 390;
    private int animTime = VIEWPAGER_SCROLL_TIME;

    public MyScroller(Context context) {
        super(context);
    }

    public MyScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, animTime);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, animTime);
    }

    public void setmDuration(int animTime) {
        this.animTime = animTime;
    }
}
