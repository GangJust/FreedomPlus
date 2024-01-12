package io.github.fplus.core.hook

import android.view.MotionEvent
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.extension.asOrNull
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.reflect.fieldGetFirst
import com.freegang.ktutils.reflect.fieldSetFirst
import com.freegang.ktutils.reflect.methodInvokeFirst
import com.freegang.ktutils.text.KTextUtils
import com.ss.android.ugc.aweme.common.widget.VerticalViewPager
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.follow.presenter.FollowFeed
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.hookClass
import io.github.xpler.core.lpparam
import io.github.xpler.core.thisView

class HVerticalViewPager : BaseHook<VerticalViewPager>() {
    companion object {
        const val TAG = "HVerticalViewPager"

        @get:Synchronized
        @set:Synchronized
        var currentAweme: Aweme? = null

        var isFilterLive = false
        var isFilterImage = false
        var isFilterAd = false
        var isFilterLongVideo = false
        var isFilterRecommendedCards = false
        var isFilterRecommendedMerchants = false
        var isFilterEmptyDesc = false

        var filterLiveCount = 0
        var filterImageCount = 0
        var filterAdCount = 0
        var filterLongVideoCount = 0
        var filterOtherCount = 0
        var filterRecommendedCardsCount = 0
        var filterRecommendedMerchantsCount = 0
        var filterEmptyDescCount = 0
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
                            if (items.size < 3) return@onBefore

                            mData.fieldSetFirst("items", filterAwemeList(items))
                            // val array = items.map { it.sortString() }.toTypedArray()
                            // KLogCat.tagD(TAG, arrayOf("推荐视频列表", array.joinToString("\n")))
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
                            if (mItems.size < 3) return@onBefore

                            mData.fieldSetFirst("mItems", filterFollowFeedList(mItems))
                            // val array = mItems.map { it.aweme.sortString() }.toTypedArray()
                            // KLogCat.tagD(TAG, arrayOf("关注视频列表", array.joinToString("\n")))
                        }
                    }
                }
        }?.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }

    @OnAfter("onInterceptTouchEvent")
    fun onInterceptTouchEvent(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        longVideoJudge(params, event)
    }

    private fun longVideoJudge(params: XC_MethodHook.MethodHookParam, event: MotionEvent) {
        hookBlockRunning(params) {
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
                HVerticalViewPager.isFilterLive = true
            }
            if ("图文" == s) {
                HVerticalViewPager.isFilterImage = true
            }
            if ("广告" == s) {
                HVerticalViewPager.isFilterAd = true
            }
            if ("长视频" == s) {
                HVerticalViewPager.isFilterLongVideo = true
            }
            if ("推荐卡片" == s) {  // awemeType=145, desc=null [热门特效/认识的人]
                HVerticalViewPager.isFilterRecommendedCards = true
            }
            if ("推荐商家" == s) {  // awemeType=140, desc=null
                HVerticalViewPager.isFilterRecommendedMerchants = true
            }
            if ("空文案" == s) {  // desc=null
                HVerticalViewPager.isFilterEmptyDesc = true
            }
        }
    }

    private fun needAweme(aweme: Aweme): Aweme? {
        return when {
            HVerticalViewPager.isFilterLive && aweme.isLive -> {
                HVerticalViewPager.filterLiveCount += 1
                null
            }

            HVerticalViewPager.isFilterImage && aweme.isMultiImage -> {
                HVerticalViewPager.filterImageCount += 1
                null
            }

            HVerticalViewPager.isFilterAd && aweme.isAd -> {
                HVerticalViewPager.filterAdCount += 1
                null
            }

            HVerticalViewPager.isFilterLongVideo && aweme.isCopyRightLongVideo -> {
                HVerticalViewPager.filterLongVideoCount += 1
                null
            }

            HVerticalViewPager.isFilterRecommendedCards && aweme.awemeType == 145 -> {
                HVerticalViewPager.filterRecommendedCardsCount += 1
                null
            }

            HVerticalViewPager.isFilterRecommendedMerchants && aweme.awemeType == 140 -> {
                HVerticalViewPager.filterRecommendedMerchantsCount += 1
                null
            }

            HVerticalViewPager.isFilterEmptyDesc && KTextUtils.isEmpty(aweme.desc) -> {
                HVerticalViewPager.filterEmptyDescCount += 1
                null
            }

            keywordsRegex.pattern.isNotBlank() && KTextUtils.get(aweme.desc).contains(keywordsRegex) -> {
                HVerticalViewPager.filterOtherCount += 1
                null
            }

            else -> aweme
        }
    }
}