package com.freegang.hook

import android.app.Application
import android.os.CountDownTimer
import android.os.Process
import android.widget.Toast
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.KActivityUtils
import com.freegang.ktutils.app.KAppCrashUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.io.hasOperationStorage
import com.freegang.ktutils.json.getIntOrDefault
import com.freegang.ktutils.json.parseJSONArray
import com.freegang.ktutils.log.KLogCat
import com.freegang.plugin.PluginBridge
import com.freegang.ui.activity.FreedomErrorActivity
import com.freegang.xpler.core.findClass
import com.freegang.xpler.core.lpparam
import com.freegang.xpler.loader.hostClassloader
import io.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Method
import kotlin.system.exitProcess

class DouYinMain(private val app: Application) {
    companion object {
        val awemeHostApplication get() = "com.ss.android.ugc.aweme.app.host.AwemeHostApplication".findClass(lpparam.classLoader)!!
        var detailPageFragmentClazz: Class<*>? = null
        var emojiMethods: List<Method> = emptyList()

        var timedExitCountDown: CountDownTimer? = null
        var freeExitCountDown: CountDownTimer? = null
    }

    init {
        runCatching {
            //日志工具
            KLogCat.init(app)
            //KLogCat.openStorage()

            //全局异常捕获工具
            KAppCrashUtils.instance.init(app, FreedomErrorActivity::class.java, "抖音异常退出!")

            //文件读写权限检查
            if (!app.hasOperationStorage) {
                Toast.makeText(app, "抖音没有文件读写权限!", Toast.LENGTH_LONG).show()
                return@runCatching
            }

            //插件化注入
            val subClazz = hostClassloader!!.loadClass("com.ss.android.ugc.aweme.bullet.ui.BulletContainerActivity")
            PluginBridge.init(app, subClazz)

            //加载配置
            ConfigV1.initialize(app)

            //初始化DexKit
            initDexKit()

            //定时退出
            initTimedExit(app)

            //Hook
            HAbsActivity(lpparam)
            HMainActivity(lpparam)
            HMainFragment(lpparam)
            HDetailActivity(lpparam)
            HFlippableViewPager(lpparam)
            HVerticalViewPager(lpparam)
            HDetailPageFragment(lpparam)
            HCommentAudioView(lpparam)
            HGifEmojiDetailActivity(lpparam)
            HEmojiDetailDialog(lpparam)
            HEmojiDetailDialogNew(lpparam)
            HHomeSideBarEntranceManagerV1(lpparam)
            HDouYinSettingNewVersionActivity(lpparam)
        }.onFailure {
            KToastUtils.show(app, "Freedom+ Error: ${it.message}")
        }
    }

    private fun initDexKit() {
        System.loadLibrary("dexkit")
        DexKitBridge.create(lpparam.appInfo.sourceDir)?.use { bridge ->
            if (detailPageFragmentClazz == null) {
                val findMaps = bridge.batchFindClassesUsingStrings {
                    addQuery(
                        "DetailPageFragment",
                        setOf(
                            "a1128.b7947",
                            "com/ss/android/ugc/aweme/detail/ui/DetailPageFragment",
                            "DetailActOtherNitaView",
                        ),
                    )
                }
                detailPageFragmentClazz = findMaps["DetailPageFragment"]?.firstOrNull()?.getClassInstance(lpparam.classLoader)
            }

            if (emojiMethods.isEmpty()) {
                emojiMethods = bridge.findMethod {
                    methodReturnType = "V"
                    methodParamTypes = arrayOf("Lcom/ss/android/ugc/aweme/emoji/model/Emoji;")
                }.filter { it.isMethod }.map { it.getMethodInstance(lpparam.classLoader) }
            }
        }
    }

    @Synchronized
    private fun initTimedExit(app: Application) {
        val config = ConfigV1.get()
        if (!config.isTimedExit) return

        val timedExitValue = config.timedExitValue.parseJSONArray()
        val timedExit = timedExitValue.getIntOrDefault(0, 0) * 60 * 1000L
        val freeExit = timedExitValue.getIntOrDefault(1, 0) * 60 * 1000L

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
                KActivityUtils.getActivities().forEach { it.finishAndRemoveTask() }
                Process.killProcess(Process.myPid())
                exitProcess(1)
            }
        }
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
                KActivityUtils.getActivities().forEach { it.finishAndRemoveTask() }
                Process.killProcess(Process.myPid())
                exitProcess(1)
            }
        }
    }
}

fun CountDownTimer.restart() {
    cancel()
    start()
}