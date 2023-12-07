package io.github.fplus.core.helper

import android.app.Application
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.json.getIntOrDefault
import com.freegang.ktutils.json.getJSONArrayOrDefault
import com.freegang.ktutils.json.getStringOrDefault
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.text.ifNotEmpty
import com.ss.android.ugc.aweme.comment.constants.CommentColorMode
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.findClass
import io.github.xpler.core.findMethod
import io.github.xpler.core.lpparam
import org.json.JSONArray
import org.json.JSONObject
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.ClassDataList
import org.luckypray.dexkit.query.MethodDataList
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.result.MethodData
import java.lang.reflect.Method
import java.lang.reflect.Modifier

object DexkitBuilder {
    const val TAG = "DexkitBuilder"
    private var app: Application? = null

    private var cacheVersion: Int = 0
    private var cacheJson: JSONObject = JSONObject()
    private var classCacheJson: JSONObject = JSONObject()
    private var methodsCacheJson: JSONObject = JSONObject()

    // class
    var sideBarNestedScrollViewClazz: Class<*>? = null
    var cornerExtensionsPopupWindowClazz: Class<*>? = null
    var mainBottomTabViewClazz: Class<*>? = null
    var mainBottomTabItemClazz: Class<*>? = null
    var commentListPageFragmentClazz: Class<*>? = null
    var conversationFragmentClazz: Class<*>? = null
    var seekBarSpeedModeBottomContainerClazz: Class<*>? = null
    var poiCreateInstanceImplClazz: Class<*>? = null
    var videoPlayerStateClazz: Class<*>? = null
    var videoPlayerHelperClazz: Class<*>? = null
    var videoPinchViewClazz: Class<*>? = null
    var videoPagerAdapterClazz: Class<*>? = null
    var recommendFeedFetchPresenterClazz: Class<*>? = null
    var fullFeedFollowFetchPresenterClazz: Class<*>? = null
    var detailPageFragmentClazz: Class<*>? = null
    var emojiApiProxyClazz: Class<*>? = null
    var emojiPopupWindowClazz: Class<*>? = null
    var ripsChatRoomFragmentClazz: Class<*>? = null

    // methods
    var videoViewHolderMethods: List<Method> = listOf()

    /**
     * 只是为了解决出现的各种稀奇古怪的情况。
     * 部分未作混淆的类在 Dexkit 搜索期间, 会跳过构造方法的勾子(勾不住构造方法), 但是它的普通方法却能勾住。
     * 还是没太想通, 而按照作者`韵の祈`的说法, 应该是搜索期间导致错过了构造方法勾子的时机。
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
        readCacheOrStartSearch(app, version)
        searchAfter.run()
    }

    /**
     * 如果缓存存在则读取缓存, 否则开启搜索并保存
     * @param app 被搜索app的Application
     * @param version 类缓存版本号, 更改会触发更新
     */
    private fun readCacheOrStartSearch(app: Application, version: Int) {
        this.app = app
        this.cacheVersion = version

        KLogCat.tagD(TAG, "当前进程: ${lpparam.processName}")
        if (readCache()) {
            KLogCat.tagD(TAG, "缓存读取成功!")
            return
        }
        startSearch()
        saveCache()
    }

    /**
     * Dexkit开始搜索
     */
    private fun startSearch() {
        KLogCat.tagD(TAG, "Dexkit开始搜索")
        System.loadLibrary("dexkit")
        DexKitBridge.create(lpparam.appInfo.sourceDir)?.use { bridge ->
            searchClass(bridge)
            searchMethod(bridge)
        }
    }

    /**
     * 搜索类
     */
    private fun searchClass(bridge: DexKitBridge) {
        sideBarNestedScrollViewClazz = bridge.findClass {
            matcher {
                className = "com.ss.android.ugc.aweme.sidebar.SideBarNestedScrollView"
            }
        }.firstInstance("sideBarNestedScrollView")

        cornerExtensionsPopupWindowClazz = bridge.findClass {
            matcher {
                superClass = "android.widget.PopupWindow"
                fields {
                    add {
                        type = "android.view.LayoutInflater"
                    }
                    add {
                        type = "android.app.Dialog"
                    }
                }
                methods {
                    add {
                        paramTypes = listOf("android.widget.PopupWindow")
                    }
                    add {
                        paramTypes = listOf("boolean")
                    }
                    add {
                        returnType = "android.view.View"
                    }
                    add {
                        name = "dismiss"
                    }
                }
            }
        }.firstInstance("coenerExtendsionsPoupWindow")

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
        }.firstInstance("mainBottomTabItem")

