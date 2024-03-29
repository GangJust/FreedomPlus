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
import io.github.fplus.Constant
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.plugin.proxy.v1.PluginBridge
import io.github.xpler.core.log.XplerLog
import kotlin.system.exitProcess

class DouYinMain(private val app: Application) {
    companion object {
        var inBackend = false
        var timedExitCountDown: CountDownTimer? = null
        var freeExitCountDown: CountDownTimer? = null
    }

    init {
        runCatching {
            // 插件化注入
            PluginBridge.init(app, "com.ss.android.ugc.aweme.setting.ui.AboutActivity")

            // 加载配置
            ConfigV1.initialize(app)

            // 全局Application
            KAppUtils.setApplication(app)
            KActivityUtils.register(app)

            // 日志工具
            XplerLog.setTag("Freedom+")
            KLogCat.init(app)
            // KLogCat.silence() //静默

            // 全局异常捕获工具
            val intent = Intent()
            val className = "${Constant.modulePackage}.activity.ErrorActivity"
            intent.setClassName(Constant.modulePackage, className)
            KAppCrashUtils.init(app, "抖音异常退出!", intent) { e, m ->
                KActivityUtils.unregister(app)
                true
            }

            // 定时退出
            initTimedShutdown(app)

            // search and hook
            DexkitBuilder.running(
                app = app,
                version = 16,
                searchBefore = {
                    HActivity()
                    HMainActivity()
                    HDetailActivity()
                    HLandscapeFeedActivity()
                    HLivePlayActivity()
                    HDisallowInterceptRelativeLayout()
                    HMainTabStripScrollView()
                    HFlippableViewPager()
                    HCustomizedUISeekBar()
                    HPlayerController()
                    HVideoViewHolderRootView()
                    HPenetrateTouchRelativeLayout()
                    HInteractStickerParent()
                    // HCommentAudioView()
                    HGifEmojiDetailActivity()
                    HEmojiDetailDialog()

                    HDialog()
                    // HDialogFragment()
                    // HPopupWindow()
                },
                searchAfter = {
                    HSideBarNestedScrollView()
                    HCornerExtensionsPopupWindow()
                    HMainBottomTabView()
                    HMainBottomTabItem()
                    HCommentListPageFragment()
                    HConversationFragment()
                    HPoiCreateInstanceImpl()
                    HSeekBarSpeedModeBottomMask()
                    HVideoPlayerHelper()
                    HVideoViewHolder()
                    HAbstractFeedAdapter()
                    HVerticalViewPager()
                    HDetailPageFragment()
                    HEmojiDetailDialogNew()
                    HEmojiPopupWindow()
                    HBottomCtrlBar()

                    HMessage()
                    HChatListRecyclerViewAdapter()
                    HChatListRecalledHint()
                }
            )

        }.onFailure {
            XplerLog.e(it)
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

        if (timedExit >= 60 * 1000L * 3) {
            timedExitCountDown = object : CountDownTimer(timedExit, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val second = millisUntilFinished / 1000
                    KLogCat.d("定时倒计时: ${second}秒")
                    if (!KAppUtils.isAppInForeground(app)) {
                        inBackend = true
                        cancel()
                    }
                    if (second == 30L) {
                        KToastUtils.show(app, "抖音将在30秒后定时退出")
                    }
                    if (second <= 5) {
                        KToastUtils.show(app, "定时退出倒计时${second}s")
                    }
                }

                override fun onFinish() {
                    if (!config.isTimedExit)
                        return

                    keepOrKill(app, config)
                }
            }
        }

        if (freeExit >= 60 * 1000L * 3) {
            freeExitCountDown = object : CountDownTimer(freeExit, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val second = millisUntilFinished / 1000
                    KLogCat.d("空闲倒计时: ${second}秒")
                    if (!KAppUtils.isAppInForeground(app)) {
                        cancel()
                    }
                    if (second == 30L) {
                        KToastUtils.show(app, "长时间无操作, 抖音将在30秒后空闲退出")
                    }
                    if (second <= 5) {
                        KToastUtils.show(app, "空闲退出倒计时${second}s")
                    }
                }

                override fun onFinish() {
                    if (!config.isTimedExit)
                        return

                    keepOrKill(app, config)
                }
            }
        }
    }

    //
    private fun keepOrKill(app: Application, config: ConfigV1) {
        if (config.keepAppBackend) {
            inBackend = true
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_HOME)
            app.startActivity(intent)
        } else {
            KActivityUtils.getActivities().forEach { it.finishAndRemoveTask() }
            Process.killProcess(Process.myPid())
            exitProcess(1)
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