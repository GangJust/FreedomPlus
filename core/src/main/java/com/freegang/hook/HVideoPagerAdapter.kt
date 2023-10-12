package com.freegang.hook

import com.freegang.base.BaseHook
import com.freegang.config.ConfigV1
import com.freegang.helper.DexkitBuilder
import com.freegang.ktutils.log.KLogCat
import com.freegang.ktutils.text.KTextUtils
import com.freegang.xpler.core.CallMethods
import com.freegang.xpler.core.NoneHook
import com.freegang.xpler.core.argsOrEmpty
import com.freegang.xpler.core.hookBlockRunning
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HVideoPagerAdapter(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<Any>(lpparam), CallMethods {
    companion object {
        const val TAG = "HVideoPagerAdapter"

        var filterLiveCount = 0
        var filterImageCount = 0
        var filterAdCount = 0
        var filterLongVideoCount = 0
        var filterOtherCount = 0
    }

    val config = ConfigV1.get()

    override fun setTargetClass(): Class<*> = DexkitBuilder.videoPagerAdapterClazz ?: NoneHook::class.java

    override fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam) {
        hookBlockRunning(param) {
            if (argsOrEmpty.size != 1) return
            if (args[0] !is List<*>) return
            val list = args[0] as? List<*> ?: return
            if (!config.isVideoFilter) return

            val keywordsAndTypes = config.videoFilterKeywords
                .replace("，", ",")
                .replace("\\s".toRegex(), "")
                .split(",")
                .toSet()

            val keywordsRegex = keywordsAndTypes
                .filter { !config.videoFilterTypes.contains(it) }
                .joinToString("|")
                .toRegex()

            var isLive = false
            var isMultiImage = false
            var isAd = false
            var isCopyRightLongVideo = false
            HVideoPagerAdapter.filterLiveCount = -1
            HVideoPagerAdapter.filterImageCount = -1
            HVideoPagerAdapter.filterAdCount = -1
            HVideoPagerAdapter.filterLongVideoCount = -1
            HVideoPagerAdapter.filterOtherCount = 0

            for (s in keywordsAndTypes) {
                if ("直播" == s) {
                    HVideoPagerAdapter.filterLiveCount = 0
                    isLive = true
                }
                if ("图文" == s) {
                    HVideoPagerAdapter.filterImageCount = 0
                    isMultiImage = true
                }
                if ("广告" == s) {
                    HVideoPagerAdapter.filterAdCount = 0
                    isAd = true
                }
                if ("长视频" == s) {
                    HVideoPagerAdapter.filterLongVideoCount = 0
                    isCopyRightLongVideo = true
                }
            }

            val awemes = mutableListOf<Aweme>()
            for (item in list) {
                val aweme = item as Aweme
                when {
                    isLive && aweme.isLive -> {
                        HVideoPagerAdapter.filterLiveCount += 1
                        continue
                    }

                    isMultiImage && aweme.isMultiImage -> {
                        HVideoPagerAdapter.filterImageCount += 1
                        continue
                    }

                    isAd && aweme.isAd -> {
                        HVideoPagerAdapter.filterAdCount += 1
                        continue
                    }

                    isCopyRightLongVideo && aweme.isCopyRightLongVideo -> {
                        HVideoPagerAdapter.filterLongVideoCount += 1
                        continue
                    }

                    keywordsRegex.pattern.isNotBlank() && KTextUtils.get(aweme.desc).contains(keywordsRegex) -> {
                        HVideoPagerAdapter.filterOtherCount += 1
                        continue
                    }

                    else -> awemes.add(aweme)
                }
            }
            args[0] = awemes
        }.onFailure {
            KLogCat.e(TAG, it)
        }
    }

    override fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam) {

    }
}