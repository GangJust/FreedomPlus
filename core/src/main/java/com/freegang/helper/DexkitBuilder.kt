package com.freegang.helper

import android.app.Application
import com.freegang.config.ConfigV1
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.json.getIntOrDefault
import com.freegang.ktutils.json.getStringOrDefault
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.text.ifNotEmpty
import com.freegang.xpler.core.findClass
import com.freegang.xpler.core.lpparam
import org.json.JSONObject
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.ClassDataList
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Modifier

object DexkitBuilder {
    const val TAG = "DexkitBuilder"
    private var app: Application? = null

    private var classCacheVersion: Int = 0
    private var classCacheJson: JSONObject = JSONObject()

    var cornerExtensionsPopupWindowClazz: Class<*>? = null
    var mainBottomTabViewClazz: Class<*>? = null
    var mainBottomTabItemClazz: Class<*>? = null
    var detailPageFragmentClazz: Class<*>? = null
    var videoPinchClazz: Class<*>? = null
    var videoPagerAdapterClazz: Class<*>? = null
    var emojiApiProxyClazz: Class<*>? = null
    var emojiPopupWindowClazz: Class<*>? = null
    var ripsChatRoomFragmentClazz: Class<*>? = null

    /**
     * Dexkit开始搜索
     * @param app 被搜索app的Application
     * @param version 类缓存版本号, 更改会触发更新
     */
    fun search(app: Application, version: Int) {
        this.app = app
        this.classCacheVersion = version

        //
        KLogCat.tagD(TAG, "当前进程: ${lpparam.processName}")
        if (readClassCache()) {
            KLogCat.tagD(TAG, "存在类缓存 - 读取")
            return
        }

        //
        KLogCat.tagD(TAG, "不存在类缓存 - Dexkit开始搜索")
        System.loadLibrary("dexkit")
        DexKitBridge.create(lpparam.appInfo.sourceDir)?.use { bridge ->
            cornerExtensionsPopupWindowClazz = bridge.findClass {
                matcher {
                    superClass = "android.widget.PopupWindow"
                    methods {
                        add {
                            name = "dismiss"
                        }
                    }
                    usingStrings = listOf("mute_notice", "delete_notice")
                }
            }.firstClass("coenerExtendsionsPoupWindow")
            mainBottomTabItemClazz = bridge.findClass {
                matcher {
                    methods {
                        add {
                            name = "getNowImageRes"
                        }
                        add {
                            name = "getOperator"
                        }
                        add {
                            name = "getRefreshTab"
                            returnType = "android.view.View"
                        }
                    }
                }
            }.firstClass("mainBottomTabItem")
            videoPinchClazz = bridge.findClass {
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
            }.firstClass("videoPinch")
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
                            returnType = "com.ss.android.ugc.aweme.feed.adapter.FeedImageViewHolder"
                        }
                    }
                }
            }.firstClass("videoPagerAdapter")
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
            }.firstClass("emojiPopupWindow")

            val findMaps = bridge.batchFindClassUsingStrings {
                addSearchGroup {
                    groupName = "mainBottomTabView"
                    usingStrings = listOf(
                        "alpha",
                        "translationY",
                        "MainBottomTabView",
                    )
                }
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
            mainBottomTabViewClazz = findMaps.firstClass("mainBottomTabView")
            detailPageFragmentClazz = findMaps.firstClass("detailPageFragment")
            emojiApiProxyClazz = findMaps.firstClass("emojiApiProxy")
            ripsChatRoomFragmentClazz = findMaps.firstClass("ripsChatRoomFragment")
        }

        //
        saveClassCache()
    }

    /**
     * 只是为了解决出现的各种稀奇古怪的情况。
     * 部分未作混淆的类在 Dexkit 搜索期间, 会跳过构造方法的勾子(勾不住构造方法), 但是它的普通方法却能勾住。
     * 还是没太想通, 而按照`韵の祈`老大的说法, 应该是搜索期间导致错过了构造方法勾子的时机。
     *
     * @param app 被搜索app的Application
     * @param version 类缓存版本号, 更改会触发更新
     * @param searchBefore 在Dexkit搜索之前做些什么, 建议在这里勾住`未混淆`的类
     * @param searchAfter 在Dexkit搜索之前做些什么, 建议在这里勾住`混淆`的类
     */
    fun running(
        app: Application,
        version: Int,
        searchBefore: Runnable,
        searchAfter: Runnable,
    ) {
        searchBefore.run()
        search(app, version)
        searchAfter.run()
    }

    /**
     * 读取类缓存
     */
    private fun readClassCache(): Boolean {
        val cache = ConfigV1.get().classCache

        // version
        val version = cache.getIntOrDefault("version")
        val appVersion = cache.getStringOrDefault("appVersion")

        // class
        val cornerExtendsionsPopupWindow = cache.getStringOrDefault("coenerExtendsionsPoupWindow")
        val mainBottomTabView = cache.getStringOrDefault("mainBottomTabView")
        val mainBottomTabItem = cache.getStringOrDefault("mainBottomTabItem")
        val videoPinch = cache.getStringOrDefault("videoPinch")
        val videoPagerAdapter = cache.getStringOrDefault("videoPagerAdapter")
        val emojiPopupWindow = cache.getStringOrDefault("emojiPopupWindow")
        val detailPageFragment = cache.getStringOrDefault("detailPageFragment")
        val emojiApiProxy = cache.getStringOrDefault("emojiApiProxy")
        val ripsChatRoomFragment = cache.getStringOrDefault("ripsChatRoomFragment")

        if (appVersion.compareTo("${app!!.appVersionName}_${app!!.appVersionCode}") != 0) {
            return false
        }

        if (version < classCacheVersion) {
            return false
        }

        cornerExtensionsPopupWindowClazz = cornerExtendsionsPopupWindow.ifNotEmpty { lpparam.findClass(it) }
        mainBottomTabViewClazz = mainBottomTabView.ifNotEmpty { lpparam.findClass(it) }
        mainBottomTabItemClazz = mainBottomTabItem.ifNotEmpty { lpparam.findClass(it) }
        videoPinchClazz = videoPinch.ifNotEmpty { lpparam.findClass(it) }
        videoPagerAdapterClazz = videoPagerAdapter.ifNotEmpty { lpparam.findClass(it) }
        emojiPopupWindowClazz = emojiPopupWindow.ifNotEmpty { lpparam.findClass(it) }
        detailPageFragmentClazz = detailPageFragment.ifNotEmpty { lpparam.findClass(it) }
        emojiApiProxyClazz = emojiApiProxy.ifNotEmpty { lpparam.findClass(it) }
        ripsChatRoomFragmentClazz = ripsChatRoomFragment.ifNotEmpty { lpparam.findClass(it) }

        KLogCat.tagD(TAG, cache.toString(4))
        return true
    }

    /**
     * 存储类缓存
     */
    private fun saveClassCache() {
        // version
        classCacheJson.put("version", "$classCacheVersion")
        classCacheJson.put("appVersion", "${app!!.appVersionName}_${app!!.appVersionCode}")
        ConfigV1.get().classCache = classCacheJson
    }

    //
    private fun Map<String, ClassDataList>.firstClass(key: String): Class<*>? {
        val clazz = this[key]?.firstOrNull()?.getInstance(lpparam.classLoader)
        classCacheJson.put(key, clazz?.name)

        KLogCat.tagD(TAG, "found[$key]: $clazz")
        return clazz
    }

    //
    private fun ClassDataList.firstClass(label: String): Class<*>? {
        val clazz = this.firstOrNull()?.getInstance(lpparam.classLoader)
        classCacheJson.put(label, clazz?.name)

        KLogCat.tagD(TAG, "found[$label]: $clazz")
        return clazz
    }
}