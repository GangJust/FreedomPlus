package io.github.fplus.core.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.app.contentView
import com.freegang.ktutils.app.isDarkMode
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.view.forEachChild
import com.freegang.ktutils.view.parentView
import com.freegang.ktutils.view.postRunning
import com.freegang.ktutils.view.removeInParent
import com.ss.android.ugc.aweme.homepage.ui.titlebar.MainTitleBar
import com.ss.android.ugc.aweme.main.MainActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.core.ui.activity.FreedomSettingActivity
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.thisActivity
import io.github.xpler.core.thisContext

class HMainActivity(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<MainActivity>(lpparam) {
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

    private var disallowInterceptRelativeLayout: View? = null

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
                KLogCat.e(it)
            }
        }
    }

    @OnAfter("onCreate")
    fun onCreateAfter(params: XC_MethodHook.MethodHookParam, savedInstanceState: Bundle?) {
        hookBlockRunning(params) {
            showToast(thisContext, "Freedom+ Attach!")
            DouYinMain.timedExitCountDown?.restart()
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onResume")
    fun onResume(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            initView(thisActivity)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnBefore("onPause")
    fun onPause(params: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(params) {
            saveConfig(thisContext)
            clearView()
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    private fun initView(activity: Activity) {
        activity.contentView.postRunning {
            forEachChild {
                if (this is MainTitleBar) {
                    mainTitleBar = this
                }

                if (DexkitBuilder.mainBottomTabViewClazz?.name == this.javaClass.name) {
                    bottomTabView = this
                }

                if (this.javaClass.name.contains("DisallowInterceptRelativeLayout")) {
                    disallowInterceptRelativeLayout = this
                }
            }

            initMainTitleBar()
            initBottomTabView()
            initDisallowInterceptRelativeLayout()
        }
    }

    private fun initMainTitleBar() {
        // 隐藏顶部选项卡
        if (config.isHideTab) {
            val hideTabKeywords = config.hideTabKeywords
                .removePrefix(",").removePrefix("，")
                .removeSuffix(",").removeSuffix("，")
                .replace("\\s".toRegex(), "")
                .replace("[,，]".toRegex(), "|")
                .toRegex()
            mainTitleBar?.forEachChild {
                if (config.isHideTab) {
                    if ("$contentDescription".contains(hideTabKeywords)) {
                        isVisible = false
                    }
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
        // 底部导航栏透明度
        if (config.isTranslucent) {
            val alphaValue = config.translucentValue[2] / 100f
            bottomTabView?.parentView?.alpha = alphaValue
        }

        // 底部导航栏全局沉浸式
        if (config.isImmersive) {
            bottomTabView?.parentView?.background = ColorDrawable(Color.TRANSPARENT)
            bottomTabView?.forEachChild {
                background = ColorDrawable(Color.TRANSPARENT)
            }
        }
    }

    private fun initDisallowInterceptRelativeLayout() {
        if (config.isImmersive) {
            disallowInterceptRelativeLayout?.postRunning {
                runCatching {
                    forEachChild {
                        // 移除顶部间隔
                        if (javaClass.name == "android.view.View") {
                            removeInParent()
                        }
                        // 移除底部间隔
                        if (javaClass.name == "com.ss.android.ugc.aweme.feed.ui.bottom.BottomSpace") {
                            removeInParent()
                        }
                    }
                }.onFailure {
                    KLogCat.tagE(TAG, it)
                }
            }
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
}