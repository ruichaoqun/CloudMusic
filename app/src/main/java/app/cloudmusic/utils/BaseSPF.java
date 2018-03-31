package app.cloudmusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


import app.cloudmusic.CloudApplication;

/**
 * Created by Administrator on 2016/10/11 0011.
 */
public abstract class BaseSPF {

    private Context context;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;

    public BaseSPF(String titleFlag){
        context = CloudApplication.getContext();
        spf = context.getSharedPreferences(titleFlag, context.MODE_PRIVATE);
        editor = spf.edit();
    }

    /** 写入String数据 */
    public void writeString(String key, String value){
        if (TextUtils.isEmpty(key))
            return;
        if (value == null)
            value = "";
        editor.putString(key, value);
        editor.commit();
    }

    /** 读取String数据 */
    public String readString(String key){
        return spf.getString(key, "");
    }

    /** 写入Int数据 */
    public void writeInt(String key, int value){
        if (TextUtils.isEmpty(key))
            return;
        editor.putInt(key, value);
        editor.commit();
    }

    /** 读取Int数据 */
    public int readInt(String key){
        return spf.getInt(key, 0);
    }

    /** 读取Int数据 */
    public int readInt(String key,int defvalue){
        return spf.getInt(key, defvalue);
    }



    /** 写入Boolean数据 */
    public void writeBoolean(String key, boolean value){
        if (TextUtils.isEmpty(key))
            return;
        editor.putBoolean(key, value);
        editor.commit();
    }

    /** 读取Boolean数据 */
    public boolean readBoolean(String key){
        return spf.getBoolean(key, true);
    }

    /** 写入Float数据 */
    public void writeFloat(String key, float value){
        if (TextUtils.isEmpty(key))
            return;
        editor.putFloat(key, value);
        editor.commit();
    }

    /** 读取Float数据 */
    public float readFloat(String key){
        return spf.getFloat(key, 0f);
    }

    /** 写入Long数据 */
    public void writeLong(String key, long value){
        if (TextUtils.isEmpty(key))
            return;
        editor.putLong(key, value);
        editor.commit();
    }

    /** 读取Long数据 */
    public long readLong(String key){
        return spf.getLong(key, 0);
    }

    /** 写入Double数据 */
    public void writeDouble(String key, double value){
        if (TextUtils.isEmpty(key))
            return;
         editor.putString(key, String.valueOf(value));
        editor.commit();
    }

    /** 读取Double数据 */
    public double readDouble(String key){
        String value = readString(key);
        if (TextUtils.isEmpty(value))
            return 0;
        return Double.parseDouble(value);
    }
}
