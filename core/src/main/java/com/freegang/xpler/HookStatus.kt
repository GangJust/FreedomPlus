package com.freegang.xpler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

object HookStatus {

    /**
     * 由该方法判断模块是否启用
     * 模块状态直接调用该方法进行判断
     */
    val isEnabled: Boolean get() = false


    /**
     * 由该方法判断模块是否被太极启用
     * 模块状态直接调用该方法进行判断
     */
    fun isExpModuleActive(context: Context): Boolean {
        val resolver = context.contentResolver
        val uri = Uri.parse("content://me.weishu.exposed.CP/")
        var result: Bundle? = null
        try {
            try {
                result = resolver.call(uri, "active", null, null)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val intent = Intent("me.weishu.exp.ACTION_ACTIVE")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            }
            if (result == null) {
                result = resolver.call(uri, "active", null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        result ?: return false
        return result.getBoolean("active", false)
    }
}