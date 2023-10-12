package com.freegang.hook

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.hook.logic.ClipboardLogic
import com.freegang.hook.logic.DownloadLogic
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.color.KColorUtils
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.ktutils.view.KViewUtils
import com.freegang.ktutils.view.findViewsByType
import com.freegang.ktutils.view.traverse
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.xpler.R
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.OnBefore
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.thisActivity
import com.freegang.xpler.core.thisContext
import com.freegang.xpler.databinding.SideFreedomSettingBinding
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.main.MainActivity
import com.ss.android.ugc.aweme.sidebar.SideBarNestedScrollView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.delay

class HMainActivity(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<MainActivity>(lpparam) {
    companion object {
        const val TAG = "HMainActivity"
    }

    private val config get() = ConfigV1.get()
    private val clipboardLogic = ClipboardLogic(this)

    @OnAfter("onCreate")
    fun onCreate(params: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlockRunning(params) {
            DouYinMain.timedExitCountDown?.restart()
            showToast(thisContext, "Freedom+ Attach!")
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }

    @OnAfter("onResume")
    fun onResume(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            //setFreedomSetting(thisActivity)
            addClipboardListener(thisActivity)
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }

    @OnBefore("onPause")
    fun onPause(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            saveConfig(thisContext)
            clipboardLogic.removeClipboardListener(thisContext)
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }

    @OnAfter("onDestroy")
    fun onDestroy(params: XC_MethodHook.MethodHookParam) {

    }

    private fun findVideoAweme(activity: Activity): Aweme? {
        var firstAweme = activity.methodInvokeFirst(returnType = Aweme::class.java)
        if (firstAweme == null) {
            val curFragment = activity.methodInvokeFirst("getCurFragment")
            firstAweme = curFragment?.methodInvokeFirst(returnType = Aweme::class.java)
        }
        return firstAweme as Aweme?
    }

    private fun addClipboardListener(activity: Activity) {
        if (!config.isDownload) return
        clipboardLogic.addClipboardListener(activity) { clipData, firstText ->
            val aweme = findVideoAweme(activity)
            DownloadLogic(this@HMainActivity, activity, aweme)
        }
    }

    // Freedom设置
    @Deprecated("淘汰区域，删除倒计时中")
    private fun setFreedomSetting(activity: Activity) {
        if (config.isDisablePlugin) return // 去插件化
        launch {
            delay(500L)
            val clazz = findClass("com.ss.android.ugc.aweme.homepage.ui.TopLeftFrameLayout") as Class<ViewGroup>
            val view = KViewUtils.findViews(activity.contentView, clazz).firstOrNull() ?: return@launch
            view.traverse { ->
                val onClickListener = KViewUtils.getOnClickListener(this) ?: return@traverse
                setOnClickListener {
                    onClickListener.onClick(it)
                    launch {
                        delay(200)
                        val contentView = activity.contentView
                        val v = contentView.findViewsByType(SideBarNestedScrollView::class.java).firstOrNull()
                            ?: return@launch

                        val sideRootView = v.children.first() as ViewGroup
                        if (sideRootView.children.last().contentDescription == "扩展功能") return@launch

                        val text = sideRootView.findViewsByType(TextView::class.java).firstOrNull() ?: return@launch
                        val isDark = KColorUtils.isDarkColor(text.currentTextColor)

                        val setting = KtXposedHelpers.inflateView<ViewGroup>(v.context, R.layout.side_freedom_setting)
                        setting.contentDescription = "扩展功能"
                        val binding = SideFreedomSettingBinding.bind(setting)

                        val backgroundRes: Int
                        val iconColorRes: Int
                        val textColorRes: Int
                        if (!isDark) {
                            backgroundRes = R.drawable.side_item_background_night
                            iconColorRes = R.drawable.ic_freedom_night
                            textColorRes = Color.parseColor("#E6FFFFFF")
                        } else {
                            backgroundRes = R.drawable.dialog_background
                            iconColorRes = R.drawable.ic_freedom
                            textColorRes = Color.parseColor("#FF161823")
                        }

                        binding.freedomSettingContainer.background = KtXposedHelpers.getDrawable(backgroundRes)
                        binding.freedomSettingText.setTextColor(textColorRes)
                        binding.freedomSettingIcon.background = KtXposedHelpers.getDrawable(iconColorRes)
                        binding.freedomSettingTitle.text = String.format("%s", "Freedom+")
                        binding.freedomSettingTitle.setTextColor(textColorRes)
                        binding.freedomSetting.setOnClickListener { view ->
                            val intent = Intent(view.context, FreedomSettingActivity::class.java)
                            intent.putExtra("isDark", view.context.isDarkMode)
                            val options = ActivityOptions.makeCustomAnimation(
                                activity,
                                android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right
                            )
                            activity.startActivity(intent, options.toBundle())
                        }
                        sideRootView.addView(binding.root)
                    }
                }
            }
        }
    }

    // 保存配置信息
    private fun saveConfig(context: Context) {
        config.versionConfig = config.versionConfig.copy(
            dyVersionName = context.appVersionName,
            dyVersionCode = context.appVersionCode
        )
    }
}