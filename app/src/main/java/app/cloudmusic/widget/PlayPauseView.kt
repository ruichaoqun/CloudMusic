package app.cloudmusic.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View

import app.cloudmusic.R
import app.cloudmusic.utils.UIUtils

/**
 * Created by Administrator on 2018/1/9.
 */

class PlayPauseView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var paint: Paint? = null
    private var state = PLAY_STATE_PLAYING//播放状态
    private var path: Path? = null
    private var path1: Path? = null
    private var path2: Path? = null//里面三角路径
    private var radius: Int = 0//半径
    private val color = ContextCompat.getColor(getContext(), R.color.colorPrimary)
    private var max: Int = 0//最大播放进度
    private var progress = 0//播放进度

    init {
        init()
    }

    private fun init() {
        val hudu = 2 * Math.PI / 360
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.strokeWidth = 5f
        paint!!.style = Paint.Style.STROKE
        radius = UIUtils.dip2px(context, 25f) / 2
        path = Path()
        path1 = Path()
        path2 = Path()
        val x = UIUtils.dip2px(context, 50f) / 2
        val y = UIUtils.dip2px(context, 50f) / 2
        val side = radius * 2 / 3
        val x1 = (x - side / (2 * Math.tan(hudu * 60))).toInt()
        val y1 = y - side / 2
        path!!.moveTo(x1.toFloat(), y1.toFloat())
        path1!!.moveTo(x1.toFloat(), y1.toFloat())
        val y2 = y1 + side
        path!!.lineTo(x1.toFloat(), y2.toFloat())
        path1!!.lineTo(x1.toFloat(), y2.toFloat())
        val x3 = (x1 + side * Math.sin(hudu * 60)).toInt()
        val y3 = y1 + side / 2
        path!!.lineTo(x3.toFloat(), y3.toFloat())
        val x4 = (x1 + side * Math.tan(hudu * 30)).toInt()
        path2!!.moveTo(x4.toFloat(), y1.toFloat())
        path2!!.lineTo(x4.toFloat(), y2.toFloat())
        path!!.close()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(UIUtils.dip2px(context, 50f), UIUtils.dip2px(context, 50f))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (state == PLAY_STATE_PAUSE) {
            paint!!.color = Color.parseColor("#444444")
            canvas.drawCircle((canvas.width / 2).toFloat(), (canvas.height / 2).toFloat(), radius.toFloat(), paint!!)
            canvas.drawPath(path!!, paint!!)
            paint!!.color = color
            val rectF = RectF((UIUtils.dip2px(context, 25f) / 2).toFloat(), (UIUtils.dip2px(context, 25f) / 2).toFloat(), (UIUtils.dip2px(context, 50f) - UIUtils.dip2px(context, 25f) / 2).toFloat(), (UIUtils.dip2px(context, 50f) - UIUtils.dip2px(context, 25f) / 2).toFloat())
            val angle = (100.0 * progress.toDouble() * 3.6 / max).toInt()
            canvas.drawArc(rectF, -90f, angle.toFloat(), false, paint!!)
        } else {
            paint!!.color = Color.parseColor("#999999")
            canvas.drawCircle((canvas.width / 2).toFloat(), (canvas.height / 2).toFloat(), radius.toFloat(), paint!!)
            paint!!.color = color
            canvas.drawPath(path1!!, paint!!)
            canvas.drawPath(path2!!, paint!!)
            val rectF = RectF((UIUtils.dip2px(context, 25f) / 2).toFloat(), (UIUtils.dip2px(context, 25f) / 2).toFloat(), (UIUtils.dip2px(context, 50f) - UIUtils.dip2px(context, 25f) / 2).toFloat(), (UIUtils.dip2px(context, 50f) - UIUtils.dip2px(context, 25f) / 2).toFloat())
            val angle = (100.0 * progress.toDouble() * 3.6 / max).toInt()
            canvas.drawArc(rectF, -90f, angle.toFloat(), false, paint!!)
        }
    }

    fun setMax(max: Int) {
        this.max = max
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        postInvalidate()
    }

    fun setState(state: Int) {
        this.state = state
        postInvalidate()
    }

    companion object {
        val PLAY_STATE_PLAYING = 1
        val PLAY_STATE_PAUSE = 0
    }
}
