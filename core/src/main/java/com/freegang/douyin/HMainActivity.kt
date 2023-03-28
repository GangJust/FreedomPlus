package com.freegang.douyin

import android.os.Bundle
import android.text.Html
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionCode
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionName
import com.freegang.xpler.xp.OnAfter
import com.freegang.xpler.xp.thisContext
import com.ss.android.ugc.aweme.main.MainActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HMainActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<MainActivity>(lpparam) {
    private val config get() = Config.get()
    private val supportVersions = listOf(
        "24.0.0",
        "24.1.0",
        "24.2.0",
        "24.3.0",
        "24.4.0",
        "24.5.0",
        "24.6.0",
        "24.7.0",
    )

    @OnAfter(methodName = "onCreate")
    fun onCreate(it: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        showToast(it.thisContext, "Freedom+ Attach!")
    }

    @OnAfter(methodName = "onResume")
    fun onResume(it: XC_MethodHook.MethodHookParam) {
        showSupportDialog(it)
    }

    //抖音版本(版本是否兼容提示)
    private fun showSupportDialog(it: XC_MethodHook.MethodHookParam) {
        val activity = it.thisObject as MainActivity
        val versionName = activity.appVersionName
        val versionCode = activity.appVersionCode

        //此版本是否继续提示
        if (!config.isSupportHint && versionCode == config.dyVersionCode && versionName == config.dyVersionName) return

        launch {
            delay(2000L)
            showMessageDialog(
                activity = activity,
                title = "Freedom+",
                content = Html.fromHtml(
                    """当前抖音版本为: <span style='color:#F56C6C;'>${versionName}</span><br/>
                       Freedom+已经兼容适配以下版本:<br/>
                       ${supportVersions.joinToString(", ") { s -> if (s == versionName) "<span style='color:#F56C6C;'>$s</span>" else s }}<br/>
                       ${if (supportVersions.contains(versionName)) "当前版本已兼容适配!" else "当前版本<span style='color:#F56C6C;'>不能保证</span>所有功能都正常使用!"}
                       """.trimIndent()
                ),
                cancel = "此版本不再提示",
                confirm = "确定",
                onCancel = {
                    config.isSupportHint = false
                    config.dyVersionName = versionName
                    config.dyVersionCode = versionCode
                    config.save(activity)
                }
            )
        }
    }
}