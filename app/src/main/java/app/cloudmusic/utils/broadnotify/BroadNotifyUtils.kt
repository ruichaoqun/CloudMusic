package app.cloudmusic.utils.broadnotify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle


import java.util.ArrayList
import java.util.Collections

import app.cloudmusic.CloudApplication

/**
 * Created by Administrator on 2016/11/9 0009.
 * 广播全局类
 */
object BroadNotifyUtils {

    private val ACTION_TYPE_NAME = "ReceiverTypeName"
    private val ACTION_BUNDLE = "bundle"

    val ACTION_BROAD_NOTIFY = "app.cloudmusic"

    private val broadCast = MessageObserverReceiver()

    private var receiverList: MutableList<MessageReceiver>? = null

    internal class MessageObserverReceiver : BroadcastReceiver() {
        init {
            CloudApplication.context!!.registerReceiver(this, IntentFilter(ACTION_BROAD_NOTIFY))
        }

        override fun onReceive(context: Context, intent: Intent) {
            val receiverType = intent.getIntExtra(ACTION_TYPE_NAME, 0)
            val bundle = intent.getBundleExtra(ACTION_BUNDLE)
            for (receiver in receiverList!!) {
                receiver.onMessage(receiverType, bundle)
            }
        }
    }

    /** 发送广播  */
    fun sendReceiver(receiverType: Int, bundle: Bundle) {
        if (!receiverList!!.isEmpty()) {
            val intent = Intent(ACTION_BROAD_NOTIFY)
            intent.putExtra(ACTION_TYPE_NAME, receiverType)
            intent.putExtra(ACTION_BUNDLE, bundle)
            CloudApplication.context!!.sendBroadcast(intent)
        }
    }

    /** 添加广播监听器  */
    fun addReceiver(receiver: MessageReceiver) {
        receiverList!!.add(receiver)
    }

    /** 移除广播监听器  */
    fun removeReceiver(receiver: MessageReceiver) {
        receiverList!!.remove(receiver)
    }

    interface MessageReceiver {

        fun onMessage(receiverType: Int, bundle: Bundle)
    }

    init {
        receiverList = Collections.synchronizedList(ArrayList())
    }

}