        commentListPageFragmentClazz = bridge.findClass {
            matcher {
                fields {
                    add {
                        type = "com.ss.android.ugc.aweme.comment.widget.CommentNestedLayout"
                    }
                    add {
                        type = "com.ss.android.ugc.aweme.comment.param.VideoCommentPageParam"
                    }
                }

                methods {
                    add {
                        returnType = CommentColorMode::class.java.name
                    }
                }

                usingStrings = listOf(
                    "com/ss/android/ugc/aweme/comment/ui/CommentListPageFragment",
                    "CommentListPageFragment",
                )
            }
        }.firstInstance("commentListPageFragment")

        conversationFragmentClazz = bridge.findClass {
            matcher {
                fields {
                    add {
                        type = "com.ss.android.ugc.aweme.conversation.CommentConversationLayout"
                    }
                    add {
                        type = "com.ss.android.ugc.aweme.comment.widget.CommentNestedLayout"
                    }
                }

                usingStrings = listOf(
                    "com/ss/android/ugc/aweme/comment/ui/ConversationFragment",
                    "ConversationFragment",
                )
            }
        }.firstInstance("conversationFragment")

        videoPinchViewClazz = bridge.findClass {
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
        }.firstInstance("videoPinchView")

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
        }.firstInstance("videoPagerAdapter")

        recommendFeedFetchPresenterClazz = bridge.findClass {
            matcher {
                methods {
                    add {
                        name = "onSuccess"
                    }
                }
                addUsingString("com.ss.android.ugc.aweme.feed.presenter.RecommendFeedFetchPresenter")
                addUsingString("enter_from")
                addUsingString("homepage_hot")
            }
        }.firstInstance("recommendFeedFetchPresenter")

        fullFeedFollowFetchPresenterClazz = bridge.findClass {
            matcher {
                methods {
                    add {
                        name = "onSuccess"
                    }
                }
                addUsingString("com.ss.android.ugc.aweme.feed.presenter.FullFeedFollowFetchPresenter")
                addUsingString("enter_from")
                addUsingString("homepage_follow")
            }
        }.firstInstance("fullFeedFollowFetchPresenter")

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
                        modifiers = Modifier.PUBLIC
                        paramTypes = listOf("android.content.Context")
                    }
                    add {
                        modifiers = Modifier.PRIVATE
                        paramTypes = listOf("com.ss.android.ugc.aweme.emoji.base.BaseEmoji")
                    }
                    add {
                        modifiers = Modifier.PRIVATE
                        paramTypes = listOf(
                            "com.ss.android.ugc.aweme.emoji.base.BaseEmoji",
                            "com.ss.android.ugc.aweme.base.ui.RemoteImageView",
                        )
                    }
                }
            }
        }.firstInstance("emojiPopupWindow")

        seekBarSpeedModeBottomContainerClazz = bridge.findClass {
            // findFirst = true
            matcher {
                methods {
                    add {
                        name = "getMSpeedText"
                        returnType = "android.widget.TextView"
                    }
                    add {
                        name = "getMBottomLayout"
                        returnType = "android.view.View"
                    }
                    add {
                        name = "getLoadingProgressBar"
                        returnType = "com.ss.android.ugc.aweme.feed.widget.LineProgressBar"
                    }
                }
            }
        }.firstInstance("seekBarSpeedModeBottomContainer")

        poiCreateInstanceImplClazz = bridge.findClass {
            matcher {
                className = "com.ss.android.ugc.aweme.poi.PoiCreateInstanceImpl"
            }
        }.firstInstance("poiCreateInstanceImpl")

        videoPlayerStateClazz = bridge.findClass {
            matcher {
                fields {
                    add {
                        type = "com.ss.android.ugc.aweme.feed.model.Aweme"
                    }
                    add {
                        type = "java.lang.String"
                    }
                    add {
                        type = "int"
                    }
                    add {
                        type {
                            modifiers = Modifier.INTERFACE
                        }
                    }
                }

                methods {
                    add {
                        name = "<init>"
                        params {
                            add {
                                type = "com.ss.android.ugc.aweme.feed.model.Aweme"
                            }
                            add {
                                type = "java.lang.String"
                            }
                            add {
                                type = "int"
                            }
                            add {
                                type = "int"
                            }
                            add {
                                type {
                                    modifiers = Modifier.INTERFACE
                                }
                            }
                        }
                    }
                }
            }
        }.firstInstance("videoPlayerState")

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
                groupName = "videoPlayerHelper"
                usingStrings = listOf(
                    "isDoubleClickResExist >>> channel empty",
                    "当前无网络，暂不可用",
                    "暂不支持点赞操作",
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
        DexkitBuilder.mainBottomTabViewClazz = findMaps.firstInstance("mainBottomTabView")
        DexkitBuilder.videoPlayerHelperClazz = findMaps.firstInstance("videoPlayerHelper")
        DexkitBuilder.detailPageFragmentClazz = findMaps.firstInstance("detailPageFragment")
        DexkitBuilder.emojiApiProxyClazz = findMaps.firstInstance("emojiApiProxy")
        DexkitBuilder.ripsChatRoomFragmentClazz = findMaps.firstInstance("ripsChatRoomFragment")
    }

    /**
     * 搜索方法
     */
    private fun searchMethod(bridge: DexKitBridge) {
        videoViewHolderMethods = bridge.findClass {
            matcher {
                className = "com.ss.android.ugc.aweme.feed.adapter.VideoViewHolder"
            }
        }.findMethod {
            matcher {
                usingFields {
                    add {
                        field {
                            type = "com.ss.android.ugc.aweme.feed.ui.PenetrateTouchRelativeLayout"
                        }
                    }
                }

                params {
                    add {
                        type = "java.lang.Boolean"
                    }
                }

                invokeMethods {
                    add {
                        name = "isCleanMode"
                    }
                    add {
                        name = "getContext"
                    }
                }
            }
        }.allMethodInstance("videoViewHolderMethods")
    }

    /**
     * 读取缓存
     */
    private fun readCache(): Boolean {
        val cache = ConfigV1.get().dexkitCache

        // version
        val version = cache.getIntOrDefault("version")
        val appVersion = cache.getStringOrDefault("appVersion")

        if (appVersion.compareTo("${app!!.appVersionName}_${app!!.appVersionCode}") != 0) {
            return false
        }

        if (version < cacheVersion) {
            return false
        }

        readClassCache(cache)
        readMethodsCache(cache)

        KLogCat.tagD(TAG, cache.toString(2))

        return true
    }

    /**
     * 从缓存中读取类
     */
    private fun readClassCache(cache: JSONObject) {
        val classCache = cache.getJSONObject("class")

        //
        val sideBarNestedScrollView = classCache.getStringOrDefault("sideBarNestedScrollView")
        val cornerExtendsionsPopupWindow = classCache.getStringOrDefault("coenerExtendsionsPoupWindow")
        val mainBottomTabView = classCache.getStringOrDefault("mainBottomTabView")
        val mainBottomTabItem = classCache.getStringOrDefault("mainBottomTabItem")
        val commentListPageFragment = classCache.getStringOrDefault("commentListPageFragment")
        val conversationFragment = classCache.getStringOrDefault("conversationFragment")
        val seekBarSpeedModeBottomContainer = classCache.getStringOrDefault("seekBarSpeedModeBottomContainer")
        val poiCreateInstanceImpl = classCache.getStringOrDefault("poiCreateInstanceImpl")
        val videoPlayerState = classCache.getStringOrDefault("videoPlayerState")
        val videoPlayerHelper = classCache.getStringOrDefault("videoPlayerHelper")
        val videoPinchView = classCache.getStringOrDefault("videoPinchView")
        val videoPagerAdapter = classCache.getStringOrDefault("videoPagerAdapter")
        val recommendFeedFetchPresenter = classCache.getStringOrDefault("recommendFeedFetchPresenter")
        val fullFeedFollowFetchPresenter = classCache.getStringOrDefault("fullFeedFollowFetchPresenter")
        val emojiPopupWindow = classCache.getStringOrDefault("emojiPopupWindow")
        val detailPageFragment = classCache.getStringOrDefault("detailPageFragment")
        val emojiApiProxy = classCache.getStringOrDefault("emojiApiProxy")
        val ripsChatRoomFragment = classCache.getStringOrDefault("ripsChatRoomFragment")

        sideBarNestedScrollViewClazz = sideBarNestedScrollView.ifNotEmpty { lpparam.findClass(it) }
        cornerExtensionsPopupWindowClazz = cornerExtendsionsPopupWindow.ifNotEmpty { lpparam.findClass(it) }
        mainBottomTabViewClazz = mainBottomTabView.ifNotEmpty { lpparam.findClass(it) }
        mainBottomTabItemClazz = mainBottomTabItem.ifNotEmpty { lpparam.findClass(it) }
        commentListPageFragmentClazz = commentListPageFragment.ifNotEmpty { lpparam.findClass(it) }
        conversationFragmentClazz = conversationFragment.ifNotEmpty { lpparam.findClass(it) }
        seekBarSpeedModeBottomContainerClazz = seekBarSpeedModeBottomContainer.ifNotEmpty { lpparam.findClass(it) }
        poiCreateInstanceImplClazz = poiCreateInstanceImpl.ifNotEmpty { lpparam.findClass(it) }
        videoPlayerStateClazz = videoPlayerState.ifNotEmpty { lpparam.findClass(it) }
        videoPlayerHelperClazz = videoPlayerHelper.ifNotEmpty { lpparam.findClass(it) }
        videoPinchViewClazz = videoPinchView.ifNotEmpty { lpparam.findClass(it) }
        videoPagerAdapterClazz = videoPagerAdapter.ifNotEmpty { lpparam.findClass(it) }
        recommendFeedFetchPresenterClazz = recommendFeedFetchPresenter.ifNotEmpty { lpparam.findClass(it) }
        fullFeedFollowFetchPresenterClazz = fullFeedFollowFetchPresenter.ifNotEmpty { lpparam.findClass(it) }
        emojiPopupWindowClazz = emojiPopupWindow.ifNotEmpty { lpparam.findClass(it) }
        detailPageFragmentClazz = detailPageFragment.ifNotEmpty { lpparam.findClass(it) }
        emojiApiProxyClazz = emojiApiProxy.ifNotEmpty { lpparam.findClass(it) }
        ripsChatRoomFragmentClazz = ripsChatRoomFragment.ifNotEmpty { lpparam.findClass(it) }
    }

    /**
     * 从缓存中读取方法
     */
    private fun readMethodsCache(cache: JSONObject) {
        val methodsCache = cache.getJSONObject("methods")

        //
        videoViewHolderMethods = methodsCache.getJSONArrayOrDefault("videoViewHolderMethods").getCacheMethods()
    }

    /**
     * 保存缓存
     */
    private fun saveCache() {
        // version
        cacheJson.put("version", "$cacheVersion")
        cacheJson.put("appVersion", "${app!!.appVersionName}_${app!!.appVersionCode}")

        // cache
        cacheJson.put("class", classCacheJson)
        cacheJson.put("methods", methodsCacheJson)
        ConfigV1.get().dexkitCache = cacheJson
    }


    // 拓展方法
    private fun Map<String, ClassDataList>.firstInstance(key: String): Class<*>? {
        val classData = this[key]?.firstOrNull()
        KLogCat.tagD(TAG, "found-class[$key]: ${classData?.name}")
        classCacheJson.put(key, classData?.name)
        return classData?.getInstance(lpparam.classLoader)
    }

    private fun ClassDataList.firstInstance(label: String): Class<*>? {
        val classData = this.firstOrNull()
        KLogCat.tagD(TAG, "found-class[$label]: ${classData?.name}")
        classCacheJson.put(label, classData?.name)
        return classData?.getInstance(lpparam.classLoader)
    }

    private fun MethodDataList.allMethodInstance(label: String): List<Method> {
        val array = JSONArray()
        methodsCacheJson.put(label, array)
        return this.filter {
            it.isMethod
        }.map {
            KLogCat.tagD(TAG, "found-method[$label]: $it")
            array.put(it.toJson())
            it.getMethodInstance(lpparam.classLoader)
        }
    }

    private fun MethodData.toJson(): JSONObject {
        val json = JSONObject()
        json.put("className", className)
        json.put("methodName", methodName)
        json.put("paramTypeNames", paramTypeNames.joinToString())
        return json
    }

    private fun JSONArray.getCacheMethods(): List<Method> {
        val methods = mutableListOf<Method>()
        for (i in 0 until this.length()) {
            runCatching {
                val json = this.getJSONObject(i)
                val className = json.getStringOrDefault("className")
                val methodName = json.getStringOrDefault("methodName")
                val paramTypeNames = json.getStringOrDefault("paramTypeNames").split(",")
                val method = lpparam.findMethod(className, methodName, *paramTypeNames.toTypedArray())
                methods.add(method)
            }
        }
        return methods
    }
}