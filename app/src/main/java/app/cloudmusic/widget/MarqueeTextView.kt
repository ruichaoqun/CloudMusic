package app.cloudmusic.widget

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by Administrator on 2018/1/9.
 */

class MarqueeTextView : TextView {

    var isMarqueeEnable = false
        set(enable) {
            if (this.isMarqueeEnable != enable) {
                field = enable
                if (enable) {
                    ellipsize = TextUtils.TruncateAt.MARQUEE
                } else {
                    ellipsize = TextUtils.TruncateAt.END
                }
                onWindowFocusChanged(enable)
            }
        }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun isFocused(): Boolean {
        return this.isMarqueeEnable
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(this.isMarqueeEnable, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(this.isMarqueeEnable)
    }
}
