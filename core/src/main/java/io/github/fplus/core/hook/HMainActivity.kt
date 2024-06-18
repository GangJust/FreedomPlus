package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.freegang.extension.appVersionCode
import com.freegang.extension.appVersionName
import com.freegang.extension.contentView
import com.freegang.extension.firstParentOrNull
import com.freegang.extension.forEachChild
import com.freegang.extension.is64BitDalvik
import com.freegang.extension.isDarkMode
import com.freegang.extension.parentView
import com.freegang.extension.postRunning
import com.freegang.extension.removeInParent
import com.ss.android.ugc.aweme.homepage.ui.titlebar.MainTitleBar
import com.ss.android.ugc.aweme.homepage.ui.view.MainTabStripScrollView
import com.ss.android.ugc.aweme.main.MainActivity
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.AutoPlayHelper
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.hook.logic.ClipboardLogic
import io.github.fplus.core.hook.logic.DownloadLogic
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.thisActivity
import io.github.xpler.core.thisContext
import kotlinx.coroutines.delay

class HMainActivity : BaseHook() {
    companion object {
        const val TAG = "HMainActivity"

        @SuppressLint("StaticFieldLeak")
        var mainTitleBar: View? = null

        @SuppressLint("StaticFieldLeak")
        var bottomTabView: View? = null

        fun toggleView(visible: Boolean) {
            mainTitleBar?.isVisible = visible
            bottomTabView?.isVisible = visible

            // val activity = mainTitleBar?.context?.asOrNull<Activity>() ?: return
            // ImmersiveHelper.immersive(activity, !visible, !visible)
        }
    }

    private val config get() = ConfigV1.get()
    private val clipboardLogic = ClipboardLogic(this)
    private var disallowInterceptRelativeLayout: View? = null

    override fun setTargetClass(): Class<*> {
        return MainActivity::class.java
    }

