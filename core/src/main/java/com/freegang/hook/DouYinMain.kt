package com.freegang.hook

import android.app.Application
import android.content.Intent
import android.os.CountDownTimer
import android.os.Process
import android.widget.Toast
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.KActivityUtils
import com.freegang.ktutils.app.KAppCrashUtils
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.io.hasOperationStorage
import com.freegang.ktutils.json.getIntOrDefault
import com.freegang.ktutils.json.parseJSONArray
import com.freegang.ktutils.log.KLogCat
import com.freegang.plugin.PluginBridge
import com.freegang.xpler.HookPackages
import com.freegang.xpler.core.findClass
import com.freegang.xpler.core.lpparam
import com.freegang.xpler.loader.hostClassloader
import com.freegang.xpler.loader.injectClassLoader
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Modifier
import kotlin.system.exitProcess

class DouYinMain(private val app: Application) {
    companion object {
        val awemeHostApplication get() = "com.ss.android.ugc.aweme.app.host.AwemeHostApplication".findClass(lpparam.classLoader)!!
        var detailPageFragmentClazz: Class<*>? = null
        var videoPinchClazz: Class<*>? = null
        var videoPagerAdapterClazz: Class<*>? = null
        var emojiApiProxyClazz: Class<*>? = null
        var emojiPopupWindowClazz: Class<*>? = null
        var ripsChatRoomFragmentClazz: Class<*>? = null

        var timedExitCountDown: CountDownTimer? = null
        var freeExitCountDown: CountDownTimer? = null
    }

    init {
        runCatching {
            injectClassLoader(app.classLoader)

            //文件读写权限检查
            if (!app.hasOperationStorage) {
                Toast.makeText(app, "抖音没有文件读写权限!", Toast.LENGTH_LONG).show()
                return@runCatching
            }

            //加载配置
            ConfigV1.initialize(app)

            //全局Application
            KAppUtils.setApplication(app)

            //日志工具
            KLogCat.init(app)
            //KLogCat.openStorage()

            //插件化注入
            if (!ConfigV1.get().isDisablePlugin) {
                val subClazz = hostClassloader!!.loadClass("com.ss.android.ugc.aweme.bullet.ui.BulletContainerActivity")
                PluginBridge.init(app, subClazz)
            }

            //全局异常捕获工具
            val intent = Intent()
            intent.setClassName(HookPackages.modulePackageName, "${HookPackages.modulePackageName}.activity.ErrorActivity")
            KAppCrashUtils.instance.init(app, intent, "抖音异常退出!")

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
            HChatRoomActivity(lpparam)
            HVideoPagerAdapter(lpparam)
        }.onFailure {
            KToastUtils.show(app, "Freedom+ Error: ${it.message}")
        }
    }

    private fun initDexKit() {
        System.loadLibrary("dexkit")
        DexKitBridge.create(lpparam.appInfo.sourceDir)?.use { bridge ->
            if (detailPageFragmentClazz == null) {
                val finds = bridge.findClass {
                    matcher {
                        usingStrings = listOf(
                            "a1128.b7947",
                            "com/ss/android/ugc/aweme/detail/ui/DetailPageFragment",
                            "DetailActOtherNitaView",
                        )
                    }
                }
                detailPageFragmentClazz = finds.firstOrNull()?.getInstance(lpparam.classLoader)
            }

            if (videoPinchClazz == null) {
                val finds = bridge.findClass {
                    matcher {
                        fields {
                            add {
                                type = "com.ss.android.ugc.aweme.feed.ui.seekbar.CustomizedUISeekBar"
                            }
                        }
                        methods {
                            add {
                                name = "getMOriginView"
                                returnType = "android.view.View"
                            }
                            add {
                                name = "handleMsg"
                                paramTypes = listOf("android.os.Message")
                            }
                        }
                    }
                }
                videoPinchClazz = finds.firstOrNull()?.getInstance(lpparam.classLoader)
            }

            if (videoPagerAdapterClazz == null) {
                val finds = bridge.findClass {
                    matcher {
                        methods {
                            add {
                                this.returnType = "java.util.List"
                            }
                            add {
                                paramTypes = listOf("com.ss.android.ugc.aweme.feed.model.Aweme")
                                returnType = "com.ss.android.ugc.aweme.feed.model.Aweme"
                            }
                            add {
                                returnType = "com.ss.android.ugc.aweme.feed.adapter.FeedImageViewHolder"
                            }
                        }
                    }
                }
                videoPagerAdapterClazz = finds.firstOrNull()?.getInstance(lpparam.classLoader)
            }

            if (emojiApiProxyClazz == null) {
                val finds = bridge.findClass {
                    matcher {
                        addUsingString("https://", StringMatchType.Equals)
                        addUsingString("/aweme/v1/", StringMatchType.Equals)
                    }
                }
                emojiApiProxyClazz = finds.firstOrNull()?.getInstance(lpparam.classLoader)
            }

            if (emojiPopupWindowClazz == null) {
                val finds = bridge.findClass {
                    matcher {
                        methods {
                            add {
                                modifiers = Modifier.PRIVATE
                                returnType = "com.ss.android.ugc.aweme.base.ui.RemoteImageView"
                            }
                            add {
                                modifiers = Modifier.PRIVATE
                                returnType = "com.bytedance.ies.dmt.ui.widget.DmtTextView"
                            }
                            add {
                                modifiers = Modifier.PRIVATE
                                paramTypes = listOf("com.ss.android.ugc.aweme.emoji.base.BaseEmoji")
                            }
                        }
                    }
                }
                emojiPopupWindowClazz = finds.firstOrNull()?.getInstance(lpparam.classLoader)
            }

            if (ripsChatRoomFragmentClazz == null) {
                val finds = bridge.findClass {
                    matcher {
                        usingStrings = listOf(
                            "com/ss/android/ugc/aweme/im/sdk/chat/rips/RipsChatRoomFragment",
                            "RipsChatRoomFragment",
                            "a1128.b17614",
                        )
                    }
                }
                ripsChatRoomFragmentClazz = finds.firstOrNull()?.getInstance(lpparam.classLoader)
            }
        }
        KLogCat.d("detailPageFragmentClazz: $detailPageFragmentClazz")
        KLogCat.d("videoPinchClazz: $videoPinchClazz")
        KLogCat.d("videoPagerAdapterClazz: $videoPagerAdapterClazz")
        KLogCat.d("emojiApiProxyClazz: $emojiApiProxyClazz")
        KLogCat.d("emojiPopupWindowClazz: $emojiPopupWindowClazz")
        KLogCat.d("ripsChatRoomFragmentClazz: $ripsChatRoomFragmentClazz")
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