package app.cloudmusic.ui


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator

import app.cloudmusic.R
import app.cloudmusic.utils.broadnotify.BroadNotifyUtils
import app.cloudmusic.utils.broadnotify.NotifyContaces
import app.cloudmusic.utils.imageloader.ImageLoader
import app.cloudmusic.widget.CircleImageView

class AblumFragment : Fragment(), BroadNotifyUtils.MessageReceiver {
    private var mediaUri: String? = null
    private var imageLoader: ImageLoader? = null
    private var circleImageView: CircleImageView? = null
    private var animator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w("AAA", "onCreate")
        mediaUri = arguments!!.getString("mediaUri")
    }

    @SuppressLint("ObjectAnimatorBinding")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.w("AAA", "onCreateView")
        val view = inflater.inflate(R.layout.fragment_ablum, container, false)
        circleImageView = view.findViewById(R.id.imageView)
        animator = ObjectAnimator.ofFloat(circleImageView, "rotation", 0.0f, 359.0f)
        //animator = ObjectAnimator.ofFloat(getView(), "rotation", new float[]{0.0F, 360.0F});
        animator!!.repeatCount = ValueAnimator.INFINITE
        animator!!.duration = 25000
        animator!!.interpolator = LinearInterpolator()
        BroadNotifyUtils.addReceiver(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.w("AAA", "onActivityCreated")
        imageLoader = ImageLoader(context!!)
        imageLoader!!.DisplayImage(mediaUri!!, circleImageView!!)
    }

    fun setStartAnimator(flag: Boolean) {
        if (flag) {
            if (animator!!.isStarted && animator!!.isPaused) {
                animator!!.resume()
            }
            if (!animator!!.isStarted) {
                animator!!.start()
            }
        } else {
            if (animator!!.isStarted && animator!!.isRunning) {
                animator!!.pause()
            }
        }
    }

    fun resetAimator() {
        animator!!.cancel()
    }

    override fun onMessage(receiverType: Int, bundle: Bundle) {
        if (receiverType == NotifyContaces.UPDATE_ABLUM_ANIMATOR) {
            val s = bundle.getString("mediaUri")
            if (TextUtils.equals(s, mediaUri)) {
                val b = bundle.getBoolean("isRunning")
                if (b)
                    setStartAnimator(true)
                else
                    setStartAnimator(false)
            } else {
                resetAimator()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BroadNotifyUtils.removeReceiver(this)
    }

    companion object {

        fun newInstance(mediaUri: String): AblumFragment {
            val fragment = AblumFragment()
            val args = Bundle()
            args.putString("mediaUri", mediaUri)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
