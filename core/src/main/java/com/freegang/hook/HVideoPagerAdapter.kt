package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.reflect.fieldSetFirst
import com.freegang.ktutils.text.KTextUtils
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.hookClass
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.follow.presenter.FollowFeed
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

@Deprecated("淘汰区, 删除倒计时中..")
class HVideoPagerAdapter(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HVideoPagerAdapter"

        var isFilterLive = false
        var isFilterImage = false
        var isFilterAd = false
        var isFilterLongVideo = false
        var isFilterPopularEffect = false

        var filterLiveCount = 0
        var filterImageCount = 0
        var filterAdCount = 0
        var filterLongVideoCount = 0
        var filterOtherCount = 0
        var filterPopularEffectCount = 0
    }

    val config = ConfigV1.get()

    private val filterKeywordsAndTypes by lazy {
        config.videoFilterKeywords
            .replace("，", ",")
            .replace("\\s".toRegex(), "")
            .split(",")
            .toSet()
    }

    private val keywordsRegex by lazy {
        filterKeywordsAndTypes
            .filter { !config.videoFilterTypes.contains(it) }
            .joinToString("|")
            .toRegex()
    }

    override fun setTargetClass(): Class<*> = DexkitBuilder.videoPagerAdapterClazz ?: NoneHook::class.java

    override fun onInit() {
        DexkitBuilder.recommendFeedFetchPresenterClazz?.runCatching {
            lpparam.hookClass(this)
                .method("onSuccess") {
                    onBefore {
                        if (!config.isVideoFilter) return@onBefore

                        val mModel = thisObject.fieldGetFirst("mModel")
                        val mData = mModel?.fieldGetFirst("mData")
                        if (mData?.javaClass?.name?.contains("FeedItemList") == true) {
                            val items = mData.fieldGetFirst("items")?.asOrNull<List<Aweme>>() ?: emptyList()
                            mData.fieldSetFirst("items", filterAwemeList(items))
                            val array = items.map { it.sortString() }.toTypedArray()
                            KLogCat.tagD(TAG, "推荐视频列表")
                            KLogCat.tagD(TAG, array.joinToString("\n"))
                        }
                    }
                }
        }?.onFailure {
            KLogCat.tagE(TAG, it)
        }

        DexkitBuilder.fullFeedFollowFetchPresenterClazz?.runCatching {
            lpparam.hookClass(this)
                .method("onSuccess") {
                    onBefore {
                        if (!config.isVideoFilter) return@onBefore

                        val mModel = thisObject.fieldGetFirst("mModel")
                        val mData = mModel?.fieldGetFirst("mData")
                        if (mData?.javaClass?.name?.contains("FollowFeedList") == true) {
                            val mItems = mData.fieldGetFirst("mItems")?.asOrNull<List<FollowFeed>>() ?: emptyList()
                            mData.fieldSetFirst("mItems", filterFollowFeedList(mItems))
                            val array = mItems.map { it.aweme.sortString() }.toTypedArray()
                            KLogCat.tagD(TAG, "关注页视频列表")
                            KLogCat.tagD(TAG, array.joinToString("\n"))
                        }
                    }
                }
        }?.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        // hookBlockRunning(param) {
        //     if (argsOrEmpty.size != 1) return
        //     if (args[0] !is List<*>) return
        //     dumpStackLog()
        // }.onFailure {
        //     KLogCat.tagE(TAG, it)
        // }
    }

    private fun Aweme.sortString(): String {
        return "awemeType=${awemeType}, desc=${desc.replace(Regex("\\s"), "")}"
    }

    private fun filterAwemeList(items: List<Aweme>): List<Aweme> {
        resetFilterCount()
        val awemes = mutableListOf<Aweme>()
        for (item in items) {
            val aweme = needAweme(item) ?: continue
            awemes.add(aweme)
        }
        return awemes
    }

    private fun filterFollowFeedList(items: List<FollowFeed>): List<FollowFeed> {
        resetFilterCount()
        val followFeeds = mutableListOf<FollowFeed>()
        for (item in items) {
            needAweme(item.aweme) ?: continue
            followFeeds.add(item)
        }

        return followFeeds
    }

    private fun resetFilterCount() {
        for (s in filterKeywordsAndTypes) {
            if ("直播" == s) {
                HVideoPagerAdapter.isFilterLive = true
            }
            if ("图文" == s) {
                HVideoPagerAdapter.isFilterImage = true
            }
            if ("广告" == s) {
                HVideoPagerAdapter.isFilterAd = true
            }
            if ("长视频" == s) {
                HVideoPagerAdapter.isFilterLongVideo = true
            }
            if ("热门特效" == s) {  // awemeType=145, desc=null
                HVideoPagerAdapter.isFilterPopularEffect = true
            }
        }
    }

    private fun needAweme(aweme: Aweme): Aweme? {
        return when {
            HVideoPagerAdapter.isFilterLive && aweme.isLive -> {
                HVideoPagerAdapter.filterLiveCount += 1
                null
            }

            HVideoPagerAdapter.isFilterImage && aweme.isMultiImage -> {
                HVideoPagerAdapter.filterImageCount += 1
                null
            }

            HVideoPagerAdapter.isFilterAd && aweme.isAd -> {
                HVideoPagerAdapter.filterAdCount += 1
                null
            }

            HVideoPagerAdapter.isFilterLongVideo && aweme.isCopyRightLongVideo -> {
                HVideoPagerAdapter.filterLongVideoCount += 1
                null
            }

            HVideoPagerAdapter.isFilterPopularEffect && aweme.awemeType == 145 -> {
                HVideoPagerAdapter.filterPopularEffectCount += 1
                null
            }

            keywordsRegex.pattern.isNotBlank() && KTextUtils.get(aweme.desc).contains(keywordsRegex) -> {
                HVideoPagerAdapter.filterOtherCount += 1
                null
            }

            else -> aweme
        }
    }
}