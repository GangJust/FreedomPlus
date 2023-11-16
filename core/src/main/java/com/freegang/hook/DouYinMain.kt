package com.freegang.hook

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.Process
import android.widget.Toast
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.app.KActivityUtils
import com.freegang.ktutils.app.KAppCrashUtils
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.io.hasStoragePermission
import com.freegang.ktutils.json.getIntOrDefault
import com.freegang.ktutils.json.parseJSONArray
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.media.hasMediaPermission
import com.freegang.plugin.v2.PluginBridgeV2
import com.freegang.xpler.HookPackages
import com.freegang.xpler.core.findClass
import com.freegang.xpler.core.log.XplerLog
import com.freegang.xpler.core.lpparam
import com.freegang.xpler.core.xposedLog
import com.freegang.xpler.loader.hostClassloader
import com.freegang.xpler.loader.injectClassLoader
import com.ss.android.ugc.aweme.feed.model.Aweme
import kotlin.system.exitProcess

class DouYinMain(private val app: Application) {
    companion object {
        val awemeHostApplication
            get() = "com.ss.android.ugc.aweme.app.host.AwemeHostApplication".findClass(lpparam.classLoader)!!

        var timedExitCountDown: CountDownTimer? = null
        var freeExitCountDown: CountDownTimer? = null
    }

    init {
        runCatching {
            injectClassLoader(app.classLoader)

            // 插件化注入
            val stubClazz = hostClassloader!!.loadClass("com.ss.android.ugc.aweme.setting.ui.AboutActivity")
            PluginBridgeV2.init(app, stubClazz)

            // 权限检查
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!app.hasStoragePermission() && !app.hasMediaPermission()) {
                    Toast.makeText(app, "抖音没有文件/媒体操作权限!", Toast.LENGTH_LONG).show()
                    return@runCatching
                }
            } else {
                if (!app.hasStoragePermission()) {
                    Toast.makeText(app, "抖音没有文件读写权限!", Toast.LENGTH_LONG).show()
                    return@runCatching
                }
            }

            // 加载配置
            ConfigV1.initialize(app)

            // 全局Application
            KAppUtils.setApplication(app)

            // 日志工具
            KLogCat.init(app)
            KLogCat.clearStorage()

            // 全局异常捕获工具
            val intent = Intent()
            val className = "${HookPackages.modulePackageName}.activity.ErrorActivity"
            intent.setClassName(HookPackages.modulePackageName, className)
            KAppCrashUtils.instance.init(app, intent, "抖音异常退出!")

            // 定时退出
            initTimedExit(app)

            // search and hook
            DexkitBuilder.running(
                app = app,
                version = 16,
                searchBefore = {
                    HActivity(lpparam)
                    HMainActivity(lpparam)
                    HLivePlayActivity(lpparam)
                    HDetailActivity(lpparam)
                    HSideBarNestedScrollView(lpparam)
                    HDisallowInterceptRelativeLayout(lpparam)
                    HMainTabStripScrollView(lpparam)
                    HFlippableViewPager(lpparam)
                    HCustomizedUISeekBar(lpparam)
                    // HVideoViewHolder(lpparam)
                    // HVideoViewHolderRootView(lpparam)
                    HVideoViewHolderNew(lpparam)
                    HVideoViewHolderRootViewNew(lpparam)
                    HPenetrateTouchRelativeLayout(lpparam)
                    HInteractStickerParent(lpparam)
                    HCommentAudioView(lpparam)
                    HGifEmojiDetailActivity(lpparam)
                    HEmojiDetailDialog(lpparam)
                },
                searchAfter = {
                    HCornerExtensionsPopupWindow(lpparam)
                    HMainBottomTabView(lpparam)
                    HMainBottomTabItem(lpparam)
                    HCommentListPageFragment(lpparam)
                    HSeekBarSpeedModeBottomMask(lpparam)
                    HVideoPlayerHelper(lpparam)
                    HVerticalViewPagerNew(lpparam)
                    HVideoPinchView(lpparam)
                    HDetailPageFragment(lpparam)
                    HEmojiDetailDialogNew(lpparam)
                }
            )
        }.onFailure {
            XplerLog.xposedLog("Freedom+ inject err..\n${it.stackTraceToString()}")
            KToastUtils.show(app, "Freedom+ Error: ${it.message}")
        }
    }

    @Synchronized
    private fun initTimedExit(app: Application) {
        val config = ConfigV1.get()
        if (!config.isTimedExit) return

        val timedExitValue = config.timedExitValue.parseJSONArray()
        val timedExit = timedExitValue.getIntOrDefault(0, 0) * 60 * 1000L
        val freeExit = timedExitValue.getIntOrDefault(1, 0) * 60 * 1000L

        if (timedExit >= 60 * 1000 * 3) {
            timedExitCountDown = object : CountDownTimer(timedExit, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val second = millisUntilFinished / 1000
                    if (second == 30L) {
                        KToastUtils.show(app, "抖音将在30秒后定时退出")
                    }
                    if (second <= 5) {
                        KToastUtils.show(app, "定时退出倒计时${second}s")
                    }
                }

                override fun onFinish() {
                    if (!config.isTimedExit) return
                    KActivityUtils.getActivities().forEach { it.finishAndRemoveTask() }
                    Process.killProcess(Process.myPid())
                    exitProcess(1)
                }
            }
        }

        if (freeExit >= 60 * 1000 * 3) {
            freeExitCountDown = object : CountDownTimer(freeExit, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val second = millisUntilFinished / 1000
                    if (second == 30L) {
                        KToastUtils.show(app, "长时间无操作, 抖音将在30秒后空闲退出")
                    }
                    if (second <= 5) {
                        KToastUtils.show(app, "空闲退出倒计时${second}s")
                    }
                }

                override fun onFinish() {
                    if (!config.isTimedExit) return
                    KActivityUtils.getActivities().forEach { it.finishAndRemoveTask() }
                    Process.killProcess(Process.myPid())
                    exitProcess(1)
                }
            }
        }
    }
}

fun CountDownTimer.restart() {
    cancel()
    start()
}

fun Aweme.sortString(): String {
    val desc = "$desc".replace(Regex("\\s"), "")
    return "awemeType=${awemeType}, desc=$desc"
}