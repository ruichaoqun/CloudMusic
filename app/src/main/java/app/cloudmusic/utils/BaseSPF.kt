package app.cloudmusic.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.text.TextUtils


import app.cloudmusic.CloudApplication

/**
 * Created by Administrator on 2016/10/11 0011.
 */
abstract class BaseSPF(titleFlag: String) {

    private val context: Context?
    private val spf: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        context = CloudApplication.context
        spf = context!!.getSharedPreferences(titleFlag, MODE_PRIVATE)
        editor = spf.edit()
    }

    /** 写入String数据  */
    fun writeString(key: String, value: String?) {
        var value = value
        if (TextUtils.isEmpty(key))
            return
        if (value == null)
            value = ""
        editor.putString(key, value)
        editor.commit()
    }

    /** 读取String数据  */
    fun readString(key: String): String {
        return spf.getString(key, "")
    }

    /** 写入Int数据  */
    fun writeInt(key: String, value: Int) {
        if (TextUtils.isEmpty(key))
            return
        editor.putInt(key, value)
        editor.commit()
    }

    /** 读取Int数据  */
    fun readInt(key: String): Int {
        return spf.getInt(key, 0)
    }

    /** 读取Int数据  */
    fun readInt(key: String, defvalue: Int): Int {
        return spf.getInt(key, defvalue)
    }


    /** 写入Boolean数据  */
    fun writeBoolean(key: String, value: Boolean) {
        if (TextUtils.isEmpty(key))
            return
        editor.putBoolean(key, value)
        editor.commit()
    }

    /** 读取Boolean数据  */
    fun readBoolean(key: String): Boolean {
        return spf.getBoolean(key, true)
    }

    /** 写入Float数据  */
    fun writeFloat(key: String, value: Float) {
        if (TextUtils.isEmpty(key))
            return
        editor.putFloat(key, value)
        editor.commit()
    }

    /** 读取Float数据  */
    fun readFloat(key: String): Float {
        return spf.getFloat(key, 0f)
    }

    /** 写入Long数据  */
    fun writeLong(key: String, value: Long) {
        if (TextUtils.isEmpty(key))
            return
        editor.putLong(key, value)
        editor.commit()
    }

    /** 读取Long数据  */
    fun readLong(key: String): Long {
        return spf.getLong(key, 0)
    }

    /** 写入Double数据  */
    fun writeDouble(key: String, value: Double) {
        if (TextUtils.isEmpty(key))
            return
        editor.putString(key, value.toString())
        editor.commit()
    }

    /** 读取Double数据  */
    fun readDouble(key: String): Double {
        val value = readString(key)
        return if (TextUtils.isEmpty(value)) 0.0 else java.lang.Double.parseDouble(value)
    }
}