    @OnBefore("onCreate")
    fun onCreateBefore(params: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlockRunning(params) {
            thisActivity.runCatching {
                val startModuleSetting = intent?.getBooleanExtra("startModuleSetting", false) ?: false
                if (startModuleSetting) {
                    intent.setClass(this, FreedomSettingActivity::class.java)
                    intent.putExtra("isModuleStart", true)
                    intent.putExtra("isDark", isDarkMode)
                    val options = ActivityOptions.makeCustomAnimation(
                        this,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    startActivity(intent, options.toBundle())
                    finish()
                }
            }.onFailure {
                XplerLog.e(it)
            }
        }
    }

    @OnAfter("onCreate")
    fun onCreateAfter(params: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlockRunning(params) {
            val activity = thisActivity
            XplerLog.d("version: ${KtXposedHelpers.moduleVersionName(activity)} - ${activity.appVersionName}(${activity.appVersionCode})")
            DouYinMain.timerExitHelper?.restart()

            openAutoPlay(activity)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnAfter("onResume")
    fun onResume(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            val activity = thisObject as Activity

            addClipboardListener(activity)
            initView(activity)
            is32BisTips(activity)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    @OnBefore("onPause")
    fun onPause(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            removeClipboardListener(thisActivity)
            clearView()
            saveConfig(thisContext)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    private fun addClipboardListener(activity: Activity) {
        if (!config.isDownload) return
        if (!config.copyLinkDownload) return

        clipboardLogic.addClipboardListener(activity) { clipData, firstText ->
            DownloadLogic(
                this@HMainActivity,
                activity,
                HVideoViewHolder.aweme,
            )
        }
    }

    private fun removeClipboardListener(activity: Activity) {
        clipboardLogic.removeClipboardListener(activity)
    }

    private fun initView(activity: Activity) {
        activity.contentView.postRunning {
            it.forEachChild { child ->
                // 新版本顶栏不居中
                if (child is MainTabStripScrollView) {
                    val lp = child.layoutParams
                    if (lp is RelativeLayout.LayoutParams) {
                        child.layoutParams = lp.apply {
                            this.addRule(RelativeLayout.CENTER_IN_PARENT)
                        }
                    }
                }

                if (child is MainTitleBar) {
                    mainTitleBar = child
                }

                if (DexkitBuilder.mainBottomTabViewClazz?.name == child.javaClass.name) {
                    bottomTabView = child
                }

                if (child.javaClass.name.contains("DisallowInterceptRelativeLayout")) {
                    disallowInterceptRelativeLayout = child
                }
            }

            initMainTitleBar()
            initBottomTabView()
            initDisallowInterceptRelativeLayout()
        }
    }

    private fun initMainTitleBar() {
        // 隐藏顶部选项卡
        if (config.isHideTopTab) {
            val keywordsRegex = config.hideTopTabKeywords
                .replace("，", ",")
                .replace("\\s".toRegex(), "")
                .removePrefix(",").removeSuffix(",")
                .replace(",", "|")
                .replace("\\|+".toRegex(), "|")
                .toRegex()

            mainTitleBar?.forEachChild { child ->
                val desc = "${child.contentDescription}"
                if (desc.contains(keywordsRegex)) {
                    child.isVisible = false
                }
            }
        }

        // 顶部选项卡透明度
        if (config.isTranslucent) {
            val alphaValue = config.translucentValue[0] / 100f
            mainTitleBar?.alpha = alphaValue
        }
    }

    private fun initBottomTabView() {
        // 隐藏底部选项卡
        if (config.isHideBottomTab) {
            val keywordsRegex = config.hideBottomTabKeywords
                .replace("，", ",")
                .replace("\\s".toRegex(), "")
                .removePrefix(",").removeSuffix(",")
                .replace(",".toRegex(), "|")
                .replace("\\|+".toRegex(), "|")
                .toRegex()

            bottomTabView?.forEachChild { child ->
                val desc = "${child.contentDescription}"
                if (desc.contains(keywordsRegex)) {
                    val tabItem = child.firstParentOrNull(ViewGroup::class.java) { parent ->
                        parent.javaClass.name.startsWith("X")
                    }

                    tabItem?.isVisible = false
                }
            }
        }

        // 底部导航栏透明度
        if (config.isTranslucent) {
            val alphaValue = config.translucentValue[3] / 100f
            bottomTabView?.parentView?.alpha = alphaValue
        }

        // 底部导航栏全局沉浸式
        if (config.isImmersive) {
            bottomTabView?.parentView?.background = ColorDrawable(Color.TRANSPARENT)
            bottomTabView?.forEachChild {
                it.background = ColorDrawable(Color.TRANSPARENT)
            }
        }
    }

    private fun initDisallowInterceptRelativeLayout() {
        if (!config.isImmersive)
            return

        disallowInterceptRelativeLayout?.postRunning {
            runCatching {
                it.forEachChild { child ->
                    // 移除顶部间隔
                    if (child.javaClass.name == "android.view.View") {
                        child.removeInParent()
                    }
                    // 移除底部间隔
                    if (child.javaClass.name == "com.ss.android.ugc.aweme.feed.ui.bottom.BottomSpace") {
                        child.removeInParent()
                    }
                }
            }.onFailure {
                XplerLog.e(it)
            }
        }
    }

    private fun openAutoPlay(context: Context) {
        if (!config.isAutoPlay)
            return

        if (!config.defaultAutoPlay)
            return

        launchMain {
            delay(2000L)
            AutoPlayHelper.openAutoPlay(context)
        }
    }

    private fun clearView() {
        mainTitleBar = null
        bottomTabView = null
        disallowInterceptRelativeLayout = null
    }

    // 保存配置信息
    private fun saveConfig(context: Context) {
        config.versionConfig = config.versionConfig.copy(
            dyVersionName = context.appVersionName,
            dyVersionCode = context.appVersionCode
        )
    }

    private fun is32BisTips(context: Context) {
        singleLaunchMain {
            delay(2000L)

            if (context.is64BitDalvik) {
                return@singleLaunchMain
            }

            val version = config.versionConfig
            val cacheVersion = "${version.dyVersionName}_${version.dyVersionCode}"
            val currentVersion = "${context.appVersionName}_${context.appVersionCode}"
            if (cacheVersion.compareTo(currentVersion) != 0) {
                config.is32BitTips = true
            }

            if (!config.is32BitTips) {
                return@singleLaunchMain
            }

            showMessageDialog(
                context = context,
                title = "温馨提示",
                content = "当前抖音32位，使用过程中可能出现严重卡顿、花屏等现象，建议更换抖音64位。",
                cancel = "此版本不再提示",
                confirm = "确定",
                onCancel = {
                    config.is32BitTips = false
                },
                onConfirm = {

                }
            )
        }

        /*showComposeDialog(context) { onClosedHandle ->
            FMessageDialog(
                title = "温馨提示",
                cancel = "此版本不再提示",
                confirm = "确定",
                onCancel = {
                    onClosedHandle.invoke()
                    config.is32BitTips = false
                },
                onConfirm = {
                    onClosedHandle.invoke()
                }
            ) {
                Text(
                    text = "当前抖音32位，使用过程中可能出现严重卡顿、花屏等现象，建议更换抖音64位。",
                )
            }
        }*/
    }
}