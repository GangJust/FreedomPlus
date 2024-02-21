package io.github.fplus.core.helper

import android.app.Application
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.json.getIntOrDefault
import com.freegang.ktutils.json.getJSONArrayOrDefault
import com.freegang.ktutils.json.getStringOrDefault
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.text.KTextUtils
import io.github.fplus.core.config.ConfigV1
import io.github.xpler.core.findClass
import io.github.xpler.core.findMethod
import io.github.xpler.core.lpparam
import org.json.JSONArray
import org.json.JSONObject
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.result.ClassDataList
import org.luckypray.dexkit.result.MethodData
import org.luckypray.dexkit.result.MethodDataList
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
    var videoPlayerHelperClazz: Class<*>? = null
    var abstractFeedAdapterClazz: Class<*>? = null
    var recommendFeedFetchPresenterClazz: Class<*>? = null
    var fullFeedFollowFetchPresenterClazz: Class<*>? = null
    var detailPageFragmentClazz: Class<*>? = null
    var emojiApiProxyClazz: Class<*>? = null
    var emojiPopupWindowClazz: Class<*>? = null
    var bottomCtrlBarClazz: Class<*>? = null
    var chatListRecyclerViewAdapterClazz: Class<*>? = null
    var chatListRecalledHintClazz: Class<*>? = null
    var restartUtilsClazz: Class<*>? = null

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

        KLogCat.tagI(TAG, "当前进程: ${lpparam.processName}")
        if (readCache()) {
            KLogCat.tagI(TAG, "缓存读取成功!")
            return
        }
        startSearch()
        saveCache()
    }

    /**
     * Dexkit开始搜索
     */
    private fun startSearch() {
        KLogCat.tagI(TAG, "Dexkit开始搜索: ${lpparam.appInfo.sourceDir}")
        System.loadLibrary("dexkit")
        DexKitBridge.create(lpparam.appInfo.sourceDir).use { bridge ->
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
        }.singleInstance("sideBarNestedScrollView")

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
        }.singleInstance("coenerExtendsionsPoupWindow")

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
        }.singleInstance("mainBottomTabItem")

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
                        returnType = "com.ss.android.ugc.aweme.comment.constants.CommentColorMode"
                    }
                }

                usingStrings = listOf(
                    "com/ss/android/ugc/aweme/comment/ui/CommentListPageFragment",
                    "CommentListPageFragment",
                )
            }
        }.singleInstance("commentListPageFragment")

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
        }.singleInstance("conversationFragment")

        abstractFeedAdapterClazz = bridge.findClass {
            matcher {
                fields {
                    add {
                        type = "android.view.LayoutInflater"
                    }
                    add {
                        type = "com.ss.android.ugc.aweme.feed.model.BaseFeedPageParams"
                    }
                }

                methods {
                    add {
                        name = "getItemPosition"
                    }
                    add {
                        name = "finishUpdate"
                    }
                }

                usingStrings {
                    add("AbstractFeedAdapter aweme.aid = ")
                }
            }
        }.singleInstance("abstractFeedAdapter")

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
        }.singleInstance("recommendFeedFetchPresenter")

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
        }.singleInstance("fullFeedFollowFetchPresenter")

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
        }.singleInstance("emojiPopupWindow")

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
        }.singleInstance("seekBarSpeedModeBottomContainer")

        poiCreateInstanceImplClazz = bridge.findClass {
            matcher {
                className = "com.ss.android.ugc.aweme.poi.PoiCreateInstanceImpl"
            }
        }.singleInstance("poiCreateInstanceImpl")

        emojiApiProxyClazz = bridge.findClass {
            matcher {
                fields {
                    add {
                        type = "com.ss.android.ugc.aweme.emoji.utils.EmojiApi"
                    }
                    add {
                        type = "com.ss.android.ugc.aweme.emoji.store.EmojiShopApi"
                    }
                }

                methods {
                    add {
                        returnType = "com.ss.android.ugc.aweme.emoji.utils.EmojiApi"
                    }
                    add {
                        returnType = "com.ss.android.ugc.aweme.emoji.store.EmojiShopApi"
                    }
                }

                usingStrings {
                    add("https://", StringMatchType.Equals)
                    add("/aweme/v1/", StringMatchType.Equals)
                }
            }
        }.singleInstance("emojiApiProxy")

        mainBottomTabViewClazz = bridge.findClass {
            matcher {
                superClass = "android.widget.FrameLayout"

                fields {
                    add {
                        type = "com.bytedance.dux.image.DuxImageView"
                    }
                }

                methods {
                    add {
                        name = "getBottomColor"
                    }
                    add {
                        name = "setBackgroundDrawable"
                        paramTypes = listOf("android.graphics.drawable.Drawable")
                    }
                    add {
                        name = "setBackgroundResource"
                    }
                    add {
                        name = "setBackgroundColor"
                    }
                    add {
                        name = "setVisibility"
                    }
                    add {
                        name = "setAlpha"
                    }
                }

                usingStrings {
                    add("alpha", StringMatchType.Equals)
                    add("translationY", StringMatchType.Equals)
                    add("MainBottomTabView", StringMatchType.Equals)
                }
            }
        }.singleInstance("mainBottomTabView")

        bottomCtrlBarClazz = bridge.findClass {
            searchPackages("X")
            matcher {
                superClass = "android.widget.FrameLayout"
                fields {
                    add {
                        annotations {
                            add {
                                type = "dalvik.annotation.Signature"
                                addElement {
                                    name = "value"
                                    arrayValue {
                                        addString("IPauseCtrlAction")
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }.singleInstance("bottomCtrlBar")

        chatListRecyclerViewAdapterClazz = bridge.findClass {
            // searchPackages("X")
            matcher {
                fields {
                    add {
                        type = "com.ss.android.ugc.aweme.im.sdk.chat.SessionInfo"
                    }
                    add {
                        type = "androidx.recyclerview.widget.RecyclerView"
                    }
                    add {
                        type = "androidx.recyclerview.widget.RecyclerView\$ItemAnimator"
                    }
                    add {
                        type = "java.util.Set"
                    }
                    add {
                        type = "java.util.Set"
                    }
                }

                methods {
                    add {
                        name = "onBindViewHolder"
                        paramTypes = listOf(
                            "androidx.recyclerview.widget.RecyclerView\$ViewHolder",
                            "int",
                            "java.util.List",
                        )
                    }
                }
            }
        }.singleInstance("chatListRecyclerViewAdapter")

        chatListRecalledHintClazz = bridge.findClass {
            matcher {
                fields {
                    add {
                        type = "android.widget.TextView"
                    }

                    add {
                        type = "com.ss.android.ugc.aweme.views.InterceptTouchLinearLayout"
                    }

                    add {
                        type {
                            superClass = "androidx.lifecycle.ViewModel"
                        }
                    }
                }

                methods {
                    add {
                        name = "getFastEventBusSubscriberClass"
                        returnType = "java.lang.Class"
                    }

                    add {
                        paramTypes = listOf(
                            null,
                            "int",
                            "java.util.List"
                        )
                    }
                }
            }
        }.singleInstance("chatListRecalledHint")

        restartUtilsClazz = bridge.findClass {
            searchPackages("X")
            matcher {
                methods {
                    add {
                        paramTypes = listOf("android.content.Context")
                        usingNumbers = listOf(0x10008000)
                    }
                }
                usingStrings {
                    add("System.exit returned normally, while it was supposed to halt JVM.")
                }
            }
        }.singleInstance("restartUtils")

        val findMaps = bridge.batchFindClassUsingStrings {
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
        }
        DexkitBuilder.videoPlayerHelperClazz = findMaps.singleInstance("videoPlayerHelper")
        DexkitBuilder.detailPageFragmentClazz = findMaps.singleInstance("detailPageFragment")
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
                    add {
                        field {
                            type = "com.ss.android.ugc.aweme.feed.model.VideoItemParams"
                        }
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

        KLogCat.tagI(TAG, cache.toString(2))

        return true
    }

    /**
     * 从缓存中读取类
     */
    private fun readClassCache(cache: JSONObject) {
        val classCache = cache.getJSONObject("class")

        //
        sideBarNestedScrollViewClazz = classCache.getStringOrDefault("sideBarNestedScrollView").loadOrFindClass()
        cornerExtensionsPopupWindowClazz = classCache.getStringOrDefault("coenerExtendsionsPoupWindow").loadOrFindClass()
        mainBottomTabViewClazz = classCache.getStringOrDefault("mainBottomTabView").loadOrFindClass()
        mainBottomTabItemClazz = classCache.getStringOrDefault("mainBottomTabItem").loadOrFindClass()
        commentListPageFragmentClazz = classCache.getStringOrDefault("commentListPageFragment").loadOrFindClass()
        conversationFragmentClazz = classCache.getStringOrDefault("conversationFragment").loadOrFindClass()
        seekBarSpeedModeBottomContainerClazz = classCache.getStringOrDefault("seekBarSpeedModeBottomContainer").loadOrFindClass()
        poiCreateInstanceImplClazz = classCache.getStringOrDefault("poiCreateInstanceImpl").loadOrFindClass()
        videoPlayerHelperClazz = classCache.getStringOrDefault("videoPlayerHelper").loadOrFindClass()
        abstractFeedAdapterClazz = classCache.getStringOrDefault("abstractFeedAdapter").loadOrFindClass()
        recommendFeedFetchPresenterClazz = classCache.getStringOrDefault("recommendFeedFetchPresenter").loadOrFindClass()
        fullFeedFollowFetchPresenterClazz = classCache.getStringOrDefault("fullFeedFollowFetchPresenter").loadOrFindClass()
        emojiPopupWindowClazz = classCache.getStringOrDefault("emojiPopupWindow").loadOrFindClass()
        detailPageFragmentClazz = classCache.getStringOrDefault("detailPageFragment").loadOrFindClass()
        emojiApiProxyClazz = classCache.getStringOrDefault("emojiApiProxy").loadOrFindClass()
        bottomCtrlBarClazz = classCache.getStringOrDefault("bottomCtrlBar").loadOrFindClass()
        chatListRecyclerViewAdapterClazz = classCache.getStringOrDefault("chatListRecyclerViewAdapter").loadOrFindClass()
        chatListRecalledHintClazz = classCache.getStringOrDefault("chatListRecalledHint").loadOrFindClass()
        restartUtilsClazz = classCache.getStringOrDefault("restartUtils").loadOrFindClass()
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
    private fun Map<String, ClassDataList>.singleInstance(key: String): Class<*>? {
        val classData = this[key]?.singleOrNull()
        KLogCat.tagI(TAG, "found-class[$key]: ${classData?.name}")
        classCacheJson.put(key, "${classData?.name}")
        return classData?.getInstance(lpparam.classLoader)
    }

    private fun ClassDataList.singleInstance(label: String): Class<*>? {
        val classData = this.singleOrNull()
        KLogCat.tagI(TAG, "found-class[$label]: ${classData?.name}")
        classCacheJson.put(label, "${classData?.name}")
        return classData?.getInstance(lpparam.classLoader)
    }

    private fun MethodDataList.allMethodInstance(label: String): List<Method> {
        val array = JSONArray()
        methodsCacheJson.put(label, array)
        return this.filter {
            it.isMethod
        }.map {
            KLogCat.tagI(TAG, "found-method[$label]: $it")
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

    private fun String.loadOrFindClass(): Class<*>? {
        if (KTextUtils.isEmpty(this)) {
            return null
        }

        return try {
            app?.classLoader?.loadClass(this) ?: lpparam.findClass(this)
        } catch (e: Throwable) {
            null
        }
    }
}