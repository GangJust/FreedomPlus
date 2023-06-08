package com.freegang.douyin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.ViewGroup
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.config.Version
import com.freegang.douyin.logic.ClipboardLogic
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.reflect.findMethodAndInvoke
import com.freegang.ktutils.view.traverse
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.call
import com.freegang.xpler.core.findMethodsByReturnType
import com.freegang.xpler.core.thisActivity
import com.freegang.xpler.core.thisContext
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.main.MainActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class HMainActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<MainActivity>(lpparam) {
    private val config get() = Config.get()
    private val clipboardLogic = ClipboardLogic(this)
    private val supportVersions = listOf(
        "23.5.0", "23.6.0", "23.7.0", "23.8.0", "23.9.0",
        "24.0.0", "24.1.0", "24.2.0", "24.3.0", "24.4.0",
        "24.5.0", "24.6.0", "24.7.0", "24.8.0", "24.9.0",
        "25.0.0", "25.1.0", "25.2.0", "25.3.0", "25.4.0",
        "25.5.0", "25.6.0",
    )

    @OnAfter("onCreate")
    fun onCreate(it: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlock(it) {
            showToast(thisContext, "Freedom+ Attach!")
        }
    }

    @OnAfter("onResume")
    fun onResume(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            changeViewAlpha(thisActivity.contentView)
            showSupportDialog(thisActivity)
            checkVersionDialog(thisActivity)
            addClipboardListener(thisActivity)
        }
    }

    @OnBefore("onPause")
    fun onPause(it: XC_MethodHook.MethodHookParam) {
        hookBlock(it) {
            saveConfig(thisContext)
            clipboardLogic.removeClipboardListener(thisContext)
        }
    }

    @OnAfter("onDestroy")
    fun onDestroy(it: XC_MethodHook.MethodHookParam) {

    }

    private fun findVideoAweme(activity: Activity): Aweme? {
        var aweme: Any? = null
        val methods = activity.findMethodsByReturnType(Aweme::class.java)
        if (methods.isNotEmpty()) {
            aweme = methods.first().call(activity)
        }

        if (aweme == null) {
            val curFragment = activity.findMethodAndInvoke("getCurFragment")
            val curFragmentMethods = curFragment?.findMethodsByReturnType(Aweme::class.java) ?: listOf()
            if (curFragmentMethods.isNotEmpty()) {
                aweme = curFragmentMethods.first().call(curFragment!!)
            }
        }
        return aweme as Aweme?
    }

    private fun addClipboardListener(activity: Activity) {
        if (!config.isDownload) return
        clipboardLogic.addClipboardListener(activity) {
            val aweme = findVideoAweme(activity)
            DownloadLogic(this@HMainActivity, activity, aweme)
        }
    }

    //透明度
    private fun changeViewAlpha(viewGroup: ViewGroup) {
        if (!config.isTranslucent) return
        launch {
            delay(200L)
            viewGroup.traverse {
                //底部
                if (it::class.java.name.contains("MainBottomTabContainer")) {
                    it.alpha = 0.5f
                }
            }
        }
    }

    //抖音版本(版本是否兼容提示)
    @Synchronized
    private fun showSupportDialog(activity: Activity) {
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
                       Freedom+最小兼容以下版本:<br/>
                       ${supportVersions.joinToString(", ") { s -> if (s == versionName) "<span style='color:#F56C6C;'>$s</span>" else s }}<br/>
                       适配列表仅作为参考，请自行测试各项功能！
                       """.trimIndent()
                ),
                cancel = "此版本不再提示",
                confirm = "确定",
                onCancel = {
                    saveConfig(activity)
                }
            )
        }
    }

    //检查模块版本
    @Synchronized
    private fun checkVersionDialog(activity: Activity) {
        launch {
            delay(2000L)
            val version = withContext(Dispatchers.IO) { Version.getRemoteReleasesLatest() } ?: return@launch
            if (version.name.compareTo("v${config.versionName}") >= 1) {
                showMessageDialog(
                    context = activity,
                    title = "发现新版本 ${version.name}!",
                    content = version.body,
                    cancel = "取消",
                    confirm = "更新",
                    onConfirm = {
                        activity.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(version.browserDownloadUrl),
                            )
                        )
                    }
                )
            }
        }
    }

    //保存配置信息
    private fun saveConfig(context: Context) {
        val dyVersionName = context.appVersionName
        val dyVersionCode = context.appVersionCode
        config.isSupportHint = false
        config.dyVersionName = dyVersionName
        config.dyVersionCode = dyVersionCode
        config.save(context)
    }
}