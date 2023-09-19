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
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.io.hasOperationStorage
import com.freegang.ktutils.json.getIntOrDefault
import com.freegang.ktutils.json.getStringOrDefault
import com.freegang.ktutils.json.parseJSONArray
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.text.ifNotEmpty
import com.freegang.plugin.PluginBridge
import com.freegang.xpler.HookPackages
import com.freegang.xpler.core.findClass
import com.freegang.xpler.core.lpparam
import com.freegang.xpler.core.xposedLog
import com.freegang.xpler.loader.hostClassloader
import com.freegang.xpler.loader.injectClassLoader
import org.json.JSONObject
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Modifier
import kotlin.system.exitProcess

class DouYinMain(private val app: Application) {
    companion object {
        val awemeHostApplication
            get() = "com.ss.android.ugc.aweme.app.host.AwemeHostApplication".findClass(lpparam.classLoader)!!
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
                val stubClazz = hostClassloader!!.loadClass("com.ss.android.ugc.aweme.bullet.ui.BulletContainerActivity")
                PluginBridge.init(app, stubClazz)
            }

            //全局异常捕获工具
            val intent = Intent()
            val className = "${HookPackages.modulePackageName}.activity.ErrorActivity"
            intent.setClassName(HookPackages.modulePackageName, className)
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
            KLogCat.xposedLog("Freedom+ inject err..\n${it.stackTraceToString()}")
            KToastUtils.show(app, "Freedom+ Error: ${it.message}")
        }
    }

    private fun readClasses(): Boolean {
        val classes = ConfigV1.get().classes
        val targetVersion = classes.getStringOrDefault("targetVersion")
        val detailPageFragment = classes.getStringOrDefault("detailPageFragment")
        val videoPinch = classes.getStringOrDefault("videoPinch")
        val videoPagerAdapter = classes.getStringOrDefault("videoPagerAdapter")
        val emojiApiProxy = classes.getStringOrDefault("emojiApiProxy")
        val emojiPopupWindow = classes.getStringOrDefault("emojiPopupWindow")
        val ripsChatRoomFragment = classes.getStringOrDefault("ripsChatRoomFragment")

        if (targetVersion != "${app.appVersionName}_${app.appVersionCode}") {
            return false
        }

        detailPageFragmentClazz = detailPageFragment.ifNotEmpty { it.findClass(lpparam.classLoader) }
        videoPinchClazz = videoPinch.ifNotEmpty { it.findClass(lpparam.classLoader) }
        videoPagerAdapterClazz = videoPagerAdapter.ifNotEmpty { it.findClass(lpparam.classLoader) }
        emojiApiProxyClazz = emojiApiProxy.ifNotEmpty { it.findClass(lpparam.classLoader) }
        emojiPopupWindowClazz = emojiPopupWindow.ifNotEmpty { it.findClass(lpparam.classLoader) }
        ripsChatRoomFragmentClazz = ripsChatRoomFragment.ifNotEmpty { it.findClass(lpparam.classLoader) }

        return true
    }

    private fun saveClasses() {
        val classJson = JSONObject()
        classJson.put("targetVersion", "${app.appVersionName}_${app.appVersionCode}")
        classJson.put("detailPageFragment", "${detailPageFragmentClazz?.name}")
        classJson.put("videoPinch", "${videoPinchClazz?.name}")
        classJson.put("videoPagerAdapter", "${videoPagerAdapterClazz?.name}")
        classJson.put("emojiApiProxy", "${emojiApiProxyClazz?.name}")
        classJson.put("emojiPopupWindow", "${emojiPopupWindowClazz?.name}")
        classJson.put("ripsChatRoomFragment", "${ripsChatRoomFragmentClazz?.name}")
        ConfigV1.get().classes = classJson
    }

    private fun initDexKit() {
        if (readClasses()) return
        System.loadLibrary("dexkit")
        DexKitBridge.create(lpparam.appInfo.sourceDir)?.use { bridge ->
            videoPinchClazz = bridge.findClass {
                matcher {
                    fields {
                        add {
                            type =
                                "com.ss.android.ugc.aweme.feed.ui.seekbar.CustomizedUISeekBar"
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
            }.firstOrNull()?.getInstance(lpparam.classLoader)
            videoPagerAdapterClazz = bridge.findClass {
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
                            returnType =
                                "com.ss.android.ugc.aweme.feed.adapter.FeedImageViewHolder"
                        }
                    }
                }
            }.firstOrNull()?.getInstance(lpparam.classLoader)
            emojiPopupWindowClazz = bridge.findClass {
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
            }.firstOrNull()?.getInstance(lpparam.classLoader)
            val findMaps = bridge.batchFindClassUsingStrings {
                addSearchGroup {
                    groupName = "detailPageFragment"
                    usingStrings = listOf(
                        "a1128.b7947",
                        "com/ss/android/ugc/aweme/detail/ui/DetailPageFragment",
                        "DetailActOtherNitaView",
                    )
                }
                addSearchGroup {
                    groupName = "emojiApiProxy"
                    add("https://", StringMatchType.Equals)
                    add("/aweme/v1/", StringMatchType.Equals)
                }
                addSearchGroup {
                    groupName = "ripsChatRoomFragment"
                    usingStrings = listOf(
                        "com/ss/android/ugc/aweme/im/sdk/chat/rips/RipsChatRoomFragment",
                        "RipsChatRoomFragment",
                        "a1128.b17614",
                    )
                }
            }
            detailPageFragmentClazz = findMaps["detailPageFragment"]?.firstOrNull()?.getInstance(lpparam.classLoader)
            emojiApiProxyClazz = findMaps["emojiApiProxy"]?.firstOrNull()?.getInstance(lpparam.classLoader)
            ripsChatRoomFragmentClazz = findMaps["ripsChatRoomFragment"]?.firstOrNull()?.getInstance(lpparam.classLoader)
        }
        saveClasses()
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