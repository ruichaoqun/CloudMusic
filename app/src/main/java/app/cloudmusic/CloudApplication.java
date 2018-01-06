package app.cloudmusic;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/11/8.
 */

public class CloudApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        CloudApplication.this.context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}
