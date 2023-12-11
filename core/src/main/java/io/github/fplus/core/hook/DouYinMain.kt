package io.github.fplus.core.hook

import android.app.Application
import android.content.Intent
import android.os.CountDownTimer
import android.os.Process
import com.freegang.ktutils.app.KActivityUtils
import com.freegang.ktutils.app.KAppCrashUtils
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.log.KLogCat
import com.ss.android.ugc.aweme.feed.model.Aweme
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.plugin.proxy.v1.PluginBridge
import io.github.xpler.HookConfig
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.lpparam
import io.github.xpler.core.xposedLog
import io.github.xpler.loader.hostClassloader
import io.github.xpler.loader.injectClassLoader
import kotlin.system.exitProcess

class DouYinMain(private val app: Application) {
    companion object {
        var timedExitCountDown: CountDownTimer? = null
        var freeExitCountDown: CountDownTimer? = null
    }

    init {
        runCatching {
            injectClassLoader(lpparam, app.classLoader)

            // 插件化注入
            val stubClazz = hostClassloader!!.loadClass("com.ss.android.ugc.aweme.setting.ui.AboutActivity")
            PluginBridge.init(app, stubClazz)

            // 权限检查
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!app.hasStoragePermission() && !app.hasMediaPermission()) {
                    Toast.makeText(app, "抖音没有文件/媒体操作权限!", Toast.LENGTH_LONG).show()
                    return@runCatching
                }
            } else {
                if (!app.hasStoragePermission()) {
                    Toast.makeText(app, "抖音没有文件读写权限!", Toast.LENGTH_LONG).show()
                    return@runCatching
                }
            }*/

            // 加载配置
            ConfigV1.initialize(app)

            // 全局Application
            KAppUtils.setApplication(app)

            // 日志工具
            KLogCat.init(app)
            KLogCat.clearStorage()
            // KLogCat.silence() //静默

            // 全局异常捕获工具
            val intent = Intent()
            val className = "${HookConfig.modulePackageName}.activity.ErrorActivity"
            intent.setClassName(HookConfig.modulePackageName, className)
            KAppCrashUtils.instance.init(app, intent, "抖音异常退出!")

            // 定时退出
            initTimedShutdown(app)

            // search and hook
            DexkitBuilder.running(
                app = app,
                version = 4,
                searchBefore = {
                    HActivity(lpparam)
                    HMainActivity(lpparam)
                    HLivePlayActivity(lpparam)
                    HDetailActivity(lpparam)
                    HDisallowInterceptRelativeLayout(lpparam)
                    HMainTabStripScrollView(lpparam)
                    HFlippableViewPager(lpparam)
                    HCustomizedUISeekBar(lpparam)
                    HPlayerController(lpparam)
                    HVideoViewHolderRootView(lpparam)
                    HPenetrateTouchRelativeLayout(lpparam)
                    HInteractStickerParent(lpparam)
                    // HCommentAudioView(lpparam)
                    HGifEmojiDetailActivity(lpparam)
                    HEmojiDetailDialog(lpparam)

                    HDialog(lpparam)
                    // HDialogFragment(lpparam)
                    // HPopupWindow(lpparam)
                },
                searchAfter = {
                    HSideBarNestedScrollView(lpparam)
                    HCornerExtensionsPopupWindow(lpparam)
                    HMainBottomTabView(lpparam)
                    HMainBottomTabItem(lpparam)
                    HCommentListPageFragment(lpparam)
                    HConversationFragment(lpparam)
                    HPoiCreateInstanceImpl(lpparam)
                    HSeekBarSpeedModeBottomMask(lpparam)
                    HVideoPlayerHelper(lpparam)
                    HVideoViewHolder(lpparam)
                    HAbstractFeedAdapter(lpparam)
                    HVerticalViewPager(lpparam)
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
    private fun initTimedShutdown(app: Application) {
        val config = ConfigV1.get()
        if (!config.isTimedExit) {
            return
        }

        val timedExit = config.timedShutdownValue[0] * 60 * 1000L
        val freeExit = config.timedShutdownValue[1] * 60 * 1000L

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