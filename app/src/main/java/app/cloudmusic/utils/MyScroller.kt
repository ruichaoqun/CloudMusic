package app.cloudmusic.utils

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller

/**
 * Created by Administrator on 2018/4/3.
 */

class MyScroller : Scroller {
    private var animTime = VIEWPAGER_SCROLL_TIME

    constructor(context: Context) : super(context) {}

    constructor(context: Context, interpolator: Interpolator) : super(context, interpolator) {}

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, animTime)
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, dy, animTime)
    }

    fun setmDuration(animTime: Int) {
        this.animTime = animTime
    }

    companion object {
        private val VIEWPAGER_SCROLL_TIME = 390
    }
}
