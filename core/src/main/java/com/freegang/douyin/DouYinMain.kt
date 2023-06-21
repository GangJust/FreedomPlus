package com.freegang.douyin

import android.app.Application
import android.widget.Toast
import com.freegang.config.Config
import com.freegang.ktutils.app.KAppCrashUtils
import com.freegang.ktutils.io.hasOperationStorage
import com.freegang.ktutils.log.KLogCat
import com.freegang.plugin.PluginBridge
import com.freegang.xpler.core.lpparam
import com.freegang.xpler.core.toClass
import com.freegang.xpler.loader.hostClassloader
import io.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Method

class DouYinMain(private val app: Application) {
    companion object {
        val awemeHostApplication get() = "com.ss.android.ugc.aweme.app.host.AwemeHostApplication".toClass(lpparam.classLoader)!!

        //var diggClazz: Class<*>? = null
        //var longPressPanel: Class<*>? = null
        var commonPageClazz: Class<*>? = null
        var emojiMethods: List<Method> = emptyList()
    }

    init {
        run {
            //日志工具
            KLogCat.init(app)
            //KLogCat.openStorage()

            //全局异常捕获工具
            KAppCrashUtils.instance.init(app, "抖音异常退出!")

            //文件读写权限检查
            if (!app.hasOperationStorage) {
                Toast.makeText(app, "抖音没有文件读写权限!", Toast.LENGTH_SHORT).show()
                return@run
            }

            //插件化注入
            val subClazz = hostClassloader!!.loadClass("com.ss.android.ugc.aweme.bullet.ui.BulletContainerActivity")
            PluginBridge.init(app, subClazz)

            //加载配置
            Config.read(app)

            //初始化DexKit
            initDexKit()

            //Hook
            HMainActivity(lpparam)
            HMainFragment(lpparam)
            HDetailActivity(lpparam)
            HFlippableViewPager(lpparam)
            HVerticalViewPager(lpparam)
            HCommonPageFragment(lpparam)
            HGifEmojiDetailActivity(lpparam)
            HEmojiDetailDialog(lpparam)
            HEmojiDetailDialogNew(lpparam)
            HHomeSideBarEntranceManagerV1(lpparam)
        }
    }

    private fun initDexKit() {
        System.loadLibrary("dexkit")
        DexKitBridge.create(lpparam.appInfo.sourceDir)?.use { bridge ->
            /*if (diggClazz == null) {
                val findMaps = bridge.batchFindClassesUsingStrings {
                    addQuery(
                        "Digg",
                        setOf(
                            "com/ss/android/ugc/aweme/feed/quick/presenter/FeedDiggPresenter",
                            "handle_digg_click",
                            "homepage_hot",
                            "homepage_familiar",
                            "feed_digg_api_monitor",
                        )
                    )
                }
                diggClazz = findMaps["Digg"]?.firstOrNull()?.getClassInstance(lpparam.classLoader)
            }*/
            /*if (longPressPanel == null) {
                val findMaps = bridge.batchFindClassesUsingStrings {
                    addQuery("longPressPanel", setOf("LongPressPanelFragmentImpl"))
                }
                longPressPanel = findMaps["longPressPanel"]?.firstOrNull()?.getClassInstance(lpparam.classLoader)
            }*/
            if (commonPageClazz == null) {
                val findMaps = bridge.batchFindClassesUsingStrings {
                    addQuery("CommonPage", setOf("a1128.b7947", "DetailActOtherNitaView"))
                }
                commonPageClazz = findMaps["CommonPage"]?.firstOrNull()?.getClassInstance(lpparam.classLoader)
            }
            if (emojiMethods.isEmpty()) {
                emojiMethods = bridge.findMethod {
                    methodReturnType = "V"
                    methodParamTypes = arrayOf("Lcom/ss/android/ugc/aweme/emoji/model/Emoji;")
                }.filter { it.isMethod }.map { it.getMethodInstance(lpparam.classLoader) }
            }
        }
    }
}