package com.freegang.hook

import android.view.MotionEvent
import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.reflect.fieldSetFirst
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.ktutils.text.KTextUtils
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.OnAfter
import com.freegang.xpler.core.hookBlockRunning
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.thisView
import com.ss.android.ugc.aweme.common.widget.VerticalViewPager
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.follow.presenter.FollowFeed
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVerticalViewPagerNew(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<VerticalViewPager>(lpparam), CallMethods {
    companion object {
        const val TAG = "HVerticalViewPager"

        @get:Synchronized
        @set:Synchronized
        var currentAweme: Aweme? = null

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

    private val config get() = ConfigV1.get()

    //
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

    private var durationRunnable: Runnable? = null

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

    @OnAfter("onInterceptTouchEvent")
    fun onInterceptTouchEvent(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        longVideoJudge(param, event)
    }

    private fun longVideoJudge(param: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(param) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val adapter = thisObject.methodInvokeFirst("getAdapter") ?: return
                    val currentItem = thisObject.methodInvokeFirst("getCurrentItem") as? Int ?: return
                    currentAweme = adapter.methodInvokeFirst(
                        returnType = Aweme::class.java,
                        args = arrayOf(currentItem),
                    ) as? Aweme

                    //
                    if (config.isLongtimeVideoToast) {
                        durationRunnable?.run {
                            handler.removeCallbacks(this)
                            durationRunnable = null
                        }
                        durationRunnable = Runnable {
                            //
                            val delayItem = thisObject.methodInvokeFirst("getCurrentItem") as? Int ?: return@Runnable
                            if (delayItem == currentItem) {
                                return@Runnable
                            }

                            //
                            val delayAweme = adapter.methodInvokeFirst(
                                returnType = Aweme::class.java,
                                args = arrayOf(delayItem),
                            ) as? Aweme
                            val duration = delayAweme?.duration ?: 0
                            if (duration >= 1000 * 60 * 10) {
                                val minute = duration / 1000 / 60
                                val second = duration / 1000 % 60
                                KToastUtils.show(thisView.context, "请注意, 本条视频时长${minute}分${second}秒!")
                            }
                        }
                        handler.postDelayed(durationRunnable!!, 3000L)
                    }
                }
            }
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {

    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {
        /*hookBlockRunning(param){
            val array = mutableListOf<String>().apply {
                add("")
                add("当前对象: $thisObject")
                add("普通方法: $method")
                addAll(argsOrEmpty.mapIndexed { index, any -> "参数[$index]: $any" })
                add("返回: $resultOrThrowable")
                add("")
            }.toTypedArray()
            KLogCat.d(*array)
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }*/
    }

    private fun Aweme.sortString(): String {
        return "awemeType=${awemeType}, desc=${"$desc".replace(Regex("\\s"), "")}"
    }

    private fun filterAwemeList(items: List<Aweme>): List<Aweme> {
        resetFilter()
        val awemes = mutableListOf<Aweme>()
        for (item in items) {
            val aweme = needAweme(item) ?: continue
            awemes.add(aweme)
        }
        return awemes
    }

    private fun filterFollowFeedList(items: List<FollowFeed>): List<FollowFeed> {
        resetFilter()
        val followFeeds = mutableListOf<FollowFeed>()
        for (item in items) {
            needAweme(item.aweme) ?: continue
            followFeeds.add(item)
        }

        return followFeeds
    }

    private fun resetFilter() {
        for (s in filterKeywordsAndTypes) {
            if ("直播" == s) {
                HVerticalViewPagerNew.isFilterLive = true
            }
            if ("图文" == s) {
                HVerticalViewPagerNew.isFilterImage = true
            }
            if ("广告" == s) {
                HVerticalViewPagerNew.isFilterAd = true
            }
            if ("长视频" == s) {
                HVerticalViewPagerNew.isFilterLongVideo = true
            }
            if ("热门特效" == s) {  // awemeType=145, desc=null
                HVerticalViewPagerNew.isFilterPopularEffect = true
            }
        }
    }

    private fun needAweme(aweme: Aweme): Aweme? {
        return when {
            HVerticalViewPagerNew.isFilterLive && aweme.isLive -> {
                HVerticalViewPagerNew.filterLiveCount += 1
                null
            }

            HVerticalViewPagerNew.isFilterImage && aweme.isMultiImage -> {
                HVerticalViewPagerNew.filterImageCount += 1
                null
            }

            HVerticalViewPagerNew.isFilterAd && aweme.isAd -> {
                HVerticalViewPagerNew.filterAdCount += 1
                null
            }

            HVerticalViewPagerNew.isFilterLongVideo && aweme.isCopyRightLongVideo -> {
                HVerticalViewPagerNew.filterLongVideoCount += 1
                null
            }

            HVerticalViewPagerNew.isFilterPopularEffect && aweme.awemeType == 145 -> {
                HVerticalViewPagerNew.filterPopularEffectCount += 1
                null
            }

            keywordsRegex.pattern.isNotBlank() && KTextUtils.get(aweme.desc).contains(keywordsRegex) -> {
                HVerticalViewPagerNew.filterOtherCount += 1
                null
            }

            else -> aweme
        }
    }
}