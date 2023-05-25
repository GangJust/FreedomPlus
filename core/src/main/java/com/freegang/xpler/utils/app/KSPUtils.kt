package com.freegang.xpler.utils.app

import android.content.Context
import android.content.SharedPreferences

object KSPUtils {
    const val DEFAULT_SP_NAME = "app_config"

    @JvmStatic
    @JvmOverloads
    fun getSp(context: Context, spName: String = DEFAULT_SP_NAME): SharedPreferences {
        return context.getSharedPreferences(spName, Context.MODE_PRIVATE)
    }

    // 获取 String 类型数据
    @JvmStatic
    @JvmOverloads
    fun putString(context: Context, key: String, value: String, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(context, spName)
        sp.edit().putString(key, value).apply()
    }

    @JvmStatic
    @JvmOverloads
    fun getString(context: Context, key: String, default: String = "", spName: String = DEFAULT_SP_NAME): String {
        val sp = getSp(context, spName)
        return sp.getString(key, default) ?: default
    }

    // 获取 Int 类型数据
    @JvmStatic
    @JvmOverloads
    fun putInt(context: Context, key: String, value: Int, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(context, spName)
        sp.edit().putInt(key, value).apply()
    }

    @JvmStatic
    @JvmOverloads
    fun getInt(context: Context, key: String, default: Int = 0, spName: String = DEFAULT_SP_NAME): Int {
        val sp = getSp(context, spName)
        return sp.getInt(key, default)
    }

    // 获取 Long 类型的值
    @JvmStatic
    @JvmOverloads
    fun putLong(context: Context, key: String, value: Long, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(context, spName)
        sp.edit().putLong(key, value).apply()
    }

    @JvmStatic
    @JvmOverloads
    fun getLong(context: Context, key: String, default: Long = 0L, spName: String = DEFAULT_SP_NAME): Long {
        val sp = getSp(context, spName)
        return sp.getLong(key, default)
    }

    // 获取 Float 类型的值
    @JvmStatic
    @JvmOverloads
    fun putFloat(context: Context, key: String, value: Float, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(context, spName)
        sp.edit().putFloat(key, value).apply()
    }

    @JvmStatic
    @JvmOverloads
    fun getFloat(context: Context, key: String, default: Float = 0f, spName: String = DEFAULT_SP_NAME): Float {
        val sp = getSp(context, spName)
        return sp.getFloat(key, default)
    }

    // 获取 Boolean 类型的值
    @JvmStatic
    @JvmOverloads
    fun putBoolean(context: Context, key: String, value: Boolean, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(context, spName)
        sp.edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    @JvmOverloads
    fun getBoolean(context: Context, key: String, default: Boolean = false, spName: String = DEFAULT_SP_NAME): Boolean {
        val sp = getSp(context, spName)
        return sp.getBoolean(key, default)
    }

    // 获取 Set<String> 类型的值
    @JvmStatic
    @JvmOverloads
    fun putStringSet(context: Context, key: String, value: Set<String>, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(context, spName)
        sp.edit().putStringSet(key, value).apply()
    }

    @JvmStatic
    @JvmOverloads
    fun getStringSet(context: Context, key: String, default: Set<String> = emptySet(), spName: String = DEFAULT_SP_NAME): Set<String> {
        val sp = getSp(context, spName)
        return sp.getStringSet(key, default) ?: default
    }

    // 删除指定 key 的数据
    @JvmStatic
    @JvmOverloads
    fun remove(context: Context, key: String, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(context, spName)
        sp.edit().remove(key).apply()
    }

    // 删除所有数据
    @JvmStatic
    @JvmOverloads
    fun clear(context: Context, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(context, spName)
        sp.edit().clear().apply()
    }
}

// 扩展方法：获取 String 类型数据
fun Context.getSpString(key: String, default: String = "", spName: String = KSPUtils.DEFAULT_SP_NAME): String {
    return KSPUtils.getString(this, key, default, spName)
}

// 扩展方法：获取 Boolean 类型数据
fun Context.getSpBoolean(key: String, default: Boolean = false, spName: String = KSPUtils.DEFAULT_SP_NAME): Boolean {
    return KSPUtils.getBoolean(this, key, default, spName)
}

// 扩展方法：获取 Int 类型数据
fun Context.getSpInt(key: String, default: Int = 0, spName: String = KSPUtils.DEFAULT_SP_NAME): Int {
    return KSPUtils.getInt(this, key, default, spName)
}

// 扩展方法：获取 Long 类型数据
fun Context.getSpLong(key: String, default: Long = 0L, spName: String = KSPUtils.DEFAULT_SP_NAME): Long {
    return KSPUtils.getLong(this, key, default, spName)
}

// 扩展方法：获取 Float 类型数据
fun Context.getSpFloat(key: String, default: Float = 0f, spName: String = KSPUtils.DEFAULT_SP_NAME): Float {
    return KSPUtils.getFloat(this, key, default, spName)
}

// 扩展方法：保存 String 类型数据
fun Context.putSpString(key: String, value: String, spName: String = KSPUtils.DEFAULT_SP_NAME) {
    KSPUtils.putString(this, key, value, spName)
}

// 扩展方法：保存 Boolean 类型数据
fun Context.putSpBoolean(key: String, value: Boolean, spName: String = KSPUtils.DEFAULT_SP_NAME) {
    KSPUtils.putBoolean(this, key, value, spName)
}

// 扩展方法：保存 Int 类型数据
fun Context.putSpInt(key: String, value: Int, spName: String = KSPUtils.DEFAULT_SP_NAME) {
    KSPUtils.putInt(this, key, value, spName)
}

// 扩展方法：保存 Long 类型数据
fun Context.putSpLong(key: String, value: Long, spName: String = KSPUtils.DEFAULT_SP_NAME) {
    KSPUtils.putLong(this, key, value, spName)
}

// 扩展方法：保存 Float 类型数据
fun Context.putSpFloat(key: String, value: Float, spName: String = KSPUtils.DEFAULT_SP_NAME) {
    KSPUtils.putFloat(this, key, value, spName)
}

// 扩展方法：删除指定 key 的数据
fun Context.removeSpValue(key: String, spName: String = KSPUtils.DEFAULT_SP_NAME) {
    KSPUtils.remove(this, key, spName)
}

// 扩展方法：删除所有数据
fun Context.clearSp(spName: String = KSPUtils.DEFAULT_SP_NAME) {
    KSPUtils.clear(this, spName)
}