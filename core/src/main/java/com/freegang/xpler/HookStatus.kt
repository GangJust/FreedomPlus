package com.freegang.xpler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.json.KJSONUtils
import com.freegang.ktutils.json.getStringOrDefault

object HookStatus {

    /**
     * 由该方法判断模块是否启用
     * 模块状态直接调用该方法进行判断
     * see at: HookInit#moduleInit
     */
    val isEnabled: Boolean get() = false

    /**
     * 似乎每个框架都自定义了 logcat tag
     * 通过反射 XposedBridge.TAG 来获取框架类型
     * see at: HookInit#moduleInit
     */
    val moduleState: String get() = "Unknown"

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

    /**
     * 由该方法判断某个App是否内置模块
     * 调用该方法需要Manifest声明权限：android.permission.QUERY_ALL_PACKAGES
     * 需要目标包名应用已安装
     * @param context Context
     * @param packageName PackageName
     * @return 字符串数组, 包含 Lspatch 的版本名、版本号等
     */
    fun isLspatchActive(context: Context, packageName: String): Array<String> {
        //see at:
        //https://github.com/LSPosed/LSPatch/blob/master/manager/src/main/java/org/lsposed/lspatch/util/LSPPackageManager.kt#L73
        //https://github.com/LSPosed/LSPatch/blob/master/manager/src/main/java/org/lsposed/lspatch/ui/viewmodel/manage/AppManageViewModel.kt#L42
        try {
            val packageInfo = KAppUtils.getPackageInfo(
                context = context,
                packageName = packageName,
                flags = PackageManager.GET_META_DATA,
            )
            val appInfo = packageInfo.applicationInfo
            val config = appInfo.metaData?.getString("lspatch") ?: ""
            if (config.isEmpty()) return emptyArray()

            val json = Base64.decode(config, Base64.DEFAULT).toString(Charsets.UTF_8)
            val patchConfig = KJSONUtils.parse(json)
            val lspConfig = patchConfig.getJSONObject("lspConfig")
            val versionName = lspConfig.getStringOrDefault("VERSION_NAME")
            val versionCode = lspConfig.getStringOrDefault("VERSION_CODE")
            return arrayOf(versionName, versionCode)
        } catch (e: Exception) {
            e.printStackTrace()
            //KLogCat.e(e.stackTraceToString())
        }
        return emptyArray()
    }
}