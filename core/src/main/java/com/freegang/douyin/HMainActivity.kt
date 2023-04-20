package com.freegang.douyin

import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.ClipboardLogic
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.xpler.utils.app.KActivityUtils.contentView
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionCode
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionName
import com.freegang.xpler.utils.view.KViewUtils
import com.freegang.xpler.xp.OnAfter
import com.freegang.xpler.xp.OnBefore
import com.freegang.xpler.xp.call
import com.freegang.xpler.xp.findMethod
import com.freegang.xpler.xp.findMethodsByReturnType
import com.freegang.xpler.xp.thisActivity
import com.freegang.xpler.xp.thisContext
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.main.MainActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HMainActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<MainActivity>(lpparam) {
    private val config get() = Config.get()
    private val clipboardLogic = ClipboardLogic(this)
    private val supportVersions = listOf(
        "23.5.0",
        "23.6.0",
        "23.7.0",
        "23.8.0",
        "23.9.0",
        "24.0.0",
        "24.1.0",
        "24.2.0",
        "24.3.0",
        "24.4.0",
        "24.5.0",
        "24.6.0",
        "24.7.0",
        "24.8.0",
        "24.9.0",
        "25.0.0",
        "25.1.0",
    )

    @OnAfter(name = "onCreate")
    fun onCreate(it: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlock(it) {
            showToast(thisContext, "Freedom+ Attach!")
        }
    }

    @OnAfter(name = "onResume")
    fun onResume(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            changeView(thisActivity.contentView)
            showSupportDialog(thisActivity as MainActivity)
            addClipboardListener(thisActivity as MainActivity)
        }
    }

    @OnBefore(name = "onPause")
    fun onPause(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            clipboardLogic.removeClipboardListener(thisContext)
        }
    }

    private fun findVideoAweme(activity: MainActivity): Aweme? {
        var aweme: Any? = null
        val methods = activity.findMethodsByReturnType(Aweme::class.java)
        if (methods.isNotEmpty()) {
            aweme = methods.first().call(activity)
        }

        if (aweme == null) {
            val curFragment = activity.findMethod("getCurFragment", *arrayOf<Any>())?.call(activity)
            val curFragmentMethods = curFragment?.findMethodsByReturnType(Aweme::class.java) ?: listOf()
            if (curFragmentMethods.isNotEmpty()) {
                aweme = curFragmentMethods.first().call(curFragment!!)
            }
        }
        return aweme as? Aweme
    }

    private fun addClipboardListener(activity: MainActivity) {
        if (!config.isDownload) return
        clipboardLogic.addClipboardListener(activity) {
            val aweme = findVideoAweme(activity)
            DownloadLogic(this@HMainActivity, activity, aweme)
        }
    }

    //抖音版本(版本是否兼容提示)
    private fun showSupportDialog(activity: MainActivity) {
        val versionName = activity.appVersionName
        val versionCode = activity.appVersionCode

        //此版本是否继续提示
        if (!config.isSupportHint && versionCode == config.dyVersionCode && versionName == config.dyVersionName) return

        launch {
            delay(2000L)
            showMessageDialog(
                context = activity,
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

    //透明度
    private fun changeView(viewGroup: ViewGroup) {
        if (!config.isTranslucent) return
        launch {
            delay(200L)
            val views = KViewUtils.findViewsExact(viewGroup, View::class.java) {
                var result = it::class.java.name.contains("MainBottomTabContainer") //底部
                result = result or it::class.java.name.contains("MainTitleBar") //顶部
                result
            }
            views.forEach { it.alpha = 0.5f }
        }
    }
}