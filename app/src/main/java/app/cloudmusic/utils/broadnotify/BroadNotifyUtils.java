package app.cloudmusic.utils.broadnotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.cloudmusic.CloudApplication;

/**
 * Created by Administrator on 2016/11/9 0009.
 * 广播全局类
 */
public class BroadNotifyUtils {

    private final static String ACTION_TYPE_NAME = "ReceiverTypeName";
    private final static String ACTION_BUNDLE = "bundle";

    public static final String ACTION_BROAD_NOTIFY = "app.cloudmusic";

    static class MessageObserverReceiver extends BroadcastReceiver {

        public MessageObserverReceiver(){
            CloudApplication.getContext().registerReceiver(this, new IntentFilter(ACTION_BROAD_NOTIFY));
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int receiverType = intent.getIntExtra(ACTION_TYPE_NAME, 0);
            Bundle bundle = intent.getBundleExtra(ACTION_BUNDLE);
            for(MessageReceiver receiver : receiverList){
                receiver.onMessage(receiverType, bundle);
            }
        }
    }

    private static MessageObserverReceiver broadCast = new MessageObserverReceiver();

    /** 发送广播 */
    public static void sendReceiver(int receiverType,Bundle bundle){
        if(!receiverList.isEmpty()){
            Intent intent = new Intent(ACTION_BROAD_NOTIFY);
            intent.putExtra(ACTION_TYPE_NAME, receiverType);
            intent.putExtra(ACTION_BUNDLE, bundle);
            CloudApplication.getContext().sendBroadcast(intent);
        }
    }

    /** 添加广播监听器 */
    public static void addReceiver(MessageReceiver receiver){
        receiverList.add(receiver);
    }

    /** 移除广播监听器 */
    public static void removeReceiver(MessageReceiver receiver){
        receiverList.remove(receiver);
    }

    public interface MessageReceiver{

        void onMessage(int receiverType, Bundle bundle);
    }

    private static List<MessageReceiver> receiverList;

    static{
        receiverList = Collections.synchronizedList(new ArrayList<MessageReceiver>());
    }

}
